package aredee.mesos.frameworks.accumulo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

public class TestServerProfile {

    
    public static final String NAME = "profile-1-1";
    public static final String DESC = "Good profile";
    public static final ServerProfile.TypeEnum TYPE = ServerProfile.TypeEnum.gc;
    public static final Integer MEM = 1024;
    public static final BigDecimal CPUS = BigDecimal.TEN;
    public static final String LAUNCHER = "test-launcher";
    public static final String USER = "meAndYou";
            
    
    
    @Test
    public void testModelServerProfile() {
        
        ServerProfile sp = new ServerProfile();
        
        // Establish a baseline;
        
        assertNull(sp.getCpus());
        assertNull(sp.getDescription());
        assertNull(sp.getLauncher());
        assertNull(sp.getMemory());
        assertNull(sp.getName());
        assertNull(sp.getType());
        assertNull(sp.getUser());
      
        sp.setCpus(CPUS);
        sp.setDescription(DESC);
        sp.setLauncher(LAUNCHER);
        sp.setMemory(MEM);
        sp.setName(NAME);
        sp.setType(TYPE);
        sp.setUser(USER);
        
        
        assertEquals(CPUS, sp.getCpus());
        assertEquals(DESC, sp.getDescription());
        assertEquals(LAUNCHER, sp.getLauncher());
        assertEquals(MEM, sp.getMemory());
        assertEquals(NAME, sp.getName());
        assertEquals(TYPE, sp.getType());
        assertEquals(USER, sp.getUser());
       
        
        assertTrue(sp.toString().indexOf(USER) > 0);
        assertTrue(sp.toString().indexOf(NAME) > 0);
       
        
    }
}
