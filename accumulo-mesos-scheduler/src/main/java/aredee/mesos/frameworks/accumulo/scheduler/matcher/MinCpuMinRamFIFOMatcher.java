package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import aredee.mesos.frameworks.accumulo.configuration.Defaults;
import aredee.mesos.frameworks.accumulo.model.Accumulo;
import aredee.mesos.frameworks.accumulo.model.ServerProfile;
import aredee.mesos.frameworks.accumulo.model.Task;
import com.google.common.collect.Lists;
import org.apache.mesos.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Matcher that finds an offer that meets minimum cpu and ram requirements for the server on a first come first
 * served basis.
 *
 */
public class MinCpuMinRamFIFOMatcher implements Matcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinCpuMinRamFIFOMatcher.class);

    private Accumulo config;

    public MinCpuMinRamFIFOMatcher(Accumulo config){
        this.config = config;
    }

    /**
     * Returns a list of matched servers and offers. If offers were not found for all servers,
     * a Match object will be present with no offer
     *
     * @param tasks
     * @param offers
     *
     */
    @Override
    public List<Match> matchOffers(List<Task> tasks, List<Protos.Offer> offers) {
        
        LOGGER.info("Matching {} tasks to {} offers", tasks.size(), offers.size());

        List<Match> matches = Lists.newArrayListWithCapacity(tasks.size());
        
        int offerCount = 0;
        for(int tt = 0; tt < tasks.size(); tt++){
            Task task = tasks.get(tt);
            boolean foundOffer = false;
            for( int oo = offerCount; (oo < offers.size()) && (!foundOffer); oo++){
                Protos.Offer offer = offers.get(offerCount);
                if( offerMatchesTask(task, offer)){
                    // create a match
                    Match match = new Match(task, offer);
                    matches.add(match);
                    offerCount = oo;
                    foundOffer = true;

                    LOGGER.info("Found match! task {} offer {}", task.getType().name(), offer.getId().getValue());
                }
            }
        }

        return matches;
    }
   
    private boolean offerMatchesTask(Task task, Protos.Offer offer){
        double offerCpus = -1;
        double offerMem = -1;

        // Get offer resources
        for( Protos.Resource resource : offer.getResourcesList()){
            if (resource.getName().equalsIgnoreCase("cpus")) {
                offerCpus = resource.hasScalar() ? resource.getScalar().getValue() : 0.0;
            } else if (resource.getName().equalsIgnoreCase("mem")) {
                offerMem = resource.hasScalar() ? resource.getScalar().getValue() : 0.0;
            }
        }
        // TODO Have to take into account the executor resources because MESOS will.

        // Get profile resources
        ServerProfile profile = task.getServerProfile();
        double profileCpus = profile.getCpus().doubleValue() + Defaults.EXECUTOR_CPUS;
        double profileMem = profile.getMemory().doubleValue() + config.getExecutorMemory();

        boolean offerMatches = cpusAndMemAreAdequate(offerCpus, offerMem, profileCpus, profileMem);

        return offerMatches;
    }

    private boolean cpusAndMemAreAdequate(double offerCpu, double offerMem, double serverCpu, double serverMem) {
        return ( offerCpu >= serverCpu ) && ( offerMem >= serverMem );
    }

}
