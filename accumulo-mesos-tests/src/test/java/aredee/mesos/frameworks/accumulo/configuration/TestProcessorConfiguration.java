package aredee.mesos.frameworks.accumulo.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.commons.configuration.XMLConfiguration;
//import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.base.Optional;
import com.google.gson.Gson;

public class TestProcessorConfiguration {

    static final String MAX = "1024";
    static final String MIN = "512";
    static final String MAXO = "1024.0";
    static final String MINO = "512.0";
    
    static final String JSON = "{'minMemory':'512','maxMemory':'1024'}";
    static final int MAX_TRIES = 10000000;
    
    @Test
    public void testXmlConfiguration() {
        
        Optional<String> value;
        
        try {
            AccumuloSiteXml xmlSite = new AccumuloSiteXml();
            System.out.println(xmlSite.toXml());
  
            value = xmlSite.getPassword();
            assertTrue(value.isPresent());
            assertTrue(value.get().equalsIgnoreCase("DEFAULT"));
            
            xmlSite.setPassword("newpassword");
            System.out.println(xmlSite.toXml());
            value = xmlSite.getPassword();
            assertTrue(value.isPresent());
            assertTrue(value.get().equalsIgnoreCase("newpassword"));            
           
            value = xmlSite.getPropertyValue("instance.volumes");
            assertTrue(value.isPresent());
            
            value = xmlSite.getPropertyValue("xyz.volumes");  
            assertTrue(!value.isPresent());
            
            xmlSite.addProperty("BLAH", "BLAHBLAH");
            
            System.out.println(xmlSite.toXml());          
            
            value = xmlSite.getPropertyValue("BLAH");  
            assertTrue(value.isPresent());
  
            String xml = xmlSite.toString();
            
            AccumuloSiteXml xmlSite2 = new AccumuloSiteXml(new ByteArrayInputStream(xml.getBytes()));
            xmlSite2.writeSiteFile(new File("./MyNewSiteFile.xml"));
       
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        /**
        try {
            
            InputStream file = new URL("file:./conf/default-accumulo-site.xml").openStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);
            
            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.evaluate("//property/name[. = 'instance.zookeeper.host']", document, XPathConstants.NODE);
            if (node != null)
            {
                node = node.getNextSibling();
                System.out.println(node + " name " +node.getNodeName() + " text ? "+node.getTextContent());  
                node.setTextContent("10.0.0.0");
             }
             
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      
        **/
  

    }
    @Test
    public void testSettersGetters() {
        
        testSettersGetters(getTestConfig(MAX, MIN), MAX, MIN);
        testSettersGetters(getTestConfig(MAXO, MINO), MAXO, MINO);
        testSettersGetters(new Gson().fromJson(JSON, ProcessorConfiguration.class), MAX, MIN);
    }
    
    @SuppressWarnings("unused")
    @Test
    public void testPerf() {
        
        IProcessorConfiguration config = getTestConfig(MAXO, MINO);

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
    
    
    public IProcessorConfiguration getTestConfig(String max, String min) {
        ProcessorConfiguration config = new ProcessorConfiguration();
        config.setMaxMemory(max);
        config.setMinMemory(min);     
        return config;
    }
    
    public void testSettersGetters(IProcessorConfiguration config, String max, String min) {
           
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
