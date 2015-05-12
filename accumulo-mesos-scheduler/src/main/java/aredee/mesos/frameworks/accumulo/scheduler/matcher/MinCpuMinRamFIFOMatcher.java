package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import aredee.mesos.frameworks.accumulo.configuration.Configuration;
import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;
import org.apache.mesos.Protos;

import java.util.*;

/**
 * Matcher that finds an offer that meets minimum cpu and ram requirements for the server on a first come first
 * served basis.
 *
 */
public class MinCpuMinRamFIFOMatcher implements Matcher {

    private Configuration config;

    public MinCpuMinRamFIFOMatcher(Configuration config){
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

        List<Match> matches = new ArrayList<>(servers.size());
        for( AccumuloServer server: servers){
            Match match = new Match(server);
            for(Protos.Offer offer: offers){
                if( offerMatchesServer(server, offer)) {
                    match.setOffer(offer);
                    break;
                }
            }
            matches.add( match );
        }
        return matches;
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
        double serverCpus = Double.MAX_VALUE;
        double serverMem = Double.MAX_VALUE;
        switch ( server.getType() ) {
            case MASTER:
                serverCpus = this.config.getMinMasterCpus();
                serverMem = this.config.getMinMasterMem();
                break;
            case MONITOR:
                serverCpus = this.config.getMinMonitorCpus();
                serverMem = this.config.getMinMonitorMem();
                break;
            case TSERVER:
                serverCpus = this.config.getMinTserverCpus();
                serverMem = this.config.getMinTserverMem();
                break;
            case GC:
                serverCpus = this.config.getMinGCCpus();
                serverMem = this.config.getMinGCMem();
                break;
            case UNKNOWN:
                // TODO log unknown here?
                return false;
        }
        return cpusAndMemAreAdequate(offerCpus, offerMem, serverCpus, serverMem);
    }

    private boolean cpusAndMemAreAdequate(double offerCpu, double offerMem, double serverCpu, double serverMem){
        boolean cpuOk = false;
        boolean memOk = false;
        if( offerCpu >= serverCpu ){ cpuOk = true; }
        if( offerMem >= offerMem ){ memOk = true; }

        return (cpuOk && memOk);
    }

}
