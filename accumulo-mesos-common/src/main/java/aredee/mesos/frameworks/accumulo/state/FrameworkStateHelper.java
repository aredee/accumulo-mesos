package aredee.mesos.frameworks.accumulo.state;

import aredee.mesos.frameworks.accumulo.model.Framework;
import aredee.mesos.frameworks.accumulo.model.IdRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.mesos.state.State;
import org.apache.mesos.state.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class FrameworkStateHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkStateHelper.class);

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String REGISTERED_FRAMEWORKS_KEY = "registered_frameworks";
    private static final String FRAMEWORK_CONFIG_PREFIX = "framework__";

    private final State state;

    public FrameworkStateHelper(State state){
        this.state = state;
    }

    /**
     * Checks if any frameworks have been written to this state interface.
     *
     * @return
     * @throws InterruptedException
     */
    public boolean hasRegisteredFrameworks() throws InterruptedException {
        return keyExists(REGISTERED_FRAMEWORKS_KEY);
    }

    private IdRegistry getFrameworkRegistry() throws ExecutionException, InterruptedException, IOException {
        IdRegistry registry;
        if( hasRegisteredFrameworks() ) {
            byte[] registryBytes = getBytesForKey(REGISTERED_FRAMEWORKS_KEY);
            registry = mapper.readValue(registryBytes, IdRegistry.class);
        } else {
            registry = new IdRegistry();
        }
        return registry;
    }

    public Map<String, String> getFrameworkIdMap() throws ExecutionException, InterruptedException, IOException {
        IdRegistry registry = getFrameworkRegistry();
        Map<String,String> retMap = Maps.newHashMap();
        for(IdRegistry.RegistryPair pair : registry.getRegistry()){
            retMap.put(pair.getId(), pair.getName());
        }
        return retMap;
    }

    /**
     * Retrieves a map of framework name:id pairs
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    public Map<String, String> getFrameworkNameMap() throws ExecutionException, InterruptedException, IOException {
        IdRegistry registry = getFrameworkRegistry();
        Map<String,String> retMap = Maps.newHashMap();
        for(IdRegistry.RegistryPair pair : registry.getRegistry()){
            retMap.put(pair.getName(), pair.getId());
        }
        return retMap;
    }

    public void saveFrameworkConfig(Framework config) throws IOException, ExecutionException, InterruptedException {

        byte[] configBytes = mapper.writeValueAsBytes(config);
        if( config.hasId() ) {
            String key = buildFrameworkConfigKey(config.getId());
            LOGGER.info("Saving framework config: {} bytes at {}", configBytes.length, key);
            saveBytesForKey(key, configBytes);
        } else {
            throw new ExecutionException(new Exception("Cannot save configuration with no defined id"));
        }
        updateRegistry(config);
    }

    public Framework getFrameworkConfig(String id) throws ExecutionException, InterruptedException, IOException {
        String key = buildFrameworkConfigKey(id);
        byte[] configBytes = getBytesForKey(key);
        LOGGER.info("Read config: {} bytes at {}", configBytes.length, key);
        Framework config = mapper.readValue(configBytes, Framework.class);

        return config;
    }

    private void updateRegistry(Framework config) throws InterruptedException, ExecutionException, IOException {
        synchronized (REGISTERED_FRAMEWORKS_KEY){
            boolean foundFramework = false;
            List<IdRegistry.RegistryPair> newRegistry = Lists.newArrayList();
            IdRegistry registry;
            if( hasRegisteredFrameworks() ) {
                registry = getFrameworkRegistry();
                for (IdRegistry.RegistryPair pair : registry.getRegistry()) {
                    if (pair.getId().equals(config.getId())) {
                        foundFramework = true;
                    }
                    newRegistry.add(pair);
                }
            } else {
                registry = new IdRegistry();
            }
            if( !foundFramework ){
                newRegistry.add(new IdRegistry.RegistryPair(config.getName(), config.getId()));
            }
            registry.setRegistry(newRegistry);
            byte[] regBytes = mapper.writeValueAsBytes(registry);
            saveBytesForKey(REGISTERED_FRAMEWORKS_KEY, regBytes);
        }
    }

    private boolean keyExists(String inKey) throws InterruptedException {
        boolean exists = false;
        try {
            Future<Iterator<String>> keys = state.names();
            for (Iterator<String> iter = keys.get(); iter.hasNext(); ) {
                String key = iter.next();
                if (key.equals(inKey)) {
                    exists = true;
                    break;
                }
            }
        } catch (ExecutionException e) {
            LOGGER.error("Key not found via Execution Exception : {}", inKey);
            // key doesn't exist
        }
        return exists;
    }

    private byte[] getBytesForKey(String key) throws InterruptedException, ExecutionException {
        Variable stateVariable = getVariableForKey(key);
        return stateVariable.value();
    }

    private Variable getVariableForKey(String key) throws ExecutionException, InterruptedException {
        Future<Variable> stateFuture = state.fetch(key);
        Variable stateVariable = stateFuture.get();

        return stateVariable;
    }

    private void saveBytesForKey(String key, byte[] bytes) throws InterruptedException, ExecutionException {
        Variable variable;
        variable = getVariableForKey(key);
        Variable toSave = variable.mutate(bytes);
        this.state.store(toSave);
    }

    private static String buildFrameworkConfigKey(String id){
        return FRAMEWORK_CONFIG_PREFIX + id;
    }
}
