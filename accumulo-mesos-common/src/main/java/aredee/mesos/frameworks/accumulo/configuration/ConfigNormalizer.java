package aredee.mesos.frameworks.accumulo.configuration;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration;
 
/**
 * 
 * Need to normalize the various configs that drive the AccumuloProcessFactory which
 * is used in the scheduler (ClusterConfiguration) and Executor (ServerProcessConfiguration)
 * and provide a flexible mechanism for defining the various environment variables so even
 * unit tests can be utilized easily.
 */
public class ConfigNormalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigNormalizer.class);
    private ServiceProcessConfiguration serviceConfiguration;
  
    /**
     * Used in the scheduler
     * @param config
     */
    public ConfigNormalizer(ClusterConfiguration config) {
        toServiceConfiguration(config);
    }
    
    /**
     * Used in the Executor
     * @param config
     */
    public ConfigNormalizer(ServerProcessConfiguration config) {
        toServiceConfiguration(config);
    }
    
    public ServiceProcessConfiguration getServiceConfiguration() {
        return serviceConfiguration;
    }
    
    public static String determineValue(String name, String defaultValue) {
        String value = System.getenv(name);
        
        if (StringUtils.isEmpty(value)) {
            value = System.getProperty(name);
        }
        
        if (StringUtils.isEmpty(value) && !StringUtils.isEmpty(defaultValue) )
            value = defaultValue;
        
        return value;
    }
    
    private void toServiceConfiguration(ClusterConfiguration config) {
        serviceConfiguration = new ServiceProcessConfiguration();
        
        // Memory for the initialization process.
        serviceConfiguration.setMaxMemory("1024m");
        serviceConfiguration.setMinMemory("512m");
        setCommonEnvironment();      
    }
    
    /**
     * 
     * @param server
     */
    private void toServiceConfiguration(ServerProcessConfiguration server) {
        
        serviceConfiguration = new ServiceProcessConfiguration();
        
        try {
        
            if( server == null ) {
                throw new RuntimeException("Failed to find server info from scheduler");
            }
            // Memory for the accumulo service, Really should not hard code this to Megs unless clearly
            // stated in configuration documentation...but for now this is the easiest way to get it up and running.
            //
            serviceConfiguration.setMaxMemory("" + server.getMaxMemory() + "m");
            serviceConfiguration.setMinMemory("" + server.getMinMemory() + "m");
            serviceConfiguration.setType(server.getServerType());
            setCommonEnvironment();
         } catch (Exception e) {
            LOGGER.error("Failed to create service configuration",e);
            throw new RuntimeException("Failed to create server configuration: " + e.getMessage());
        }          
    }
    
    private void setCommonEnvironment() {
        
        String mesosHome = determineValue("MESOS_HOME", null);
        LOGGER.info("Mesos Home? " + mesosHome);
        
        String installDir = new File("./").getAbsolutePath();
        LOGGER.info("Executor Home? " + installDir);
       
        setAccumuloDir(mesosHome, installDir);
        
        String accumuloHome = serviceConfiguration.getAccumuloDir().getAbsolutePath();
        
        serviceConfiguration.setAccumuloLibDir(new File(accumuloHome + "/lib"));
        serviceConfiguration.setAccumuloLogDir(new File(accumuloHome + "/logs"));   
        serviceConfiguration.setAccumuloConfDir(new File(accumuloHome + "/conf/"));
        serviceConfiguration.setAccumuloClientConfFile(new File(
                determineValue(Environment.ACCUMULO_CLIENT_CONF_PATH, accumuloHome+"/conf/accumuilo-site.xml")));
      
        serviceConfiguration.setExecutorDir(new File (installDir));
       
        serviceConfiguration.setHadoopHomeDir(new File(determineValue(Environment.HADOOP_PREFIX, null)));
        serviceConfiguration.setHadoopConfDir(new File(determineValue(Environment.HADOOP_CONF_DIR, null)));

        // This is optional, is not recommend to be set in the newest versions of accumulo since
        // it defaults to hdfs.
        String walog = determineValue(Environment.ACCUMULO_WALOG, null);
        if (!StringUtils.isEmpty(walog)) {
            serviceConfiguration.setWalogDir(new File(walog));        
        }
        serviceConfiguration.setZooKeeperDir(new File(determineValue(Environment.ZOOKEEPER_HOME, null)));
        
        setLibPaths();
        setSystemProperties();        
    }


    private void setAccumuloDir(String mesosHome, String installDir) {
        
        // Properties and Environment variables take precedence. This should not be set
        // for the executor....
        String home = determineValue(Environment.ACCUMULO_HOME, null);
        
        LOGGER.info("setAccumuloDir: ACCUMULO_HOME? " + home);
        
        if (StringUtils.isEmpty(home)) {
            if (!StringUtils.isEmpty(mesosHome)) {
                // Add version? MESOS_HOME has never been set
                serviceConfiguration.setAccumuloDir(new File(mesosHome + "/frameworks/accumulo"));
            } else {
                serviceConfiguration.setAccumuloDir(new File(installDir));
            }
        } else {
            serviceConfiguration.setAccumuloDir(new File(home));
        }        
    }
    
    private void setLibPaths() {
        
        String paths = determineValue(Environment.NATIVE_LIB_PATHS, null);
        
        if(!StringUtils.isEmpty(paths)) {
             serviceConfiguration.setNativeLibPaths(Arrays.asList(paths.split(",")));
        }
    }
    
    private void setSystemProperties() {
        Properties props = System.getProperties();
        Map<String, String> newMap = new HashMap<String, String>(props.size());
        
        for(Object prop : props.keySet()) {
            Object value  = props.get(prop);
            newMap.put(prop.toString(), value.toString());
        }
        serviceConfiguration.setSystemProperties(newMap);
    }
}
