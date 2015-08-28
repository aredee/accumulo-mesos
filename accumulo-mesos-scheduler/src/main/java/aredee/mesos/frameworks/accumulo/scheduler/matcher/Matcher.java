package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import aredee.mesos.frameworks.accumulo.model.Task;
import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;

import org.apache.mesos.Protos;

import java.util.List;
import java.util.Set;

/**
 * Responsible for matching required servers to available offers
 */
public interface Matcher {
    /**
        Returns a list of matched servers and offers. If offers were not found for all servers,
        a Match object will be present with no offer

        @param tasks tasks that require resources
        @param offers to match servers against
        @param opCheck is operational considerations that may exist, optional
     */
    public List<Match> matchOffers(List<Task> tasks, List<Protos.Offer> offers);
}
