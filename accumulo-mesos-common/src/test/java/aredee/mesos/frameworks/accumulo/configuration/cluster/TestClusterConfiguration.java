package aredee.mesos.frameworks.accumulo.configuration.cluster;

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.junit.Test;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import aredee.mesos.frameworks.accumulo.configuration.process.ProcessConfiguration;

 

public class TestClusterConfiguration {
    
    public static final String BADCLUSTER_RES = "/TestBadCluster.json";
    public static final String GOODCLUSTER_RES = "/TestGoodCluster.json";
    
    static final String TARBALL = "/usr/local/accumulo/accumulo-1.1.1.tar.gz";
    static final String ZOOKEEPERS = "10.0.2.15:2181,192,168.0.1:2181";
    static final String VERSION = "1.1.1";
    static final String PORT = "8080";
    static final String MASTER_ADDR = "192,168.0.1:5050";
    static final String FRAMEWORK_NAME = "AccumuloMesos";
    static final String BIND_ADDR = "10.10.1.1";
   
   
    @SuppressWarnings("unused")
    @Test
    public void testGoodClusterCmdLine_helpAndVersion() {

        System.out.println("Running testGoodClusterCmdLine_version unit test");
        String version[] = new String[]{"-v"};
        String help[] = new String[]{"-h"};
    
        try {
            ClusterConfiguration cluster = CommandLineClusterConfiguration.newInstance(version);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
        
        try{
            ClusterConfiguration cluster = CommandLineClusterConfiguration.newInstance(help);
            fail();    
        } catch (Exception e) {
            assertTrue(true);
        }
        System.out.println("Ending testGoodClusterCmdLine_version unit test");
    }

    @Test
    public void testGoodClusterCmdLine() {

        System.out.println("Running testGoodClusterCmdLine unit test");
        try {
            String args[] = new String[]{"-P", PORT, "-b", BIND_ADDR, "-m", MASTER_ADDR,
                    "-f", FRAMEWORK_NAME, "-z", ZOOKEEPERS, "-tarball",TARBALL};
         
            ClusterConfiguration cluster = CommandLineClusterConfiguration.newInstance(args);
            
            System.out.println(cluster);

            assertNotNull(cluster);
            assertEquals(FRAMEWORK_NAME, cluster.getFrameworkName());
            assertEquals(BIND_ADDR,cluster.getBindAddress());
            assertEquals(PORT, "" + cluster.getHttpPort());
            assertEquals(ZOOKEEPERS, cluster.getZkServers());
            assertEquals(MASTER_ADDR, cluster.getMesosMaster());
            assertEquals(TARBALL, cluster.getTarballUri());
            
            // Defaults 
            assertNotNull(cluster.getProcessorConfigurations());
            
            Map<ServerType, ProcessConfiguration> map = cluster.getProcessorConfigurations();
            
            assertEquals(5, map.size());
            testProcessConfig(map.get(ServerType.TABLET_SERVER), "512", "8192", "1", "tserver");
            testProcessConfig(map.get(ServerType.MONITOR), "128", "1024", "1", "monitor");
            testProcessConfig(map.get(ServerType.GARBAGE_COLLECTOR), "128", "2048", "1", "gc");
            testProcessConfig(map.get(ServerType.TRACER), "128", "1024", "1", "tracer");
            testProcessConfig(map.get(ServerType.MASTER), "512", "8192", "1", "master");
                     
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        System.out.println("Ending testGoodClusterCmdLine unit test");     
    }
    
    @SuppressWarnings("unused")
    @Test
    public void testBadClusterCmdLine() {
        
       System.out.println("Running testBadClusterCmdLine unit test");
       try
        {
            // This should fail because no tarball option
            String args[] = new String[] {"-b","http://localhost","-m", "10.0.2.15:5050", 
                    "-f", "Accumulo-1-Mesos","-z", "10.0.2.15:2181"};
            
            // This should print throw an exception.
            ClusterConfiguration cluster = CommandLineClusterConfiguration.newInstance(args);
           
            fail("Expected a RuntimeException");
        
        } catch(Exception e) {
            System.out.println("Parse of command line failed as expected");
            assertTrue(true);
        }
       System.out.println("Running testBadClusterCmdLine unit test");
     
    }


    @SuppressWarnings("unused")
    @Test(expected= RuntimeException.class)
    public void testBadClusterJson() {
        System.out.println("Running testBadClusterJson unit test");
     
        // This should fail because no tarball option
        String args[] = new String[] { "-j", getClusterFile(BADCLUSTER_RES) , "-t", "/xyz"};
        
        CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);
        if( cmdLine.hasOption('j') ){
            // JSON file specified
             ClusterConfiguration cluster = new JSONClusterConfiguration(cmdLine.getOptionValue('j'));
        }
    }
    
    @Test
    public void testGoodClusterJson() {
        System.out.println("Running testGoodClusterJson unit test");
     
        try
        {
            String args[] = new String[] { "-j", getClusterFile(GOODCLUSTER_RES)};
            
            CommandLine cmdLine = CommandLineClusterConfiguration.parseArgs(args);
            if( cmdLine.hasOption('j') ){
                // JSON file specified
                 ClusterConfiguration cluster = new JSONClusterConfiguration(cmdLine.getOptionValue('j'));
                 
                 assertEquals("1.7.0", cluster.getAccumuloVersion());
                 assertEquals("/vagrant/accumulo-mesos-dist/target/accumulo-mesos-dist-0.1.0-SNAPSHOT.tar.gz", cluster.getTarballUri());
                 assertEquals("file:./conf/test-1-accumulo-site.xml", cluster.getAccumuloSiteUri());
                 assertEquals("0.0.0.0", cluster.getBindAddress());
                 assertEquals(18120, cluster.getHttpPort());
                 assertEquals("10.0.2.15:5050", cluster.getMesosMaster());
                 assertEquals("Accumulo-Mesos", cluster.getFrameworkName());
                 assertEquals("127.0.0.1:2181", cluster.getZkServers());
                 assertEquals(10, cluster.getMinTservers());
                 assertEquals("default-instance", cluster.getAccumuloInstanceName());
                 assertEquals("password", cluster.getAccumuloRootPassword());
                 assertEquals(128.0, cluster.getMaxExecutorMemory(), 0);
                 assertEquals(16.0, cluster.getMinExecutorMemory(),0);
                 assertNotNull(cluster.getProcessorConfigurations());
                 
                 Map<ServerType, ProcessConfiguration> map = cluster.getProcessorConfigurations();
                 
                 assertEquals(5, map.size());
                 testProcessConfig(map.get(ServerType.TABLET_SERVER), "4096", "8192", "4", "tserver");
                 testProcessConfig(map.get(ServerType.MONITOR), "500", "1024", "1", "monitor");
                 testProcessConfig(map.get(ServerType.GARBAGE_COLLECTOR), "1024", "2048", "2", "gc");
                 testProcessConfig(map.get(ServerType.TRACER), "500", "1024", "1", "tracer");
                 testProcessConfig(map.get(ServerType.MASTER), "4096", "8192", "4", "master");
                
                
            } else {
                fail("j option missing");
            }
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        System.out.println("Ending testGoodClusterJson unit test");
      
    }
    
    public static String getClusterFile(String resource) {
        String f = ClassLoader.class.getResource(resource).getFile();
        System.out.println("Cluster config file : " + f);
        return f;
     }
    
    public void testProcessConfig(ProcessConfiguration config, String min, String max, String cpus, String type) {
        assertNotNull(config);
        assertEquals(min, config.getMinMemory());
        assertEquals(max, config.getMaxMemory());
        assertEquals(cpus, config.getCpus());
        assertEquals(type, config.getType());
    }
}
