package aredee.mesos.frameworks.accumulo.executor;

import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){

        // TODO check that required environment variables are set
        // perhaps source accumulo-site.env file?

        // TODO parse args, first should be server type, check others based on type of server.
        LOGGER.info("Parsing Executor Arguments: " + args);

        // start the executor process
        StartServerExecutor executor = new StartServerExecutor(args);

        ExecutorDriver executorDriver = new MesosExecutorDriver(executor);
        final int status;
        // run() blocks until driver finishes somehow
        switch (executorDriver.run()) {
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

        // Ensure that the driver process terminates.
        LOGGER.info("Executor Driver stopped.");
        executorDriver.stop();

        LOGGER.info("Exiting executor process");
        System.exit(status);
    }


}
