/**
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aredee.mesos.frameworks.accumulo.framework;

import aredee.mesos.frameworks.accumulo.Protos;
import aredee.mesos.frameworks.accumulo.configuration.*;
import aredee.mesos.frameworks.accumulo.process.AccumuloProcessFactory;
import aredee.mesos.frameworks.accumulo.state.FrameworkStateProtobufPersister;
import aredee.mesos.frameworks.accumulo.framework.api.WebServer;
import aredee.mesos.frameworks.accumulo.framework.guice.ApiServletModule;
import aredee.mesos.frameworks.accumulo.framework.guice.ConfigurationModule;
import aredee.mesos.frameworks.accumulo.scheduler.Cluster;
import aredee.mesos.frameworks.accumulo.scheduler.Scheduler;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.apache.accumulo.core.conf.SiteConfiguration;
import org.apache.accumulo.server.Accumulo;
import org.apache.accumulo.server.conf.ServerConfiguration;
import org.apache.accumulo.server.init.Initialize;
import org.apache.commons.cli.CommandLine;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.SchedulerDriver;
import org.apache.mesos.state.ZooKeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private WebServer webServer;

    public static void main(String[] args) {


        // initialize the config object
        CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);
        CommandLineClusterConfiguration.checkHelpOrVersion(cmdLine);  // early exit

        // TODO check commandline for .yml or properties file

        // create injector with command line
        ClusterConfiguration config = new CommandLineClusterConfiguration(cmdLine);

        int exitStatus = -1;
        try {
            LOGGER.info("Starting mesos-accumumlo framework version " + Constants.FRAMEWORK_VERSION);
            LOGGER.info("Java Classpath: " + System.getProperty("java.class.path"));
            exitStatus = new Main().run(config, cmdLine.getArgs());
        } catch (Exception e) {
            LOGGER.error("Unhandled exception encountered, exiting: " + e.getMessage());
            e.printStackTrace();
        }

        LOGGER.info("Exiting Accumulo Framework with status: " + exitStatus);
        System.exit(exitStatus);
    }

    private int run(ClusterConfiguration config, String[] args) throws Exception{

        Injector injector = Guice.createInjector(
                Stage.PRODUCTION,
                new ConfigurationModule(config),
                new ApiServletModule()
        );


        checkEnvironmentOrDie();
        startWebserverOrDie(injector);

        LOGGER.info("Checking Framework State");
        // Check Framework State to see if this is a failover framework.
        LOGGER.info("Reading from ZooKeeperState: {}", config.getZkServers() );
        org.apache.mesos.state.State frameworkState = new ZooKeeperState(config.getZkServers(), 60L, TimeUnit.SECONDS, "accumulo-mesos" );
        FrameworkStateProtobufPersister stateProxy = new FrameworkStateProtobufPersister(frameworkState);
        boolean frameworkExists = false;
        String frameworkId = "";
        String frameworkName = "";
        LOGGER.info("Fetching frameworks from ZooKeeper");
        for(Protos.FrameworkIdentity fid : stateProxy.getFrameworks()){
            LOGGER.info("Found framework {} - {}", fid.getFrameworkName(), fid.getFrameworkId());
            if( fid.getFrameworkName().equals(config.getFrameworkName()) ){
                LOGGER.info("Found matching framework");
                frameworkExists = true;
                frameworkId = fid.getFrameworkId();
                frameworkName = fid.getFrameworkName();
                break;
            }
        }

        String accumuloInstanceName = "";
        if( frameworkExists ){
            LOGGER.info("Found Existing Accumulo Instance");
            accumuloInstanceName = stateProxy.getAccumuloInstanceName(frameworkId);
        } else {
            LOGGER.info("Framework not found, Initializing New Accumulo Instance");
            accumuloInstanceName = initializeAccumuloInstance(config);

            //TODO save new instance info to Framework State
            frameworkName = config.getFrameworkName();
        }

        // TODO reconcile registered frameworks with framework name saved here.
        //     If name exists, grab the ID and pass that
        //     into the the framework info when creating the scheduler driver.
        //     make this work with framework id and/or framework name.

        // Start the schedulerDriver
        LOGGER.info("Initializing mesos-accumulo Scheduler");
        String master = config.getMesosMaster();
        config.setAccumuloInstanceName(accumuloInstanceName);

        Cluster cluster = new Cluster(frameworkState, config);

        org.apache.mesos.Scheduler scheduler = new Scheduler(cluster);

        FrameworkInfo frameworkInfo = FrameworkInfo.newBuilder()
                .setId(createMesosFrameworkID(frameworkId)) // empty string creates new random name
                .setName(frameworkName)
                .setCheckpoint(true)
                .setHostname("") // let mesos set to current hostname
                .setUser("")  // empty string is current user of mesos process
                .setWebuiUrl("http://" + config.getBindAddress() + ":" + config.getHttpPort() + "/")
                .build();

        final SchedulerDriver schedulerDriver =
                new MesosSchedulerDriver( scheduler, frameworkInfo, master);

        LOGGER.info("Running mesos-accumulo SchedulerDriver");
        final int status;
        // run() blocks until driver finishes somehow
        switch (schedulerDriver.run()) {
            case DRIVER_STOPPED:
                status = 0;
                break;
            case DRIVER_ABORTED:
                status = 1;
                break;
            case DRIVER_NOT_STARTED:
                status = 2;
                break;
            default:
                status = 3;
                break;
        }

        webServer.stop();

        // Ensure that the driver process terminates.
        schedulerDriver.stop(true);

        return status;
    }

    private static org.apache.mesos.Protos.FrameworkID createMesosFrameworkID(String frameworkId){
        return org.apache.mesos.Protos.FrameworkID.newBuilder()
                .setValue(frameworkId)
                .build();
    }

    private void startWebserverOrDie(Injector injector){
        // Start the webserver
        LOGGER.info("Starting Framework Webserver");
        final WebServer webServer = injector.getInstance(WebServer.class);
        try {
            webServer.start();
        } catch (Exception e) {
            //TODO throw some webserver fail exception?
            LOGGER.error("Failed to instantiate webservice.");
            e.printStackTrace();
            System.exit(-1);
        }
        this.webServer = webServer;
    }


    private void checkEnvironmentOrDie(){
        // Check that environment Variables are defined
        List<String> missingEnv = Environment.getMissingVariables();
        if( !missingEnv.isEmpty() ){
            LOGGER.error("Missing environment variables {} ", missingEnv);
            System.err.println("Missing Environments Variables:");
            for(String env: missingEnv){
                System.err.println(env);
            }
            System.err.println("Define environment and restart");
            System.exit(-1);
        }
    }

    private String initializeAccumuloInstance(ClusterConfiguration config){
        // run accumulo init procedure
        LOGGER.info("Writing accumulo-site.xml");
        File accumuloSiteFile = writeAccumuloSiteClasspathsOrDie();

        String accumuloInstanceName = config.getAccumuloInstanceName();
        LinkedList<String> initArgs  = new LinkedList<>();
        initArgs.add("--instance-name");
        initArgs.add(accumuloInstanceName);
        // TODO handle SASL see MiniAccumuloClusterImpl
        initArgs.add("--password");
        initArgs.add(config.getAccumuloRootPassword());


        ProcessConfiguration processConfiguration = new ProcessConfiguration();
        AccumuloProcessFactory processFactory = new AccumuloProcessFactory(processConfiguration);
        File accumuloConfDir = accumuloSiteFile.getParentFile();
        File hadoopConfDir = new File(System.getenv(Environment.HADOOP_CONF_DIR));

        processConfiguration.setDir(accumuloConfDir.getParentFile());
        processConfiguration.setClientConfFile(accumuloConfDir);
        processConfiguration.setLibDir(new File(processConfiguration.getDir(), "lib"));
        processConfiguration.setLibExtDir(new File(processConfiguration.getLibDir(), "ext"));
        processConfiguration.setLogDir(new File(processConfiguration.getDir(), "logs"));
        processConfiguration.setConfDir(accumuloConfDir);
        processConfiguration.setHadoopConfDir(hadoopConfDir);

        Process initProcess = null;
        try {
            initProcess = processFactory.exec(Initialize.class, null, initArgs.toArray(new String[initArgs.size()]));
            initProcess.waitFor();
            LOGGER.info("New Accumulo instance initialized");
        } catch (IOException ioe) {
            LOGGER.error("IOException while trying to initialize Accumulo", ioe);
            System.exit(-1);
        } catch (InterruptedException ie) {
            LOGGER.error("Init process was interrupted", ie);
            System.exit(-1);
        }
        //Initialize.main(initArgs.toArray(new String[initArgs.size()]));


        return accumuloInstanceName;
    }

    private File writeAccumuloSiteClasspathsOrDie() {

        File accumuloSiteFile = null;

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("configuration");
            doc.appendChild(rootElement);

            Element propertyElement = doc.createElement("property");
            rootElement.appendChild(propertyElement);

            Element nameElement = doc.createElement("name");
            nameElement.appendChild(doc.createTextNode("general.classpaths"));
            propertyElement.appendChild(nameElement);

            Element valueElement = doc.createElement("value");
            valueElement.appendChild(doc.createTextNode(getGeneralClasspathsLiteral()));
            propertyElement.appendChild(valueElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            String accumulo_home = System.getenv(Environment.ACCUMULO_HOME);
            accumuloSiteFile = new File(accumulo_home + File.separator +
                    "conf" + File.separator + "accumulo-site.xml");
            StreamResult result = new StreamResult(accumuloSiteFile);

            transformer.transform(source, result);

        } catch (ParserConfigurationException e) {
            logErrorAndDie("Error Creating accumulo-site.xml\n" +
                            e.getMessage());
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            logErrorAndDie("Error Creating accumulo-site.xml\n" +
                            e.getMessage());
            e.printStackTrace();
        } catch (TransformerException e) {
            logErrorAndDie("Error Creating accumulo-site.xml\n" +
                            e.getMessage());
            e.printStackTrace();
        }

        return accumuloSiteFile;
    }

    private void logErrorAndDie(String message){
        LOGGER.error(message);
        System.exit(-1);
    }

    private static final String getGeneralClasspathsLiteral(){
        return (new StringBuilder())
                .append("\n$ACCUMULO_HOME/lib/accumulo-server.jar,\n")
                .append("$ACCUMULO_HOME/lib/accumulo-core.jar,\n")
                .append("$ACCUMULO_HOME/lib/accumulo-start.jar,\n")
                .append("$ACCUMULO_HOME/lib/accumulo-fate.jar,\n")
                .append("$ACCUMULO_HOME/lib/accumulo-proxy.jar,\n")
                .append("$ACCUMULO_HOME/lib/[^.].*.jar,\n")
                .append("$ZOOKEEPER_HOME/zookeeper[^.].*.jar,\n")
                .append("$HADOOP_CONF_DIR,\n")
                .append("$HADOOP_PREFIX/share/hadoop/common/[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/share/hadoop/common/lib/(?!slf4j)[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/share/hadoop/hdfs/[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/share/hadoop/mapreduce/[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/share/hadoop/yarn/[^.].*.jar,\n")
                .append("/usr/lib/hadoop/[^.].*.jar,\n")
                .append("/usr/lib/hadoop/lib/[^.].*.jar,\n")
                .append("/usr/lib/hadoop-hdfs/[^.].*.jar,\n")
                .append("/usr/lib/hadoop-mapreduce/[^.].*.jar,\n")
                .append("/usr/lib/hadoop-yarn/[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/lib/(?!slf4j)[^.].*.jar\n")
                .toString();
    }

/*
 * Useful code from MiniAccumuloClusterImpl.java
 *
    private Process exec(Class<?> clazz, String... args) throws IOException {
        return exec(clazz, null, args);
    }

    private Process exec(Class<?> clazz, List<String> jvmArgs, String... args) throws IOException {
        ArrayList<String> jvmArgs2 = new ArrayList<String>(1 + (jvmArgs == null ? 0 : jvmArgs.size()));
        jvmArgs2.add("-Xmx" + config.getDefaultMemory());
        if (jvmArgs != null)
            jvmArgs2.addAll(jvmArgs);
        Process proc = _exec(clazz, jvmArgs2, args);
        cleanup.add(proc);
        return proc;
    }

    private Process _exec(Class<?> clazz, List<String> extraJvmOpts, String... args) throws IOException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = getClasspath();

        String className = clazz.getName();

        ArrayList<String> argList = new ArrayList<String>();
        argList.addAll(Arrays.asList(javaBin, "-Dproc=" + clazz.getSimpleName(), "-cp", classpath));
        argList.addAll(extraJvmOpts);
        for (Map.Entry<String,String> sysProp : config.getSystemProperties().entrySet()) {
            argList.add(String.format("-D%s=%s", sysProp.getKey(), sysProp.getValue()));
        }
        // @formatter:off
        argList.addAll(Arrays.asList(
                "-XX:+UseConcMarkSweepGC",
                "-XX:CMSInitiatingOccupancyFraction=75",
                "-Dapple.awt.UIElement=true",
                "-Djava.net.preferIPv4Stack=true",
                "-XX:+PerfDisableSharedMem",
                "-XX:+AlwaysPreTouch",
                org.apache.accumulo.start.Main.class.getName(), className));
        // @formatter:on
        argList.addAll(Arrays.asList(args));

        ProcessBuilder builder = new ProcessBuilder(argList);

        builder.environment().put("ACCUMULO_HOME", config.getDir().getAbsolutePath());
        builder.environment().put("ACCUMULO_LOG_DIR", config.getLogDir().getAbsolutePath());
        builder.environment().put("ACCUMULO_CLIENT_CONF_PATH", config.getClientConfFile().getAbsolutePath());
        String ldLibraryPath = Joiner.on(File.pathSeparator).join(config.getNativeLibPaths());
        builder.environment().put("LD_LIBRARY_PATH", ldLibraryPath);
        builder.environment().put("DYLD_LIBRARY_PATH", ldLibraryPath);

        // if we're running under accumulo.start, we forward these env vars
        String env = System.getenv("HADOOP_PREFIX");
        if (env != null)
            builder.environment().put("HADOOP_PREFIX", env);
        env = System.getenv("ZOOKEEPER_HOME");
        if (env != null)
            builder.environment().put("ZOOKEEPER_HOME", env);
        builder.environment().put("ACCUMULO_CONF_DIR", config.getConfDir().getAbsolutePath());
        // hadoop-2.2 puts error messages in the logs if this is not set
        builder.environment().put("HADOOP_HOME", config.getDir().getAbsolutePath());
        if (config.getHadoopConfDir() != null)
            builder.environment().put("HADOOP_CONF_DIR", config.getHadoopConfDir().getAbsolutePath());

        Process process = builder.start();

        LogWriter lw;
        lw = new LogWriter(process.getErrorStream(), new File(config.getLogDir(), clazz.getSimpleName() + "_" + process.hashCode() + ".err"));
        logWriters.add(lw);
        lw.start();
        lw = new LogWriter(process.getInputStream(), new File(config.getLogDir(), clazz.getSimpleName() + "_" + process.hashCode() + ".out"));
        logWriters.add(lw);
        lw.start();

        return process;
    }

    Process _exec(Class<?> clazz, ServerType serverType, String... args) throws IOException {

        List<String> jvmOpts = new ArrayList<String>();
        jvmOpts.add("-Xmx" + config.getMemory(serverType));

        if (config.isJDWPEnabled()) {
            Integer port = PortUtils.getRandomFreePort();
            jvmOpts.addAll(buildRemoteDebugParams(port));
            debugPorts.add(new Pair<ServerType,Integer>(serverType, port));
        }
        return _exec(clazz, jvmOpts, args);
    }

*/
}
