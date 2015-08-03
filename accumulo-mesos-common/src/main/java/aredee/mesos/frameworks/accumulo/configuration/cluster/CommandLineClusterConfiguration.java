package aredee.mesos.frameworks.accumulo.configuration.cluster;

import aredee.mesos.frameworks.accumulo.configuration.Constants;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineClusterConfiguration extends BaseClusterConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineClusterConfiguration.class);

    // TODO maybe this should be more like how mesos slaves define their resources and attributes.
    private static final Options options;
    static {
        options = new Options();
        options.addOption("h", "help", false, "Print this message and exit");
        options.addOption("v", "version", false, "Show version number");
        options.addOption("P", "port", true, "Port number to serve HTTP interface");
        options.addOption("b", "bind-address", true, "IP address of interface to bind HTTP interface to");
        options.addOption("m", "master", true, "Location of mesos master to connect to");
        options.addOption("f", "framework-name", true, "Name of this mesos framework");
        options.addOption("z", "zookeepers", true, "List of Zookeeper servers");
        options.addOption("t", "tarball", true, "URI of framework tarball");
        options.addOption("j", "json", true, "JSON file containing configuration");
    }

    private static final Parser parser = new GnuParser();

    public CommandLineClusterConfiguration(CommandLine cmdLine){

        super();

        // Populate configuration
        if( cmdLine.hasOption('P') ){
            try {
                this.setHttpPort(Integer.parseInt(cmdLine.getOptionValue('P')));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port specification " + cmdLine.getOptionValue('P'));
            }
        }

        if( cmdLine.hasOption('b') ){
            this.setBindAddress( cmdLine.getOptionValue('b') );
        }

        if ( cmdLine.hasOption('m') ){
            this.setMesosMaster( cmdLine.getOptionValue('m'));
        }

        if ( cmdLine.hasOption('f') ){
            this.setFrameworkName(cmdLine.getOptionValue('f'));
        }

        if (cmdLine.hasOption('z') ){
            this.setZkServers( cmdLine.getOptionValue('z'));
        }

        if (cmdLine.hasOption('t')){
            this.setTarballUri( cmdLine.getOptionValue('t'));
        }
    }

    public static CommandLine parseArgs(String args[]) {
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e);
            printHelpAndExit();
        }
        return cmdLine;
    }

    // Parses command line options,
    public static void checkHelpOrVersion(CommandLine cmdLine) {

        // Handle exiting options
        if (cmdLine.hasOption('h')){
            printHelpAndExit();
        }
        if (cmdLine.hasOption('v')){
            System.out.format("%s : %s\n", Constants.EXE_NAME, Constants.FRAMEWORK_VERSION);
            System.exit(0);
        }
    }
    
    private static void printHelpAndExit(){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( Constants.EXE_NAME, options, true );
        System.exit(-1);
    }

}
