package aredee.mesos.frameworks.accumulo.configuration.file;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import com.google.common.base.Optional;

import aredee.mesos.frameworks.accumulo.TestSupport;

public class TestSiteXml {

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
            
            URI uri = ClassLoader.class.getResource("/TestAccumuloSite.xml").toURI();
       
            System.out.println("Using test accumulo site file: " + uri);
                        
            AccumuloSiteXml xmlSite = new AccumuloSiteXml(uri.toURL().openStream());
            
            //System.out.println(xmlSite.toXml());
  
            value = xmlSite.getPassword();
            assertTrue(value.isPresent());
            assertTrue(value.get().equalsIgnoreCase("DEFAULT"));
            
            xmlSite.setPassword("newpassword");
            
           // System.out.println(xmlSite.toXml());
            value = xmlSite.getPassword();
            assertTrue(value.isPresent());
            assertTrue(value.get().equalsIgnoreCase("newpassword"));            
           
            value = xmlSite.getPropertyValue("instance.volumes");
            assertTrue(value.isPresent());
            
            value = xmlSite.getPropertyValue("xyz.volumes");  
            assertTrue(!value.isPresent());
            
            xmlSite.addProperty("BLAH", "BLAHBLAH");
            
           // System.out.println(xmlSite.toXml());          
            
            value = xmlSite.getPropertyValue("BLAH");  
            assertTrue(value.isPresent());
  
            String xml = xmlSite.toString();
            
            AccumuloSiteXml xmlSite2 = new AccumuloSiteXml(new ByteArrayInputStream(xml.getBytes()));
            File xmlFile = new File("./MyNewSiteFile.xml");
            xmlSite2.writeSiteFile(xmlFile);
            assertTrue(xmlFile.exists());
            xmlFile.delete();
       
            
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    
    }
    /**
     * This test will check that a blank site file can have properties added.
     */
    @Test
    public void testAddingPropertiesToBlankSiteFile() {
        Optional<String> value;
   
        try {
            URI uri = ClassLoader.class.getResource("/TestBlankAccumuloSite.xml").toURI();
            
            System.out.println("Using test blank accumulo site file: " + uri);
                        
            AccumuloSiteXml xmlSite = new AccumuloSiteXml(uri.toURL().openStream());
            
            value = xmlSite.getPropertyValue(AccumuloSiteXml.PASSWORD_PROP);
            assertFalse(value.isPresent());
            
            xmlSite.addProperty(AccumuloSiteXml.PASSWORD_PROP, "passwordxxxx");
            
            value = xmlSite.getPropertyValue(AccumuloSiteXml.PASSWORD_PROP);
            assertTrue(value.isPresent());          
            
            File xmlFile = new File("/tmp/TestSiteFile.xml");
            xmlSite.writeSiteFile(xmlFile);
            assertTrue(xmlFile.exists());
            
            // Now load it into a new xml object and check to make sure the property is there.
            
            AccumuloSiteXml xmlSite2 = new AccumuloSiteXml(FileUtils.openInputStream(xmlFile));
          
            value = xmlSite2.getPropertyValue(AccumuloSiteXml.PASSWORD_PROP);
            assertTrue(value.isPresent());            
            
            xmlFile.delete();
            
            
        } catch(Exception e) {
           e.printStackTrace();
           fail();
        }
        
    }
}
