package aredee.mesos.frameworks.accumulo.state;

import aredee.mesos.frameworks.accumulo.model.Framework;
import aredee.mesos.frameworks.accumulo.model.IdRegistry;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.mesos.state.State;
import org.apache.mesos.state.Variable;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class FrameworkStateHelper {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String REGISTERED_FRAMEWORKS_KEY = "registered-frameworks";
    private static final String FRAMEWORK_CONFIG_PREFIX = "framework:";

    private final State state;

    public FrameworkStateHelper(State state){
        this.state = state;
    }

    public boolean hasRegisteredFrameworks() throws InterruptedException {
        return keyExists(REGISTERED_FRAMEWORKS_KEY);
    }

    private IdRegistry getFrameworkRegistry() throws ExecutionException, InterruptedException, IOException {
        byte[] registryBytes = getBytesForKey(REGISTERED_FRAMEWORKS_KEY);
        IdRegistry registry = mapper.readValue(registryBytes, IdRegistry.class);
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

    public Map<String, String> getFrameworkNameMap() throws ExecutionException, InterruptedException, IOException {
        IdRegistry registry = getFrameworkRegistry();        Map<String,String> retMap = Maps.newHashMap();
        for(IdRegistry.RegistryPair pair : registry.getRegistry()){
            retMap.put(pair.getName(), pair.getId());
        }
        return retMap;
    }

    public void saveFrameworkConfig(Framework config) throws JsonProcessingException, ExecutionException, InterruptedException {

        byte[] configBytes = mapper.writeValueAsBytes(config);
        if( config.hasId() ) {
            String key = buildFrameworkConfigKey(config.getId());
            saveBytesForKey(key, configBytes);
        } else {
            throw new ExecutionException(new Exception("Cannot save configuration with no defined id"));
        }
    }

    public Framework getFrameworkConfig(String id) throws ExecutionException, InterruptedException, IOException {
        String key = buildFrameworkConfigKey(id);
        byte[] configBytes = getBytesForKey(key);
        Framework config = mapper.readValue(configBytes, Framework.class);

        return config;
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
        Variable state = getVariableForKey(key);
        state.mutate(bytes);
        return;
    }

    private static String buildFrameworkConfigKey(String id){
        return FRAMEWORK_CONFIG_PREFIX + id;
    }
}
