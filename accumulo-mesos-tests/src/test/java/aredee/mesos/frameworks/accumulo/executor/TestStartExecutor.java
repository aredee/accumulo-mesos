package aredee.mesos.frameworks.accumulo.executor;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.mesos.Protos.Value;
import org.apache.mesos.Protos.Value.Scalar;
import org.junit.Test;

import aredee.mesos.frameworks.accumulo.configuration.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.CommandLineClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.Environment;
import aredee.mesos.frameworks.accumulo.configuration.IProcessorConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ProcessorConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import aredee.mesos.frameworks.accumulo.initialize.AccumuloInitializer;
import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;

public class TestStartExecutor {   
    
    @Test
    public void testProp() {
        System.out.println(System.getProperty("PWD"));
        System.out.println(System.getenv("PWD"));
    }
    
    /**
     * This is expected to run on a vagrant cluster, with hadoop and mesos up.
     */
    @Test
    public void testStartMaster() { 
        
        System.setProperty(Environment.ACCUMULO_HOME, "/vagrant/accumulo-install");
        System.setProperty(Environment.HADOOP_CONF_DIR, "/usr/local/hadoop/etc/hadoop");
        System.setProperty(Environment.HADOOP_PREFIX, "/usr/local/hadoop");
        System.setProperty(Environment.ACCUMULO_CLIENT_CONF_PATH, "/vagrant/accumulo-install/conf/accumulo-site.xml");
        System.setProperty(Environment.ZOOKEEPER_HOME, "/etc/zookeeper");
   
        
        /**
         * Make sure Environment.ACCUMULO_HOME is set.
         */
        ClusterConfiguration config = buildConfig();
   
  /**      
        MiniAccumuloCluster accumulo = null;
        try {
            accumulo = new MiniAccumuloCluster(tempDir,config.getAccumuloRootPassword());
            accumulo.start();
            config.setZkServers(accumulo.getZooKeepers());          
        } catch (Exception e1) {
            e1.printStackTrace();
            fail();
        }
        **/
        try {
            AccumuloInitializer initializer = new AccumuloInitializer(config);
        } catch (Exception e) {
            e.printStackTrace();
            fail();    
        }
        
        System.out.println("Initialized accumulo...launching executor!!");
        
        AccumuloStartExecutor executor = new AccumuloStartExecutor();
        ExecutorDriver driver = new MyDriver();
        TaskInfo taskInfo = buildTaskInfo(new MyServer(),config);
        
         executor.launchTask(driver, taskInfo);
        for(int i=0; i<300; i++) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                
            }
        }
        /**
        try {
            accumulo.stop();
        } catch (Exception e) {
            e.printStackTrace();
            fail();    
        } 
        **/
    }
    
    public ClusterConfiguration buildConfig() {
        
        String args[] = new String[] {"-P=8711","-b=126.0.0.0","-f=testAccumulo-1","-z=localhost:2181"};        
        
        CommandLine cmd = CommandLineClusterConfiguration.parseArgs(args);
        ClusterConfiguration config = CommandLineClusterConfiguration.getConfiguration(cmd);
        
        config.setAccumuloTarballUri("file:///usr/local/accumulo/accumulo-dist.tgz");
        config.setExecutorJarUri("file:///usr/local/accumulo/accumulo-executor.jar");
        config.setMaxExecutorMemory(2048.0);
        config.setMinExecutorMemory(1000.0);
        config.setAccumuloRootPassword("password");
        Map<ServerType, ProcessorConfiguration> processors = new HashMap<ServerType, ProcessorConfiguration>(2);
        processors.put(ServerType.MASTER, new ProcessorConfiguration("512", "1024","1",ServerType.MASTER.getName()));
        config.setProcessorConfigurations(processors);
        return config;
    }
    
    public TaskInfo buildTaskInfo(AccumuloServer server, ClusterConfiguration config) {

        Map<ServerType, ProcessorConfiguration> servers = config.getProcessorConfigurations();
        IProcessorConfiguration inServerConfig = servers.get(server.getType());
        
        List<Protos.CommandInfo.URI> uris = new ArrayList<>();
        System.out.println("Tarball URI ? " + config.getAccumuloTarballUri());
        
        Protos.CommandInfo.URI tarballUri = Protos.CommandInfo.URI.newBuilder()
                .setValue(config.getAccumuloTarballUri())
                .setExtract(true)
                .setExecutable(false)
                .build();

        Protos.CommandInfo.URI executorJarUri = Protos.CommandInfo.URI.newBuilder()
                .setValue(config.getExecutorJarUri())
                .setExtract(true)
                .setExecutable(false)
                .build();

        uris.add(tarballUri);
        uris.add(executorJarUri);

        // TODO get java -XX stuff from config
        // TODO get executor jar name from URI
        StringBuilder sb =  new StringBuilder("$JAVA_HOME/bin/java")
                .append(" -Dserver=").append(server.getType().getName())
                .append(" -Xmx").append((int)config.getMaxExecutorMemory() + "m")
                .append(" -Xms").append((int)config.getMinExecutorMemory() + "m")
                .append(" -jar ").append(getExecutorJarFromURI(config.getExecutorJarUri()));

        Protos.CommandInfo commandInfo = Protos.CommandInfo.newBuilder()
                .setValue(sb.toString())
                .addAllUris(uris)
                .build();

        String accumuloHome = System.getProperty(Environment.ACCUMULO_HOME);
                
        // configure the server
        aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration serverConfig
                = aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration.newBuilder()
                .setServerType(server.getType().getName())
                .setAccumuloDir(accumuloHome)
                .setHadoopConfDir("/usr/local/hadoop/etc/hadoop/yarn")
                .setMaxMemory(inServerConfig.getMaxMemorySize())
                .setMinMemory(inServerConfig.getMinMemorySize())
                .build();
       
        ExecutorID executorId = Protos.ExecutorID.newBuilder()
                .setValue("ExceutorId-6")
                .build();

        Scalar mem = Value.Scalar.newBuilder().setValue(1024.0).build();
        // TODO only get desired resources of offer
        Protos.ExecutorInfo executorInfo = Protos.ExecutorInfo.newBuilder()
                .setCommand(commandInfo)
                .setData(serverConfig.toByteString())  // serialize model here.
                .addResources(Resource.newBuilder()
                        .setName("mem")
                        .setScalar(mem)
                        .setType(Value.Type.SCALAR)
                        .build())
                //.addAllResources(offer.getResourcesList())
                .setName("accumuloExecutor")
                .setExecutorId(executorId)
                .build();

        Protos.TaskInfo.Builder taskBuilder = Protos.TaskInfo.newBuilder()
                .setName("MasterTask")
                .setTaskId(Protos.TaskID.newBuilder().setValue(server.getTaskId()))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue(server.getSlaveId()));
                
      
        Protos.TaskInfo taskInfo = taskBuilder
                .addResources(Resource.newBuilder()
                        .setName("mem")
                        .setScalar(mem)
                        .setType(Value.Type.SCALAR)
                        .build())               
                .setExecutor(executorInfo)
                .setData(serverConfig.toByteString())
                .build();
        return taskInfo;
    }
    
    public String getExecutorJarFromURI(String uriString){
        String[] parts = uriString.split(File.pathSeparator);
        return parts[parts.length-1];
    }
    
    public static class MyServer implements AccumuloServer {
        public String slaveId = "Slave-1";
        private int maxMemory;
        private int minMemory;
        public ServerType getType() {
            return ServerType.MASTER;
        }
        public String getSlaveId() {
            return slaveId;
        }
        public void setSlaveId(String newId) {
            slaveId=newId;
        }
        public String getTaskId() {
            return "62134-accumulo-master";
        }
        public int getMaxMemorySize() {
            return maxMemory;
        }
        public int getMinMemorySize() {
            return minMemory;
        }
        public void setMaxMemorySize(int memory) {
            maxMemory = memory;            
        }
        public void setMinMemorySize(int memory) {
            minMemory = memory;            
        }
    }
    
    public static class MyDriver implements ExecutorDriver {
        public Status abort() {
            return null;
        }
        public Status join() {
            return null;
        }
        public Status run() {
            return null;
        }
        public Status sendFrameworkMessage(byte[] arg0) {
            return null;
        }
        public Status sendStatusUpdate(TaskStatus arg0) {
            return null;
        }
        public Status start() {
            return null;
        }
        public Status stop() {
            return null;
        }
    }
}
