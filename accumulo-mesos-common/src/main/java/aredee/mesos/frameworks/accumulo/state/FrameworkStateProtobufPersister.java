package aredee.mesos.frameworks.accumulo.state;

import aredee.mesos.frameworks.accumulo.Protos.*;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.mesos.state.State;
import org.apache.mesos.state.Variable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Proxy for the FrameworkState protocol buffers, Mesos state ui, futures, etc.
 *
 * Meant to just put in methods to make dealing with the protocol buffers easier.
 *
 */
public class FrameworkStateProtobufPersister implements FrameworkStatePersister {

    private final State state;

    private static final String REGISTERED_FRAMEWORKS_KEY = "registered-frameworks";
    private static final String FRAMEWORK_CONFIG_KEY = "framework-config";
    private static final String FRAMEWORK_STATE_KEY = "framework-state";

    public FrameworkStateProtobufPersister(State state){
        this.state = state;
    }

    /**
     * Fetches saved FrameworkIdentities from State at key "registered-frameworks"
     *
     * @return
     * @throws InvalidProtocolBufferException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Override
    public List<FrameworkIdentity> getFrameworks() throws InvalidProtocolBufferException, InterruptedException, ExecutionException {
        List<FrameworkIdentity> frameworkList = null;
        if( keyExists(REGISTERED_FRAMEWORKS_KEY) ){
            RegisteredFrameworks frameworks = RegisteredFrameworks.parseFrom(
                    getBytesForKey(REGISTERED_FRAMEWORKS_KEY));
            frameworkList = frameworks.getFrameworksList();
        } else {
            frameworkList = new ArrayList<>();
        }
        return frameworkList;
    }

    /**
     * Saves list of FrameworkIdentities into the State
     *
     * @param frameworkIdentity
     * @throws InvalidProtocolBufferException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Override
    public void saveFrameworkIdentity(FrameworkIdentity frameworkIdentity) throws InvalidProtocolBufferException, InterruptedException, ExecutionException {
        byte[] regFrameBytes = getBytesForKey(REGISTERED_FRAMEWORKS_KEY);
        RegisteredFrameworks registeredFrameworks = RegisteredFrameworks.parseFrom(regFrameBytes);
        List<FrameworkIdentity> frameworkIdentities = new ArrayList<>(registeredFrameworks.getFrameworksCount()+1);
        for( FrameworkIdentity fid: registeredFrameworks.getFrameworksList()){
            frameworkIdentities.add(fid);
        }
        frameworkIdentities.add(frameworkIdentity);
        RegisteredFrameworks updatedFrameworks = RegisteredFrameworks.newBuilder()
                .addAllFrameworks(frameworkIdentities)
                .build();
        saveBytesForKey(REGISTERED_FRAMEWORKS_KEY, updatedFrameworks.toByteArray());
    }

    public void saveFrameworkIdentity(String id, String name) throws InvalidProtocolBufferException, InterruptedException, ExecutionException {
        FrameworkIdentity fid = FrameworkIdentity.newBuilder()
                .setFrameworkName(name)
                .setFrameworkId(id)
                .build();
        saveFrameworkIdentity(fid);
    }

    /**
     * Retrieves a particular FrameworkConfiguration from State
     *
     * @param frameworkId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws InvalidProtocolBufferException
     */
    @Override
    public FrameworkConfiguration getFrameworkConfig(String frameworkId) throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        String key = createFrameworkConfigKey(frameworkId);
        byte[] configBytes = getBytesForKey(key);
        FrameworkConfiguration frameworkConfig = FrameworkConfiguration.parseFrom(configBytes);
        return frameworkConfig;
    }

    /**
     * Saves a particular FrameworkConfiguration to State
     *
     * @param frameworkConfig
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public void saveFrameworkConfig(FrameworkConfiguration frameworkConfig) throws ExecutionException, InterruptedException {
        String frameworkId = frameworkConfig.getId().getFrameworkId();
        String key = createFrameworkConfigKey(frameworkId);
        saveBytesForKey(key, frameworkConfig.toByteArray());
    }

    @Override
    public String getAccumuloInstanceName(String frameworkId) throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
        FrameworkConfiguration fConfig = getFrameworkConfig(frameworkId);
        String instance = "";
        if ( fConfig.hasAccumuloInstance() ){
            instance = fConfig.getAccumuloInstance();
        }
        return instance;
    }


    @Override
    public void saveAccumuloInstanceName(String frameworkId, String accumuloInstancename){
    }

    private boolean keyExists(String inKey) throws ExecutionException, InterruptedException {
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

    /**
     * FrameworkConfig is saved at key "framework-config::(framework-id)"
     *
     * @param frameworkId
     * @return
     */
    private String createFrameworkConfigKey(String frameworkId){
        return FRAMEWORK_CONFIG_KEY + "::" + frameworkId;
    }
    /**
     * FrameworkConfig is saved at key "framework-state::(framework-id)"
     *
     * @param frameworkId
     * @return
     */
    private String createFrameworkStateKey(String frameworkId){
        return FRAMEWORK_STATE_KEY + "::" + frameworkId;
    }
}
