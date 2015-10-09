package aredee.mesos.frameworks.accumulo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;


public class ServerProfile  {

    public enum TypeEnum {
        master("master"),
        tserver("tserver"),
        gc("gc"),
        tracer("tracer"),
        monitor("monitor"),
        init("init"),
        shell("shell");

        private final String keyword;

        TypeEnum(String keyword){
            this.keyword = keyword;
        }

        public String getServerKeyword(){ return keyword;}
    }

    private String name = null;
    private String description = null;
    private TypeEnum type = null;
    private Integer memory = 128;
    private BigDecimal cpus = null;
    private String launcher = null;
    private String user = null;
    private String siteXml = null;
    private List<String> serverKeywordArgs = null;
    private boolean useNativeMaps = false;

    /**
     * A short name for this profile\n
     **/
    @JsonProperty("name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Description of this profile\n
     **/
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Accumulo server type\n
     **/
    @JsonProperty("type")
    public TypeEnum getType() {
        return type;
    }
    public void setType(TypeEnum type) {
        this.type = type;
    }


    /**
     * Memory to allocate to this server in MB. -Xmx yyyyM\n
     **/
    @JsonProperty("mem")
    public Integer getMemory() {
        return memory;
    }
    public void setMemory(Integer memory) {
        this.memory = memory;
    }


    /**
     * Number of cpus to allocate to this server\n
     **/
    @JsonProperty("cpus")
    public BigDecimal getCpus() {
        return cpus;
    }
    public void setCpus(BigDecimal cpus) {
        this.cpus = cpus;
    }


    /**
     * Fully qualified class name of launcher class to launch with.\n
     **/
    //TODO implement defining launcher as config
    @JsonProperty("launcher")
    public String getLauncher() {
        return launcher;
    }
    public void setLauncher(String launcher) {
        this.launcher = launcher;
    }


    /**
     * System user name to run server processes as.\n
     **/
    @JsonProperty("user")
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public boolean hasUser(){
        boolean hasUser = false;
        if( user != null ){
            hasUser = !user.isEmpty();
        }
        return hasUser;
    }

    @JsonProperty("siteXml")
    public String getSiteXml() { return siteXml; }
    public void setSiteXml(String siteXml) { this.siteXml = siteXml; }

    @JsonProperty("serverKeywordArgs")
    public List<String> getServerKeywordArgs(){
        if( serverKeywordArgs == null ){
            serverKeywordArgs = Lists.newArrayList();
        }
        return this.serverKeywordArgs;
    }
    public void setServerKeywordArgs(List<String> serverKeywordArgs){
        this.serverKeywordArgs = serverKeywordArgs;
    }

    @JsonProperty("useNativeMaps")
    public boolean getUseNativeMaps(){ return useNativeMaps; }
    public void setUseNativemaps(boolean useNativeMaps){
        this.useNativeMaps = useNativeMaps;
    }


    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServerProfile {\n");

        sb.append("  name: ").append(name).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("  type: ").append(type).append("\n");
        sb.append("  memory: ").append(memory).append("\n");
        sb.append("  cpus: ").append(cpus).append("\n");
        sb.append("  launcher: ").append(launcher).append("\n");
        sb.append("  user: ").append(user).append("\n");
        sb.append("  siteXml: ").append(siteXml).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
