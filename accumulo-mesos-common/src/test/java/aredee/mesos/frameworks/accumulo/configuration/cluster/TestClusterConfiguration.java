package aredee.mesos.frameworks.accumulo.configuration.cluster;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.junit.Test;

import com.google.gson.Gson;


public class TestClusterConfiguration {

    static final String DEFAULT_CLUSTER = "./accumulo-mesos-common/src/test/resources/DefaultCluster.json";
    static final String BAD_CLUSTER = "./accumulo-mesos-common/src/test/resources/BadCluster.json";
 
    // TODO there is no code checking any of these scenarios
    // @Test(expected=RuntimeException.class)
    public void testNoMesosCmdLine() {

        String args[] = new String[]{
                "-b", "0.0.0.0",
                "-f", "Accumulo-1-Mesos",
                "-z", "10.0.2.15:2181"};
/*
        CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);

        ClusterConfiguration cluster = new CommandLineClusterConfiguration(cmdLine);
*/

    }

    // TODO get test working
    // @Test
    public void testGoodClusterCmdLine() {
/*
        String args[] = new String[]{"-P", "8080", "-b", "http://localhost", "-m", "10.0.2.15:5050",
                "-f", "Accumulo-1-Mesos", "-z", "10.0.2.15:2181"};

        CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);

        // Should print Help and exit
        ClusterConfiguration cluster = new CommandLineClusterConfiguration(cmdLine);
        assertNotNull(cluster);
*/
    }


    // TODO get test working
    // @Test
    public void testClusterToJson() {
        
        BaseClusterConfiguration config = new BaseClusterConfiguration();
        
        config.setAccumuloRootPassword("password");
         
        try {
            File f = new File(DEFAULT_CLUSTER);
            System.out.println("Creating json file " + f);
            FileWriter fr = new FileWriter(f);
            fr.write(config.toString());
            fr.flush();
            fr.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
     }

    // TODO get test working
    // @Test
    public void testJsonToGoodCluster() {
        
        try
        {
            File f = new File(DEFAULT_CLUSTER);          
            FileReader fr = new FileReader(f);
            BaseClusterConfiguration config = new Gson().fromJson(fr, BaseClusterConfiguration.class);
            fr.close();
            
            System.out.println(config);
        
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    // TODO get test working
    // @Test
    public void testBadClusterCmdLine() {
        /*
        try
        {
            // This should fail because no port -P option
            String args[] = new String[] {"-b","http://localhost","-m", "10.0.2.15:5050", 
                    "-f", "Accumulo-1-Mesos","-z", "10.0.2.15:2181"};

            CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);
            
            // Should print Help and exit
            ClusterConfiguration cluster = new CommandLineClusterConfiguration(cmdLine);
           // fail();
        
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
        */
    }


    // TODO get test working
    //@Test
    public void testBadClusterJson() {
        /*
        try
        {
            // This should fail because no port -P option
            String args[] = new String[] { "-j=" + BAD_CLUSTER};
            
            CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);
            if( cmdLine.hasOption('j') ){
                // JSON file specified
                // Should print Help and exit               
                ClusterConfiguration cluster = new JSONClusterConfiguration(cmdLine.getOptionValue('j'));
            }          
            //fail();
        
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
        */
    }  
}
