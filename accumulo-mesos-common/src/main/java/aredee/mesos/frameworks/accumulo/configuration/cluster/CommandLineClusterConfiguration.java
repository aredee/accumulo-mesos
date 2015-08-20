package aredee.mesos.frameworks.accumulo.configuration.cluster;

import aredee.mesos.frameworks.accumulo.configuration.Constants;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
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
        
        //                opt, longOpt, hasArgs, description
        options.addOption("h", "help", false, "Print this message and exit");
        options.addOption("v", "version", false, "Show version number for Accumulo Mesos");
        options.addOption("P", "port", true, "Port number to serve HTTP interface");
        options.addOption("b", "bind-address", true, "IP address of interface to bind HTTP interface to");
        options.addOption("m", "master", true, "Location of mesos master to connect to");
        options.addOption("f", "framework-name", true, "Name of this mesos framework");
        options.addOption("z", "zookeepers", true, "List of Zookeeper servers");
        options.addOption("t", "tarball", true, "URI of framework/executor tarball");
        options.addOption("j", "json", true, "JSON file containing configuration");
        //options.addOption("y", "yaml", true, "Yaml file containing configuration");
        
        options.addOption("a", "accumulo-version", true, "Accumulo version");
        options.addOption("r", "root-password", true, "Accumulo root password");
        options.addOption("i", "instance-name",true,"Accumulo instance name");
        
    }

    private static final Parser parser = new GnuParser();

    /**
     * If options h or v are present they are processed and a RuntimeException is thrown.
     *  
     * @param cmdLine
     * 
     */
    public CommandLineClusterConfiguration(CommandLine cmdLine){

        super();
               
        // In the complexity world switch stmts normalize to 1, if stmts do not.
        //
        for (Option opt : cmdLine.getOptions()){
            switch (opt.getOpt() ) {
            case "P" :
                try {
                    this.setHttpPort(Integer.parseInt(cmdLine.getOptionValue('P')));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid port specification " + cmdLine.getOptionValue('P'));
                }         
                break;
            case "f" :
                this.setFrameworkName(opt.getValue());
                break;
            case "b" : 
                this.setBindAddress(opt.getValue());
                break;
            case "m":
                this.setMesosMaster(opt.getValue());
                break;
            case "z":
                this.setZkServers(opt.getValue());
                break;
            case "t" :
                this.setTarballUri(opt.getValue());
                break;
            case "a":
                this.setAccumuloVersion(opt.getValue());
                break;
            case "r":
                this.setAccumuloRootPassword(opt.getValue());
                break;
            case "i":
                this.setAccumuloInstanceName(opt.getValue());
                break;
            case "h":
                printHelp();
                throw new RuntimeException("Exiting");
            case "v":
                System.out.format("%s : %s\n", Constants.EXE_NAME, Constants.FRAMEWORK_VERSION);
                throw new RuntimeException("Exiting");
            default: 
                break;
            }
        }
        // If not json or yaml then test for only required option and thats the tarball. All
        // the other params have defaults.
        //
        if (!cmdLine.hasOption("j") && !cmdLine.hasOption("y")) {
            ClusterUtils.verifyCluster(this);
        }
    }

    public static CommandLine parseArgs(String args[]) {
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            LOGGER.error("Failed to parse command line: ", e);
            printHelp();
            throw new RuntimeException("CommandLine parse error: " + e.getMessage());
        }
        return cmdLine;
    }

    public static ClusterConfiguration newInstance(String args[]) {
        return new CommandLineClusterConfiguration(CommandLineClusterConfiguration.parseArgs(args));
    }
   
    
    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( Constants.EXE_NAME, options, true );     
    }

 
}
