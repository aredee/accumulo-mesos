package aredee.mesos.frameworks.accumulo.framework.api.impl;

import aredee.mesos.frameworks.accumulo.framework.api.ConfigApiService;
import aredee.mesos.frameworks.accumulo.framework.api.NotFoundException;
import aredee.mesos.frameworks.accumulo.scheduler.Cluster;

import javax.ws.rs.core.Response;

public class ConfigApiServiceImpl extends ConfigApiService {

    Cluster cluster = Cluster.INSTANCE;

    @Override
    public Response configGet() throws NotFoundException {
        return null;
    }
}
