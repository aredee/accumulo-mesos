package aredee.mesos.frameworks.accumulo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestModelSlave {

    public static final String HOSTNAME = "localhost-1";
    public static final String SLAVE_ID = "Slave-xyz-1";
    
    @SuppressWarnings("unused")
    @Test
    public void testModelSlave(){
        
        Slave s = getSlave();
    }
    
    public static Slave getSlave() {
       Slave s = new Slave();
        
        assertNull(s.getHostname());
        assertNull(s.getID());
        
        s.setHostname(HOSTNAME);
        s.setID(SLAVE_ID);
        
        assertEquals(HOSTNAME,s.getHostname());
        assertEquals(SLAVE_ID,s.getID());
        assertNotNull(s.toString());
        assertTrue(s.toString().indexOf(SLAVE_ID) > 0);
        assertTrue(s.toString().indexOf(HOSTNAME) > 0);
        
        return s;
    }
}
