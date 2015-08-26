package aredee.mesos.frameworks.accumulo.configuration;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.cluster.CommandLineClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.cluster.JSONClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.file.AccumuloSiteXml;
import aredee.mesos.frameworks.accumulo.configuration.process.ServerProcessConfiguration;

public class TestConfigNormalizer {
   
    final static String ACCUMULO_VERSION = "1.8.0";
    
    final static String ACCUMULO_HOME = "/home/accumulo/accumulo-1.7.0";
    final static String ACCUMULO_WALOG = "/home/hdfs/data/walog";
    final static String ACCUMULO_CLIENT_CONF = "/home/accumulo/accumulo-1.7.0/client/accumulo-site.xml";
           
    final static String MESOS_DIRECTORY = "/home/mesos/slave/1998-OJIIJEW-343-H";
    final static String MESOS_DIRECTORY_PROP = "MESOS_DIRECTORY";
    
    final static String HADOOP_PREFIX = "/usr/local/hadoop";
    final static String HADOOP_CONF_DIR = "/usr/local/hadoop/etc/hadoop";
    final static String ZOOKEEPER_HOME = "/home/zookeeper";
    
    // These should always match TestGoodCluster.json
    final static String MAX_MEM = "1024";
    final static int INT_MAX_MEM = 1024;
    final static double DBL_MAX_MEM = 1024.0;
    final static String MIN_MEM = "512";
    final static int INT_MIN_MEM = 512;
    final static double DBL_MIN_MEM = 512.0;
    
    
    // This will represent the scheduler side
    @Test
    public void testSchedulerProcessConfig() {
        try {
            ClusterConfiguration cconfig = loadGoodClusterConfig();
            
            System.out.println("\n");
  
            // Environment is not set
            failNormalizerTest(cconfig);
            
            // Standard Environment is set
            successNormalizerTest_w_Env(cconfig, null, null);
            
            // Set ACCUMULO_CONF_DIR instead of derive it 
            successNormalizerTest_w_accConfDirSet(cconfig);
            
            // Test the setting of the optional WaLog var
            successNormalizerTest_w_accWaLogSet(cconfig);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    // This will represent the executor
    @Test
    public void testExecutorProcessConfig () {
        
        try {
            URI uri = ClassLoader.class.getResource("/TestAccumuloSite.xml").toURI();
            
            System.out.println("\nUsing test accumulo site file: " + uri);
                        
            AccumuloSiteXml xmlSite = new AccumuloSiteXml(uri.toURL().openStream());
            
            //System.out.println(xmlSite.toXml());     
            
            aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration serverConfig
                = aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration.newBuilder()
                    .setServerType(ServerType.TABLET_SERVER.getName())
                    .setMaxMemory(INT_MAX_MEM)
                    .setMinMemory(INT_MIN_MEM)
                    .setAccumuloSiteXml(xmlSite.toXml())
                    .setAccumuloVersion(ACCUMULO_VERSION)
                    .build();
                       
            //failNormalizerTest_executor(serverConfig);  
  
            successNormalizerTest_executor(serverConfig);  
      
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    @SuppressWarnings("unused")
    public void failNormalizerTest(ClusterConfiguration cconfig) {
        System.out.println("Running failNormalizerTest ");   
        unsetEnvironment();
        try {
            // This should throw an exception, assuming the environment variables are not set
            ConfigNormalizer normalizer = new ConfigNormalizer(cconfig);
            
            System.out.println("Either environment variables are set or there is something wrong!! ");
            
            fail();
        } catch (RuntimeException e) {
            System.out.println("Expected exception thrown: " + e.getMessage());
            assertTrue(true);
        } catch(Exception e) {
            e.printStackTrace();
            fail("Unexpected exception");
        }
    }
    
    public void successNormalizerTest_executor(aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration config) {
        System.out.println("Running successNormalizerTest_executor");   
        
        unsetEnvironment();
        setEnvironment(false);
         
        System.setProperty(MESOS_DIRECTORY_PROP, MESOS_DIRECTORY);
        
        // Shouldn't throw and exception
        ConfigNormalizer normalizer = new ConfigNormalizer(config);
      
        ServerProcessConfiguration srvConfig = normalizer.getServiceConfiguration();
        
        checkServiceConfig(srvConfig, true);       
    }  
    
    public void successNormalizerTest_w_accWaLogSet(ClusterConfiguration cconfig) {
        
        System.out.println("Running successNormalizerTest_w_accWaLogSet");
        successNormalizerTest_w_Env(cconfig, Environment.ACCUMULO_WALOG, ACCUMULO_WALOG); 
    }    
    public void successNormalizerTest_w_accConfDirSet(ClusterConfiguration cconfig) {
        
        System.out.println("Running successNormalizerTest_w_accConfDirSet");   
        successNormalizerTest_w_Env(cconfig, Environment.ACCUMULO_CLIENT_CONF_PATH, ACCUMULO_CLIENT_CONF);
    }
    
    /**
     * For other tests.
     * @return
     */
    public ServerProcessConfiguration getStandardTestServerConfig() {
        ClusterConfiguration cconfig = loadGoodClusterConfig();
        this.setEnvironment(true);   
        // Shouldn't throw and exception
        ConfigNormalizer normalizer = new ConfigNormalizer(cconfig);
      
        ServerProcessConfiguration srvConfig = normalizer.getServiceConfiguration();
        
        checkServiceConfig(srvConfig, true);  
        
        return srvConfig;
     }
    
    public void successNormalizerTest_w_Env(ClusterConfiguration cconfig, String prop, String propValue) {
        
        System.out.println("Running successNormalizerTest_w_stdEnv");   
       
        this.setEnvironment(true);   
        
        if(prop != null) {
            System.setProperty(prop, propValue);           
        }
        
        // Shouldn't throw and exception
        ConfigNormalizer normalizer = new ConfigNormalizer(cconfig);
      
        ServerProcessConfiguration srvConfig = normalizer.getServiceConfiguration();
        
        checkServiceConfig(srvConfig, false);  
    }
    
    public ClusterConfiguration loadJsonClusterConfig(String resource) {
        // This is in the test resources directory
        String f = ClassLoader.class.getResource(resource).getFile();
        System.out.println("Cluster config file : " + f);

        String args[] = new String[]{ "-j", f};
        
        CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);
         
        // create injector with command line
        ClusterConfiguration cconfig;
        if( cmdLine.hasOption('j') ){
            // JSON file specified
            cconfig = new JSONClusterConfiguration(cmdLine.getOptionValue('j'));
        } else {
            // parse all the command line options
            cconfig = new CommandLineClusterConfiguration(cmdLine);
        }
         
        return cconfig;                 
    }
    
    public ClusterConfiguration loadBadClusterConfig() {
        return loadJsonClusterConfig("/TestBadCluster.json");           
    }
    
    public ClusterConfiguration loadGoodClusterConfig() {
        return loadJsonClusterConfig("/TestGoodCluster.json");
    }
    
    public void setEnvironment(boolean setAccumuloHome) {
        if (setAccumuloHome)
            System.setProperty(Environment.ACCUMULO_HOME, ACCUMULO_HOME);
        System.setProperty(Environment.HADOOP_PREFIX, HADOOP_PREFIX);
        System.setProperty(Environment.HADOOP_CONF_DIR, HADOOP_CONF_DIR);
        System.setProperty(Environment.ZOOKEEPER_HOME, ZOOKEEPER_HOME); 
        System.setProperty(Environment.NATIVE_LIB_PATHS, "/usr/lib/native" + File.pathSeparator + "/sys/lib/native"); 
    }
    public void unsetEnvironment() {
        System.clearProperty(Environment.ACCUMULO_HOME);
        System.clearProperty(Environment.HADOOP_PREFIX);
        System.clearProperty(Environment.HADOOP_CONF_DIR);
        System.clearProperty(Environment.ZOOKEEPER_HOME);
        System.clearProperty(Environment.NATIVE_LIB_PATHS);
        System.clearProperty(MESOS_DIRECTORY_PROP);
        System.clearProperty(Environment.ACCUMULO_CLIENT_CONF_PATH);
        System.clearProperty(Environment.ACCUMULO_WALOG);
      
    }
    public void checkServiceConfig(ServerProcessConfiguration srvConfig, boolean print) {

        if (print) {    
            System.out.println("\nACCUMULO_HOME " + srvConfig.getAccumuloDir());
            System.out.println("ACCUMULO CONF DIR " + srvConfig.getAccumuloConfDir());      
            System.out.println("ACCUMULO LIB DIR " + srvConfig.getAccumuloLibDir());
            System.out.println("ACCUMULO LIB EXT DIR " + srvConfig.getAccumuloLibExtDir());
            System.out.println("ACCUMULO LOG DIR " + srvConfig.getAccumuloLogDir());
            System.out.println("ACCUMULO CLIENT CONF FILE " + srvConfig.getAccumuloClientConfFile());
           
            System.out.println("CPU COUNT " + srvConfig.getCpuCount());
            System.out.println("CPU OFFER " + srvConfig.getCpuOffer());
            System.out.println("CPUs " + srvConfig.getCpus());
            
            System.out.println("EXECUTOR DIR " + srvConfig.getExecutorDir());
            System.out.println("HADOOP CONF DIR " + srvConfig.getHadoopConfDir());
            System.out.println("HADOOP HOME DIR " + srvConfig.getHadoopHomeDir());
            
            System.out.println("MAX MEM " + srvConfig.getMaxMemory());
            System.out.println("MAX MEM OFFER " + srvConfig.getMaxMemoryOffer());
            System.out.println("MAX MEM SIZE " + srvConfig.getMaxMemorySize());
            System.out.println("MIN MEM " + srvConfig.getMinMemory());
            System.out.println("MIN OFFER " + srvConfig.getMinMemoryOffer());
            System.out.println("MIN SIZE " + srvConfig.getMinMemorySize());
            System.out.println("SERVER MEM " + srvConfig.getServerMemory());
           
            System.out.println("NATIVE LIB PATH " + srvConfig.getNativeLibPaths());
            System.out.println("SYSTEM PROPS " + srvConfig.getSystemProperties());
            System.out.println("SERVER TYPE " + srvConfig.getType());
            System.out.println("WALOG DIR " + srvConfig.getWalogDir());
            System.out.println("ZOOKEEPER DIR " + srvConfig.getZooKeeperDir());
            System.out.println("CLASSPATH " + srvConfig.getClasspathItems());
        }
        
        // The scheduler derives its accumulo home from the Enviroment.ACCUMULO_HOME but
        // the executor derives its accumulo home from MESOS_DIRECTORY.
        String accumuloHome;
        
        // ACCUMULO_HOME SHOULD BE SET FOR THE SCHEDULER, BUT NOT SET FOR THE EXECUTOR
        if (System.getProperty(MESOS_DIRECTORY_PROP) == null) {
            accumuloHome = ACCUMULO_HOME;
            assertNull(srvConfig.getType());
            
        } else {
            accumuloHome = MESOS_DIRECTORY + File.separator + Constants.ACCUMULO_DISTRO + 
                    File.separator + "accumulo-" + ACCUMULO_VERSION;
            
            assertEquals(MESOS_DIRECTORY, srvConfig.getExecutorDir().getAbsolutePath());
            assertEquals( ServerType.TABLET_SERVER.getName(), srvConfig.getType());
        }
        
        assertEquals(accumuloHome, srvConfig.getAccumuloDir().getAbsolutePath());
        assertEquals(accumuloHome + "/conf", srvConfig.getAccumuloConfDir().getAbsolutePath());
        assertEquals(accumuloHome + "/lib", srvConfig.getAccumuloLibDir().getAbsolutePath());
        assertEquals(accumuloHome + "/lib/ext", srvConfig.getAccumuloLibExtDir().getAbsolutePath());
        assertEquals(accumuloHome + "/logs", srvConfig.getAccumuloLogDir().getAbsolutePath());
        
        if (System.getProperty(Environment.ACCUMULO_CLIENT_CONF_PATH) == null) {
            assertEquals(accumuloHome + "/conf/accumuilo-site.xml", srvConfig.getAccumuloClientConfFile().getAbsolutePath());
        } else {
            assertEquals(ACCUMULO_CLIENT_CONF, srvConfig.getAccumuloClientConfFile().getAbsolutePath());
        }
       
        assertEquals(HADOOP_PREFIX, srvConfig.getHadoopHomeDir().getAbsolutePath());
        assertEquals(HADOOP_CONF_DIR, srvConfig.getHadoopConfDir().getAbsolutePath());
        assertEquals(0, srvConfig.getCpuCount());
        assertEquals(0.0, srvConfig.getCpuOffer(), 0);
        assertNull(srvConfig.getCpus());
        
        assertEquals(MAX_MEM, srvConfig.getMaxMemory());
        assertEquals(DBL_MAX_MEM, srvConfig.getMaxMemoryOffer(), 0);
        assertEquals(INT_MAX_MEM, srvConfig.getMaxMemorySize());
        
        assertEquals(MIN_MEM, srvConfig.getMinMemory());
        assertEquals(DBL_MIN_MEM, srvConfig.getMinMemoryOffer(), 0);
        assertEquals(INT_MIN_MEM, srvConfig.getMinMemorySize());
        assertNotNull(srvConfig.getNativeLibPaths());
        assertNotNull(srvConfig.getSystemProperties());

        if (System.getProperty(Environment.ACCUMULO_WALOG) != null) {
            assertEquals(ACCUMULO_WALOG, srvConfig.getWalogDir().getAbsolutePath());
        } else {
            assertNull(srvConfig.getWalogDir());
        }
        
        assertEquals(ZOOKEEPER_HOME, srvConfig.getZooKeeperDir().getAbsolutePath());
       
        // Not really sure this is used
        assertTrue(CollectionUtils.isEmpty(srvConfig.getClasspathItems()));
     }
}