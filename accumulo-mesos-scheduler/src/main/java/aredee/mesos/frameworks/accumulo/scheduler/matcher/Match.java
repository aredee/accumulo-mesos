package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;
import org.apache.mesos.Protos;

public class Match {
    private AccumuloServer server = null;
    private Protos.Offer offer = null;

    /**
        Constructs a Match object with no offer. These matches are used to pass back servers that were requested to
        be launched, but had no matching offers.
     */
    public Match(AccumuloServer server){
        this(server, null);
    }

    /**
     * Constructs a match with no server. Typically used to signal extra offers were available when appropriate.
     * @param offer
     */
    public Match(Protos.Offer offer){
        this(null, offer);
    }

    /**
     * Constructs a Match between a server and an offer.
     */
    public Match(AccumuloServer server, Protos.Offer offer){
        this.server = server;
        this.offer = offer;
    }
    
    public boolean hasServer(){ 
        return this.server != null; 
    }
    public boolean hasOffer(){ 
        return this.offer != null; 
    }
    public void setOffer(Protos.Offer offer){
        this.offer = offer;
    }
    public void setServer(AccumuloServer server){
        this.server = server;
    }
    public Protos.Offer getOffer(){ 
        return this.offer; 
    }
    public AccumuloServer getServer(){ 
        return this.server; 
    }

}
