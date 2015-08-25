package aredee.mesos.frameworks.accumulo.framework.api.impl;

import aredee.mesos.frameworks.accumulo.framework.api.NotFoundException;
import aredee.mesos.frameworks.accumulo.framework.api.StatusApiService;
import aredee.mesos.frameworks.accumulo.scheduler.Cluster;

import javax.ws.rs.core.Response;

public class StatusApiServiceImpl extends StatusApiService {

    Cluster cluster = Cluster.INSTANCE;

    @Override
    public Response statusGet() throws NotFoundException {
        return null;
    }

}
