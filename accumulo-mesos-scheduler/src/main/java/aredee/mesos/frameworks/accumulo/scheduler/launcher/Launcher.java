package aredee.mesos.frameworks.accumulo.scheduler.launcher;

import aredee.mesos.frameworks.accumulo.scheduler.matcher.Match;
import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

/**
 * Interface to launch a server based on a Mesos offer.
 */
public interface Launcher {

    /**
     * Interface used to launch Accumulo Server tasks.
     *
     * @param driver Mesos interface to use to launch a server
     * @param match {@link Match} to launch. A Match must contain a server and an offer
     */
    public Protos.TaskInfo launch(SchedulerDriver driver, Match match);
}
