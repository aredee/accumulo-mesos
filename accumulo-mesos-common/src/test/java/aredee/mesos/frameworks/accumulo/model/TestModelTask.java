package aredee.mesos.frameworks.accumulo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestModelTask {

    public static final Slave SLAVE = TestModelSlave.getSlave();
    public static final Executor EXECUTOR = TestModelExecutor.getExecutor();
    public static final String ID = "accumulo-mesos-task-193-kdKKss-198328";
    
    
    @Test
    public void testModelTask() {
        
        Task t = new Task();
    
        // Establish a baseline
        assertNull(t.getExecutor());
        assertNull(t.getID());
        assertNull(t.getSlave());
       
        t.setExecutor(EXECUTOR);
        t.setID(ID);
        t.setSlave(SLAVE);
        
        assertEquals(SLAVE, t.getSlave());
        assertEquals(EXECUTOR, t.getExecutor());
        assertEquals(ID, t.getID());
      
        assertTrue(t.toString().indexOf(ID) > 0);
        assertTrue(t.toString().indexOf(TestModelSlave.SLAVE_ID) > 0);
        assertTrue(t.toString().indexOf(TestModelExecutor.TEST_E_ID) > 0);
      
    }
}
