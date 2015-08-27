package aredee.mesos.frameworks.accumulo.initialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.mesos.MesosNativeLibrary;
import org.apache.mesos.state.State;
import org.apache.mesos.state.ZooKeeperState;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.protobuf.InvalidProtocolBufferException;

import aredee.mesos.frameworks.accumulo.Protos.FrameworkIdentity;
import aredee.mesos.frameworks.accumulo.TestSupport;
import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.cluster.TestClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.file.AccumuloSiteXml;
import aredee.mesos.frameworks.accumulo.configuration.process.ServerProcessConfiguration;
import aredee.mesos.frameworks.accumulo.state.FrameworkStateProtobufPersister;
import mockit.Mock;
import mockit.MockUp;


public class TestAccumuloInitializer {

    
    /**
     * Mesos statically tries to load its native libs and thats not good so stop it here.
     *
     */
    public static class MockMesosNativeLibrary extends MockUp<MesosNativeLibrary> {
        @Mock
        static void load() {
            System.out.println("MockMesosNativeLibrary: load()");
        }
    }
    
    /**
     * Disconnect Zookeeper
     */
    public static class MockZookeeperState extends MockUp<ZooKeeperState> {
        @Mock
        void $init(String servers, long timeout, TimeUnit unit, String znode) {
            System.out.println("MockZookeeperState(): " +servers+" "+timeout+ " " + unit + " " + znode);
            assertNotNull(servers);
            assertEquals(60L, timeout);
            assertTrue(unit == TimeUnit.SECONDS);
            assertEquals("accumulo-mesos",znode);
        }
    }
    
    /**
     * Disconnect Mesos/Zookeeper protobuf
     */
    public static class MockFrameworkStateProtobufPersister extends MockUp<FrameworkStateProtobufPersister> {
        @Mock
        void $init(State state) {
            System.out.println("MockFrameworkStateProtobufPersister(): state " + state);
            assertNotNull(state);
        }
        
        @Mock
        String getAccumuloInstanceName(String frameworkId) 
                throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
            System.out.println("MockFrameworkStateProtobufPersister: getAccumuloInstanceName frameworkId " + frameworkId);

            assertNotNull(frameworkId);
            return "default-instance";
        }
        
        @Mock
        void saveAccumuloInstanceName(String frameworkId, String accumuloInstancename){
            System.out.println("MockFrameworkStateProtobufPersister: saveAccumuloInstanceName frameworkId " + 
                    frameworkId + " accumuloInstance " + accumuloInstancename);
         
            assertNotNull(frameworkId);
            assertNotNull(accumuloInstancename);
        }
        @Mock
        List<FrameworkIdentity> getFrameworks() throws InvalidProtocolBufferException, InterruptedException, ExecutionException {
            return new ArrayList<FrameworkIdentity>(0);
        }
    }
    
    /**
     * Disconnect building the process that does the accumulo initialization since accumulo is not there.
     *
     */
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
    
    
    /**
     * Fake the process the MockProcessBuilder builds
     */
    public static class MockProcess extends MockUp<Process> {
        @Mock
        InputStream getErrorStream() {
            return System.in;
        }
        @Mock
        InputStream getInputStream() {
            return System.in;
        }
        @Mock
        int waitFor() throws InterruptedException {
            return 0;
        }
    }  
    /**
     * Other unit tests will need this
     */
    public static void setupMocks() {
        
        System.out.println("Setting up AccumuloInitializer mocks");
        
        // Not really going to initialize anything so dummy up the process builder
        new MockProcessBuilder();
        
        // Mesos wanted to statically load the native libs, intercept
        new MockMesosNativeLibrary();
        
        // No need for zookeeper...another test
        new MockZookeeperState();
        
        // Mesos State saver...another test
        new MockFrameworkStateProtobufPersister();        
    }
    
    @BeforeClass
    public static void setup () {
        setupMocks();
    }
   
    @Test
    public void testInitializer_goodJson() {
        
        AccumuloInitializer initializer = getGoodAccumuloInitializer();
    }
    
    public static AccumuloInitializer getGoodAccumuloInitializer() {
        ClusterConfiguration config = TestSupport.
                getJsonClusterConfigWithTestSite(TestClusterConfiguration.GOODCLUSTER_RES);
        
        TestSupport.setSchedulerEnviron();

        AccumuloInitializer initializer = null;
        
        try {
            TestSupport.setupConfDir();
       
            initializer = new AccumuloInitializer(config);
            
            assertEquals("default-instance", initializer.getAccumuloInstanceName());
            assertEquals("Accumulo-Mesos-default-instance", initializer.getFrameworkId());
            assertEquals("Accumulo-Mesos",initializer.getFrameworkName());
            assertFalse(initializer.frameworkExists());
            assertNotNull(initializer.getFrameworkState());
            
            ServerProcessConfiguration serverConfig = initializer.getProcessConfiguration();

            assertEquals("1024", serverConfig.getMaxMemory());
            assertEquals(1024.0, serverConfig.getMaxMemoryOffer(),0);
            assertEquals(1024,serverConfig.getMaxMemorySize());
            
            assertEquals(0, serverConfig.getCpuCount());
            assertEquals(0.0, serverConfig.getCpuOffer(),0);
            assertNull(serverConfig.getCpus());

            assertEquals("512",serverConfig.getMinMemory());
            assertEquals(512.0,serverConfig.getMinMemoryOffer(),0);
            assertEquals(512, serverConfig.getMinMemorySize());
            assertNull(serverConfig.getType());
            
            assertEquals(TestSupport.ACCUMULO_CLIENT_CONF_PATH, serverConfig.getAccumuloClientConfFile().getAbsolutePath());
            
            // Site file should have been created here
            assertTrue(serverConfig.getAccumuloClientConfFile().exists());
            
            assertEquals(TestSupport.ACCUMULO_HOME+"/conf", serverConfig.getAccumuloConfDir().getAbsolutePath());
            assertEquals(TestSupport.ACCUMULO_HOME, serverConfig.getAccumuloDir().getAbsolutePath());
            assertEquals(TestSupport.ACCUMULO_HOME+"/lib", serverConfig.getAccumuloLibDir().getAbsolutePath());
            assertEquals(TestSupport.ACCUMULO_HOME+"/lib/ext", serverConfig.getAccumuloLibExtDir().getAbsolutePath());            
            assertEquals(TestSupport.ACCUMULO_HOME+"/logs", serverConfig.getAccumuloLogDir().getAbsolutePath());
    
            assertTrue(CollectionUtils.isEmpty(serverConfig.getClasspathItems()));
            assertNull(serverConfig.getExecutorDir());
            assertEquals(TestSupport.HADOOP_CONF_DIR,serverConfig.getHadoopConfDir().getAbsolutePath());
            assertEquals(TestSupport.HADOOP_PREFIX, serverConfig.getHadoopHomeDir().getAbsolutePath());
            
            assertTrue(CollectionUtils.isEmpty(serverConfig.getNativeLibPaths()));
            assertTrue(MapUtils.isEmpty(serverConfig.getServerMemory()));
            assertNotNull(serverConfig.getSystemProperties());
            assertNull(serverConfig.getWalogDir());
            assertEquals(TestSupport.ZOOKEEPER_HOME,serverConfig.getZooKeeperDir().getAbsolutePath());
           
            
            // TODO: Test this xml against input site xml and output location
            AccumuloSiteXml siteXml = initializer.getSiteXml();
            
           // System.out.println("siteXml ? " + siteXml);
          
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to create AccumuloInitializer");
        }        
        
        return initializer;
    }
}
