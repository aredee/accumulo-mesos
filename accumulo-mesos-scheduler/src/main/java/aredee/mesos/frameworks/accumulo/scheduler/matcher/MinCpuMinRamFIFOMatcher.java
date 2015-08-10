package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.process.ProcessConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.Defaults;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;

import org.apache.mesos.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Matcher that finds an offer that meets minimum cpu and ram requirements for the server on a first come first
 * served basis.
 *
 */
public class MinCpuMinRamFIFOMatcher implements Matcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinCpuMinRamFIFOMatcher.class);

    private ClusterConfiguration config;

    public MinCpuMinRamFIFOMatcher(ClusterConfiguration config){
        this.config = config;
    }

    /**
     * Returns a list of matched servers and offers. If offers were not found for all servers,
     * a Match object will be present with no
     *
     * @param servers
     * @param offers
     * @param optCheck operational considerations, optional, applied after offer determination.
     */
    @Override
    public List<Match> matchOffers(Set<AccumuloServer> servers, List<Protos.Offer> offers, OperationalCheck optCheck) {
        
        LOGGER.info("Matching {} servers to {} offers", servers.size(), offers.size());
        List<Match> matches = new ArrayList<>(servers.size());      
        
         Set<AccumuloServer> localservers = new HashSet<AccumuloServer>(servers);
        Iterator<AccumuloServer> itr = localservers.iterator();
        List<Protos.Offer> takenOffers = new ArrayList<>(offers.size());
        
        while (itr.hasNext() && (takenOffers.size() < offers.size())) {
            AccumuloServer server = itr.next();
            for(Protos.Offer offer: offers){
                Match match = new Match(server);
                if( offerMatchesServer(server, offer) && !takenOffers.contains(offer))
                {
                    boolean optCheckStatus = true;
                    
                    // If operational considerations check here.
                    if (optCheck != null) {
                        optCheckStatus = optCheck.accept(server, offer.getSlaveId().getValue());
                    }
                    if (optCheckStatus) {
                        match.setOffer(offer);
                        matches.add(match);
                        takenOffers.add(offer);
                        
                        // Need to remove the server before setting the slavedId because it 
                        // causes the hash to change 
                        servers.remove(server);
                        server.setSlaveId(offer.getSlaveId().getValue());
                        servers.add(server);
                        
                         LOGGER.info("Found match! server {} offer {} ", 
                                match.getServer().getType().getName(), match.getOffer().getId().getValue());
                         
                         break;
                    }                   
                }
            }
        }
  
        return matches;
    }
   
    @SuppressWarnings("unchecked")
    private boolean offerMatchesServer(AccumuloServer server, Protos.Offer offer){
        double offerCpus = -1;
        double offerMem = -1;
        boolean offerMatches = false;
        
        Map<ServerType,ProcessConfiguration> servers = config.getProcessorConfigurations();
        
        if (servers.containsKey(server.getType())) {
            for( Protos.Resource resource : offer.getResourcesList()){
                if (resource.getName().equalsIgnoreCase("cpus")) {
                    offerCpus = resource.hasScalar() ? resource.getScalar().getValue() : 0.0;        
                } else if (resource.getName().equalsIgnoreCase("mem")) {
                    offerMem = resource.hasScalar() ? resource.getScalar().getValue() : 0.0;         
                }
            }
            // Have to take into account the executor resources because MESOS will.
            double serverCpus = servers.get(server.getType()).getCpuOffer() + Defaults.EXECUTOR_CPUS;
            double serverMem = servers.get(server.getType()).getMaxMemoryOffer() + config.getMaxExecutorMemory();
            offerMatches = cpusAndMemAreAdequate(offerCpus, offerMem, serverCpus, serverMem);   
        }
        return offerMatches;
    }

    private boolean cpusAndMemAreAdequate(double offerCpu, double offerMem, double serverCpu, double serverMem) {
        LOGGER.info("offerCpu "+offerCpu+" offerMem "+offerMem+" serverCpu " + serverCpu+" serverMem " + serverMem);
        return ( offerCpu >= serverCpu ) && ( offerMem >= serverMem );
    }

}
