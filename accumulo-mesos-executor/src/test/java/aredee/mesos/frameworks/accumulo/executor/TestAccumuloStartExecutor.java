package aredee.mesos.frameworks.accumulo.executor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import aredee.mesos.frameworks.accumulo.configuration.Constants;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import aredee.mesos.frameworks.accumulo.TestSupport;
import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.cluster.JSONClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.file.AccumuloSiteXml;
import aredee.mesos.frameworks.accumulo.initialize.AccumuloInitializer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mockit.*;

public class TestAccumuloStartExecutor {

    public static TaskID testTaskId ;
    public static TaskState  testState;
    public static TaskInfo taskInfo;
    public static String testSiteXml;
    public static ClusterConfiguration clusterConfig;
    public static String stringTaskId = "gc_task-id-01234-hdjy";
    public static final String EXECUTOR_HOME = TestSupport.MESOS_DIRECTORY + File.separator + 
            Constants.ACCUMULO_DISTRO + File.separator + "accumulo-1.7.0";
    
    
    /**
     * Stub out the Mesos Driver, check what is passed in is what is expected.
     */
    ExecutorDriver driver = new MockUp<ExecutorDriver>() {
        @Mock
        Status sendStatusUpdate(TaskStatus status) {
            
            assertNotNull(status);
            assertNotNull(testState);
            assertEquals(testState, status.getState());
            assertEquals(testTaskId, status.getTaskId());
            
            return null;
        }
    }.getMockInstance();
      
    public static class MockProcessBuilder extends MockUp<ProcessBuilder> {
        @Mock
        void $init(List<String> argList) {
            assertNotNull(argList);
        }
        @Mock
        Process start()  throws IOException {            
            return new MockProcess().getMockInstance();
        }
    }
    public static class MockProcess extends MockUp<Process> {
        @Mock
        InputStream getErrorStream() {
            return System.in;
        }
        @Mock
        InputStream getInputStream() {
            return System.in;
        }
    }
   
    /**
     * 
     * Stub out the creation of the Site XML file at MESOS_DIRECTORY. Test ACCUMULO_HOME is what we expect
     * and the siteXml file is what was passed in.
     *
     */
    public static class MockAccumuloInitializer extends MockUp<AccumuloInitializer> {
        @Mock
        void $init(ClusterConfiguration config) {
            assertNotNull(config);
        }  
        @Mock
        static void writeAccumuloSiteFile(String accumuloHomeDir, AccumuloSiteXml siteXml) {
            assertNotNull(accumuloHomeDir);
            assertEquals(accumuloHomeDir, EXECUTOR_HOME);
            assertNotNull(siteXml);
            // The test site file passed in should equal the local one.
            assertEquals(testSiteXml, siteXml.toXml());
        }
    }
   
    static TestSupport.EnvironContext envCtx = new TestSupport.EnvironContext();
    static TestSupport.ServerContext srvCtx;
    
    
    @BeforeClass
    public static void setup() {
        System.out.println("class setup");
        
        // Environment for AccumuloStartExecutor is created in the AccumuloStartExecutorLauncher
        // So mimic that here.
        TestSupport.setExecutorEnviron();
        
        testTaskId = TaskID.newBuilder().setValue(stringTaskId).build();
        
        srvCtx = new TestSupport.ServerContext(ServerType.GARBAGE_COLLECTOR, 
                1024, 512, stringTaskId, "accumulo-mesos-slave-122201");    
        
        
        testSiteXml = TestAccumuloStartExecutor.getResourceFile("/TestAccumuloSite.xml");
        clusterConfig = new JSONClusterConfiguration(getResourceFileLocation("/TestGoodCluster.json"));
        taskInfo = TestSupport.createTaskInfo(srvCtx, clusterConfig, testSiteXml, envCtx);

        // Create Mocked AccumuloStartExecutor internal classes
        new MockProcessBuilder();
    }
    
    @AfterClass
    public static void tearDown() {
        TestSupport.tearDownTestDirs();
    }
    
    /**
     * This test checks that the expected environment is being created and not causing 
     * any exceptions. Nothing is being launched.
     */
    @Test
    public void testLaunchTaskSuccess_mockSite() {
        
        System.out.println("testLaunchTaskSuccess_mockSite");
        new MockAccumuloInitializer();
 
        AccumuloStartExecutor executor = new AccumuloStartExecutor();
        
        testState = TaskState.TASK_RUNNING;
        
        // This is a fairly weak test, only tests if there are no exceptions. 
        // Does not really launch anything because everything is stubbed out.
        //
        executor.launchTask(driver, taskInfo);
        
        System.out.println("end testLaunchTaskSuccess_mockSite");
    }
    
    /**
     * This test checks that the expected environment is being created and not causing 
     * any exceptions. Nothing is being launched.
     */
    @Test
    public void testLaunchTaskSuccess_writeSite() {
        
        System.out.println("testLaunchTaskSuccess_writeSite");
  
        File siteFile = new File(EXECUTOR_HOME+AccumuloInitializer.siteUri());
        
        // Insure there is no file from a previous test
        FileUtils.deleteQuietly(siteFile);
        
        AccumuloStartExecutor executor = new AccumuloStartExecutor();
        
        testState = TaskState.TASK_RUNNING;
        
        // This is a fairly weak test, only tests if there are no exceptions. 
        // Does not really launch anything because everything is stubbed out.
        //
        executor.launchTask(driver, taskInfo);
        
        assertTrue(siteFile.exists());
        
        System.out.println("end testLaunchTaskSuccess_writeSite");
   
    }  
    public static String getResourceFile(String resource) {
        String contents = null;
        InputStream input = null;
        try {
            input = ClassLoader.class.getResource(resource).openStream();
            contents = IOUtils.toString(input);
        } catch (Exception e) {
            System.err.println("Failed to get resource for " + resource + " error " + e.getMessage());
            throw new RuntimeException(e);
        } finally  {
            IOUtils.closeQuietly(input);
        }
        return contents;      
    }
    
    public static String getResourceFileLocation(String resource) {
        String f = ClassLoader.class.getResource(resource).getFile();
        System.out.println("Cluster config file : " + f);
        return f;
     }
       
    
}
