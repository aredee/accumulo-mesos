package aredee.mesos.frameworks.accumulo.configuration.cluster;

import aredee.mesos.frameworks.accumulo.configuration.process.ProcessConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

/**
 * Created by klucar on 7/23/15.
 */
public class JSONClusterConfiguration implements ClusterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONClusterConfiguration.class);

    private ClusterConfiguration jsonAdaptor;

    public JSONClusterConfiguration(String fileLocation) {

        FileReader jsonReader = null;
        try {
            jsonReader = new FileReader(new File(fileLocation));
            
            JsonBaseClusterConfiguration json = new Gson().fromJson(jsonReader, JsonBaseClusterConfiguration.class);
            jsonAdaptor = (BaseClusterConfiguration)json;
            
            // TODO verify JSON contains everything needed to get the job done
        } catch(Exception e) {
            LOGGER.error("Failed to find input json configuration: " + fileLocation, e);
        } finally {
            IOUtils.closeQuietly(jsonReader);
        }
    }

    @Override
    public String getBindAddress() {
        return jsonAdaptor.getBindAddress();
    }

    @Override
    public void setBindAddress(String address) {
        jsonAdaptor.setBindAddress(address);
    }

    @Override
    public int getHttpPort() {
        return jsonAdaptor.getHttpPort();
    }

    @Override
    public void setHttpPort(int port) {
        jsonAdaptor.setHttpPort(port);
    }

    @Override
    public String getMesosMaster() {
        return jsonAdaptor.getMesosMaster();
    }

    @Override
    public void setMesosMaster(String master) {
        jsonAdaptor.setMesosMaster(master);
    }

    @Override
    public String getFrameworkName() {
        return jsonAdaptor.getFrameworkName();
    }

    @Override
    public void setFrameworkName(String name) {
        jsonAdaptor.setFrameworkName(name);
    }

    @Override
    public String getZkServers() {
        return jsonAdaptor.getZkServers();
    }

    @Override
    public void setZkServers(String servers) {
        jsonAdaptor.setZkServers(servers);
    }

    @Override
    public String getTarballUri() {
        return jsonAdaptor.getTarballUri();
    }

    @Override
    public void setTarballUri(String uriString) {
        jsonAdaptor.setTarballUri(uriString);
    }

    @Override
    public void setProcessorConfigurations(Map<ServerType, ProcessConfiguration> processors) {
        jsonAdaptor.setProcessorConfigurations(processors);
    }

    @Override
    public Map<ServerType, ProcessConfiguration> getProcessorConfigurations() {
        return jsonAdaptor.getProcessorConfigurations();
    }

    @Override
    public String getAccumuloInstanceName() {
        return jsonAdaptor.getAccumuloInstanceName();
    }

    @Override
    public void setAccumuloInstanceName(String instance) {
        jsonAdaptor.setAccumuloInstanceName(instance);
    }

    @Override
    public String getAccumuloRootPassword() {
        return jsonAdaptor.getAccumuloRootPassword();
    }

    @Override
    public void setAccumuloRootPassword(String password) {
        jsonAdaptor.setAccumuloRootPassword(password);
    }

    @Override
    public double getMaxExecutorMemory() {
        return jsonAdaptor.getMaxExecutorMemory();
    }

    @Override
    public void setMaxExecutorMemory(double maxExecutorMemory) {
        jsonAdaptor.setMaxExecutorMemory(maxExecutorMemory);
    }

    @Override
    public double getMinExecutorMemory() {
        return jsonAdaptor.getMinExecutorMemory();
    }

    @Override
    public void setMinExecutorMemory(double minExecutorMemory) {
        jsonAdaptor.setMinExecutorMemory(minExecutorMemory);
    }

    @Override
    public void setMinTservers(int servers) {
        jsonAdaptor.setMinTservers(servers);
    }

    @Override
    public int getMinTservers() {
        return jsonAdaptor.getMinTservers();
    }
     
}
