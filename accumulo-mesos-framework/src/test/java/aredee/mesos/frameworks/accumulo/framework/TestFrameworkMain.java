package aredee.mesos.frameworks.accumulo.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos.Status;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.Module;

import aredee.mesos.frameworks.accumulo.TestSupport;
import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.cluster.TestClusterConfiguration;
import aredee.mesos.frameworks.accumulo.framework.api.WebServer;
import aredee.mesos.frameworks.accumulo.initialize.TestAccumuloInitializer;
import mockit.Mock;
import mockit.MockUp;

public class TestFrameworkMain {
       
    /**
     * Disconnect MESOS
     */
    public static class MockMesosSchedulerDriver extends MockUp<MesosSchedulerDriver> {
        @Mock
        void $init(org.apache.mesos.Scheduler scheduler, org.apache.mesos.Protos.FrameworkInfo frameInfo, String mesosMaster) {
            assertNotNull(scheduler);
            assertNotNull(frameInfo);
            assertNotNull(mesosMaster);
        }
        
        @Mock
        Status run() {
            return org.apache.mesos.Protos.Status.DRIVER_STOPPED;
        }
        
        @Mock
        Status stop(boolean stop) {
            assertTrue(stop);
            return org.apache.mesos.Protos.Status.DRIVER_STOPPED;
        }
    } 
    
    /**
     * Disconnect the webserver
     */
    static WebServer webserver = new MockUp<WebServer> () {
        @Mock
        void start() {
            System.out.println("Webserver started");
        }
        @Mock
        void stop() {
            System.out.println("Webserver stop");
        }
    }.getMockInstance();
    
    /**
     * Fake the guice injector that creates the webserver
     */
    static Injector injector = new MockUp<Injector> () {
        @Mock
        WebServer getInstance(Class<?> server) {
            return webserver;
        }
    }.getMockInstance();
    
    /**
     * Fake Guice
     *
     */
    public static class MockGuice extends MockUp<Guice> {
        @Mock
        Injector createInjector(Stage stage, Module[] modules ) {
            return injector;
        }
    }
    
    @BeforeClass
    public static void setUp() {
      
        try {
            TestSupport.setupConfDir();
        } catch(Exception e) {
            fail("Failed to copy site xml to " + TestSupport.TEST_CONF_DIR);
        }
        // JMockit will now replace any reference to MesosSchedulerDriver with this mock version.
        new MockMesosSchedulerDriver();
    }
    
    @AfterClass
    public static void tearDown() {
        TestSupport.tearDownTestDirs();
    }
    
    /**
     * If no args then this should fail
     */
    @Test(expected= RuntimeException.class)
    public void testMain_noArgs() {
        Main.startCluster(Main.initializeClusterConfig(new String[]{""}));
    }
    
    /**
     * Test with a Good json cluster definition.
     */
    @Test
    public void testMain_argsJson() {
        
        String args[] = new String[] { "-j", TestSupport.getResourceFileLocation(
                TestClusterConfiguration.GOODCLUSTER_RES)};
    
        // Replace any reference to Guice with MockGuice
        new MockGuice();
        
        // Setup mocks for the initializer
        TestAccumuloInitializer.setupMocks();
  
        TestSupport.setSchedulerEnviron();
        
        // Copy the site file to a place Main can find it and update the config.
        ClusterConfiguration config = Main.initializeClusterConfig(args);
        TestSupport.setTestSiteFileLocation(config);
        
        //TODO: add assertions for configuration
        
        // -1 then something went bad, 
        // 0 == stopped -- see MockMesosSchedulerDriver
        // 1 == aborted
        // 2 == driver not started
        // 3 == default
        assertEquals(0, Main.startCluster(config));
    }
    
    /**
     * Test with a Good json cluster definition but a bad environment
     */
    @Test
    public void testMain_argsJson_badEnviron() {
        
        String args[] = new String[] { "-j", TestSupport.getResourceFileLocation(
                TestClusterConfiguration.GOODCLUSTER_RES)};
    
        // Replace any reference to Guice with MockGuice
        new MockGuice();
        
        // Setup mocks for the initializer
        TestAccumuloInitializer.setupMocks();

        // Main should generate a RuntimeException which will cause a -1 
        // returned as the status.
        TestSupport.setBadSchedulerEnviron();
        
        // Copy the site file to a place Main can find it and update the config.
        ClusterConfiguration config = Main.initializeClusterConfig(args);
        TestSupport.setTestSiteFileLocation(config);      
        
        //TODO: add assertions for configuration
        
        // -1 then something went bad, 
        // 0 == stopped -- see MockMesosSchedulerDriver
        // 1 == aborted
        // 2 == driver not started
        // 3 == default
        assertEquals(-1, Main.startCluster(config));
    }
      
}
