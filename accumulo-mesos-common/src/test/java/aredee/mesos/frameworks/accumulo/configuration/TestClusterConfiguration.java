package aredee.mesos.frameworks.accumulo.configuration;

import static org.junit.Assert.*;

import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.cluster.CommandLineClusterConfiguration;
import org.apache.commons.cli.CommandLine;
import org.junit.Test;


public class TestClusterConfiguration {


    // TODO there is no code checking any of these scenarios
    // @Test(expected=RuntimeException.class)
    public void testNoMesosCmdLine() {

        String args[] = new String[]{
                "-b", "0.0.0.0",
                "-f", "Accumulo-1-Mesos",
                "-z", "10.0.2.15:2181"};

        CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);

        ClusterConfiguration cluster = new CommandLineClusterConfiguration(cmdLine);


    }

    @Test
    public void testGoodClusterCmdLine() {

        String args[] = new String[]{"-P", "8080", "-b", "http://localhost", "-m", "10.0.2.15:5050",
                "-f", "Accumulo-1-Mesos", "-z", "10.0.2.15:2181"};

        CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);

        // Should print Help and exit
        ClusterConfiguration cluster = new CommandLineClusterConfiguration(cmdLine);
        assertNotNull(cluster);

    }


}
