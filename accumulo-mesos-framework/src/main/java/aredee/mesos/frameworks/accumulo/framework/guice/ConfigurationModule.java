package aredee.mesos.frameworks.accumulo.framework.guice;

import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.model.Framework;
import com.google.inject.AbstractModule;

public class ConfigurationModule extends AbstractModule {

    private final Framework config;

    public ConfigurationModule(Framework config){
        this.config = config;
    }

    @Override
    protected void configure() {
        bind(Framework.class).toInstance(config);
    }

}
