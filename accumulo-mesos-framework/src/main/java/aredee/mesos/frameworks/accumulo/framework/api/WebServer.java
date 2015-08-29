package aredee.mesos.frameworks.accumulo.framework.api;


import aredee.mesos.frameworks.accumulo.model.Framework;
import com.google.inject.Inject;
import com.google.inject.servlet.GuiceFilter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.*;

import java.util.EnumSet;

public class WebServer {

    private final Framework config;
    private Server server;

    @Inject
    public WebServer(Framework config) {
        this.config = config;
    }

    public void start() throws java.lang.Exception {

        checkParam(this.config.getBindAddress(), "Bind address");
        checkParam(this.config.getHttpPort() + "", "Http Port");
       
        this.server = new Server();
        // configure server
        ServerConnector http = new ServerConnector(server);
        http.setHost(this.config.getBindAddress());
        http.setPort(this.config.getHttpPort());
        server.addConnector(http);

        ServletContextHandler context = new ServletContextHandler(this.server, "/", ServletContextHandler.NO_SESSIONS);
        context.addFilter(GuiceFilter.class, "/*", EnumSet.of(javax.servlet.DispatcherType.REQUEST, javax.servlet.DispatcherType.ASYNC));
        context.addServlet(DefaultServlet.class, "/*");

        String staticDir = this.getClass().getClassLoader().getResource("webapp/public").toExternalForm();
        context.setResourceBase(staticDir);

        this.server.start();
    }

    public void stop() throws java.lang.Exception{
        this.server.stop();
    }
    
    private void checkParam(String value, String name) {
        if (StringUtils.isEmpty(value)) {
            throw new RuntimeException("Required parameter " + name + " is missing");
        }
    }
}
