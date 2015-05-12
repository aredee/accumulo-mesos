package aredee.mesos.frameworks.accumulo.scheduler;

import aredee.mesos.frameworks.accumulo.scheduler.launcher.AccumuloStartClassLauncher;
import aredee.mesos.frameworks.accumulo.scheduler.launcher.Launcher;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Match;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Matcher;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.MinCpuMinRamFIFOMatcher;
import aredee.mesos.frameworks.accumulo.scheduler.server.*;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import aredee.mesos.frameworks.accumulo.configuration.Configuration;

import java.util.*;

// TODO (klucar) refactor to use Launcher and Matcher interfaces.

public class Cluster {
    private static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

    private final Configuration config;

    private String accumuloInstance;

    private Master master = null;
    private GarbageCollector gc = null;
    private Monitor monitor = null;
    // TODO tracer?
    private Set<AccumuloServer> tservers = new HashSet<>();

    private Set<Protos.TaskStatus> runningServers = new HashSet<>();
    private Set<AccumuloServer> serversToLaunch = new HashSet<>();

    private Matcher matcher;
    private Launcher launcher;

    public Cluster(final Configuration config){
        this.config = config;
        this.launcher = new AccumuloStartClassLauncher(config);
        this.matcher = new MinCpuMinRamFIFOMatcher(config);
    }

    public void setAccumuloInstance(String instanceName){
        this.accumuloInstance = instanceName;
    }

    public void handleOffers(SchedulerDriver driver, List<Protos.Offer> offers){

        List<Match> matchedServers = matcher.matchOffers(serversToLaunch, offers);
        List<Match> unlaunchedServers = new ArrayList<>();
        List<AccumuloServer> launchedServers = new ArrayList<>();

        for (Match match: matchedServers){
            if( match.hasServer() && match.hasOffer() ){
                Protos.TaskInfo taskInfo = launcher.launch(driver, match);
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
    }


    public void restore(SchedulerDriver driver) {

        // reconcileTasks causes the framework to call updateTaskStatus, which
        // will update the tasks list.
        // TODO handle return of reconcileTasks
        driver.reconcileTasks(runningServers);
        clearServers();

        // process the existing tasks
        for (Protos.TaskStatus status : runningServers ){
            String slaveId = status.getSlaveId().getValue();
            String taskId = status.getTaskId().getValue();

            if( ServerUtils.isMaster(taskId)){
                master = new Master(slaveId, taskId);
            } else if( ServerUtils.isTserver(taskId)) {
                tservers.add(new Tserver(slaveId, taskId));
            } else if( ServerUtils.isGarbageCollector(taskId)) {
                gc = new GarbageCollector(slaveId, taskId);
            } else if( ServerUtils.isMonitor(taskId)){
                monitor = new Monitor(slaveId, taskId);
            }
        }
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
                } else if (ServerUtils.isTserver(taskId)) {
                    tservers.remove(new Tserver(taskId, slaveId));
                    serversToLaunch.add(ServerUtils.newTserver());
                } else if (ServerUtils.isMonitor(taskId)) {
                    serversToLaunch.add(ServerUtils.newMonitor());
                    clearMonitor();
                } else if (ServerUtils.isGarbageCollector(taskId)) {
                    serversToLaunch.add(ServerUtils.newGC());
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
