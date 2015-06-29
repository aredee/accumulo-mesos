package aredee.mesos.frameworks.accumulo.framework.guice;

import aredee.mesos.frameworks.accumulo.configuration.ClusterConfiguration;
import com.google.inject.AbstractModule;

public class ConfigurationModule extends AbstractModule {

    private final ClusterConfiguration config;

    public ConfigurationModule(ClusterConfiguration config){
        this.config = config;
    }

    @Override
    protected void configure() {
        bind(ClusterConfiguration.class).toInstance(config);
    }

}
