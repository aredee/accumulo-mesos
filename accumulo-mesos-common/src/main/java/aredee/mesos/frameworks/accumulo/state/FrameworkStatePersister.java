package aredee.mesos.frameworks.accumulo.state;

import aredee.mesos.frameworks.accumulo.Protos;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by klucar on 6/9/15.
 */
public interface FrameworkStatePersister {
    /**
     * Fetches saved FrameworkIdentities
     *
     * @return
     * @throws InvalidProtocolBufferException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    List<Protos.FrameworkIdentity> getFrameworks() throws InvalidProtocolBufferException, InterruptedException, ExecutionException;

    /**
     * Adds Protos.FrameworkIdentity to List of saved frameworks
     *
     * @param frameworkIdentity
     * @throws InvalidProtocolBufferException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    void saveFrameworkIdentity(Protos.FrameworkIdentity frameworkIdentity) throws InvalidProtocolBufferException, InterruptedException, ExecutionException;

    /**
     * Retrieves a particular FrameworkConfiguration
     *
     * @param frameworkId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws InvalidProtocolBufferException
     */
    Protos.FrameworkConfiguration getFrameworkConfig(String frameworkId) throws ExecutionException, InterruptedException, InvalidProtocolBufferException;

    /**
     * Saves a particular FrameworkConfiguration
     *
     * @param frameworkConfig
     * @throws ExecutionException
     * @throws InterruptedException
     */
    void saveFrameworkConfig(Protos.FrameworkConfiguration frameworkConfig) throws ExecutionException, InterruptedException;

    //TODO Accumulo info should be in Cluster Configuration
    /**
     *
     * @param frameworkId
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws InvalidProtocolBufferException
     */
    String getAccumuloInstanceName(String frameworkId) throws InterruptedException, ExecutionException, InvalidProtocolBufferException;
    // Move this to ClusterConfig
    void saveAccumuloInstanceName(String frameworkId, String accumuloInstancename);
}
