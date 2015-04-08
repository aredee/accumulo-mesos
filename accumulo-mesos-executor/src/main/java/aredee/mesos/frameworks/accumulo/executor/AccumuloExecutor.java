package aredee.mesos.frameworks.accumulo.executor;


import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;

public class AccumuloExecutor implements Executor{
    @Override
    public void registered(ExecutorDriver executorDriver, Protos.ExecutorInfo executorInfo, Protos.FrameworkInfo frameworkInfo, Protos.SlaveInfo slaveInfo) {
        
    }

    @Override
    public void reregistered(ExecutorDriver executorDriver, Protos.SlaveInfo slaveInfo) {

    }

    @Override
    public void disconnected(ExecutorDriver executorDriver) {

    }

    @Override
    public void launchTask(ExecutorDriver executorDriver, Protos.TaskInfo taskInfo) {

    }

    @Override
    public void killTask(ExecutorDriver executorDriver, Protos.TaskID taskID) {

    }

    @Override
    public void frameworkMessage(ExecutorDriver executorDriver, byte[] bytes) {

    }

    @Override
    public void shutdown(ExecutorDriver executorDriver) {

    }

    @Override
    public void error(ExecutorDriver executorDriver, String s) {

    }
}
