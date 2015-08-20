package aredee.mesos.frameworks.accumulo.configuration.process;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

public class BaseProcessConfiguration implements ProcessConfiguration {

    private String minMemory;
    private String maxMemory;
    private String cpus;
    private String type;
    
    
    public BaseProcessConfiguration() {
    }
    /**
     * 
     * @param minMemory minimum memory, all digits
     * @param maxMemory maximum memory, all digits
     * @param cpus number of cpus, all digits
     * @param type server type, matches ServerType
     */
    public BaseProcessConfiguration(String minMemory, String maxMemory, String cpus, String type) {
        setMinMemory(minMemory);
        setMaxMemory(maxMemory);
        setCpus(cpus);
        setType(type);
    }
    
    public BaseProcessConfiguration(ProcessConfiguration process) {
        this(process.getMinMemory(),process.getMaxMemory(),process.getCpus(),process.getType());
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
    public void setMinMemory(String mem) {
        checkMemory(mem);
        minMemory = mem;
    }

    @Override
    public String getMinMemory() {
        return minMemory;
    }
    
    @Override
    public void setMaxMemory(String mem) {
        checkMemory(mem);
        maxMemory = mem;
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
    
    private void checkMemory(String mem) {
        if (!StringUtils.containsOnly(mem,"0123456789.")) {
            throw new RuntimeException("Memory is only digit or float, not " + mem);
        }       
    }

}
