package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import aredee.mesos.frameworks.accumulo.model.Task;
import org.apache.mesos.Protos;

import java.util.List;

/**
 * Responsible for matching required servers to available offers
 */
public interface Matcher {
    /**
        Returns a list of matched servers and offers. If offers were not found for all servers,
        a Match object will be present with no offer

        @param tasks tasks that require resources
        @param offers to match servers against
     */
    public List<Match> matchOffers(List<Task> tasks, List<Protos.Offer> offers);
}
