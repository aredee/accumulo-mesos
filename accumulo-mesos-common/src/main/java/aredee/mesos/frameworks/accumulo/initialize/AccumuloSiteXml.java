package aredee.mesos.frameworks.accumulo.initialize;

import aredee.mesos.frameworks.accumulo.model.Accumulo;
import com.google.common.base.Optional;
import com.jcabi.xml.XMLDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;


public class AccumuloSiteXml {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloSiteXml.class);

    private static final String INSTANCE_VOLUMES = "instance.volumes";  // hdfs://localhost:9000/accumulo ...
    private static final String INSTANCE_ZOOKEEPER_HOST = "instance.zookeeper.host";

    private static final String TRACE_USER = "trace.user";
    private static final String TRACE_USER_DEFAULT = "root";
    private static final String TRACE_TOKEN_PROPERTY_PASSWORD = "trace.password";
    private static final String TRACE_TOKEN_PROPERTY_PASSWORD_DEFAULT = "secret";

    private static final String INSTANCE_SECRET = "instance.secret";
    private static final String INSTANCE_SECRET_DEFAULT = "DEFAULT";

    private static final String GENERAL_CLASSPATHS = "general.classpaths";
    private static final String GENERAL_CLASSPATHS_DEFAULT = new StringBuilder()
            .append("$ACCUMULO_HOME/lib/accumulo-server.jar,")
            .append("$ACCUMULO_HOME/lib/accumulo-core.jar,")
            .append("$ACCUMULO_HOME/lib/accumulo-start.jar,")
            .append("$ACCUMULO_HOME/lib/accumulo-fate.jar,")
            .append("$ACCUMULO_HOME/lib/accumulo-proxy.jar,")
            .append("$ACCUMULO_HOME/lib/[^.].*.jar,")
            .append("$ZOOKEEPER_HOME/zookeeper[^.].*.jar,")
            .append("$HADOOP_CONF_DIR,")
            .append("$HADOOP_PREFIX/share/hadoop/common/[^.].*.jar,")
            .append("$HADOOP_PREFIX/share/hadoop/common/lib/(?!slf4j)[^.].*.jar,")
            .append("$HADOOP_PREFIX/share/hadoop/hdfs/[^.].*.jar,")
            .append("$HADOOP_PREFIX/share/hadoop/mapreduce/[^.].*.jar,")
            .append("$HADOOP_PREFIX/share/hadoop/yarn/[^.].*.jar,")
            .append("$HADOOP_PREFIX/share/hadoop/yarn/lib/jersey.*.jar,")
            .toString();

    // These are set on the Executor because different tservers might have different memory profiles
    private static final String TSERVER_MEMORY_MAPS_MAX = "tserver.memory.maps.max";
    private static final double TSERVER_MEMORY_MAPS_MAX_FACTOR = 0.75;
    private static final String TSERVER_CACHE_DATA_SIZE = "tserver.cache.data.size";
    private static final double TSERVER_CACHE_DATA_SIZE_FACTOR = 0.25;
    private static final String TSERVER_CACHE_INDEX_SIZE = "tserver.cache.index.size";
    private static final double TSERVER_CACHE_INDEX_SIZE_FACTOR = 0.1;
    private static final String TSERVER_SORT_BUFFER_SIZE = "tserver.sort.buffer.size";
    private static final double TSERVER_SORT_BUFFER_SIZE_FACTOR = 0.25;
    private static final String TSERVER_WALOG_MAX_SIZE = "tserver.walog.max.size";
    private static final double TSERVER_WALOG_MAX_SIZE_FACTOR = 0.1;

    private static final String TSERVER_MEMORY_MAPS_NATIVE_ENABLED = "tserver.memory.maps.native.enabled";

    private Document document;
    private XPath xPath;

    private Accumulo config = null;

    public AccumuloSiteXml(){
        // empty, used by Executor
    }

    public AccumuloSiteXml(Accumulo config) {
        this.config = config;
    }

    public void initializeFromScheduler(String xmlString){
        initXml(xmlString);

        addDefaultProperties();
        addPropertiesFromConfig();
        defineNativeMaps();
    }

    /**
     * Most of the properties should be set by the scheduler. This initializes the XML
     * to override memory properties, and set native maps based on local os.
     *
     * @param xmlString
     */
    public void initializeFromExecutor(String xmlString){
        initXml(xmlString);
        defineNativeMaps();
    }

    /**
     *
     * Breaks apart given total memory into sane tserver memory settings
     *
     * @param memory
     */
    public void defineTserverMemory(double memory) {
        // TODO this should probably be more sophisticated. Some of these may have a pratical cap
        setMemoryValue(memory, TSERVER_MEMORY_MAPS_MAX, TSERVER_MEMORY_MAPS_MAX_FACTOR);
        setMemoryValue(memory, TSERVER_CACHE_DATA_SIZE, TSERVER_CACHE_DATA_SIZE_FACTOR);
        setMemoryValue(memory, TSERVER_CACHE_INDEX_SIZE, TSERVER_CACHE_INDEX_SIZE_FACTOR);
        setMemoryValue(memory, TSERVER_SORT_BUFFER_SIZE, TSERVER_SORT_BUFFER_SIZE_FACTOR);
        setMemoryValue(memory, TSERVER_WALOG_MAX_SIZE, TSERVER_WALOG_MAX_SIZE_FACTOR);
    }

    private void setMemoryValue(double memory, String name, double factor){
        Double result = memory * factor;
        Integer whole = result.intValue();
        String mem = whole.toString() + "M";
        setPropertyValue(name, mem);
    }

    private void defineNativeMaps(){
        // TODO make this an option?
        String enableNativeMaps = "false";
        String os = System.getProperty("os.name");
        if( os.contains("Linux")){
            enableNativeMaps = "true";
        }
        setPropertyValue(TSERVER_MEMORY_MAPS_NATIVE_ENABLED, enableNativeMaps);

    }

    private void addPropertiesFromConfig(){
        setPropertyValue(INSTANCE_ZOOKEEPER_HOST, config.getZkServers());
        String hdfsUri = config.getHdfsUri();
        if( !hdfsUri.endsWith(File.pathSeparator)){
            hdfsUri += File.pathSeparator;
        }
        hdfsUri += config.getInstance();
        setPropertyValue(INSTANCE_VOLUMES, hdfsUri);
    }

    private void addDefaultProperties(){
        setPropertyValue(GENERAL_CLASSPATHS, GENERAL_CLASSPATHS_DEFAULT);
        setPropertyValue(INSTANCE_SECRET, INSTANCE_SECRET_DEFAULT);
        setPropertyValue(TRACE_USER, TRACE_USER_DEFAULT);
        setPropertyValue(TRACE_TOKEN_PROPERTY_PASSWORD, TRACE_TOKEN_PROPERTY_PASSWORD_DEFAULT);
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
    private void setPropertyValue(String propertyName, String value) {

        try {
            Node node = null;
            node = getPropertyValueNode(propertyName);
            if (node != null){
                node.setTextContent(value);
            } else {
                addProperty(propertyName, value);
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

    private void initXml(String input) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(input);
            xPath = XPathFactory.newInstance().newXPath();
        } catch (ParserConfigurationException e) {
            LOGGER.error("Error reconsituting XML from String: {}", input);
            throw new RuntimeException(e);
        } catch (SAXException e) {
            LOGGER.error("Error reconsituting XML from String: {}", input);
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.error("Error reconsituting XML from String: {}", input);
            throw new RuntimeException(e);
        }
    }

/*
    private void initXml(){
        this.xPath = XPathFactory.newInstance().newXPath();

        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // TODO handle this failure
            throw new RuntimeException(e);
        }

        this.document = builder.newDocument();
    }
*/
    /**
     * Creates an xml string containing just the configuration node
     *
     * @return
     */
    public static String getEmptySiteXml(){
        DocumentBuilder docBuilder = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.error("Unable to create xml document");
            throw new RuntimeException(e);
        }

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("configuration");
        doc.appendChild(rootElement);

        return new XMLDocument(doc).toString();
    }
}
