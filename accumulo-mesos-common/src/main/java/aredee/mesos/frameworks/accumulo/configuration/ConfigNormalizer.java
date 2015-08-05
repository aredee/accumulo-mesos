package aredee.mesos.frameworks.accumulo.configuration;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.process.ServerProcessConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Need to normalize the various configs that drive the AccumuloProcessFactory which
 * is used in the scheduler (ClusterConfiguration) and Executor (ServerProcessConfiguration)
 * and provide a flexible mechanism for defining the various environment variables so even
 * unit tests can be utilized easily.
 */
public class ConfigNormalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigNormalizer.class);
    private ServerProcessConfiguration serviceConfiguration;
    private String siteXml;
  
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
    public ConfigNormalizer(aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration config) {
        toServiceConfiguration(config);
    }
    
    public ServerProcessConfiguration getServiceConfiguration() {
        return serviceConfiguration;
    }
    /**
    * This will only be non-null when a Proto.ServerProcessConfiguration is used as input.
    * @return accumulo site xml.
    */
    public String getSiteXml() {
        return siteXml;
    }
   
    public static String determineValue(String name, String defaultValue) {
        String value = System.getenv(name);
        
        LOGGER.debug("Environment name: " + name + " value: " + value );
        
        if (StringUtils.isEmpty(value)) {
            value = System.getProperty(name);
        }
        
        if (StringUtils.isEmpty(value) && !StringUtils.isEmpty(defaultValue) )
            value = defaultValue;
        
        return value;
    }
    
    private void toServiceConfiguration(ClusterConfiguration config) {
        serviceConfiguration = new ServerProcessConfiguration();
        
        // Memory for the initialization process.
        serviceConfiguration.setMaxMemory("1024m");
        serviceConfiguration.setMinMemory("512m");
        setCommonEnvironment(config.getAccumuloVersion());      
    }
    
    /**
     * 
     * @param server
     */
    private void toServiceConfiguration(aredee.mesos.frameworks.accumulo.Protos.ServerProcessConfiguration server) {
        
        serviceConfiguration = new ServerProcessConfiguration();
        
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
            if (server.hasAccumuloSiteXml()) {
                siteXml = server.getAccumuloSiteXml();
            }
           
            setCommonEnvironment(server.getAccumuloVersion());
         } catch (Exception e) {
            LOGGER.error("Failed to create service configuration",e);
            throw new RuntimeException("Failed to create server configuration: " + e.getMessage());
        }          
    }
    
    private void setCommonEnvironment(String accumuloVersion) {
        
        String mesosDir = determineValue("MESOS_DIRECTORY", null);
        LOGGER.info("Mesos directory? " + mesosDir);
        
        setAccumuloDir(mesosDir, accumuloVersion);
        
        String accumuloHome = serviceConfiguration.getAccumuloDir().getAbsolutePath();
        LOGGER.info("Accumulo Home? " + accumuloHome);
       
        serviceConfiguration.setAccumuloLibDir(new File(accumuloHome + "/lib"));
        serviceConfiguration.setAccumuloLogDir(new File(accumuloHome + "/logs"));   
        serviceConfiguration.setAccumuloConfDir(new File(accumuloHome + "/conf/"));
        serviceConfiguration.setAccumuloClientConfFile(new File(
                determineValue(Environment.ACCUMULO_CLIENT_CONF_PATH, accumuloHome+"/conf/accumuilo-site.xml")));
      
        // On the supervisor side this will be empty.
        if (!StringUtils.isEmpty(mesosDir))
            serviceConfiguration.setExecutorDir(new File (mesosDir));
       
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

    // mesosDir and installDir should be one and the same...test test test
    private void setAccumuloDir(String mesosDir, String accumuloVersion) {
        
        // Properties and Environment variables take precedence. This should not be set
        // for the executor....
        String home = determineValue(Environment.ACCUMULO_HOME, null);
        
        LOGGER.info("setAccumuloDir: ACCUMULO_HOME? " + home);
        
        if (StringUtils.isEmpty(home)) {
            home = createAccumuloPath(mesosDir, accumuloVersion); 
        }  
        serviceConfiguration.setAccumuloDir(new File(home));
    }
    
    private String createAccumuloPath(String dir, String accumuloVersion) {
        return dir + File.separator + Constants.ACCUMULO_DISTRO + File.separator + "accumulo-" + accumuloVersion;
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
