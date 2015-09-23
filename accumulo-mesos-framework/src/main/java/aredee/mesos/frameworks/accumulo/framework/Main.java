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

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private WebServer webServer;

    public static void main(String[] args) throws IOException {

        if( !environmentVariablesAreSet() ){
            System.exit(-1);
        }

        CommandLineHandler cmdHandler = new CommandLineHandler(args);
        if( cmdHandler.checkHelpOrVersion() ){
            System.exit(0);  // checkHelpOrVersion prints appropriate info to System.out
        }

        Framework frameworkConfig = cmdHandler.getFrameworkDefinition();

        int exitStatus = -1;
        try {
            LOGGER.info("Starting mesos-accumumlo framework version " + Constants.FRAMEWORK_VERSION);
            LOGGER.info("Java Classpath: " + System.getProperty("java.class.path"));
            exitStatus = new Main().run(frameworkConfig);
        } catch (Exception e) {
            LOGGER.error("Unhandled exception encountered, exiting: ", e);
        }

        LOGGER.info("Exiting Accumulo Framework with status: " + exitStatus);
        System.exit(exitStatus);
    }

    private int run(Framework config) throws Exception {

        // before injector is created, Framework config must be validated/completely populated.

        // check zk for existing accumulo-mesos framework id/name pairs.
        State mesosState = new ZooKeeperState(config.getZkServers(),
                Defaults.ZOOKEEPER_TIMEOUT,
                Defaults.ZK_TIMEOUT_UNIT,
                Defaults.ZK_STATE_ZNODE );

        LOGGER.info("Connected to Zookeeper for mesos state: {} {}", mesosState.toString());

        FrameworkStateHelper stateHelper = new FrameworkStateHelper(mesosState);

        // Check if any accumulo-mesos frameworks have run here.
        if( !stateHelper.hasRegisteredFrameworks() ){
            // make sure Accumulo exists in config
            if( !config.hasCluster() ){
                LOGGER.error("No Accumulo Cluster Definiton");
                throw new IllegalStateException("No Accumulo Cluster definition exists");
            }
            // otherwise, things should be good to go.
        }

        // TODO check if state store exists in zookeeper
        boolean hasFrameworks = stateHelper.hasRegisteredFrameworks();

        // if config has a name, but no ID, it may either not exist yet or may be trying to restart with name.
        if( config.hasName() && !config.hasId() ){
            LOGGER.info("Found configuration with name, but no id ? {}", config.getName());
            // lookup id for name in state store
            if( hasFrameworks ){
                Map<String,String> nameMap = stateHelper.getFrameworkNameMap();
                String id = nameMap.get(config.getName());
                if( id != null ) {
                    config.setId(id);
                }
            } else if( config.hasCluster() ) {
                // Initializes accumulo or gets the instance from the state store if one exists.
                //
                AccumuloInitializer accumuloInitializer =
                        new AccumuloInitializer(config.getCluster());

                accumuloInitializer.initialize();

                // after initialization, save the config
                config.setId(UUID.randomUUID().toString());
                stateHelper.saveFrameworkConfig(config);
            }

        } else if( config.hasId() && !config.hasName() ){
            // lookup name from ZK using id.
            Map<String, String> idMap = stateHelper.getFrameworkIdMap();
            String name = idMap.get( config.getId() );
            if( name != null){
                config.setName(name);
            } else {
                throw new IllegalStateException("Found saved Framework Id without name");
            }
            // populate cluster information from state store
            if( !config.hasCluster() ){
                // TODO what if no saved config here? need test cases.
                try {
                    Framework savedConfig = stateHelper.getFrameworkConfig(config.getId());
                    config.setCluster(savedConfig.getCluster());
                } catch (Exception e){
                    throw new IllegalStateException("Could not find Cluster definition for framework: " + config.getId());
                }
            }
        }

        // Sanity check before firing up processes.
        assert( config.getId() != null );
        assert( config.getName() != null );
        assert( !config.getName().isEmpty() );
        assert( !config.getName().isEmpty() );
        assert( config.hasCluster() );

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
