package aredee.mesos.frameworks.accumulo.executor;

import aredee.mesos.frameworks.accumulo.configuration.ProcessConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import aredee.mesos.frameworks.accumulo.process.AccumuloProcessFactory;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AccumuloStartExecutor implements Executor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloStartExecutor.class);

    public AccumuloStartExecutor(){}

    private Process serverProcess = null;
    private Protos.ExecutorInfo executorInfo = null;
    private Protos.FrameworkInfo frameworkInfo = null;
    private Protos.SlaveInfo slaveInfo = null;
    private Protos.TaskInfo taskInfo = null;

    /**
     * Invoked once the executor driver has been able to successfully
     * connect with Mesos. In particular, a scheduler can pass some
     * data to it's executors through the {@link Protos.ExecutorInfo#getData()}
     * field.
     *
     * @param executorDriver
     * @param executorInfo   Describes information about the executor that was
     *                       registered.
     * @param frameworkInfo  Describes the framework that was registered.
     * @param slaveInfo      Describes the slave that will be used to launch
     *                       the tasks for this executor.
     * @see org.apache.mesos.ExecutorDriver
     * @see org.apache.mesos.MesosSchedulerDriver
     */
    @Override
    public void registered(ExecutorDriver executorDriver, Protos.ExecutorInfo executorInfo, Protos.FrameworkInfo frameworkInfo, Protos.SlaveInfo slaveInfo) {
        LOGGER.info("Executor Registered: " + executorInfo.getName());
        this.executorInfo = executorInfo;
        this.frameworkInfo = frameworkInfo;
        this.slaveInfo = slaveInfo;
    }

    /**
     * Invoked when the executor re-registers with a restarted slave.
     *
     * @param executorDriver
     * @param slaveInfo      Describes the slave that will be used to launch
     *                       the tasks for this executor.
     * @see org.apache.mesos.ExecutorDriver
     */
    @Override
    public void reregistered(ExecutorDriver executorDriver, Protos.SlaveInfo slaveInfo) {
        LOGGER.info("Re-registered with mesos slave: " + slaveInfo.getHostname());
    }

    /**
     * Invoked when the executor becomes "disconnected" from the slave
     * (e.g., the slave is being restarted due to an upgrade).
     *
     * @param executorDriver
     */
    @Override
    public void disconnected(ExecutorDriver executorDriver) {
        // TODO set timer and destroy server if slave doesn't come back?
        LOGGER.info("Disconnected from Mesos slave");
    }

    /**
     * Invoked when a task has been launched on this executor (initiated
     * via {@link SchedulerDriver#launchTasks}. Note that this task can be
     * realized with a thread, a process, or some simple computation,
     * however, no other callbacks will be invoked on this executor
     * until this callback has returned.
     *
     * @param executorDriver
     * @param taskInfo
     * @see org.apache.mesos.ExecutorDriver
     * @see org.apache.mesos.Protos.TaskInfo
     */
    @Override
    public void launchTask(ExecutorDriver executorDriver, Protos.TaskInfo taskInfo) {
        LOGGER.info("Launch Task Requested: " + taskInfo.getCommand());

        this.taskInfo = taskInfo;

        if(this.serverProcess != null){
            if(this.serverProcess.isAlive()){
                LOGGER.error("This executor is already running a server process");
                System.exit(-1);
            } else {
                serverProcess.destroy();
                serverProcess = null;
            }
        }

        aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration server = null;
        try {
            server = aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration.parseFrom(taskInfo.getData());
        } catch (Exception e) {
            //TODO don't swallow this!
            LOGGER.error("Failed to parse AccumuloServer protobuf");
        }

        //Server type
        String serverType = "unknown";
        if( server != null ) {
            serverType = server.getServerType();
        }

        Class serverClass = getClassForServer(serverType);
        ProcessConfiguration procConfig = new ProcessConfiguration();
        AccumuloProcessFactory factory = new AccumuloProcessFactory(procConfig);

        //TODO get jvmArgs and args from protobuf?
        List<String> jvmArgs = new ArrayList<>();
        String[] args = new String[0];

        LOGGER.info("Launching {}", serverType);
        try {
            this.serverProcess = factory.exec(serverClass, jvmArgs, args);

        } catch (IOException e) {
            LOGGER.error("Unable to launch server process!");
            System.exit(-1);
        }

    }

    private Class getClassForServer(String serverName){
        ServerType serverType = ServerType.getTypeFromName(serverName);
        Class clazz = null;
        switch(serverType){
            case TABLET_SERVER:
                clazz = org.apache.accumulo.tserver.TabletServer.class;
                break;
            case MASTER:
                clazz = org.apache.accumulo.master.Master.class;
                break;
            case GARBAGE_COLLECTOR:
                clazz = org.apache.accumulo.gc.SimpleGarbageCollector.class;
                break;
            case MONITOR:
                clazz = org.apache.accumulo.monitor.Monitor.class;
                break;
            case TRACER:
                clazz = org.apache.accumulo.tracer.TraceServer.class;
                break;
            case UNKNOWN:
            default:
                break;
        }
        return clazz;
    }

    /**
     * Invoked when a task running within this executor has been killed
     * (via {@link org.apache.mesos.SchedulerDriver#killTask}). Note that no
     * status update will be sent on behalf of the executor, the executor is
     * responsible for creating a new TaskStatus (i.e., with TASK_KILLED)
     * and invoking {@link org.apache.mesos.ExecutorDriver#sendStatusUpdate}.
     *
     * @param executorDriver
     * @param taskID
     * @see org.apache.mesos.ExecutorDriver
     * @see org.apache.mesos.Protos.TaskID
     */
    @Override
    public void killTask(ExecutorDriver executorDriver, Protos.TaskID taskID) {
        LOGGER.info("Killing Task: " + taskID.getValue());
        destroyServer();
    }

    private void destroyServer(){
        if( this.serverProcess != null ){
            this.serverProcess.destroy();
            this.serverProcess = null;
            LOGGER.info("Server destroyed.");
        } else {
            LOGGER.info("No server process is running.");
        }
    }

    private void sendFailMessageAndExit(ExecutorDriver driver, Protos.TaskStatus.Reason reason, String message){
        Protos.TaskStatus status = Protos.TaskStatus.newBuilder()
                .setState(Protos.TaskState.TASK_FAILED)
                .setExecutorId(this.executorInfo.getExecutorId())
                .setSlaveId(this.slaveInfo.getId())
                .setTaskId(this.taskInfo.getTaskId())
                .setReason(reason)
                .setMessage(message)
                .build();
        driver.sendStatusUpdate(status);

        System.exit(-1);
    }

    /**
     * Invoked when a framework message has arrived for this
     * executor. These messages are best effort; do not expect a
     * framework message to be retransmitted in any reliable fashion.
     *
     * @param executorDriver
     * @param bytes
     * @see org.apache.mesos.ExecutorDriver
     */
    @Override
    public void frameworkMessage(ExecutorDriver executorDriver, byte[] bytes) {
        LOGGER.info("Received Framework Message: " + bytes.length + " bytes");
    }

    /**
     * Invoked when the executor should terminate all of it's currently
     * running tasks. Note that after Mesos has determined that an
     * executor has terminated any tasks that the executor did not send
     * terminal status updates for (e.g. TASK_KILLED, TASK_FINISHED,
     * TASK_FAILED, etc) a TASK_LOST status update will be created.
     *
     * @param executorDriver@see org.apache.mesos.ExecutorDriver
     */
    @Override
    public void shutdown(ExecutorDriver executorDriver) {
        LOGGER.info("Shutdown Received");
        destroyServer();
    }

    /**
     * Invoked when a fatal error has occurred with the executor and/or
     * executor driver. The driver will be aborted BEFORE invoking this
     * callback.
     *
     * @param executorDriver
     * @param s
     * @see org.apache.mesos.ExecutorDriver
     */
    @Override
    public void error(ExecutorDriver executorDriver, String s) {
        LOGGER.info("Error: " + s);
        try{
            this.destroyServer();
        } catch (Exception e) {
            LOGGER.error("Exception while trying to destroy server {} ", e.getMessage());
        }
    }
}