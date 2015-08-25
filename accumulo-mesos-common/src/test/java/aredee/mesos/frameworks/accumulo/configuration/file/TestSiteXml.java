package aredee.mesos.frameworks.accumulo.configuration.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;

import aredee.mesos.frameworks.accumulo.initialize.AccumuloSiteXml;
//import org.apache.commons.configuration.XMLConfiguration;

import com.google.common.base.Optional;

public class TestSiteXml {

    static final String MAX = "1024";
    static final String MIN = "512";
    static final String MAXO = "1024.0";
    static final String MINO = "512.0";
    
    static final String JSON = "{'minMemory':'512','maxMemory':'1024'}";
    static final int MAX_TRIES = 10000000;

// TODO get test working
//    @SuppressWarnings("unchecked")
//    @Test
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
            
            xmlSite.setPropertyValue("BLAH", "BLAHBLAH");
            
            System.out.println(xmlSite.toXml());          
            
            value = xmlSite.getPropertyValue("BLAH");  
            assertTrue(value.isPresent());
  
            String xml = xmlSite.toString();
            
            AccumuloSiteXml xmlSite2 = new AccumuloSiteXml(new ByteArrayInputStream(xml.getBytes()));
            File xmlFile = new File("./MyNewSiteFile.xml");
            xmlSite2.writeSiteFile(xmlFile);
            assertTrue(xmlFile.exists());
       
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
    
    }
  
}
