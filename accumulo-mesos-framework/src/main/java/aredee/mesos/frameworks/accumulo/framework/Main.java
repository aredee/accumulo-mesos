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

import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.cluster.CommandLineClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.Constants;
import aredee.mesos.frameworks.accumulo.configuration.Environment;
import aredee.mesos.frameworks.accumulo.configuration.cluster.JSONClusterConfiguration;
import aredee.mesos.frameworks.accumulo.framework.api.WebServer;
import aredee.mesos.frameworks.accumulo.framework.guice.ApiServletModule;
import aredee.mesos.frameworks.accumulo.framework.guice.ConfigurationModule;
import aredee.mesos.frameworks.accumulo.initialize.AccumuloInitializer;
import aredee.mesos.frameworks.accumulo.scheduler.Cluster;
import aredee.mesos.frameworks.accumulo.scheduler.Scheduler;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.apache.commons.cli.CommandLine;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private WebServer webServer;

    public static void main(String[] args) {

        // initialize the config object
        CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);
        CommandLineClusterConfiguration.checkHelpOrVersion(cmdLine);  // early exit

        // create injector with command line
        ClusterConfiguration clusterConfiguration;
        if( cmdLine.hasOption('j') ){
            // JSON file specified
            clusterConfiguration = new JSONClusterConfiguration(cmdLine.getOptionValue('j'));
        } else {
            // parse all the command line options
            clusterConfiguration = new CommandLineClusterConfiguration(cmdLine);
        }

        int exitStatus = -1;
        try {
            LOGGER.info("Starting mesos-accumumlo framework version " + Constants.FRAMEWORK_VERSION);
            LOGGER.info("Java Classpath: " + System.getProperty("java.class.path"));
            exitStatus = new Main().run(clusterConfiguration, cmdLine.getArgs());
        } catch (Exception e) {
            LOGGER.error("Unhandled exception encountered, exiting: ", e);
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
        
        // Initializes accumulo or gets the instance from the state store if one exists.
        //
        AccumuloInitializer accumuloInitializer = new AccumuloInitializer(config);
   
        // TODO reconcile registered frameworks with framework name saved here.
        //     If name exists, grab the ID and pass that
        //     into the the framework info when creating the scheduler driver.
        //     make this work with framework id and/or framework name.

        // Start the schedulerDriver
        LOGGER.info("Initializing mesos-accumulo Scheduler");
  
        Cluster cluster = new Cluster(accumuloInitializer);

        FrameworkInfo frameworkInfo = FrameworkInfo.newBuilder()
                .setId(createMesosFrameworkID(accumuloInitializer.getFrameworkId())) // empty string creates new random name
                .setName(accumuloInitializer.getFrameworkName())
                .setCheckpoint(true)
                .setHostname("") // let mesos set to current hostname
                .setUser("")  // empty string is current user of mesos process
                .setWebuiUrl("http://" + config.getBindAddress() + ":" + config.getHttpPort() + "/")
                .build();

        final SchedulerDriver schedulerDriver =
                new MesosSchedulerDriver( new Scheduler(cluster), frameworkInfo, config.getMesosMaster());

        LOGGER.info("Running mesos-accumulo SchedulerDriver");
        final int status;
        
        // run() blocks until driver finishes somehow
        org.apache.mesos.Protos.Status driverStatus = schedulerDriver.run();
        
        switch (driverStatus) {
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
        LOGGER.info("mesos-accumulo stopped with status " +
                driverStatus.name() + " " + driverStatus.getNumber());

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
            LOGGER.error("Failed to instantiate webservice.", e);
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
}
