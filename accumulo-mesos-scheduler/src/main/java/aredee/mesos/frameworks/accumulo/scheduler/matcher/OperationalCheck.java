package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;

public interface OperationalCheck {
    public boolean accept(AccumuloServer server, String slaveId);
}
