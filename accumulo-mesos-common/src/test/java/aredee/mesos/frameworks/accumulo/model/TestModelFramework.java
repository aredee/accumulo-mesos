package aredee.mesos.frameworks.accumulo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestModelFramework {

    
    public static final String BIND_ADDRESS = "192.168.1.1";
    public static final Integer HTTP_PORT = 8081;
    public static final String MESOS_MASTER = "141.10.10.1";
    public static final String FRAME_NAME = "accumulo-mesos-framework";
    public static final Integer EXECUTOR_MEM = 512;
    public static final String NAME = "test-accumulo-frame-name";
    public static final String ID = "frame-id-098-hjjk-03888";
    
    public static final Cluster CLUSTER = TestModelCluster.getCluster();
    public static final List<Cluster> TEST_CLUSTER = Arrays.asList(CLUSTER);
    
    
    @Test
    public void testModelFramework() {
        
        Framework fw = new Framework();
        
        // Establish a baseline
        assertNull(fw.getBindAddress());
        assertNull(fw.getExecutorMemory());
        assertNull(fw.getFrameworkName());
        assertNull(fw.getHttpPort());
        assertNull(fw.getId());
        assertNull(fw.getMesosMaster());
        assertNull(fw.getName());
        assertNotNull(fw.getClusters());
        assertTrue(fw.getClusters().isEmpty());
       
        fw.setBindAddress(BIND_ADDRESS);
        fw.setClusters(TEST_CLUSTER);
        fw.setExecutorMemory(EXECUTOR_MEM);
        fw.setFrameworkName(FRAME_NAME);
        fw.setId(ID);
        fw.setName(NAME);
        fw.setMesosMaster(MESOS_MASTER);
        fw.setHttpPort(HTTP_PORT);
        
        assertEquals(BIND_ADDRESS, fw.getBindAddress());
        assertEquals(HTTP_PORT, fw.getHttpPort());
        assertEquals(MESOS_MASTER, fw.getMesosMaster());
        assertEquals(FRAME_NAME, fw.getFrameworkName());
        assertEquals(EXECUTOR_MEM, fw.getExecutorMemory());
        assertEquals(NAME, fw.getName());
        assertEquals(ID, fw.getId());
        assertEquals(1, fw.getClusters().size());
        assertEquals(CLUSTER, fw.getClusters().get(0));
        
        assertTrue(fw.toString().indexOf(NAME) > 0);
        
    }
}
