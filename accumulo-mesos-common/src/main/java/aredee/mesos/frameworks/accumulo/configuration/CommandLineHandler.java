package aredee.mesos.frameworks.accumulo.configuration;

import aredee.mesos.frameworks.accumulo.model.Accumulo;
import aredee.mesos.frameworks.accumulo.model.Framework;
import com.fasterxml.jackson.core.JsonParseException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;


public class CommandLineHandler  {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineHandler.class);

    private static final String OPTION_HELP = "h";
    private static final String OPTION_VERSION = "v";
    private static final String OPTION_PORT = "P";
    private static final String OPTION_BIND = "b";
    private static final String OPTION_MASTER = "m";
    private static final String OPTION_NAME = "n";
    private static final String OPTION_ZOOKEEPERS = "z";
    private static final String OPTION_TARBALL = "t";
    private static final String OPTION_FRAMEWORK = "f";
    private static final String OPTION_CLUSTER = "c";

    private static final Options options;
    static {
        options = new Options();
        options.addOption(OPTION_HELP, "help", false, "Print this message and exit");
        options.addOption(OPTION_VERSION, "version", false, "Show version number");
        options.addOption(OPTION_PORT, "port", true, "Port number to serve HTTP interface");
        options.addOption(OPTION_BIND, "bind-address", true, "IP address of interface to bind HTTP interface to");
        options.addOption(OPTION_MASTER, "master", true, "Location of mesos master to connect to");
        options.addOption(OPTION_NAME, "name", true, "Name of this mesos framework");
        options.addOption(OPTION_ZOOKEEPERS, "zookeepers", true, "List of Zookeeper servers");
        options.addOption(OPTION_TARBALL, "tarball", true, "URI of framework tarball");

        options.addOption(OPTION_FRAMEWORK, "framework", true, "JSON file of entire framework configuration");
        options.addOption(OPTION_CLUSTER, "cluster", true, "JSON file containing cluster configuration");
    }

    private static final Parser parser = new GnuParser();
    private final CommandLine cmdLine;

    public CommandLineHandler(CommandLine cmdLine){
        this.cmdLine = cmdLine;
    }

    public CommandLineHandler(String args[]){
        this(CommandLineHandler.parseArgs(args));
    }

    /**
     * Parses command line arguments for configuration information.
     *
     * Explicit options override anything in JSON. For example if JSON specifies
     * port as 8080 and commandline has -P 5150, the port will be 5150. The returned
     * object might not contain enough information to initialize and run a cluster.
     * This is because a recovery re-launch of the framework should just be able to
     * read the configuration from the Mesos store.
     *
     * @return Framework configuration
     *
     */
    public Framework getFrameworkDefinition() throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        Framework config = null;
        // Read full config if available
        if( cmdLine.hasOption(OPTION_FRAMEWORK)){
            File frameworkFile = new File(cmdLine.getOptionValue(OPTION_FRAMEWORK));
            config = mapper.readValue(frameworkFile, Framework.class);
        } else {
            config = new Framework();
        }

        // Override config with explicit command line options.
        if( cmdLine.hasOption(OPTION_PORT) ){
            config.setHttpPort(Integer.parseInt(cmdLine.getOptionValue(OPTION_PORT)));
        }

        if( cmdLine.hasOption(OPTION_BIND) ){
            config.setBindAddress(cmdLine.getOptionValue(OPTION_BIND));
        }

        if ( cmdLine.hasOption(OPTION_MASTER) ){
            config.setMesosMaster(cmdLine.getOptionValue(OPTION_MASTER));
        }

        if ( cmdLine.hasOption(OPTION_NAME) ){
            config.setName(cmdLine.getOptionValue(OPTION_NAME));
        }

        if (cmdLine.hasOption(OPTION_ZOOKEEPERS) ){
            config.setZkServers(cmdLine.getOptionValue(OPTION_ZOOKEEPERS));
        }

        if (cmdLine.hasOption(OPTION_TARBALL)){
            config.setTarballUri(cmdLine.getOptionValue(OPTION_TARBALL));
        }

        // If there's an Accumulo cluster JSON provided, override.
        if (cmdLine.hasOption(OPTION_CLUSTER)) {
            File clusterFile = new File(cmdLine.getOptionValue(OPTION_CLUSTER));
            Accumulo clusterConfig = mapper.readValue(clusterFile, Accumulo.class);
            config.setCluster(clusterConfig);
        }

        LOGGER.info("Configuration after command line handling: " + config.toString());

        return config;
    }

    private static CommandLine parseArgs(String args[]) {
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {

            System.err.println(e);
        }
        return cmdLine;
    }

    // print help or version information.
    public boolean checkHelpOrVersion() {

        // Handle exiting options
        if (cmdLine.hasOption('h')){
            printHelp();
            return true;
        }
        if (cmdLine.hasOption('v')){
            printVersion();
            return true;
        }
        return false;
    }

    private void printVersion(){
        System.out.format("%s : %s\n", Constants.EXE_NAME, Constants.FRAMEWORK_VERSION);
    }

    private void printHelp(){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( Constants.EXE_NAME, options, true );
    }

}
