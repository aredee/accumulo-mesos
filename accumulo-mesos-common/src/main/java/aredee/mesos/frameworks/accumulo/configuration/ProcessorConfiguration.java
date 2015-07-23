package aredee.mesos.frameworks.accumulo.configuration;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

public class ProcessorConfiguration implements IProcessorConfiguration {

    private String minMemory;
    private String maxMemory;
    private String cpus;
    private String type;
    
    
    public ProcessorConfiguration() {
    }
    /**
     * 
     * @param minMemory minimum memory, all digits
     * @param maxMemory maximum memory, all digits
     * @param cpus number of cpus, all digits
     * @param type server type, matches ServerType
     */
    public ProcessorConfiguration(String minMemory, String maxMemory, String cpus, String type) {
        this.minMemory = minMemory;
        this.maxMemory = maxMemory;
        this.cpus = cpus;
        this.type = type;
    }
    
    @Override
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public ServerType toServerType() {
        return ServerType.getTypeFromName(type);
    }
    
    @Override
    public void setCpus(String cpuCount) {  
        cpus = cpuCount;
    }
    
    @Override
    public String getCpus() {
        return cpus;
    }
    
    @Override
    public int getCpuCount() {
        return (int)getCpuOffer();
    }
    
    @Override
    public double getCpuOffer() {
        if(!StringUtils.isEmpty(cpus)) {
            return Double.parseDouble(cpus);
        } else {
            return 0.0;
        }
   }
    
    @Override
    public void setMinMemory(String size) {
        minMemory = size;
    }

    @Override
    public String getMinMemory() {
        return minMemory;
    }
    
    @Override
    public void setMaxMemory(String size) {
        maxMemory = size;
    }

    @Override   
    public String getMaxMemory() {
        return maxMemory;
    }

    @Override   
    public double getMinMemoryOffer() {
        if(!StringUtils.isEmpty(minMemory)) {
            return Double.parseDouble(minMemory);
        } else {
            return 0.0;
        }
    }
    
    @Override
    public double getMaxMemoryOffer() {
        if(!StringUtils.isEmpty(maxMemory)) {
            return Double.parseDouble(maxMemory);
        } else {
            return 0.0;
        }
    }

    @Override
    public int getMinMemorySize() {
        /**
         * Done this way to enable doubles as input, though in real use this should
         * never be called when its a double since it will initialized in different configs.
         */
        return (int)getMinMemoryOffer();
    }
    
    @Override
    public int getMaxMemorySize() {
        return (int)getMaxMemoryOffer();
    }
    
    public String toString() {
        return new Gson().toJson(this);
    }

}
