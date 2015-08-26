package aredee.mesos.frameworks.accumulo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.Value;
import org.apache.mesos.Protos.Value.Scalar;
import org.apache.mesos.Protos.Value.Type;

import aredee.mesos.frameworks.accumulo.configuration.Constants;
import aredee.mesos.frameworks.accumulo.configuration.Defaults;
import aredee.mesos.frameworks.accumulo.configuration.Environment;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.process.ProcessConfiguration;
 
public class TestSupport {
    
    public static long executorCount = 0;
    public static final String HADOOP_PREFIX = "/usr/local/hadoop";
    public static final String HADOOP_CONF_DIR = HADOOP_PREFIX+"/etc/hadoop";
    public static final String ZOOKEEPER_HOME = "/opt/zookeeper";
    public static final String MESOS_DIRECTORY = "/tmp/testMesos";
    public static final String ACCUMULO_HOME = "/tmp/accumulo";
    public static final String ACCUMULO_CLIENT_CONF_PATH = ACCUMULO_HOME+"/conf/accumulo-site.xml";
    public static final File TEST_CONF_DIR = new File("/tmp/conf");
    
    public static final String TEST_SITE_RESOURCE = "/TestAccumuloSite.xml";
    
    /**
     * The reason AccumuloServer was not used is because its in the accumulo-mesos-scheduler
     * project and I wanted to use this class across several projects.
     *
     */
    public static class ServerContext {
        
        public ServerType type;
        public int maxMem;
        public int minMem;
        public String taskId;
        public String slaveId;
        
        public ServerContext(){
        }
        public ServerContext(ServerType type, int maxMem, int minMem, String task, String slave) {
            this.type = type;
            this.maxMem = maxMem;
            this.minMem = minMem;
            this.taskId = task;
            this.slaveId = slave;
        }
        
        public ServerType getType(){
            return type;
        }
        public int getMaxMem() {
            return maxMem;
        }
        public int getMinMem(){
            return minMem;
        }
        public String getTask() {
            return taskId;
        }
        public String getSlave() {
            return slaveId;
        }
    }
    
    public static class EnvironContext {
        public String getHadoopPrefix() {
            return HADOOP_PREFIX;
        }
        public String getHadoopConf() {
            return HADOOP_CONF_DIR;
        }
        public String getZookeeperHome() {
            return ZOOKEEPER_HOME;
        }
    }
    
    public static void setSchedulerEnviron() {
    
        System.setProperty("ACCUMULO_HOME", ACCUMULO_HOME);
        System.setProperty("HADOOP_PREFIX", HADOOP_PREFIX);
        System.setProperty("HADOOP_CONF_DIR", HADOOP_CONF_DIR);
        System.setProperty("ZOOKEEPER_HOME", ZOOKEEPER_HOME);
        System.setProperty("ACCUMULO_CLIENT_CONF_PATH", ACCUMULO_CLIENT_CONF_PATH);
    }
    
    
    public static TaskInfo createTaskInfo(ServerContext serverCtx, 
            ClusterConfiguration config, String siteXml, EnvironContext envCtx) {
        
        List<Protos.CommandInfo.URI> uris = new ArrayList<>();
        Protos.CommandInfo.URI tarballUri = Protos.CommandInfo.URI.newBuilder()
                .setValue(config.getTarballUri())
                .setExtract(true)
                .setExecutable(false)
                .build();

        uris.add(tarballUri);

        StringBuilder sb = new StringBuilder("env ; /usr/bin/java")
                .append(" -Dserver=").append(serverCtx.getType().getName())  
                .append(" -Xmx").append(((int)config.getMaxExecutorMemory())+"m")
                .append(" -Xms").append(((int)config.getMinExecutorMemory())+"m")
                .append(" -jar $MESOS_DIRECTORY/").append(Constants.ACCUMULO_DISTRO)
                .append("/").append(Constants.EXECUTOR_JAR);

        Protos.Environment env = Protos.Environment.newBuilder()
                .addVariables(Protos.Environment.Variable.newBuilder()
                        .setName(Environment.HADOOP_PREFIX)
                        .setValue(envCtx.getHadoopPrefix()))
                .addVariables(Protos.Environment.Variable.newBuilder()
                        .setName(Environment.HADOOP_CONF_DIR)
                        .setValue(envCtx.getHadoopConf()))
                 .addVariables(Protos.Environment.Variable.newBuilder()
                        .setName(Environment.ZOOKEEPER_HOME)
                        .setValue(envCtx.getZookeeperHome()))
                .build();
                
        Protos.CommandInfo commandInfo = Protos.CommandInfo.newBuilder()
                .setValue(sb.toString())
                .setEnvironment(env)
                .addAllUris(uris)
                .build();
        
        // configure the server
        aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration serverConfig
                = aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration.newBuilder()
                .setServerType(serverCtx.getType().getName())
                .setMaxMemory(serverCtx.getMaxMem())
                .setMinMemory(serverCtx.getMinMem())
                .setAccumuloSiteXml(siteXml)
                .setAccumuloVersion(config.getAccumuloVersion())
                .build();      
        
        Scalar executorMem = Value.Scalar.newBuilder().setValue(config.getMaxExecutorMemory()).build();
        String executorId = "accumuloExecutor-" + serverCtx.getType().getName()+"-" + executorCount++;
 
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
        ProcessConfiguration processor = pmap.get(serverCtx.getType());
  
        Protos.TaskInfo taskInfo = Protos.TaskInfo.newBuilder()
                .setName(serverCtx.getType().getName())
                .setTaskId(Protos.TaskID.newBuilder().setValue(serverCtx.getTask()))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue(serverCtx.getSlave()))
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
        return taskInfo;
    }
    
    public static String getResourceFileLocation(String resource) {
        return ClassLoader.class.getResource(resource).getFile();
    }
    
    public static void setupConfDir() throws IOException {
        if (!TEST_CONF_DIR.exists())
            TEST_CONF_DIR.mkdir();
        
        File src = new File(getResourceFileLocation(TEST_SITE_RESOURCE));
        FileUtils.copyFileToDirectory(src, TEST_CONF_DIR);
    }
    
    public static void tearDownConfDir() {
        if (TEST_CONF_DIR.exists())
            FileUtils.deleteQuietly(TEST_CONF_DIR);
    }
    
    public static void tearDownAccumuloHome() {
        FileUtils.deleteQuietly(new File(ACCUMULO_HOME));
    }
    
    public static void tearDownMesosDir() {
        FileUtils.deleteQuietly(new File(MESOS_DIRECTORY));
    }
    
    public static void tearDownTestDirs() {
        tearDownConfDir();
        tearDownAccumuloHome();
        tearDownMesosDir();
    }
       
}
