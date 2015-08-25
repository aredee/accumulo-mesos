package aredee.mesos.frameworks.accumulo.framework.api;

import aredee.mesos.frameworks.accumulo.framework.api.impl.ClusterApiServiceImpl;
import aredee.mesos.frameworks.accumulo.framework.api.impl.ConfigApiServiceImpl;
import aredee.mesos.frameworks.accumulo.framework.api.impl.DefaultApiServiceImpl;
import aredee.mesos.frameworks.accumulo.framework.api.impl.StatusApiServiceImpl;

public final class ApiServiceFactory {

    public static final ClusterApiService getClusterApi(){
        return new ClusterApiServiceImpl();
    }

    public static final ConfigApiService getConfigApi(){
        return new ConfigApiServiceImpl();
    }

    public static final DefaultApiService getDefaultApi(){
        return new DefaultApiServiceImpl();
    }


    public static final StatusApiService getStatusApi(){
        return new StatusApiServiceImpl();
    }



}
