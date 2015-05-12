package aredee.mesos.frameworks.accumulo.executor;

import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StartServerExecutor implements Executor{

    private static final Logger LOGGER = LoggerFactory.getLogger(StartServerExecutor.class);

    private final String[] serverArgs;

    public StartServerExecutor(final String[] args){
        this.serverArgs = args;
    }

    /**
     * Invoked once the executor driver has been able to successfully
     * connect with Mesos. In particular, a scheduler can pass some
     * data to it's executors through the {@link ExecutorInfo#getData()}
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

        org.apache.accumulo.start.Main.main(serverArgs);
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
        LOGGER.info("Task Killed: " + taskID.getValue());

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
    }
}
