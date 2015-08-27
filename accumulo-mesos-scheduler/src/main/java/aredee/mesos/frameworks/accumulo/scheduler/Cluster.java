package aredee.mesos.frameworks.accumulo.scheduler;

import aredee.mesos.frameworks.accumulo.scheduler.launcher.AccumuloStartExecutorLauncher;
import aredee.mesos.frameworks.accumulo.scheduler.launcher.Launcher;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Match;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Matcher;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.MinCpuMinRamFIFOMatcher;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.OperationalCheck;
import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;
import aredee.mesos.frameworks.accumulo.scheduler.server.GarbageCollector;
import aredee.mesos.frameworks.accumulo.scheduler.server.Master;
import aredee.mesos.frameworks.accumulo.scheduler.server.Monitor;
import aredee.mesos.frameworks.accumulo.scheduler.server.ServerUtils;
import aredee.mesos.frameworks.accumulo.scheduler.server.TabletServer;
import aredee.mesos.frameworks.accumulo.scheduler.server.Tracer;

import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.apache.mesos.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.process.ProcessConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import aredee.mesos.frameworks.accumulo.initialize.AccumuloInitializer;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Cluster {
    private static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

    private final ClusterConfiguration config;

    private String frameworkId;
    private State state;
        
    // Just in case there can be multiple GC/Tracer/Master/Monitor/ on multiple slaves.
    // This is basically the same as launcheServers but indexed a different way
    //
    private ConcurrentMap<ServerType, Set<AccumuloServer>>serversAvailable = 
            new ConcurrentHashMap<ServerType,Set<AccumuloServer>>();
    
    // currently sync'ed in methods but could be Concurrent
    private Map<String, Map<ServerType,AccumuloServer>> launchedServers = 
            new HashMap<String, Map<ServerType,AccumuloServer>>();
    
    private Set<Protos.TaskStatus> runningServers = new HashSet<Protos.TaskStatus>();
    private Set<AccumuloServer> serversToLaunch = new HashSet<AccumuloServer>();
    private Map<ServerType, ProcessConfiguration> clusterServers;

    private Matcher matcher;
    private Launcher launcher;

    public Cluster(AccumuloInitializer initializer){
        this.state = initializer.getFrameworkState();
        this.config = initializer.getClusterConfiguration();
        this.launcher = new AccumuloStartExecutorLauncher(initializer);
        this.matcher = new MinCpuMinRamFIFOMatcher(config);
        
        clusterServers = config.getProcessorConfigurations();
           
        LOGGER.info("Servers in the cluster? " + clusterServers);
        
        // Take the cluster configuration from the input cluster configuration.
        for(Entry<ServerType, ProcessConfiguration> entry : clusterServers.entrySet()) {
            if (entry.getKey() == ServerType.TABLET_SERVER) {
                for(int ii = 0; ii < config.getMinTservers(); ii++) {
                    ServerUtils.addServer(serversToLaunch, entry.getValue());              
                }
            } else {
                ServerUtils.addServer(serversToLaunch, entry.getValue());
            }
        }
     }

    public void setFrameworkId(String fid){
        this.frameworkId = fid;
        config.getFrameworkName();
        //TODO persist configuration
    }

    public void handleOffers(SchedulerDriver driver, List<Protos.Offer> offers){

        LOGGER.debug("Mesos Accumulo Cluster handling offers: for servers {}", serversToLaunch);
        
        OperationalCheck opCheck =  new OperationalCheck() {
            public boolean accept(AccumuloServer server, String slaveId) {
                boolean accepted = !isServerLaunched(slaveId, server);
                // This is a bit pre-mature but if not done here the offers just keep 
                // getting accepted.
                if (accepted) {
                    addLaunchedServer(slaveId,server);
                }
                return accepted;
            }
        };
        
        List<Match> matchedServers = matcher.matchOffers(serversToLaunch, offers, opCheck);
        
        LOGGER.debug("Found {} matches for servers from {} offers", matchedServers.size(), offers.size());

        // Launch all the matched servers.
        for (Match match: matchedServers){
             
            LOGGER.info("Launching Server: {} on {}", match.getServer().getType().getName(), 
                    match.getOffer().getSlaveId().getValue() );
            
            Protos.TaskInfo taskInfo = launcher.launch(driver, match);
            
            LOGGER.info("Created Task {} on {}", taskInfo.getTaskId(), taskInfo.getSlaveId().getValue());
            
            serversToLaunch.remove(match.getServer());
        }

        declineUnmatchedOffers(driver, offers, matchedServers);
        // TODO call restore here?
    }


    public void restore(SchedulerDriver driver) {

        // reconcileTasks causes the framework to call updateTaskStatus, which
        // will update the tasks list.
        // TODO handle return of reconcileTasks
        Protos.Status reconcileStatus = driver.reconcileTasks(runningServers);
        clearAvailServers();
        
        String slaveId;
        String taskId;
        
        // process the existing tasks
        for (Protos.TaskStatus status : runningServers ){
            slaveId = status.getSlaveId().getValue();
            taskId = status.getTaskId().getValue();
             
             if( Master.isMaster(taskId)){
                addAvailServer(ServerUtils.newServer(clusterServers.get(ServerType.MASTER), taskId, slaveId));
            } else if( TabletServer.isTabletServer(taskId)) {
                addAvailServer(ServerUtils.newServer(clusterServers.get(ServerType.TABLET_SERVER), taskId, slaveId));
            } else if( GarbageCollector.isGarbageCollector(taskId)) {
                addAvailServer(ServerUtils.newServer(clusterServers.get(ServerType.GARBAGE_COLLECTOR), taskId, slaveId));
            } else if(Monitor.isMonitor(taskId)){
                addAvailServer(ServerUtils.newServer(clusterServers.get(ServerType.MONITOR), taskId, slaveId));
            } else if (Tracer.isTracer(taskId)) {
                addAvailServer(ServerUtils.newServer(clusterServers.get(ServerType.TRACER), taskId, slaveId));
            }
        }

        //TODO save cluster state
    }

    /**
     * Updates Cluster state based on task status.
     *
     * @param status
     */
    public void updateTaskStatus(Protos.TaskStatus status){

        String slaveId = status.getSlaveId().getValue();
        String taskId = status.getTaskId().getValue();
        AccumuloServer serverToLaunch = null;
        
        LOGGER.info("Task Status Update: Status: {} Slave: {} Task: {}", status.getState(), slaveId, taskId);

        switch (status.getState()){
            case TASK_RUNNING:
                runningServers.add(status);
                
                if( Master.isMaster(taskId)){
                    addAvailServer(new Master(taskId,slaveId));
                } else if (TabletServer.isTabletServer(taskId)) {
                    addAvailServer(new TabletServer(taskId, slaveId));
                } else if (Monitor.isMonitor(taskId)) {
                    addAvailServer(new Monitor(taskId,slaveId));
                } else if (GarbageCollector.isGarbageCollector(taskId)) {
                    addAvailServer(new GarbageCollector(taskId,slaveId));
                } else if (Tracer.isTracer(taskId)) {
                    addAvailServer(new Tracer(taskId,slaveId));
                }
                break;
            case TASK_FINISHED:
            case TASK_FAILED:
            case TASK_KILLED:
            case TASK_LOST:
                runningServers.remove(status);
                
                // re-queue tasks when servers are lost.
                if( Master.isMaster(taskId)){
                    // Don't save the slave id, it maybe re-assigned to a new slave 
                    serverToLaunch = ServerUtils.newServer(clusterServers.get(ServerType.MASTER), taskId, null);
                    clearMaster(new Master(taskId,slaveId));
                } else if (TabletServer.isTabletServer(taskId)) {
                    clearTservers(new TabletServer(taskId, slaveId));
                    serverToLaunch = ServerUtils.newServer(clusterServers.get(ServerType.TABLET_SERVER), taskId, null);
                } else if (Monitor.isMonitor(taskId)) {
                    serverToLaunch = ServerUtils.newServer(clusterServers.get(ServerType.MONITOR), taskId, null);
                    clearMonitor(new Monitor(taskId,slaveId));
                } else if (GarbageCollector.isGarbageCollector(taskId)) {
                    serverToLaunch = ServerUtils.newServer(clusterServers.get(ServerType.GARBAGE_COLLECTOR), taskId, null);
                    clearGC(new GarbageCollector(taskId,slaveId));
                } else if (Tracer.isTracer(taskId)) {
                    serverToLaunch = ServerUtils.newServer(clusterServers.get(ServerType.TRACER), taskId, null);
                    clearTracer(new Tracer(taskId,slaveId));
                }
                if (serverToLaunch != null) {
                    removeLaunchedServer(slaveId, serverToLaunch.getType());
                    serversToLaunch.add(serverToLaunch);
                }
                break;
            case TASK_STARTING:
            case TASK_STAGING:
                break;
            default:
                LOGGER.info("Unknown Task Status received: {}", status.getState().toString());

        }
    }

    public boolean isMasterRunning(){
        return serversAvailable.containsKey(ServerType.MASTER);
    }
    public boolean isGCRunning(){
        return serversAvailable.containsKey(ServerType.GARBAGE_COLLECTOR);
    }
    public boolean isMonitorRunning(){
        return serversAvailable.containsKey(ServerType.MONITOR);
    }
    public boolean isTracerRunning(){
        return serversAvailable.containsKey(ServerType.TRACER);
    }
    
    public int numTserversRunning(){
        Set<AccumuloServer> tservers = serversAvailable.get(ServerType.TABLET_SERVER);
        return (tservers != null ? tservers.size() : 0);
    }
    
    // Remove used offers from the available offers and decline the rest.
    private void declineUnmatchedOffers(SchedulerDriver driver, List<Protos.Offer> offers, List<Match> matches){
        List<Protos.Offer> usedOffers = new ArrayList<>(matches.size());
        for(Match match: matches){
            if(match.hasOffer()){
                usedOffers.add(match.getOffer());
            }
        }
        offers.removeAll(usedOffers);
        for( Protos.Offer offer: offers){
            driver.declineOffer(offer.getId());
        }
    }

    private void addLaunchedServer(String slaveId, AccumuloServer server) {
        synchronized(launchedServers) {
            Map<ServerType,AccumuloServer> servers = launchedServers.get(slaveId);
            if (servers == null) {
                servers = new HashMap<ServerType,AccumuloServer>();
                launchedServers.put(slaveId, servers);              
            }
            servers.put(server.getType(),server);
        }
    }
    
    private boolean isServerLaunched(String slaveId, AccumuloServer server) {
        boolean launched = false;
        synchronized(launchedServers) {
            Map<ServerType,AccumuloServer> servers = launchedServers.get(slaveId);
            if (servers != null) {
               launched = servers.containsKey(server.getType());
            }
        }
        return launched;
    }
    
    private void removeLaunchedServer(String slaveId, ServerType type) {
        synchronized(launchedServers) {
            Map<ServerType,AccumuloServer> servers = launchedServers.get(slaveId);
            if (servers != null) {
                servers.remove(type);
            }
        }
    }
    
    private void clearAvailServers(){
        serversAvailable.clear();
    }

    private void clearMaster(Master server){ 
        removeAvailServer(serversAvailable.get(ServerType.MASTER),server); 
    }
    private void clearMonitor(Monitor server){ 
        removeAvailServer(serversAvailable.get(ServerType.MONITOR),server); 
    }
    private void clearGC(GarbageCollector server){ 
        removeAvailServer(serversAvailable.get(ServerType.GARBAGE_COLLECTOR), server); 
    }
    private void clearTracer(Tracer server){ 
        removeAvailServer(serversAvailable.get(ServerType.TRACER), server); 
    }  
    private void clearTservers(TabletServer server){ 
        removeAvailServer(serversAvailable.get(ServerType.TABLET_SERVER), server); 
    }
    private void removeAvailServer(Set<AccumuloServer> servers, AccumuloServer server) {
        if (servers != null) {
            servers.remove(server);
            if (servers.size() == 0) {
                serversAvailable.remove(server.getType());
            }
        } else {
            LOGGER.warn("Tried to remove a server that was not available...could be a problem? " + server);
        }
    }
    private void addAvailServer(AccumuloServer server) {
        Set<AccumuloServer> set;
        if ((set = serversAvailable.get(server.getType() ) ) == null) {
                set = new HashSet<AccumuloServer>(2);
        }
        set.add(server);
        serversAvailable.put(server.getType(), set); 
    }
}
