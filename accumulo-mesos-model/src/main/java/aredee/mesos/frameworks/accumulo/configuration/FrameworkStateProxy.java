package aredee.mesos.frameworks.accumulo.configuration;

import aredee.mesos.frameworks.accumulo.Protos.*;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.mesos.state.State;
import org.apache.mesos.state.Variable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Proxy for the FrameworkState protocol buffers, Mesos state ui, futures, etc.
 *
 * Meant to just put in methods to make dealing with the protocol buffers easier.
 *
 */
public class FrameworkStateProxy {

    private final State state;

    private static final String FRAMEWORKS_KEY = "registered-frameworks";
    private static final String FRAMEWORK_CONFIG_KEY = "framework-config";

    public static class FrameworkTuple {
        public final String name;
        public final String id;
        public FrameworkTuple(FrameworkIdentity fid){
            if(fid.hasFrameworkId()){
                this.id = fid.getFrameworkId();
            } else {
                this.id = "";
            }
            if(fid.hasFrameworkName()){
                this.name = fid.getFrameworkName();
            } else {
                this.name = "";
            }

        }
    }

    public FrameworkStateProxy(State state){
        this.state = state;
    }

    private byte[] getBytesForKey(String key) throws InterruptedException, ExecutionException {
        Future<Variable> stateFuture = state.fetch(key);
        Variable stateVariable = stateFuture.get();
        return stateVariable.value();
    }

    public Set<FrameworkTuple> getFrameworkTuples() throws InvalidProtocolBufferException, InterruptedException, ExecutionException {

        RegisteredFrameworks frameworks = RegisteredFrameworks.parseFrom(
                getBytesForKey(FRAMEWORKS_KEY));

        Set<FrameworkTuple> frameworkTuples = new HashSet<>();
        for( FrameworkIdentity id: frameworks.getFrameworksList()){
            frameworkTuples.add(new FrameworkTuple(id));
        }
        return frameworkTuples;
    }

    public FrameworkConfiguration getFrameworkConfig(String frameworkId) throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        String key = createFrameworkConfigKey(frameworkId);
        byte[] configBytes = getBytesForKey(key);
        FrameworkConfiguration frameworkConfig = FrameworkConfiguration.parseFrom(configBytes);
        return frameworkConfig;
    }

    public String getAccumuloInstance(String frameworkId) throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
        FrameworkConfiguration fConfig = getFrameworkConfig(frameworkId);
        String instance = "";
        if ( fConfig.hasAccumuloInstance() ){
            instance = fConfig.getAccumuloInstance();
        }
        return instance;
    }

    private String createFrameworkConfigKey(String frameworkId){
        return FRAMEWORK_CONFIG_KEY + "::" + frameworkId;
    }
}
