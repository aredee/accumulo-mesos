package aredee.mesos.frameworks.accumulo.initialize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import aredee.mesos.frameworks.accumulo.model.Accumulo;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import aredee.mesos.frameworks.accumulo.configuration.Defaults;

import com.google.common.base.Optional;
import com.jcabi.xml.XMLDocument;


public class AccumuloSiteXml {

    // TODO get these from Accumulo code? does that add unnecessary dependency on accumulo?
    public static final String PASSWORD_PROP = "instance.secret";
    public static final String ZOOKEEPER_PROP = "instance.zookeeper.host";
    
    private Document document;
    private XPath xPath;

    private Accumulo config = null;

    public AccumuloSiteXml(Accumulo config){
        this.config = config;

        setPropertyValue(PASSWORD_PROP, config.getRootPassword());
        setPropertyValue(ZOOKEEPER_PROP, config.getZkServers());

    }

    // TODO remove this assumption. should be at $ACCUMULO_HOME/conf
    /**
     * Assumes the accumulo-site.xml is at the Defaults.ACCUMULO_SITE_URI location.
     * @throws Exception
     */
    public AccumuloSiteXml() throws Exception {
        this(Defaults.ACCUMULO_SITE_URI);
    }


    public AccumuloSiteXml(String siteUri) throws Exception {
        initialize(new URI(siteUri).toURL().openStream());
    }

    public AccumuloSiteXml(InputStream xmlSiteInput) throws Exception {
        initialize(xmlSiteInput);
    }
    
    public void writeSiteFile(File siteFile) throws IOException {
        writeSiteFile(new FileOutputStream(siteFile));
    }
    
    public void writeSiteFile(String siteFile) throws IOException {
        writeSiteFile(new FileOutputStream(siteFile));
    }   
    
    public void writeSiteFile(OutputStream siteFile) throws IOException {
        IOUtils.write(toXml(), siteFile);
        IOUtils.closeQuietly(siteFile);
    }
    
    /**
     * 
     * @return accumulo-site.xml as a string
     * 
     */
    public String toXml() {
        return new XMLDocument(document).toString();
    }
    
    /**
     * @return accumulo-site.xml as a string
     */
    public String toString() {
        return toXml();
    }

    /**
     * Get the value of a property
     * @param propertyName
     * @return property value, Optional.isPresent == true if property is there but could be empty
     * otherwise Optional.isPresent == false.
     * @throws Exception
     */
    private Optional<String> getPropertyValue(String propertyName) throws Exception {
        Optional<String> value = Optional.fromNullable(null);
        Node node = getPropertyValueNode(propertyName);
        if (node != null){
            value = Optional.fromNullable(node.getTextContent());
        }
        return value;
    }
    /**
     * This will set and existing property to a new value. If its a new property
     * then use addProperty().
     *
     * @param propertyName of property
     * @param value of property
     * @throws Exception
     */
    // TODO why not catch exception and then call addProperty here?
    private void setPropertyValue(String propertyName, String value) {

        try {
            Node node = null;
            node = getPropertyValueNode(propertyName);
            if (node != null){
                node.setTextContent(value);
            }
        } catch (XPathExpressionException e) {
            addProperty(propertyName, value);
        }
    }

    private void addProperty(String name, String value) {

        Element propertyElement = document.createElement("property");
        document.getDocumentElement().appendChild(propertyElement);

        Element nameElement = document.createElement("name");
        nameElement.appendChild(document.createTextNode(name));
        propertyElement.appendChild(nameElement);

        Element valueElement = document.createElement("value");
        valueElement.appendChild(document.createTextNode(value));
        propertyElement.appendChild(valueElement);
    }

    private void initialize(InputStream input) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(input);
        IOUtils.closeQuietly(input);
        xPath = XPathFactory.newInstance().newXPath();      
    }
   
    private Node getPropertyValueNode(String propertyName) throws XPathExpressionException {
        String xpath = "//property/name[. = '"+propertyName+"']";
        Node node = (Node) xPath.evaluate(xpath, document, XPathConstants.NODE);
        if (node != null){
            while (!node.getNextSibling().getNodeName().equalsIgnoreCase("value")) {
                node = node.getNextSibling();
            }
            node = node.getNextSibling();
        }
        return node;
    }
    
    
}
