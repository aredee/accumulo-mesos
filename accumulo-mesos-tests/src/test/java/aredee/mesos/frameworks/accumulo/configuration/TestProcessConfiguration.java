package aredee.mesos.frameworks.accumulo.configuration;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import aredee.mesos.frameworks.accumulo.configuration.process.BaseProcessConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.process.ProcessConfiguration;
import org.junit.Test;

import com.google.gson.Gson;

public class TestProcessConfiguration {

    static final String MAX = "1024";
    static final String MIN = "512";
    static final String MAXO = "1024.0";
    static final String MINO = "512.0";
    
    static final String JSON = "{'minMemory':'512','maxMemory':'1024'}";
    static final int MAX_TRIES = 10000000;
    
    @Test
    public void testSettersGetters() {
        
        testSettersGetters(getTestConfig(MAX, MIN), MAX, MIN);
        testSettersGetters(getTestConfig(MAXO, MINO), MAXO, MINO);
        testSettersGetters(new Gson().fromJson(JSON, BaseProcessConfiguration.class), MAX, MIN);
    }
    
    @SuppressWarnings("unused")
    @Test
    public void testPerf() {
        
        ProcessConfiguration config = getTestConfig(MAXO, MINO);

        long start = System.currentTimeMillis();
        String s;
        // under 10 milli
        for (int i=0; i<MAX_TRIES; i++) {
           s = config.getMaxMemory();
        }
        
        System.out.println("test 1 " + (System.currentTimeMillis()-start));
        
        start = System.currentTimeMillis();  
        
        double d;
        // about 500 milli
        for (int i=0; i<MAX_TRIES; i++) {
            d = config.getMaxMemoryOffer();
        }
         
        System.out.println("test 2 " + (System.currentTimeMillis()-start));
  
        start = System.currentTimeMillis();  
         
        int ii;
        // under 500 milli
        for (int i=0; i<MAX_TRIES; i++) {
            ii = config.getMaxMemorySize();
        }
          
        System.out.println("test 3 " + (System.currentTimeMillis()-start)); 
    }
    
    @Test
    public void testConfPerf() {
        long start = System.currentTimeMillis();
        String ii;
        // 309 milli
        for(int i=0; i<MAX_TRIES; i++) {
            ii = ""+i;
        }
        System.out.println("test 1 " + (System.currentTimeMillis()-start));
        start = System.currentTimeMillis();  
  
        // 367 milli
        for(int i=0; i<MAX_TRIES; i++) {
            ii = Integer.toString(i);
        }    
        System.out.println("test 2 " + (System.currentTimeMillis()-start));
       
    }
    
    
    public ProcessConfiguration getTestConfig(String max, String min) {
        ProcessConfiguration config = new BaseProcessConfiguration();
        config.setMaxMemory(max);
        config.setMinMemory(min);     
        return config;
    }
    
    public void testSettersGetters(ProcessConfiguration config, String max, String min) {
           
        assertEquals(config.getMaxMemory(),max);
        assertEquals(config.getMinMemory(),min);
      
        assertEquals(config.getMaxMemoryOffer(),1024.0,0);
        assertEquals(config.getMinMemoryOffer(),512.0,0);
        
        assertEquals(config.getMaxMemorySize(),1024);
        assertEquals(config.getMinMemorySize(),512);
    }
    
    
    public void testi() {
        Map<String,String> map = new HashMap<String,String>(9);
        
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getValue());
        }
    }
}
