package aredee.mesos.frameworks.accumulo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import aredee.mesos.frameworks.accumulo.configuration.Defaults;

public class TestModelAccumulo {

    
    public static final String RFILE_LOCATION = "file:/tmp/accumulo/rfiles";
    
   
    @SuppressWarnings("unused")
    @Test
    public void testModelAccumulo() {
        
        Accumulo a = getTestedAccumulo();
    }
   
    /**
     * Used by other tests
     * @return
     */
    public static Accumulo getTestedAccumulo() {
       Accumulo a = new Accumulo();
        
        assertNull(a.getInitLocation());
        assertNull(a.getInstance());
        assertNull(a.getRootPassword());
        assertNull(a.getRootUser());
        assertNull(a.getZkServers());
         
        a.setInitLocation(RFILE_LOCATION);
        a.setInstance(Defaults.DEFAULT_ACCUMULO_INSTANCE);
        a.setRootPassword(Defaults.ROOT_PASSWORD);
        a.setRootUser(Defaults.ROOT_USER);
        a.setZkServers(Defaults.ZK_SERVERS);
        
        assertEquals(RFILE_LOCATION, a.getInitLocation());
        assertEquals(Defaults.DEFAULT_ACCUMULO_INSTANCE, a.getInstance());
        assertEquals(Defaults.ROOT_PASSWORD, a.getRootPassword());
        assertEquals(Defaults.ROOT_USER, a.getRootUser());
        assertEquals(Defaults.ZK_SERVERS, a.getZkServers());
        
        assertTrue(a.toString().indexOf(RFILE_LOCATION) > 0);
        assertTrue(a.toString().indexOf(Defaults.DEFAULT_ACCUMULO_INSTANCE) > 0);
        assertTrue(a.toString().indexOf(Defaults.ROOT_PASSWORD) > 0);
        assertTrue(a.toString().indexOf(Defaults.ROOT_USER) > 0);
        assertTrue(a.toString().indexOf(Defaults.ZK_SERVERS) > 0);
        return a;      
    }
}
