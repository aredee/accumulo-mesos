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

import aredee.mesos.frameworks.accumulo.framework.api.WebServer;
import aredee.mesos.frameworks.accumulo.configuration.CommandLineConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.Configuration;
import aredee.mesos.frameworks.accumulo.configuration.FrameworkStateProxy;
import aredee.mesos.frameworks.accumulo.framework.guice.ApiServletModule;
import aredee.mesos.frameworks.accumulo.framework.guice.ConfigurationModule;
import aredee.mesos.frameworks.accumulo.scheduler.Cluster;
import aredee.mesos.frameworks.accumulo.scheduler.Scheduler;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.apache.commons.cli.CommandLine;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.SchedulerDriver;
import org.apache.mesos.state.ZooKeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;


public final class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        // initialize the config object
        CommandLine cmdLine = CommandLineConfiguration.parseArgs(args);
        CommandLineConfiguration.checkHelpOrVersion(cmdLine);  // early exit

        // TODO check commandline for .yml or properties file

        // create injector with command line
        Configuration config = new CommandLineConfiguration(cmdLine);

        int exitStatus = -1;
        try {
            exitStatus = new Main().run(config, cmdLine.getArgs());
        } catch (Exception e) {
            LOGGER.error("Unhandled exception encountered, exiting: " + e.getMessage());
            e.printStackTrace();
        }

        LOGGER.info("Exiting Accumulo Framework with status: " + exitStatus);
        System.exit(exitStatus);
    }

    private int run(Configuration config, String[] args) throws Exception{

        Injector injector = Guice.createInjector(
                Stage.PRODUCTION,
                new ConfigurationModule(config),
                new ApiServletModule()
        );

        // Start the webserver
        final WebServer webServer = injector.getInstance(WebServer.class);
        try {
            webServer.start();
        } catch (Exception e) {
            //TODO throw some webserver fail exception
            e.printStackTrace();
            System.exit(-1);
        }

        // Check Framework State to see if this is a failover framework.
        org.apache.mesos.state.State frameworkState = new ZooKeeperState(config.getZkServers(), 60L, TimeUnit.SECONDS, "accumulo-mesos" );
        FrameworkStateProxy stateProxy = new FrameworkStateProxy(frameworkState);
        Set<FrameworkStateProxy.FrameworkTuple> frameworkTuples = stateProxy.getFrameworkTuples();
        boolean frameworkExists = false;
        String frameworkId = "";
        String accumuloInstance = "";
        for(FrameworkStateProxy.FrameworkTuple tuple: frameworkTuples){
            if( tuple.name.contentEquals(config.getFrameworkName()) ){
                frameworkExists = true;
                frameworkId = tuple.id;
                // read accumulo instance location from state
                accumuloInstance = stateProxy.getAccumuloInstance(frameworkId);
                break;
            }
        }

        if( frameworkExists ){

        }



        // TODO reconcile registered frameworks with framework name saved here. If name exists, grab the ID and pass that
        //      into the the framework info when creating the scheduler driver.
        //      make this work with framework id and/or framework name.

        // Start the schedulerDriver
        String master = config.getMesosMaster();

        Cluster cluster = new Cluster(config);
        if( frameworkExists ){
            cluster.setAccumuloInstance(accumuloInstance);
        }
        org.apache.mesos.Scheduler scheduler = new Scheduler(cluster);


        FrameworkInfo frameworkInfo = FrameworkInfo.newBuilder()
                .setName(config.getFrameworkName())
                .setCheckpoint(true)
                .setHostname("") // let mesos set to current hostname
                //.setId()
                //.setPrincipal()
                //.setRole()
                .setUser("")  // empty string is current user of mesos process
                .setWebuiUrl("http://" + config.getBindAddress() + ":" + config.getHttpPort() + "/")
                .build();


        final SchedulerDriver schedulerDriver =
                new MesosSchedulerDriver( scheduler, frameworkInfo, master);

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
}
