package aredee.mesos.frameworks.accumulo.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Environment {

    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);
   
    public static final String HADOOP_PREFIX = "HADOOP_PREFIX";
    public static final String HADOOP_CONF_DIR = "HADOOP_CONF_DIR";
    public static final String HADOOP_HOME = "HADOOP_HOME";
    
    public static final String ACCUMULO_HOME = "ACCUMULO_HOME";
    public static final String ACCUMULO_LOG_DIR = "ACCUMULO_LOG_DIR";
    public static final String ACCUMULO_CLIENT_CONF_PATH = "ACCUMULO_CLIENT_CONF_PATH";
    public static final String ACCUMULO_CONF_DIR = "ACCUMULO_CONF_DIR";
    public static final String ACCUMULO_WALOG = "ACCUMULO_WALOG";
   
    public static final String LD_LIBRARY_PATH = "LD_LIBRARY_PATH";
    public static final String DYLD_LIBRARY_PATH = "DYLD_LIBRARY_PATH";
   
    // List of paths separated by commas
    public static final String NATIVE_LIB_PATHS = "NATIVE_LIB_PATHS";
    
    public static final String ZOOKEEPER_HOME = "ZOOKEEPER_HOME";
 
    public static final List<String> REQUIRED_VARS = Arrays.asList(new String[]
            {ACCUMULO_HOME, HADOOP_PREFIX, HADOOP_CONF_DIR, ZOOKEEPER_HOME, ACCUMULO_CLIENT_CONF_PATH});

    public static List<String> getMissingVariables() {
         List<String> missingVariables = new ArrayList<>();
         for(String var : getRequiredVariables() ) {
            if (determineValue(var, null) == null) {
                missingVariables.add(var);
            }
        }
        return missingVariables;
    }

    public static List<String> getRequiredVariables() {
        return REQUIRED_VARS;
    }
    
    /**
     * The property can either be in the Environment or a System property, this really
     * eases unit testing.
     * 
     * @param name of environment variable or property
     * @param defaultValue
     * @return either value or defaultValue or null
     */
    public static String determineValue(String name, String defaultValue) {
        String value = System.getenv(name);
        
        if (StringUtils.isEmpty(value)) {
            value = System.getProperty(name);
        }
        
        if (StringUtils.isEmpty(value) && !StringUtils.isEmpty(defaultValue) ) {
            value = defaultValue;
        }
        
        LOGGER.debug("Environment name: " + name + " value: " + value );
      
        return value;
    }   

}
