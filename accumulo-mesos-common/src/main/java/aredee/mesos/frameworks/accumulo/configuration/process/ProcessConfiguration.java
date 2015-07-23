package aredee.mesos.frameworks.accumulo.configuration.process;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;

/**
 * 
 * Processor configuration across system.
 *
 */
public interface ProcessConfiguration {

    /**
     * 
     * @param size in megs
     */
    public void setMinMemory(String size);
    public String getMinMemory();
    
    /**
     * 
     * @param size in megs
     */
    public void setMaxMemory(String size);
    public String getMaxMemory();
    
    public double getMinMemoryOffer();
    public double getMaxMemoryOffer();
    
    public int getMinMemorySize();
    public int getMaxMemorySize();
    
    public String getCpus();
    public void setCpus(String cpuCount);
    public int getCpuCount();
    public double getCpuOffer();
    
    public String getType();
    public void setType(String type);
    public ServerType toServerType();
   
}
