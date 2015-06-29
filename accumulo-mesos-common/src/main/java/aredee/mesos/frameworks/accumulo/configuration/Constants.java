package aredee.mesos.frameworks.accumulo.configuration;

import java.util.ResourceBundle;

public final class Constants {

    // bundler used for items defined by Maven
    private static final ResourceBundle rb = ResourceBundle.getBundle("accumulo-mesos");

    public static final String FRAMEWORK_VERSION = rb.getString("application.version");
    public static final String EXE_NAME = rb.getString("application.name");

    public static final String EXECUTOR_JAR = rb.getString("application.name.executor")+"-"+FRAMEWORK_VERSION+".jar";

}
