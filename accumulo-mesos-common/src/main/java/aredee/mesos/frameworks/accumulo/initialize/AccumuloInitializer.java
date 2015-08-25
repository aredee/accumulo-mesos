package aredee.mesos.frameworks.accumulo.initialize;

import java.io.File;
import java.util.LinkedList;

import aredee.mesos.frameworks.accumulo.model.Accumulo;
import org.apache.accumulo.server.init.Initialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aredee.mesos.frameworks.accumulo.process.AccumuloProcessFactory;

public class AccumuloInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloInitializer.class);

    private Accumulo config;
    
    public AccumuloInitializer(Accumulo config) throws Exception {
        this.config = config;
    }
    
    /**
     * Write the accumulo site file and initialize accumulo.
     * 
     * The system property Environment.ACCUMULO_HOME and HADOOP_CONF_DIR must be set and config must have
     * the accumulo instance name, root password, along with the accumulo directories set.
     *
     * @return accumulo instance name
     */
    public void initialize() throws Exception{
        
        // run accumulo init procedure
        LOGGER.info("Writing accumulo-site.xml");
    
        //processConfiguration = new ConfigNormalizer(config).getServiceConfiguration();
        //String accumuloHome = processConfiguration.getAccumuloDir().getAbsolutePath();
        
        AccumuloSiteXml siteXml = new AccumuloSiteXml(this.config);

        this.writeAccumuloSiteFile(accumuloHome, siteXml);

        // TODO create instance name
        String accumuloInstanceName = config.getInstance();

        LinkedList<String> initArgs  = new LinkedList<>();
        initArgs.add("--instance-name");
        initArgs.add(accumuloInstanceName);
        initArgs.add("--password");
        initArgs.add(config.getRootPassword());
        // This clears the instance name out of zookeeper, this may need revisited, but was
        // needed during testing.
        initArgs.add("--clear-instance-name");


        AccumuloProcessFactory processFactory = new AccumuloProcessFactory("256");
       
        Process initProcess = null;
        try {
            initProcess = processFactory.exec(Initialize.class, null, initArgs.toArray(new String[initArgs.size()]));
            LOGGER.info("Initializing Accumulo");
            initProcess.waitFor();
            LOGGER.info("New Accumulo instance initialized");
        } catch (Exception ioe) {
            LOGGER.error("IOException while trying to initialize Accumulo", ioe);
            System.exit(-1);
        }  
        return;
    }

/*
    public static AccumuloSiteXml createAccumuloSiteXml(String xml) throws Exception {
        return new AccumuloSiteXml(new ByteArrayInputStream(xml.getBytes()));
    }
*/

    private void writeAccumuloSiteFile(String accumuloHomeDir, AccumuloSiteXml siteXml) {
        LOGGER.info("ACCUMULO HOME? " + accumuloHomeDir);
        try {
  
            File accumuloSiteFile = new File(accumuloHomeDir + File.separator +
                    "conf" + File.separator + "accumulo-site.xml");
  
            siteXml.writeSiteFile(accumuloSiteFile);
             
        } catch (Exception e) {
            logErrorAndDie("Error Creating accumulo-site.xml\n",e);
        }
    }

    private static void logErrorAndDie(String message, Exception e){
        LOGGER.error(message,e);
        throw new RuntimeException(e);
    }

}
