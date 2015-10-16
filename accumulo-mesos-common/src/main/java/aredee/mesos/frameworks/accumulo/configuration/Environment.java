package aredee.mesos.frameworks.accumulo.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class Environment {

    public static final String JAVA_HOME = "JAVA_HOME";

    public static final String CLASSPATH = "CLASSPATH";

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

    public static final String MESOS_DIRECTORY = "MESOS_DIRECTORY";

    public static final List<String> REQUIRED_FRAMEWORK_VARS = Arrays.asList(
                   JAVA_HOME,
                   ACCUMULO_HOME,
                   HADOOP_PREFIX,
                   HADOOP_CONF_DIR,
                   ZOOKEEPER_HOME);

    public static final List<String> REQUIRED_EXECUTOR_VARS = Arrays.asList(
                   JAVA_HOME,
                   ACCUMULO_HOME,
                   HADOOP_PREFIX,
                   ZOOKEEPER_HOME,
                   ACCUMULO_LOG_DIR);


    /*
        Optional Accumulo vars for executor
     */
    public static final String ACCUMULO_XTRAJARS = "ACCUMULO_XTRAJARS";
    public static final String ACCUMULO_GENERAL_OPTS = "ACCUMULO_GENERAL_OPTS";
    public static final String ACCUMULO_JAAS_CONF = "ACCUMULO_JAAS_CONF"; //-Djava.security.auth.login.config=${ACCUMULO_JAAS_CONF}
    public static final String ACCUMULO_KRB5_CONF = "ACCUMULO_KRB5_CONF"; //-Djava.security.krb5.conf=${ACCUMULO_KRB5_CONF}

    public static final String ACCUMULO_MASTER_OPTS = "ACCUMULO_MASTER_OPTS";
    public static final String ACCUMULO_GC_OPTS = "ACCUMULO_GC_OPTS";
    public static final String ACCUMULO_TSERVER_OPTS = "ACCUMULO_TSERVER_OPTS";
    public static final String ACCUMULO_MONITOR_OPTS = "ACCUMULO_MONITOR_OPTS";
    public static final String ACCUMULO_LOGGER_OPTS = "ACCUMULO_LOGGER_OPTS";
    public static final String ACCUMULO_OTHER_OPTS = "ACCUMULO_OTHER_OPTS";
    public static final String ACCUMULO_KILL_CMD = "ACCUMULO_KILL_CMD";

    // generated
    public static final String START_JAR = "START_JAR";
    public static final String LIB_PATH = "LIB_PATH";
    /*
    if [ -e "${HADOOP_PREFIX}/lib/native/libhadoop.so" ]; then
            LIB_PATH="${HADOOP_PREFIX}/lib/native"
    LD_LIBRARY_PATH="${LIB_PATH}:${LD_LIBRARY_PATH}"     # For Linux
    DYLD_LIBRARY_PATH="${LIB_PATH}:${DYLD_LIBRARY_PATH}" # For Mac
    fi
    */


    public static List<String> getMissingVariables(List<String> vars) {
        List<String> missingVariables = new ArrayList<>();
        Set<String> envKeys = System.getenv().keySet();
        for(String var : vars ) {
            if(!envKeys.contains(var)){
                missingVariables.add(var);
            }
        }
        return missingVariables;
    }

    public static String get(String var){
        return System.getenv(var);
    }

}
