package aredee.mesos.frameworks.accumulo.executor;

import aredee.mesos.frameworks.accumulo.configuration.Environment;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){

        //TODO Should executors have REST API?

        // TODO check for required environment variables
        List<String> missingVars = Environment.getMissingVariables(Environment.REQUIRED_EXECUTOR_VARS);
        if( !missingVars.isEmpty() ){
            LOGGER.error("Missing required environment variables {}", missingVars);
            System.err.println("Missing required environment variables: " + missingVars);
            System.exit(-1);
        }

        // start the executor process
        AccumuloStartExecutor executor = new AccumuloStartExecutor();

        ExecutorDriver executorDriver = new MesosExecutorDriver(executor);
        
        // run() blocks until driver finishes somehow    
        final Protos.Status runStatus = executorDriver.run();
  
        // Ensure that the driver process terminates.
        LOGGER.info("Executor Driver stopped, with value " + runStatus.name());
        executorDriver.stop();

        LOGGER.info("Exiting executor process");
        System.exit(runStatus.getNumber());
    }


}
