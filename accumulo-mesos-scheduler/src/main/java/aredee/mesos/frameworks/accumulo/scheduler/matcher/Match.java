package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import aredee.mesos.frameworks.accumulo.model.Task;
import org.apache.mesos.Protos;

public class Match {
    private Task task = null;
    private Protos.Offer offer = null;

    /**
        Constructs a Match object with no offer. These matches are used to pass back servers that were requested to
        be launched, but had no matching offers.
     */
    public Match(Task task){
        this(task, null);
    }

    /**
     * Constructs a match with no task. Typically used to signal extra offers were available when appropriate.
     * @param offer
     */
    public Match(Protos.Offer offer){
        this(null, offer);
    }

    /**
     * Constructs a Match between a task and an offer.
     */
    public Match(Task task, Protos.Offer offer){
        this.task = task;
        this.offer = offer;
    }
    
    public boolean hasTask(){
        return this.task != null;
    }
    public boolean hasOffer(){ 
        return this.offer != null; 
    }
    public void setOffer(Protos.Offer offer){
        this.offer = offer;
    }
    public void setTask(Task task){
        this.task = task;
    }
    public Protos.Offer getOffer(){ 
        return this.offer; 
    }
    public Task getTask(){
        return this.task;
    }

}
