package aredee.mesos.frameworks.accumulo.scheduler;

import aredee.mesos.frameworks.accumulo.model.Accumulo;
import aredee.mesos.frameworks.accumulo.model.Framework;
import aredee.mesos.frameworks.accumulo.model.ServerGroup;
import aredee.mesos.frameworks.accumulo.model.Task;
import aredee.mesos.frameworks.accumulo.scheduler.launcher.AccumuloStartExecutorLauncher;
import aredee.mesos.frameworks.accumulo.scheduler.launcher.Launcher;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Match;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Matcher;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.MinCpuMinRamFIFOMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.apache.mesos.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum Cluster {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

    private volatile Framework config;
    private State state;

    private volatile List<Task> tasks = new ArrayList<>();
    private volatile Set<Protos.TaskStatus> runningTasks = Sets.newConcurrentHashSet();

    private Matcher matcher;
    private Launcher launcher;

    // enum constructors are private by definition. This is where some static initializations could go.
    Cluster(){}

    /**
        Initializes the cluster state from the config.

        Must be called before running Cluster with Mesos.
     */
    public void initialize(Framework config, State state){
        this.config = config;
        this.state = state;

        // TODO consider singleton launchers and matchers defined per server group
        this.launcher = new AccumuloStartExecutorLauncher(config);
        this.matcher = new MinCpuMinRamFIFOMatcher(config.getCluster());

        initializeTasks();
    }

    private void initializeTasks(){
        Accumulo cluster = config.getCluster();
        List<ServerGroup> serverGroups = cluster.getServerGroups();
        for( ServerGroup group : serverGroups){
            for( int ii = 0; ii < group.getCount(); ii++){
                Task task = new Task();
                task.setServerProfile(group.getProfile());
                tasks.add(task);
            }
        }
    }

    public void setFrameworkId(String fid){
        config.setId(fid);
    }

    public void handleOffers(SchedulerDriver driver, List<Protos.Offer> offers){
        int originalOfferSize = offers.size();

        LOGGER.info("Received {} offers", originalOfferSize);
        List<Task> tasksToLaunch = this.getTasksToLaunch();
        if( !tasksToLaunch.isEmpty() ) {
            //TODO right now matchOffers removes offers it uses from the offers collection
            // this assumed side-effect is bad if we actually want people to write their own
            // matchers.
            List<Match> matchedServers = matcher.matchOffers(tasksToLaunch, offers);

            LOGGER.info("Found {} matches for servers from {} offers",
                    matchedServers.size(), originalOfferSize);

            // Launch all the matched servers.
            for (Match match : matchedServers) {
                LOGGER.info("Launching Server: {} using offer {}", match.getTask().getType().name(),
                        match.getOffer().getId());

                match.getTask().assignTaskId();
                Protos.TaskInfo taskInfo = launcher.launch(driver, match);
                match.getTask().setSlaveId(taskInfo.getSlaveId().getValue());

                LOGGER.info("Created Task {} on slave {}", taskInfo.getTaskId(), taskInfo.getSlaveId().getValue());
            }

        } else {
            LOGGER.info("No tasks currently waiting launch.");
        }
        LOGGER.info("Declining {} offers", offers.size());
        declineUnmatchedOffers(driver, offers);
    }

    /**
     * Iterates through tasks finding ones without taskId or slaveId
     *
     * @return List of unlaunched tasks
     */
    private List<Task> getTasksToLaunch(){
        List<Task> toLaunch = Lists.newArrayList();
        for( Task task: tasks){
            if( !task.hasTaskId() && !task.hasSlaveId() ){
                toLaunch.add(task);
            }
        }
        return toLaunch;
    }

    public void restore(SchedulerDriver driver) {

        // reconcileTasks causes the framework to call updateTaskStatus, which
        // will update the tasks list.
        // TODO handle return of reconcileTasks
        Set<Protos.TaskStatus> emptyStatus = Sets.newHashSet();
        Protos.Status reconcileStatus = driver.reconcileTasks(emptyStatus);

        //TODO save cluster state?
    }

    /**
     * Updates Cluster state based on task status.
     *
     * @param status
     */
    public void updateTaskStatus(Protos.TaskStatus status){

        String slaveId = status.getSlaveId().getValue();
        String taskId = status.getTaskId().getValue();


        Task task = findTask(taskId, slaveId);

        if( task == null ){
            // TODO kill task? illegal state exception?
        }

        LOGGER.info("Task Status Update: Status: {} Slave: {} Task: {}", status.getState(), slaveId, taskId);

        switch (status.getState()){
            case TASK_RUNNING:
                runningTasks.add(status);
                break;
            case TASK_FINISHED:
            case TASK_FAILED:
            case TASK_KILLED:
            case TASK_LOST:
                runningTasks.remove(status);
                // Reset the task, so it will be re-launched
                synchronized(task) {
                    task.setSlaveId(null);
                    task.setTaskId(null);
                }
                
                break;
            // TODO task error
            case TASK_STARTING:
            case TASK_STAGING:
                break;
            default:
                LOGGER.info("Unknown Task Status received: {}", status.getState().toString());

        }
    }

    // Finds a Task based on taskId and slaveId
    private Task findTask(@NotNull String taskId, @NotNull String slaveId){
        LOGGER.info("Finding task {} slaveId {}", taskId, slaveId);
        for( Task task : tasks ){
            if( task.hasTaskId() && task.getTaskId().equals(taskId)
                    && task.hasSlaveId() && task.getSlaveId().equals(slaveId)){
                return task;
            }
        }
        return null;
    }

    // Decline any offers not claimed by a match
    private void declineUnmatchedOffers(SchedulerDriver driver, List<Protos.Offer> offers){
        for (Protos.Offer offer : offers){
            LOGGER.debug("Declining Offer: ", offer.getId().getValue());
            driver.declineOffer(offer.getId());
        }
    }

}
