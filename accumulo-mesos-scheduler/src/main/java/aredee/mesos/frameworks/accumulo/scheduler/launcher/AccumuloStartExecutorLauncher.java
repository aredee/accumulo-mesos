package aredee.mesos.frameworks.accumulo.scheduler.launcher;

import aredee.mesos.frameworks.accumulo.configuration.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Match;
import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Launches an Executor process that starts Accumulo Servers using the accumulo-start jar.
 */
public class AccumuloStartExecutorLauncher implements Launcher {
    private ClusterConfiguration config;

    public AccumuloStartExecutorLauncher(ClusterConfiguration config){
        this.config = config;
    }

    /**
     * Interface used to launch Accumulo Server tasks.
     *
     * @param driver Mesos interface to use to launch a server
     * @param match AccumuloServer and Offer to launch
     */
    public Protos.TaskInfo launch(SchedulerDriver driver, Match match){
        AccumuloServer server = match.getServer();
        Protos.Offer offer = match.getOffer();

        String args[] = new String[1];
        args[0] = server.getType().getName();

        List<Protos.CommandInfo.URI> uris = new ArrayList<>();
        Protos.CommandInfo.URI tarballUri = Protos.CommandInfo.URI.newBuilder()
                .setValue(this.config.getAccumuloTarballUri())
                .setExtract(true)
                .setExecutable(false)
                .build();

        Protos.CommandInfo.URI executorJarUri = Protos.CommandInfo.URI.newBuilder()
                .setValue(this.config.getExecutorJarUri())
                .setExtract(true)
                .setExecutable(false)
                .build();

        uris.add(tarballUri);
        uris.add(executorJarUri);

        // TODO get java -XX stuff from config
        // TODO get executor jar name from URI
        StringBuilder sb = new StringBuilder("$JAVA_HOME/bin/java")
                .append(" -Dserver=").append(server.getType().getName())
                .append(" -Xmx").append(this.config.getMaxExecutorMemory())
                .append(" -Xms").append(this.config.getMinExecutorMemory())
                .append(" -jar ").append(getExecutorJarFromURI(this.config.getExecutorJarUri()));

        Protos.CommandInfo commandInfo = Protos.CommandInfo.newBuilder()
                .setValue(sb.toString())
                .addAllUris(uris)
                .build();

        // configure the server
        aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration serverConfig
                = aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration.newBuilder()
                .setServerType(match.getServer().getType().getName())
                .build();

        // TODO only get desired resources of offer
        Protos.ExecutorInfo executorInfo = Protos.ExecutorInfo.newBuilder()
                .setCommand(commandInfo)
                .setData(serverConfig.toByteString())  // serialize model here.
                .addAllResources(offer.getResourcesList())
                .build();

        Protos.TaskInfo taskInfo = Protos.TaskInfo.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(server.getTaskId()))
                .setData(null)
                .setExecutor(executorInfo)
                .setSlaveId(Protos.SlaveID.newBuilder().setValue(server.getSlaveId()))
                .build();

        // TODO handle driver Status
        Protos.Status status = driver.launchTasks(Arrays.asList(new Protos.OfferID[]{offer.getId()}),
                Arrays.asList(new Protos.TaskInfo[]{taskInfo}));

        return taskInfo;
    }

    public String getExecutorJarFromURI(String uriString){
        String[] parts = uriString.split(File.pathSeparator);
        return parts[parts.length-1];
    }
}
