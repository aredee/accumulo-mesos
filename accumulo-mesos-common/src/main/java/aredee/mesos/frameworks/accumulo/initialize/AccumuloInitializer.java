package aredee.mesos.frameworks.accumulo.initialize;

import aredee.mesos.frameworks.accumulo.configuration.Constants;
import aredee.mesos.frameworks.accumulo.configuration.Environment;
import aredee.mesos.frameworks.accumulo.model.Accumulo;
import aredee.mesos.frameworks.accumulo.model.ServerProfile;
import aredee.mesos.frameworks.accumulo.process.AccumuloProcessFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;

public class AccumuloInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloInitializer.class);

    private Accumulo config;
    private String accumuloHome;
    
    public AccumuloInitializer(Accumulo config) {
        this.config = config;
        this.accumuloHome = Environment.get(Environment.ACCUMULO_HOME);
    }
    
    /**
     * Write the accumulo site file and initialize accumulo.
     * 
     * The system property Environment.ACCUMULO_HOME and HADOOP_CONF_DIR must be set and config must have
     * the accumulo instance name, root password, along with the accumulo directories set.
     *
     * @return accumulo instance name
     */
    public int initialize() throws IOException {
        
        // run accumulo init procedure
        LOGGER.info("Writing accumulo-site.xml");

        AccumuloSiteXml siteXml = new AccumuloSiteXml(this.config);
        siteXml.initializeFromScheduler(AccumuloSiteXml.getEmptySiteXml());

        writeAccumuloSiteFile(accumuloHome, siteXml);
        config.setSiteXml(siteXml.toString());

        // accumulo-env.sh
        copyAccumuloEnvFile(accumuloHome); // IOException

        LinkedList<String> initArgs  = new LinkedList<>();
        initArgs.add("--instance-name");
        initArgs.add(config.getInstance());
        initArgs.add("--password");
        initArgs.add(config.getRootPassword());
        initArgs.add("--clear-instance-name");

        AccumuloProcessFactory processFactory = new AccumuloProcessFactory();
       
        Process initProcess;
        int status = 0;
        try {
            initProcess = processFactory.exec(ServerProfile.TypeEnum.init.getServerKeyword(),
                                              initArgs.toArray(new String[initArgs.size()]));
            initProcess.waitFor();
            status = initProcess.exitValue();
        } catch (Exception ioe) {
            LOGGER.error("IOException while trying to initialize Accumulo", ioe);
            status = -1;
        }  
        return status;
    }

    public static void writeAccumuloSiteFile(String accumuloHomeDir, AccumuloSiteXml siteXml) {
        LOGGER.info("ACCUMULO HOME? " + accumuloHomeDir);
        try {
  
            File accumuloSiteFile = new File(accumuloHomeDir + File.separator +
                    "conf" + File.separator + "accumulo-site.xml");

            LOGGER.info("Writing accumulo-site.xml to {}", accumuloSiteFile.getAbsolutePath());

            OutputStream siteFile = new FileOutputStream(accumuloSiteFile);
            IOUtils.write(siteXml.toXml(), siteFile);
            IOUtils.closeQuietly(siteFile);

        } catch (Exception e) {
            logErrorAndDie("Error Creating accumulo-site.xml\n",e);
        }
    }

    /**
     * This is required because the bin/accumulo script complains without it. The framework should be setting
     * all the environment variables that this sets, and the script uses them if already set.
     *
     * @param accumuloHomeDir
     * @throws IOException
     */
    public static void copyAccumuloEnvFile(String accumuloHomeDir) throws IOException {
        File inputFile = new File(accumuloHomeDir+File.separator+"conf/examples/1GB/native-standalone/accumulo-env.sh");
        File destFile = new File(accumuloHomeDir+File.separator+"conf/accumulo-env.sh");
        Files.copy(inputFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copy native maps from mesosDirectory to accumuloHome/lib/native if the maps exist
     *
     * @param mesosDirectory
     * @param accumuloHome
     * @throws IOException
     */
    public static void copyAccumuloNativeMaps( String mesosDirectory, String accumuloHome) throws IOException {
        File inputFile = new File(mesosDirectory+File.separator+ Constants.ACCUMULO_NATIVE_LIB);
        File destFile = new File(accumuloHome+File.separator+"lib/native");
        if( inputFile.exists() ) {
            Files.copy(inputFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }


    private static void logErrorAndDie(String message, Exception e){
        LOGGER.error(message, e);
        throw new RuntimeException(message, e);
    }


}
