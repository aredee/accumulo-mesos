package aredee.mesos.frameworks.accumulo.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class Environment {

    public static final String ACCUMULO_HOME = "ACCUMULO_HOME";
    public static final String HADOOP_PREFIX = "HADOOP_PREFIX";
    public static final String HADOOP_CONF_DIR = "HADOOP_CONF_DIR";

    public static final List<String> REQUIRED_VARS = Arrays.asList(new String[]
            {ACCUMULO_HOME, HADOOP_PREFIX, HADOOP_CONF_DIR});

    public static List<String> getMissingVariables() {
        List<String> req = Environment.getRequiredVariables();
        List<String> missingVariables = new ArrayList<>();
        Set<String> envKeys = System.getenv().keySet();
        for(String var : getRequiredVariables() ) {
            if(!envKeys.contains(var)){
                missingVariables.add(var);
            }
        }
        return missingVariables;
    }

    public static List<String> getRequiredVariables() {
        return REQUIRED_VARS;
    }

}
