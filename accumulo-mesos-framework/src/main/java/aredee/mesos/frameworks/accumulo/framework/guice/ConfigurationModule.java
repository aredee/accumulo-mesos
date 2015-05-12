package aredee.mesos.frameworks.accumulo.framework.guice;

import aredee.mesos.frameworks.accumulo.configuration.Configuration;
import com.google.inject.AbstractModule;

public class ConfigurationModule extends AbstractModule {

    private final Configuration config;

    public ConfigurationModule(Configuration config){
        this.config = config;
    }

    @Override
    protected void configure() {
        bind(Configuration.class).toInstance(config);
    }

}
