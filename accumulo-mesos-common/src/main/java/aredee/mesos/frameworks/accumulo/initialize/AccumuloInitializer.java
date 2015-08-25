package aredee.mesos.frameworks.accumulo.initialize;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import aredee.mesos.frameworks.accumulo.model.Accumulo;
import aredee.mesos.frameworks.accumulo.model.Framework;
import org.apache.accumulo.server.init.Initialize;
import org.apache.commons.lang3.StringUtils;
import org.apache.mesos.state.ZooKeeperState;
import org.apache.mesos.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aredee.mesos.frameworks.accumulo.configuration.file.AccumuloSiteXml;
import aredee.mesos.frameworks.accumulo.configuration.ConfigNormalizer;
import aredee.mesos.frameworks.accumulo.configuration.process.ServerProcessConfiguration;
import aredee.mesos.frameworks.accumulo.process.AccumuloProcessFactory;
import aredee.mesos.frameworks.accumulo.state.FrameworkStateProtobufPersister;

public class AccumuloInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloInitializer.class);

    private String frameworkId;
    private String accumuloInstanceName;
    private String frameworkName = "";    
    private State frameworkState;
    private FrameworkStateProtobufPersister stateProxy;
    private ServerProcessConfiguration processConfiguration;
    //private ClusterConfiguration config;
    private AccumuloSiteXml siteXml;
    
    
    public AccumuloInitializer(Accumulo accumuloConfig) throws Exception {
         initializeIfNoInstance(accumuloConfig);
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
    
    //public void setClusterConfiguration(ClusterConfiguration config) {
    //    this.config = config;
    //}

    //public ClusterConfiguration getClusterConfiguration() {
    //    return config;
    //}

    /**
     * Write the accumulo site file and initialize accumulo.
     * 
     * The system property Environment.ACCUMULO_HOME and HADOOP_CONF_DIR must be set and config must have
     * the accumulo instance name, root password, along with the accumulo directories set.
     *
     * @param accumuloConfig
     * @return accumulo instance name
     */
    public String initializeAccumuloInstance(Accumulo accumuloConfig) throws Exception{
        
        //setClusterConfiguration(config);
        
        // run accumulo init procedure
        LOGGER.info("Writing accumulo-site.xml");
    
        processConfiguration = new ConfigNormalizer(accumuloConfig).getServiceConfiguration();
        
        String accumuloHome = processConfiguration.getAccumuloDir().getAbsolutePath();
        
        siteXml = createAccumuloSiteXml( accumuloConfig.getRootPassword(),
                accumuloConfig.getZkServers());

        writeAccumuloSiteFile(accumuloHome, siteXml);

        String accumuloInstanceName = accumuloConfig.getInstanceName();
        LinkedList<String> initArgs  = new LinkedList<>();
        initArgs.add("--instance-name");
        initArgs.add(accumuloInstanceName);
        initArgs.add("--password");
        initArgs.add(accumuloConfig.getRootPassword());
        
        // This clears the instance name out of zookeeper, this may need revisited, but was
        // needed during testing.
        //
        initArgs.add("--clear-instance-name");
        
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
  
    public AccumuloSiteXml getSiteXml() {
        return siteXml;
    }

    /*
    public static AccumuloSiteXml createAccumuloSiteXml(String xml) throws Exception {
        return new AccumuloSiteXml(new ByteArrayInputStream(xml.getBytes()));
    }   
    */

    public static AccumuloSiteXml createAccumuloSiteXml(String password, String zooKeepers) throws Exception {
        AccumuloSiteXml siteXml;
         
         siteXml = new AccumuloSiteXml();

         if (!StringUtils.isEmpty(password)) {
             siteXml.setPassword(password);
         }
         if (!StringUtils.isEmpty(zooKeepers)) {
             siteXml.setZookeeper(zooKeepers);
         }
         return siteXml;
     }
    
    public static void writeAccumuloSiteFile(String accumuloHomeDir, AccumuloSiteXml siteXml) {
        LOGGER.info("ACCUMULO HOME? " + accumuloHomeDir);
        try {
  
            File accumuloSiteFile = new File(accumuloHomeDir + File.separator +
                    "conf" + File.separator + "accumulo-site.xml");
  
            siteXml.writeSiteFile(accumuloSiteFile);
             
        } catch (Exception e) {
            logErrorAndDie("Error Creating accumulo-site.xml\n",e);
        }
    }

    private void initializeIfNoInstance(Accumulo accumuloConfig) throws Exception {
        LOGGER.info("Checking Framework State");
        // Check Framework State to see if this is a failover framework.
        LOGGER.info("Reading from ZooKeeperState: {}", accumuloConfig.getZkServers() );
        frameworkState = new ZooKeeperState(accumuloConfig.getZkServers(), 60L, TimeUnit.SECONDS, "accumulo-mesos" );
        stateProxy = new FrameworkStateProtobufPersister(frameworkState);
        boolean exists = false;
 
        LOGGER.info("Fetching frameworks from state store");
        for(Framework framework : stateProxy.getFrameworks()){
            LOGGER.info("Found framework {} - {}", framework.getName(), framework.getId());
            for( Cluster cluster : framework.getClusters() ){
                if( cluster.getAccumulo().getInstanceName().equals(
                    accumuloConfig.getInstanceName())
                ){
                    LOGGER.info("Found matching framework");
                    exists = true;
                    // TODO why are these mixed in here?
                    //frameworkId = fid.getFrameworkId();
                    //frameworkName = fid.getFrameworkName();
                    break;
                }
            }
        }
        if( exists ){
            LOGGER.info("Found Existing Accumulo Instance");
            try {
                accumuloInstanceName = stateProxy.getAccumuloInstanceName(frameworkId);
            } catch (Exception e) {
                LOGGER.error("Failed to acquire accumulo instance from state proxy",e);
                throw new RuntimeException("Access to state store failed: " + e.getMessage());
            }
        } else {
            initializeAccumulo(accumuloConfig);
        }

    }
    
    private void initializeAccumulo(Accumulo accumuloConfig) throws Exception{
        LOGGER.info("Framework not found in state store, Initializing New Accumulo Instance");
        accumuloInstanceName = initializeAccumuloInstance(accumuloConfig);
        frameworkName = accumuloConfig.getFrameworkName();
        frameworkId = frameworkName+"-"+accumuloInstanceName;
        LOGGER.info("FrameworkName? " + frameworkName + " frameworkId? " + frameworkId);

        stateProxy.saveAccumuloInstanceName(frameworkId, accumuloInstanceName);
        accumuloConfig.setInstanceName(accumuloInstanceName);
    }
    
    private static void logErrorAndDie(String message, Exception e){
        LOGGER.error(message,e);
        throw new RuntimeException(e);
    }
 
    
}
