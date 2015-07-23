package aredee.mesos.frameworks.accumulo.scheduler;

import aredee.mesos.frameworks.accumulo.scheduler.launcher.AccumuloStartExecutorLauncher;
import aredee.mesos.frameworks.accumulo.scheduler.launcher.Launcher;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Match;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Matcher;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.MinCpuMinRamFIFOMatcher;
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

public class Cluster {
    private static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

    private final ClusterConfiguration config;

    private String frameworkId;
    private State state;
    private AccumuloInitializer initializer;
    
    private Master master = null;
    private GarbageCollector gc = null;
    private Monitor monitor = null;
    private Tracer tracer = null;
    private Set<AccumuloServer> tservers = new HashSet<AccumuloServer>();

    private Set<Protos.TaskStatus> runningServers = new HashSet<Protos.TaskStatus>();
    private Set<AccumuloServer> serversToLaunch = new HashSet<AccumuloServer>();
    private Map<ServerType, ProcessConfiguration> clusterServers;

    private Matcher matcher;
    private Launcher launcher;

    @SuppressWarnings("unchecked")
    public Cluster(AccumuloInitializer initializer){
        this.initializer = initializer;
        this.state = initializer.getFrameworkState();
        this.config = initializer.getClusterConfiguration();
        this.launcher = new AccumuloStartExecutorLauncher(initializer.getProcessConfiguration(), config);
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

    @SuppressWarnings("unchecked")
    public void handleOffers(SchedulerDriver driver, List<Protos.Offer> offers){

        LOGGER.info("Mesos Accumulo Cluster handling offers: for servers " + serversToLaunch);

        List<Match> matchedServers = matcher.matchOffers(serversToLaunch, offers);
        LOGGER.info("Found {} matches for servers from {} offers", matchedServers.size(), offers.size());

         List<AccumuloServer> launchedServers = new ArrayList<>();
         LOGGER.info("serversToLaunch before launching? " + serversToLaunch);

        // Launch all the matched servers.
        for (Match match: matchedServers){
             
            LOGGER.info("Launching Server: {} on {}", match.getServer().getType().getName(), 
                    match.getOffer().getSlaveId() );
            
            Protos.TaskInfo taskInfo = launcher.launch(driver, match);
            
            LOGGER.info("Created Task {} on {}", taskInfo.getTaskId(), taskInfo.getSlaveId());
            
            launchedServers.add(match.getServer());
            serversToLaunch.remove(match.getServer());
        }
        LOGGER.info("serversToLaunch after launching? " + serversToLaunch);

        declineUnmatchedOffers(driver, offers, matchedServers);
        // TODO call restore here?
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


    public void restore(SchedulerDriver driver) {

        // reconcileTasks causes the framework to call updateTaskStatus, which
        // will update the tasks list.
        // TODO handle return of reconcileTasks
        Protos.Status reconcileStatus = driver.reconcileTasks(runningServers);
        clearServers();

        // process the existing tasks
        for (Protos.TaskStatus status : runningServers ){
            String slaveId = status.getSlaveId().getValue();
            String taskId = status.getTaskId().getValue();

            if( Master.isMaster(taskId)){
                master = (Master)ServerUtils.newServer(clusterServers.get(ServerType.MASTER), taskId, slaveId);
            } else if( TabletServer.isTabletServer(taskId)) {
                tservers.add((TabletServer)ServerUtils.newServer(clusterServers.get(ServerType.TABLET_SERVER), taskId, slaveId));
            } else if( GarbageCollector.isGarbageCollector(taskId)) {
                gc = (GarbageCollector)ServerUtils.newServer(clusterServers.get(ServerType.GARBAGE_COLLECTOR), taskId, slaveId);
            } else if(Monitor.isMonitor(taskId)){
                monitor = (Monitor)ServerUtils.newServer(clusterServers.get(ServerType.MONITOR), taskId, slaveId);
            } else if (Tracer.isTacer(taskId)) {
                tracer = (Tracer)ServerUtils.newServer(clusterServers.get(ServerType.TRACER), taskId, slaveId);
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
        LOGGER.info("Task Status Update: Status: {} Slave: {} Task: {}", status.getState(), slaveId, taskId);

        switch (status.getState()){
            case TASK_RUNNING:
                runningServers.add(status);
                break;
            case TASK_FINISHED:
            case TASK_FAILED:
            case TASK_KILLED:
            case TASK_LOST:
                runningServers.remove(status);
                // re-queue tasks when servers are lost.
                if( Master.isMaster(taskId)){
                    // Don't save the slave id, it maybe re-assigned to a new slave 
                    serversToLaunch.add(ServerUtils.newServer(clusterServers.get(ServerType.MASTER), taskId, null));
                    clearMaster();
                } else if (TabletServer.isTabletServer(taskId)) {
                    tservers.remove(new TabletServer(taskId, slaveId));
                    serversToLaunch.add(ServerUtils.newServer(clusterServers.get(ServerType.TABLET_SERVER), taskId, null));
                } else if (Monitor.isMonitor(taskId)) {
                    serversToLaunch.add(ServerUtils.newServer(clusterServers.get(ServerType.MONITOR), taskId, null));
                    clearMonitor();
                } else if (GarbageCollector.isGarbageCollector(taskId)) {
                    serversToLaunch.add(ServerUtils.newServer(clusterServers.get(ServerType.GARBAGE_COLLECTOR), taskId, null));
                    clearGC();
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
        return master == null;
    }
    public boolean isGCRunning(){
        return gc == null;
    }
    public boolean isMonitorRunning(){
        return monitor == null;
    }
    public int numTserversRunning(){
        return tservers.size();
    }

    private void clearServers(){
        clearMaster();
        clearMonitor();
        clearGC();
        clearTservers();
    }

    private void clearMaster(){ master = null; }
    private void clearMonitor(){ monitor = null; }
    private void clearGC(){ gc = null; }
    private void clearTservers(){ tservers.clear(); }

}
