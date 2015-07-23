package aredee.mesos.frameworks.accumulo.configuration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import aredee.mesos.frameworks.accumulo.configuration.cluster.BaseClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.cluster.CommandLineClusterConfiguration;
import org.apache.commons.cli.CommandLine;
import org.junit.Test;

import com.google.gson.Gson;

public class TestClusterConfiguration {

    static final String DEFAULT_CLUSTER = "./accumulo-mesos-tests/src/test/resources/DefaultCluster.json";
    static final String BAD_CLUSTER = "./accumulo-mesos-tests/src/test/resources/BadCluster.json";
  
           
    @Test
    public void testClusterToJson() {
        
        BaseClusterConfiguration config = new BaseClusterConfiguration();
        
        config.setAccumuloTarballUri("/usr/local/accumulo/AccumuloDist-1.7.0.tgz");
        config.setAccumuloRootPassword("password");
 
        System.out.println("\nExecutorURI? " + config.getExecutorJarUri());
        
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
    
    @Test
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
    
    @Test
    public void testBadClusterCmdLine() {
        
        try
        {
            // This should fail because no port -P option
            String args[] = new String[] {"-b","http://localhost","-m", "10.0.2.15:5050", 
                    "-f", "Accumulo-1-Mesos","-z", "10.0.2.15:2181"};
            
            CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);
            
            // Should print Help and exit
            ClusterConfiguration cluster = CommandLineClusterConfiguration.getConfiguration(cmdLine);
            fail();
        
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void testGoodClusterCmdLine() {
        
        try
        {
             String args[] = new String[] {"-P","8080", "-b","http://localhost","-m", "10.0.2.15:5050", 
                    "-f", "Accumulo-1-Mesos","-z", "10.0.2.15:2181"};
            
            CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);
            
            // Should print Help and exit
            ClusterConfiguration cluster = CommandLineClusterConfiguration.getConfiguration(cmdLine);
        
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }   
    
  
    
    @Test
    public void testBadClusterJson() {
        
        try
        {
            // This should fail because no port -P option
            String args[] = new String[] { "-j=" + BAD_CLUSTER};
            
            CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);
            
            // Should print Help and exit
            ClusterConfiguration cluster = CommandLineClusterConfiguration.getConfiguration(cmdLine);
            fail();
        
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }  
       
}
