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

import aredee.mesos.frameworks.accumulo.configuration.CommandLineHandler;
import aredee.mesos.frameworks.accumulo.configuration.Constants;
import aredee.mesos.frameworks.accumulo.configuration.Defaults;
import aredee.mesos.frameworks.accumulo.configuration.Environment;
import aredee.mesos.frameworks.accumulo.framework.api.WebServer;
import aredee.mesos.frameworks.accumulo.framework.guice.ApiServletModule;
import aredee.mesos.frameworks.accumulo.framework.guice.ConfigurationModule;
import aredee.mesos.frameworks.accumulo.initialize.AccumuloInitializer;
import aredee.mesos.frameworks.accumulo.model.Framework;
import aredee.mesos.frameworks.accumulo.scheduler.Cluster;
import aredee.mesos.frameworks.accumulo.scheduler.Scheduler;
import aredee.mesos.frameworks.accumulo.state.FrameworkStateHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.SchedulerDriver;
import org.apache.mesos.state.State;
import org.apache.mesos.state.ZooKeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private WebServer webServer;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {


        CommandLineHandler cmdHandler = new CommandLineHandler(args);
        if( cmdHandler.checkHelpOrVersion() ){
            System.exit(0);  // checkHelpOrVersion prints appropriate info to System.out
        }

        if( !environmentVariablesAreSet() ){
            System.exit(-1);
        }

        Framework frameworkConfig = cmdHandler.getFrameworkDefinition();

        LOGGER.info("Connecting to Mesos state");
        State state = getMesosState(frameworkConfig);
        FrameworkStateHelper stateHelper = new FrameworkStateHelper(state);

        // at this point perhaps only the framework name was given, so
        // read framework config
        String frameworkName = frameworkConfig.getName();
        Map<String,String> frameworks = stateHelper.getFrameworkNameMap();
        LOGGER.info("frameworks ? {}", frameworks);
        if( frameworks.containsKey(frameworkName) ){
            LOGGER.info("Found previous framework with name {}", frameworkName);

            Framework savedConfig = stateHelper.getFrameworkConfig(frameworks.get(frameworkName));
            LOGGER.info("Found saved framework config: {}", savedConfig );
            frameworkConfig.merge(savedConfig);
            LOGGER.info("Merged command line configuration with saved configuration. Command line trumps.");
        }

        // check if this is an initialization request
        if( cmdHandler.isInitializeRequest() ){

            LOGGER.info("Initializing Accumulo Instance: {}", frameworkConfig.getCluster().getInstance());
            AccumuloInitializer accumuloInitializer =
                    new AccumuloInitializer(frameworkConfig.getCluster());

            int status = accumuloInitializer.initialize();
            if( status != 0 ){
                LOGGER.error("Accumulo failed to initialize instance: {}", frameworkConfig.getCluster().getInstance());
                LOGGER.error("Either remove instance from HDFS or re-launch framework without init option");
                System.exit(status);
            } else {
                LOGGER.info("Accumulo instance initialized: {}", frameworkConfig.getCluster().getInstance());
            }

            // after initialization, save the config
            frameworkConfig.setId(UUID.randomUUID().toString());

            try {
                stateHelper.saveFrameworkConfig(frameworkConfig);
                LOGGER.info("Saved Framework Configuration: {}", frameworkConfig.getId());

            } catch (Exception e) {
                LOGGER.error("Unable to save framework configuration to Mesos State");
                System.err.println("Unable to save framework configuration to Mesos State");
                e.printStackTrace();
                System.exit(1);
            }
        } else {

            int exitStatus = -1;
            try {
                LOGGER.info("Starting mesos-accumumlo framework version " + Constants.FRAMEWORK_VERSION);
                exitStatus = new Main().run(frameworkConfig, state);
            } catch (Exception e) {
                LOGGER.error("Unhandled exception encountered, exiting: ", e);
            }

            LOGGER.info("Exiting Accumulo Framework with status: " + exitStatus);
            System.exit(exitStatus);
        }

        System.exit(0); // necessary for init to finish because state launches a process
    }

    private int run(Framework config, State mesosState) throws Exception {

        // Sanity check before firing up processes.
        LOGGER.info("Preconditions check: {}", config);

        Preconditions.checkState(!Strings.isNullOrEmpty(config.getId()));
        Preconditions.checkState( !Strings.isNullOrEmpty(config.getName()));
        Preconditions.checkState( config.hasCluster(), "No cluster definition found, exiting" );

        // Initialize the Cluster singleton. Do this before creating the webservice in case the
        // webservice touches it.
        Cluster cluster = Cluster.INSTANCE;
        cluster.initialize(config, mesosState);

        Injector injector = Guice.createInjector(
                Stage.PRODUCTION,
                new ConfigurationModule(config),
                new ApiServletModule()
        );

        startWebserverOrDie(injector);
        
        // Start the schedulerDriver
        LOGGER.info("Initializing mesos-accumulo Scheduler");
  
        FrameworkInfo frameworkInfo = FrameworkInfo.newBuilder()
                .setId(createMesosFrameworkID(config.getId())) // empty string creates new random name
                .setName(config.getName())
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

    private static State getMesosState(Framework config){
        return new ZooKeeperState(config.getZkServers(),
                Defaults.ZOOKEEPER_TIMEOUT,
                Defaults.ZK_TIMEOUT_UNIT,
                Defaults.ZK_STATE_ZNODE );
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
            throw new RuntimeException("Unable to launch webserver");
        }
        this.webServer = webServer;
    }


    private static boolean environmentVariablesAreSet(){
        // Check that environment Variables are defined
        List<String> missingEnv = Environment.getMissingVariables(Environment.REQUIRED_FRAMEWORK_VARS);
        if( !missingEnv.isEmpty() ){
            LOGGER.error("Missing environment variables {} ", missingEnv);
            System.err.println("Missing Environments Variables:");
            for(String env: missingEnv){
                System.err.println(env);
            }
            System.err.println("Define environment and restart");
            return false;
        }
        return true;
    }
}
