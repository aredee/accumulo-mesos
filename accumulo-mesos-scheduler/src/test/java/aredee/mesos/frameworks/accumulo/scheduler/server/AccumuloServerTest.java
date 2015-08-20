package aredee.mesos.frameworks.accumulo.scheduler.server;

import static org.junit.Assert.*;

import org.junit.Test;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

 
public class AccumuloServerTest {

    static final String SLAVE_ID = "test-slave-id";
    static final int MAX_MEMORY = 1024;
    static final int MIN_MEMORY = 512;
    static final String BAD_TASK_ID = "thisIsABadTaskId";
    
    
    @Test
    public void testSettersGetters() {
        
        System.out.println("Running testSettersGetters");
        /**
         * This will set the taskId
         */
        BaseServer server = new Master();
      
        server.setMaxMemorySize(MAX_MEMORY);
        server.setMinMemorySize(MIN_MEMORY);
        server.setSlaveId(SLAVE_ID);
        
        assertNotNull(server.getTaskId());
      
        String taskId = server.getTaskId();
        
        assertTrue(server.isServer(taskId));
        
        server.setTaskId(taskId);
        
        assertEquals(taskId, server.getTaskId());
    
        try {
            // This should throw a runtime exception;
            server.setTaskId(BAD_TASK_ID);
            fail();
        } catch (Exception e ) {
            assertTrue(true);
        }
        
        assertEquals(MAX_MEMORY, server.getMaxMemorySize());
        assertEquals(MIN_MEMORY, server.getMinMemorySize());
        assertEquals(SLAVE_ID, server.getSlaveId());
        assertEquals(ServerType.MASTER, server.getType());
        
    }
    
    @Test
    public void testSettingOneServerTypeAsAnother() {
        System.out.println("Running testSettingOneServerTypeAsAnother");
    
        
        try{
            Master master = new Master();
            TabletServer tserver = new TabletServer();
            
            // This should raise a runtime exception;
            master.setTaskId(tserver.getTaskId());
            fail();
        }catch(Exception e) {
            assertTrue(true);
        }   
    }
    
    @Test
    public void testEqualsHash() {
        System.out.println("Running testEqualsHash");
      
        BaseServer m1 = new Master();
        BaseServer m2 = new Master();
        BaseServer t3 = new Tracer();
       
        assertNotEquals(m1, m2);
        assertNotEquals(m1, t3);
        assertNotEquals(m1.hashCode(), m2.hashCode());
        assertEquals(m1, m1);
        
        // The equals is based on the slaveId and taskId, if the slaveId and 
        // taskId are the same then its the same server.
        //
        BaseServer m4 = new Master();
        m4.setTaskId(m1.getTaskId());
        assertEquals(m1, m4);
        assertEquals(m1.hashCode(), m4.hashCode());
        
        // Change the SLAVE ID and s4 should be considered a new server.
        // You have to be careful in accumulo mesos because the slave id is added
        // at later date and if the server object is in a hashtable then when you
        // set the slave id then its hash will change...so generally its good 
        // practice to delete a server from a hash table before adding the slave id.
        //
        m4.setSlaveId(SLAVE_ID);
        
        assertNotEquals(m1, m4);
        assertNotEquals(m1.hashCode(), m4.hashCode());
    }
    
    @Test
    public void testIsServer() {
        Master master = new Master();
        Monitor monitor = new Monitor();
        Tracer tracer = new Tracer();
        TabletServer tserver = new TabletServer();
        GarbageCollector gc = new GarbageCollector();
       
        // This is mostly for code coverage... shoot for 70%
        //
        testAsserts(master, monitor, tracer, tserver, gc);
        testAsserts(monitor, master, tracer, tserver, gc);
        testAsserts(tracer, monitor, master, tserver, gc);
        testAsserts(tserver, monitor, tracer, master, gc);
        testAsserts(gc, monitor, tracer, tserver, master);
       
    }
    
    public void testAsserts(BaseServer tgt, BaseServer b1, BaseServer b2, BaseServer b3, BaseServer b4) {
        
        assertTrue(tgt.isServer(tgt.getTaskId()));
        
        assertFalse(tgt.isServer(b1.getTaskId()));
        assertFalse(tgt.isServer(b2.getTaskId()));
        assertFalse(tgt.isServer(b3.getTaskId()));
        assertFalse(tgt.isServer(b4.getTaskId()));      
    }
}
