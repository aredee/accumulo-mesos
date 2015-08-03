package aredee.mesos.frameworks.accumulo.scheduler.matcher;

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
        a Match object will be present with no
     */
    public List<Match> matchOffers(Set<AccumuloServer> servers, List<Protos.Offer> offers);
}
