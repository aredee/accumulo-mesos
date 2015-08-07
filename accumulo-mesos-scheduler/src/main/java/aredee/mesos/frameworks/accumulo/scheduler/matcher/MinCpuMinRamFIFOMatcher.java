package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.process.ProcessConfiguration;
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
     */
    @Override
    public List<Match> matchOffers(Set<AccumuloServer> servers, List<Protos.Offer> offers, OperationalCheck optCheck) {
        
        LOGGER.info("Matching {} servers to {} offers", servers.size(), offers.size());
        List<Match> matches = new ArrayList<>(servers.size());      
        
        int offersAccepted=0;
        Iterator<AccumuloServer> itr = servers.iterator();
        while (itr.hasNext() && (offersAccepted < offers.size())) {
            AccumuloServer server = itr.next();
            for(Protos.Offer offer: offers){
                Match match = new Match(server);
                if( offerMatchesServer(server, offer))
                {
                    boolean optCheckStatus = true;
                    if (optCheck != null) {
                        optCheckStatus = optCheck.accept(server, offer.getSlaveId().getValue());
                    }
                    if (optCheckStatus) {
                        match.setOffer(offer);
                        matches.add(match);
                        
                        // Need to remove the server before setting the slavedId because it 
                        // causes the hash to change 
                        servers.remove(server);
                        server.setSlaveId(offer.getSlaveId().getValue());
                        servers.add(server);
                        
                         LOGGER.info("Found match! server {} offer {} ", 
                                match.getServer().getType().getName(), match.getOffer().getId().getValue());
                         
                        offersAccepted++;
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
        
        Map<ServerType,ProcessConfiguration> servers = this.config.getProcessorConfigurations();
        
        if (servers.containsKey(server.getType())) {
            for( Protos.Resource resource : offer.getResourcesList()){
                if (resource.getName().equalsIgnoreCase("cpus")) {
                    offerCpus = resource.hasScalar() ? resource.getScalar().getValue() : 0.0;        
                } else if (resource.getName().equalsIgnoreCase("mem")) {
                    offerMem = resource.hasScalar() ? resource.getScalar().getValue() : 0.0;         
                }
            }
            double serverCpus = servers.get(server.getType()).getCpuOffer();
            double serverMem = servers.get(server.getType()).getMaxMemoryOffer();
            offerMatches = cpusAndMemAreAdequate(offerCpus, offerMem, serverCpus, serverMem);   
        }
        return offerMatches;
    }

    private boolean cpusAndMemAreAdequate(double offerCpu, double offerMem, double serverCpu, double serverMem){
        return ( offerCpu >= serverCpu ) && ( offerMem >= offerMem );
    }

}
