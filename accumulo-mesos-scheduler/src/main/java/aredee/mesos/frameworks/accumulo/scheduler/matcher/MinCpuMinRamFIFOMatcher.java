package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import aredee.mesos.frameworks.accumulo.configuration.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ProcessorConfiguration;
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
    public List<Match> matchOffers(Set<AccumuloServer> servers, List<Protos.Offer> offers) {

        // TODO refactor using for loops with counters to make clearer

        LOGGER.info("Matching {} servers to {} offers", servers.size(), offers.size());
        List<Match> matches = new ArrayList<>(servers.size());
        List<Protos.Offer> takenOffers = new ArrayList<>(offers.size());
        
        for( AccumuloServer server: servers){
            Match match = new Match(server);
            LOGGER.info("Checking offers for server: {}", server.getType().getName());
            for(Protos.Offer offer: offers){
                if( offerMatchesServer(server, offer) && !takenOffers.contains(offer)) {
                    match.setOffer(offer);
                    updateServer(servers, server, offer);
                    takenOffers.add(offer);
                    LOGGER.info("Found match! server {} offer {} ", 
                            match.getServer().getType().getName(), match.getOffer().getId().getValue());
                    matches.add(match);              
                    break;
                }
            }
            
            if( matches.size() == offers.size() ){
                LOGGER.info("Exhausted all offers");
                break;
            }
        }
        return matches;
    }

    private void updateServer(Set<AccumuloServer> servers, AccumuloServer server, Protos.Offer offer) {
        servers.remove(server);
        server.setSlaveId(offer.getSlaveId().getValue());
        servers.add(server);     
    }
    
    
    private boolean offerMatchesServer(AccumuloServer server, Protos.Offer offer){
        double offerCpus = -1;
        double offerMem = -1;
        for( Protos.Resource resource : offer.getResourcesList()){
            resource.getName();
            switch ( resource.getName() ) {
                case "cpus":
                    offerCpus = resource.hasScalar() ? resource.getScalar().getValue() : 0.0;
                    break;
                case "mem":
                    offerMem = resource.hasScalar() ? resource.getScalar().getValue() : 0.0;
                    break;
            }
        }
      
        Map<ServerType,ProcessorConfiguration> servers = this.config.getProcessorConfigurations();
        boolean offerMatches = false;
        
        if (servers.containsKey(server.getType())) {
            double serverCpus = servers.get(server.getType()).getCpuOffer();
            double serverMem = servers.get(server.getType()).getMinMemoryOffer();
            offerMatches = cpusAndMemAreAdequate(offerCpus, offerMem, serverCpus, serverMem);
        }
        
        return offerMatches;
    }

    private boolean cpusAndMemAreAdequate(double offerCpu, double offerMem, double serverCpu, double serverMem){
        boolean cpuOk = false;
        boolean memOk = false;
        if( offerCpu >= serverCpu ){ cpuOk = true; }
        if( offerMem >= offerMem ){ memOk = true; }

        return (cpuOk && memOk);
    }

}
