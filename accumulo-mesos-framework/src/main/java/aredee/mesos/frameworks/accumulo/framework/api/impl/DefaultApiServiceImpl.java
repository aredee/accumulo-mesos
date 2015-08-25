package aredee.mesos.frameworks.accumulo.framework.api.impl;

import aredee.mesos.frameworks.accumulo.framework.api.DefaultApiService;
import aredee.mesos.frameworks.accumulo.framework.api.NotFoundException;
import aredee.mesos.frameworks.accumulo.scheduler.Cluster;

import javax.ws.rs.core.Response;

public class DefaultApiServiceImpl extends DefaultApiService {

    Cluster cluster = Cluster.INSTANCE;

    @Override
    public Response rootGet() throws NotFoundException {
        return null;
    }
}
