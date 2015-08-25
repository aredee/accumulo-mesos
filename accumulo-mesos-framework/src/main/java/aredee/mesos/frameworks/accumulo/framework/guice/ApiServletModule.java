package aredee.mesos.frameworks.accumulo.framework.guice;

import aredee.mesos.frameworks.accumulo.framework.api.*;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.eclipse.jetty.servlet.DefaultServlet;

public class ApiServletModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bind(DefaultServlet.class).in(Singleton.class);

        bind(EchoResource.class);
        bind(ClusterApi.class);
        bind(ConfigApi.class);
        bind(DefaultApi.class);
        bind(StatusApi.class);

        serve("/api/*").with(GuiceContainer.class);
    }

}
