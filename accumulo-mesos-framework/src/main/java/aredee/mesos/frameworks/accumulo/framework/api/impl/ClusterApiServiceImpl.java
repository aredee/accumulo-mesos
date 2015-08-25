package aredee.mesos.frameworks.accumulo.framework.api.impl;

import aredee.mesos.frameworks.accumulo.framework.api.ClusterApiService;
import aredee.mesos.frameworks.accumulo.framework.api.NotFoundException;
import aredee.mesos.frameworks.accumulo.scheduler.Cluster;

import javax.ws.rs.core.Response;

public class ClusterApiServiceImpl extends ClusterApiService {

    Cluster cluster = Cluster.INSTANCE;

    @Override
    public Response clusterKillPost() throws NotFoundException {
        cluster.isMonitorRunning();
        return null;
    }

    @Override
    public Response clusterMasterReprovisionPost() throws NotFoundException {
        return null;
    }

    @Override
    public Response clusterMasterRestartPost() throws NotFoundException {
        return null;
    }

    @Override
    public Response clusterMonitorGet() throws NotFoundException {
        return null;
    }

    @Override
    public Response clusterStartPost() throws NotFoundException {
        return null;
    }

    @Override
    public Response clusterStopPost() throws NotFoundException {
        return null;
    }

    @Override
    public Response clusterTserverReprovisionPost(String id) throws NotFoundException {
        return null;
    }

    @Override
    public Response clusterTserverRestartPost(String id) throws NotFoundException {
        return null;
    }

    @Override
    public Response clusterTserverRollingrestartPost(Boolean master, Integer group, Boolean reprovision) throws NotFoundException {
        return null;
    }
}
