package aredee.mesos.frameworks.accumulo.configuration;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

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
        options.addOption("j", "json-config-location", true, "Instead of many command line args use json");
       // options.addOption("y", "yaml-config-location", true, "Instead of many command line args use yaml");
        
    }
    private static final Parser parser = new GnuParser();

    public static ClusterConfiguration getConfiguration(CommandLine cmdLine) {
        
        ClusterConfiguration cc;
        if (cmdLine.hasOption('j')) {
            cc = getClusterFromJson(cmdLine.getOptionValue('j'));
        } else {
            cc = new CommandLineClusterConfiguration(cmdLine);    
        }
        //validateInput(cc, cmdLine);
        return cc;
    }
   
    public static CommandLine parseArgs(String args[]) {
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e);
            printHelpAndExit();
        }
        LOGGER.info("CommandLine? " + cmdLine.getArgList());
        return cmdLine;
    }

    // Parses command line options,
    public static void checkHelpOrVersion(CommandLine cmdLine) {

        // Handle exiting options
        if (cmdLine.hasOption('h')){
            printHelpAndExit();
        }
        if (cmdLine.hasOption('v')){
            System.out.format("%s : %s", Constants.EXE_NAME, Constants.FRAMEWORK_VERSION);
            System.exit(0);
        }
    }
    
    public static ClusterConfiguration getClusterFromJson(String fileLocation) {
        FileReader jsonReader = null;
        ClusterConfiguration cc = null;
        try {
            jsonReader = new FileReader(new File(fileLocation));
            cc = new Gson().fromJson(jsonReader, BaseClusterConfiguration.class);
        } catch(Exception e) {
            LOGGER.error("Failed to find input json configuration: " + fileLocation, e);
        } finally {
            IOUtils.closeQuietly(jsonReader);
        }
        return cc;
    }
    private CommandLineClusterConfiguration(CommandLine cmdLine){
        
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
            this.setBindAddress( cmdLine.getOptionValue('P') );
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
    }

    private static void printHelpAndExit(){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( Constants.EXE_NAME, options, true );
        System.exit(-1);
    }
/**
    private static void validateInput(ClusterConfiguration cluster, CommandLine cmdLine) {
        // j disables any required cmdLine options so check them manually
        if(!cmdLine.hasOption('j')) {
            if (!(cmdLine.hasOption('P') && cmdLine.hasOption('b') && 
                cmdLine.hasOption('m') && cmdLine.hasOption('f') && cmdLine.hasOption('z'))) {
                printHelpAndExit();            
            }
        } else {
            if (cluster.getHttpPort() == 0 ||
                StringUtils.isEmpty(cluster.getBindAddress()) ||
                StringUtils.isEmpty(cluster.getMesosMaster()) ||
                StringUtils.isEmpty(cluster.getFrameworkName()) ||
                StringUtils.isEmpty(cluster.getZkServers())) {
                printHelpAndExit();
            }
        }
    }
    **/
}
