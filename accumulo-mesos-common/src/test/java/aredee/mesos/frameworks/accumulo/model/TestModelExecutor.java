package aredee.mesos.frameworks.accumulo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestModelExecutor {
    
    
    public static final String TEST_E_ID = "executor-id-1234";
    public static final String TEST_FW_ID = "framework-id-123-xss";
    public static final String TEST_NAME = "accumulo-mesos-executor";
    
    @SuppressWarnings("unused")
    @Test
    public void testModelExecutor() {
        
        Executor e = getExecutor();
    }
    
    public static Executor getExecutor() {
        Executor e = new Executor();
        
        // Start with a known state
        assertNull(e.getExecutorID());
        assertNull(e.getFrameworkID());
        assertNull(e.getName());
        
        e.setExecutorID(TEST_E_ID);
        e.setFrameworkID(TEST_FW_ID);
        e.setName(TEST_NAME);
        
        assertEquals(TEST_E_ID, e.getExecutorID());
        assertEquals(TEST_FW_ID, e.getFrameworkID());
        assertEquals(TEST_NAME, e.getName());
       
        assertTrue(e.toString().indexOf(TEST_NAME) > 0);
        assertTrue(e.toString().indexOf(TEST_FW_ID) > 0);
        assertTrue(e.toString().indexOf(TEST_E_ID) > 0);
        
        return e;
    }

}
