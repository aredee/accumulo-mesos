package aredee.mesos.frameworks.accumulo.scheduler;

import aredee.mesos.frameworks.accumulo.scheduler.launcher.AccumuloStartExecutorLauncher;
import aredee.mesos.frameworks.accumulo.scheduler.launcher.Launcher;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Match;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Matcher;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.MinCpuMinRamFIFOMatcher;
import aredee.mesos.frameworks.accumulo.scheduler.server.*;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.apache.mesos.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import aredee.mesos.frameworks.accumulo.configuration.ClusterConfiguration;

import java.util.*;

public class Cluster {
    private static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

    private final ClusterConfiguration config;

    private String frameworkId;
    private State state;

    private Master master = null;
    private GarbageCollector gc = null;
    private Monitor monitor = null;
    // TODO tracer?
    private Set<AccumuloServer> tservers = new HashSet<>();

    private Set<Protos.TaskStatus> runningServers = new HashSet<>();
    private Set<AccumuloServer> serversToLaunch = new HashSet<>();

    private Matcher matcher;
    private Launcher launcher;

    public Cluster(final State state, final ClusterConfiguration config){
        this.state = state;
        this.config = config;
        this.launcher = new AccumuloStartExecutorLauncher(config);
        this.matcher = new MinCpuMinRamFIFOMatcher(config);

        // Launch servers
        // TODO need to populate this from Cluster state for recovery purposes
        this.serversToLaunch.add(ServerUtils.newMaster());
        this.serversToLaunch.add(ServerUtils.newGarbageCollector());
        this.serversToLaunch.add(ServerUtils.newMonitor());
        for(int ii = 0; ii < config.getMinTservers(); ii++) {
            this.serversToLaunch.add(ServerUtils.newTabletServer());
        }

    }

    public void setFrameworkId(String fid){
        this.frameworkId = fid;
        config.getFrameworkName();
        //TODO persist configuration
    }

    public void handleOffers(SchedulerDriver driver, List<Protos.Offer> offers){

        LOGGER.info("Mesos Accumulo Cluster handling offers");

        List<Match> matchedServers = matcher.matchOffers(serversToLaunch, offers);
        LOGGER.info("Found {} matches for servers from {} offers", matchedServers.size(), offers.size());

        List<Match> unlaunchedServers = new ArrayList<>();
        List<AccumuloServer> launchedServers = new ArrayList<>();

        for (Match match: matchedServers){
            if( match.hasServer() && match.hasOffer() ){
                LOGGER.info("Launching Server: {} on {}", match.getServer().getType().getName(), match.getOffer().getSlaveId() );
                Protos.TaskInfo taskInfo = launcher.launch(driver, match);
                LOGGER.info("Created Task {} on {}", taskInfo.getTaskId(), taskInfo.getSlaveId());
                launchedServers.add(match.getServer());
            } else {
                unlaunchedServers.add(match);
            }
        }
        for( AccumuloServer server: launchedServers) {
            serversToLaunch.remove(server);
        }

        for( Match match: unlaunchedServers) {
            serversToLaunch.add(match.getServer());
        }

        LOGGER.info("Still need to launch {} servers", serversToLaunch.size());

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

            if( ServerUtils.isMaster(taskId)){
                master = new Master(slaveId, taskId);
            } else if( ServerUtils.isTabletServer(taskId)) {
                tservers.add(new TabletServer(slaveId, taskId));
            } else if( ServerUtils.isGarbageCollector(taskId)) {
                gc = new GarbageCollector(slaveId, taskId);
            } else if( ServerUtils.isMonitor(taskId)){
                monitor = new Monitor(slaveId, taskId);
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
        LOGGER.info("Task Status Update: Slave: {} Task: {}", slaveId, taskId);

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
                if( ServerUtils.isMaster(taskId)){
                    serversToLaunch.add(ServerUtils.newMaster());
                    clearMaster();
                } else if (ServerUtils.isTabletServer(taskId)) {
                    tservers.remove(new TabletServer(taskId, slaveId));
                    serversToLaunch.add(ServerUtils.newTabletServer());
                } else if (ServerUtils.isMonitor(taskId)) {
                    serversToLaunch.add(ServerUtils.newMonitor());
                    clearMonitor();
                } else if (ServerUtils.isGarbageCollector(taskId)) {
                    serversToLaunch.add(ServerUtils.newGarbageCollector());
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
