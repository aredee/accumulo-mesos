package aredee.mesos.frameworks.accumulo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;


public class TestModelCluster {
    
    public static final String DESCR = "This is a cluster model";
    public static final String ID = "accumulo-1";
    public static final Integer NUM_T_SERVERS = 5;
    public static final Integer MIN_T_SERVERS = 1;
    public static final String NAME = "1";
    public static final String TAR_URI = "/usr/local/accumulo/tarball.gz";
    public static final Accumulo TEST_ACCUMULO = TestModelAccumulo.getTestedAccumulo();
    public static final List<Object> servers = Arrays.asList((Object)TEST_ACCUMULO);
    
    @SuppressWarnings("unused")
    @Test
    public void testCluster() {
        Cluster c = getCluster();
    }
    
    /**
     * Used by other tests
     * @return tested cluster
     */
    public static Cluster getCluster() {
        Cluster c = new Cluster();
        
        assertNull(c.getAccumulo());
        assertNull(c.getAccumuloRootPassword());
        assertNull(c.getDescription());
        assertNull(c.getId());
        assertNull(c.getMinTservers());
        assertNull(c.getName());
        assertNull(c.getNumTservers());
        assertNull(c.getTarballURI());
        assertNotNull(c.getServers());
        assertEquals(0, c.getServers().size());
        
        c.setAccumulo(TEST_ACCUMULO);
        c.setAccumuloRootPassword(TEST_ACCUMULO.getRootPassword());
        c.setDescription(DESCR);
        c.setId(ID);
        c.setMinTservers(MIN_T_SERVERS);
        c.setName(NAME);
        c.setNumTservers(NUM_T_SERVERS);
        c.setTarballURI(TAR_URI);
        c.setServers(servers);
        
        assertEquals(TEST_ACCUMULO, c.getAccumulo());
        assertEquals(TEST_ACCUMULO.getRootPassword(), c.getAccumuloRootPassword());
        assertEquals(DESCR, c.getDescription());
        assertEquals(ID, c.getId());
        assertEquals(MIN_T_SERVERS, c.getMinTservers());
        assertEquals(NAME, c.getName());
        assertEquals(NUM_T_SERVERS,c.getNumTservers());
        assertEquals(TAR_URI, c.getTarballURI());
        assertEquals(1, c.getServers().size());
        assertEquals(TEST_ACCUMULO, c.getServers().get(0));
        
        assertTrue(c.toString().indexOf(NAME) > 0);
        
        return c;
    }

}
