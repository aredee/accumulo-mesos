package aredee.mesos.frameworks.accumulo.initialize;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.accumulo.server.init.Initialize;
import org.apache.mesos.state.ZooKeeperState;
import org.apache.mesos.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import aredee.mesos.frameworks.accumulo.Protos;
import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ConfigNormalizer;
import aredee.mesos.frameworks.accumulo.configuration.process.ServerProcessConfiguration;
import aredee.mesos.frameworks.accumulo.process.AccumuloProcessFactory;
import aredee.mesos.frameworks.accumulo.state.FrameworkStateProtobufPersister;

public class AccumuloInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloInitializer.class);

    private boolean exists;
    private String frameworkId;
    private String accumuloInstanceName;
    private String frameworkName = "";    
    private State frameworkState;
    private FrameworkStateProtobufPersister stateProxy;
    private ServerProcessConfiguration processConfiguration;
    private ClusterConfiguration config;
    
    public AccumuloInitializer(ClusterConfiguration config) throws Exception {
         initializeIfNoInstance(config);
    }
    
    public boolean frameworkExists() {
        return exists;    
    }
    
    public String getAccumuloInstanceName() {
        return accumuloInstanceName;
    }
    
    public State getFrameworkState() {
        return frameworkState;
    }
    
    public String getFrameworkName() {
        return frameworkName;
    }
    
    public String getFrameworkId() {
        return frameworkId;
    }
    
    public ServerProcessConfiguration getProcessConfiguration() {
        return processConfiguration;
    }
    
    public void setClusterConfiguration(ClusterConfiguration config) {
        this.config = config;
    }

    public ClusterConfiguration getClusterConfiguration() {
        return config;
    }

    /**
     * Write the accumulo site file and initialize accumulo.
     * 
     * The system property Environment.ACCUMULO_HOME and HADOOP_CONF_DIR must be set and config must have
     * the accumulo instance name, root password, along with the accumulo directories set.
     * 
     * @param config
     * @return accumulo instance name
     */
    public String initializeAccumuloInstance(ClusterConfiguration config){
        
        setClusterConfiguration(config);
        
        // run accumulo init procedure
        LOGGER.info("Writing accumulo-site.xml");
    
        processConfiguration = new ConfigNormalizer(config).getServiceConfiguration();
        
        String accumuloHome = processConfiguration.getAccumuloDir().getAbsolutePath();
        
        File accumuloSiteFile = writeAccumuloSiteFile(accumuloHome, config.getAccumuloRootPassword(), config.getZkServers());

        LOGGER.info("New site file at " + accumuloSiteFile.getAbsolutePath());
        
        String accumuloInstanceName = config.getAccumuloInstanceName();
        LinkedList<String> initArgs  = new LinkedList<>();
        initArgs.add("--instance-name");
        initArgs.add(accumuloInstanceName);
        initArgs.add("--password");
        initArgs.add(config.getAccumuloRootPassword());
        
        AccumuloProcessFactory processFactory = new AccumuloProcessFactory(processConfiguration);
       
        Process initProcess = null;
        try {
            initProcess = processFactory.exec(Initialize.class, null, initArgs.toArray(new String[initArgs.size()]));
            initProcess.waitFor();
            LOGGER.info("New Accumulo instance initialized");
        } catch (Exception ioe) {
            LOGGER.error("IOException while trying to initialize Accumulo", ioe);
            System.exit(-1);
        }  
        return accumuloInstanceName;
    }
    
    public static File writeAccumuloSiteFile(String accumuloHomeDir, String secret, String zooHosts) {
        File accumuloSiteFile = null;

        LOGGER.info("ACCUMULO HOME? " + accumuloHomeDir);
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("configuration");
            doc.appendChild(rootElement);
            
            addDocProperty("instance.secret",secret,doc,rootElement);
            addDocProperty("instance.zookeeper.host",zooHosts,doc,rootElement);
            addDocProperty("general.classpaths",getGeneralClasspathsLiteral(),doc,rootElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            accumuloSiteFile = new File(accumuloHomeDir + File.separator +
                    "conf" + File.separator + "accumulo-site.xml");
            StreamResult result = new StreamResult(accumuloSiteFile);

            transformer.transform(source, result);

        } catch (Exception e) {
            logErrorAndDie("Error Creating accumulo-site.xml\n",e);
        } 

        return accumuloSiteFile;      
    }
    
    private static void addDocProperty(String name, String value, Document doc, Element rootElement) {
        Element propertyElement = doc.createElement("property");
        rootElement.appendChild(propertyElement);

        Element nameElement = doc.createElement("name");
        nameElement.appendChild(doc.createTextNode(name));
        propertyElement.appendChild(nameElement);

        Element valueElement = doc.createElement("value");
        valueElement.appendChild(doc.createTextNode(value));
        propertyElement.appendChild(valueElement);       
    }
    
    private void initializeIfNoInstance(ClusterConfiguration config) throws Exception {
        LOGGER.info("Checking Framework State");
        // Check Framework State to see if this is a failover framework.
        LOGGER.info("Reading from ZooKeeperState: {}", config.getZkServers() );
        frameworkState = new ZooKeeperState(config.getZkServers(), 60L, TimeUnit.SECONDS, "accumulo-mesos" );
        stateProxy = new FrameworkStateProtobufPersister(frameworkState);
        exists = false;
 
        LOGGER.info("Fetching frameworks from state store");
        for(Protos.FrameworkIdentity fid : stateProxy.getFrameworks()){
            LOGGER.info("Found framework {} - {}", fid.getFrameworkName(), fid.getFrameworkId());
            if( fid.getFrameworkName().equals(config.getFrameworkName()) ){
                LOGGER.info("Found matching framework");
                exists = true;
                frameworkId = fid.getFrameworkId();
                frameworkName = fid.getFrameworkName();
                break;
            }
        }
        initializeAccumulo(config);
    }
    
    private void initializeAccumulo(ClusterConfiguration config) {
        if( exists ){
            LOGGER.info("Found Existing Accumulo Instance");
            try {
                accumuloInstanceName = stateProxy.getAccumuloInstanceName(frameworkId);
            } catch (Exception e) {
                LOGGER.error("Failed to acquire accumulo instance from state proxy",e);
                throw new RuntimeException("Access to state store failed: " + e.getMessage());
            }  
        } else {
            LOGGER.info("Framework not found in state store, Initializing New Accumulo Instance");
            accumuloInstanceName = initializeAccumuloInstance(config);
            frameworkName = config.getFrameworkName();
            frameworkId = frameworkName+"-"+accumuloInstanceName;
            LOGGER.info("FrameworkName? " + frameworkName + " frameworkId? " + frameworkId);
            
            stateProxy.saveAccumuloInstanceName(frameworkId, accumuloInstanceName);
        }
        config.setAccumuloInstanceName(accumuloInstanceName);
    }
    
    private static void logErrorAndDie(String message, Exception e){
        LOGGER.error(message,e);
        System.exit(-1);
    }

    private static final String getGeneralClasspathsLiteral(){
        return (new StringBuilder())
                .append("\n$ACCUMULO_HOME/lib/accumulo-server.jar,\n")
                .append("$ACCUMULO_HOME/lib/accumulo-core.jar,\n")
                .append("$ACCUMULO_HOME/lib/accumulo-start.jar,\n")
                .append("$ACCUMULO_HOME/lib/accumulo-fate.jar,\n")
                .append("$ACCUMULO_HOME/lib/accumulo-proxy.jar,\n")
                .append("$ACCUMULO_HOME/lib/[^.].*.jar,\n")
                .append("$ZOOKEEPER_HOME/zookeeper[^.].*.jar,\n")
                .append("$HADOOP_CONF_DIR,\n")
                .append("$HADOOP_PREFIX/share/hadoop/common/[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/share/hadoop/common/lib/(?!slf4j)[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/share/hadoop/hdfs/[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/share/hadoop/mapreduce/[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/share/hadoop/yarn/[^.].*.jar,\n")
                .append("/usr/lib/hadoop/[^.].*.jar,\n")
                .append("/usr/lib/hadoop/lib/[^.].*.jar,\n")
                .append("/usr/lib/hadoop-hdfs/[^.].*.jar,\n")
                .append("/usr/lib/hadoop-mapreduce/[^.].*.jar,\n")
                .append("/usr/lib/hadoop-yarn/[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/[^.].*.jar,\n")
                .append("$HADOOP_PREFIX/lib/(?!slf4j)[^.].*.jar\n")
                .toString();
    }
    
}
