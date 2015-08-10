package aredee.mesos.frameworks.accumulo.scheduler.launcher;

import aredee.mesos.frameworks.accumulo.configuration.Constants;
import aredee.mesos.frameworks.accumulo.configuration.Defaults;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.Environment;
import aredee.mesos.frameworks.accumulo.configuration.process.ProcessConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.process.ServerProcessConfiguration;
import aredee.mesos.frameworks.accumulo.initialize.AccumuloInitializer;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Match;
import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;

import org.apache.mesos.Protos;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.Value;
import org.apache.mesos.Protos.Value.Scalar;
import org.apache.mesos.Protos.Value.Type;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Launches an Executor process that starts Accumulo Servers using the accumulo-start jar.
 */
public class AccumuloStartExecutorLauncher implements Launcher {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloStartExecutorLauncher.class);
    
    private ClusterConfiguration config;
    private ServerProcessConfiguration serviceConfig;
    private AccumuloInitializer initializer;
    private long executorCount = 0;
    
    public AccumuloStartExecutorLauncher(ServerProcessConfiguration serviceConfig, ClusterConfiguration config){
        this.config = config;
        this.serviceConfig = serviceConfig;
    }
    
    public AccumuloStartExecutorLauncher(AccumuloInitializer initializer) {
        this.initializer = initializer; 
        this.config = initializer.getClusterConfiguration();;
        this.serviceConfig = initializer.getProcessConfiguration();     
    }
    
    /**
     * Interface used to launch Accumulo Server tasks.
     *
     * @param driver Mesos interface to use to launch a server
     * @param match AccumuloServer and Offer to launch
     */
    @SuppressWarnings("unchecked")
    public Protos.TaskInfo launch(SchedulerDriver driver, Match match){
        AccumuloServer server = match.getServer();
        Protos.Offer offer = match.getOffer();

        String args[] = new String[1];
        args[0] = server.getType().getName();

        LOGGER.debug("Cluster Config? " + config);
        
      
        List<Protos.CommandInfo.URI> uris = new ArrayList<>();
        Protos.CommandInfo.URI tarballUri = Protos.CommandInfo.URI.newBuilder()
                .setValue(this.config.getTarballUri())
                .setExtract(true)
                .setExecutable(false)
                .build();

        uris.add(tarballUri);

        // TODO get java -XX stuff from config
        // TODO get executor jar name from URI
        // The "m" is hard coded here to get the cluster up...should be handled in another manner, maybe another method that
        // leveraged off the *ExecutorMemory, or if this is not being used to check against offers then just make it the
        // correct string....
        
        // Since JAVA_HOME is usually installed here...hard code it for now. Should we pass it in or instead
        // of launching it directly use a script that checks the local server(environment) for JAVA_HOME...and
        // the rest of the environment var?
        StringBuilder sb = new StringBuilder("env ; /usr/bin/java")
                .append(" -Dserver=").append(server.getType().getName())  // this is just candy to see what's running using jps or ps
                .append(" -Xmx").append(((int)this.config.getMaxExecutorMemory())+"m")
                .append(" -Xms").append(((int)this.config.getMinExecutorMemory())+"m")
                .append(" -jar $MESOS_DIRECTORY/").append(Constants.ACCUMULO_DISTRO)
                .append("/").append(Constants.EXECUTOR_JAR);

        Protos.Environment env = Protos.Environment.newBuilder()
                .addVariables(Protos.Environment.Variable.newBuilder()
                        .setName(Environment.HADOOP_PREFIX)
                        .setValue(serviceConfig.getHadoopHomeDir().getAbsolutePath()))
                .addVariables(Protos.Environment.Variable.newBuilder()
                        .setName(Environment.HADOOP_CONF_DIR)
                        .setValue(serviceConfig.getHadoopConfDir().getAbsolutePath()))
                 .addVariables(Protos.Environment.Variable.newBuilder()
                        .setName(Environment.ZOOKEEPER_HOME)
                        .setValue(serviceConfig.getZooKeeperDir().getAbsolutePath()))
                .build();
                
        Protos.CommandInfo commandInfo = Protos.CommandInfo.newBuilder()
                .setValue(sb.toString())
                .setEnvironment(env)
                .addAllUris(uris)
                .build();
        
        // configure the server
        aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration serverConfig
                = aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration.newBuilder()
                .setServerType(server.getType().getName())
                .setMaxMemory(server.getMaxMemorySize())
                .setMinMemory(server.getMinMemorySize())
                .setAccumuloSiteXml(initializer.getSiteXml().toXml())
                .setAccumuloVersion(config.getAccumuloVersion())
                .build();      
        
        Scalar executorMem = Value.Scalar.newBuilder().setValue(config.getMaxExecutorMemory()).build();
        String executorId = "accumuloExecutor-" + server.getType().getName()+"-" + executorCount++;
 
        // Launch a new executor per accumulo server.
        // TODO only get desired resources of offer
        Protos.ExecutorInfo executorInfo = Protos.ExecutorInfo.newBuilder()
                .setExecutorId(ExecutorID.newBuilder().setValue(executorId))
                .setCommand(commandInfo)
                .setData(serverConfig.toByteString())  // serialize model here.
                         .addResources(Resource.newBuilder()
                              .setName("cpus")
                              .setType(Type.SCALAR)
                              .setScalar(Scalar.newBuilder().setValue(Defaults.EXECUTOR_CPUS))
                              .setRole("*"))
                          .addResources(Resource.newBuilder()
                              .setName("mem")
                              .setType(Type.SCALAR)
                              .setScalar(executorMem)
                              .setRole("*"))
                .setName(executorId)
                .build();
        
        Map<ServerType, ProcessConfiguration>pmap = config.getProcessorConfigurations();
        ProcessConfiguration processor = pmap.get(server.getType());
  
        Protos.TaskInfo taskInfo = Protos.TaskInfo.newBuilder()
                .setName(server.getType().getName())
                .setTaskId(Protos.TaskID.newBuilder().setValue(server.getTaskId()))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue(server.getSlaveId()))
                .setData(serverConfig.toByteString())  // serialize model here.
                         .addResources(Resource.newBuilder()
                              .setName("cpus")
                              .setType(Type.SCALAR)
                              .setScalar(Scalar.newBuilder().setValue(processor.getCpuOffer()))
                              .setRole("*"))
                          .addResources(Resource.newBuilder()
                              .setName("mem")
                              .setType(Type.SCALAR)
                              .setScalar(Scalar.newBuilder().setValue(processor.getMaxMemoryOffer()))
                              .setRole("*"))                
                              .setExecutor(executorInfo)               
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
