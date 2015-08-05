package aredee.mesos.frameworks.accumulo.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Framework  {
  
  private String bindAddress = null;
  private Integer httpPort = null;
  private String mesosMaster = null;
  private String frameworkName = null;
  private Integer executorMemory = null;
  private String name = null;
  private String id = null;
  private List<Cluster> clusters = new ArrayList<Cluster>();

  
  /**
   **/
  @JsonProperty("bindAddress")
  public String getBindAddress() {
    return bindAddress;
  }
  public void setBindAddress(String bindAddress) {
    this.bindAddress = bindAddress;
  }

  
  /**
   **/
  @JsonProperty("httpPort")
  public Integer getHttpPort() {
    return httpPort;
  }
  public void setHttpPort(Integer httpPort) {
    this.httpPort = httpPort;
  }

  
  /**
   **/
  @JsonProperty("mesosMaster")
  public String getMesosMaster() {
    return mesosMaster;
  }
  public void setMesosMaster(String mesosMaster) {
    this.mesosMaster = mesosMaster;
  }

  
  /**
   **/
  @JsonProperty("frameworkName")
  public String getFrameworkName() {
    return frameworkName;
  }
  public void setFrameworkName(String frameworkName) {
    this.frameworkName = frameworkName;
  }

  
  /**
   **/
  @JsonProperty("executorMemory")
  public Integer getExecutorMemory() {
    return executorMemory;
  }
  public void setExecutorMemory(Integer executorMemory) {
    this.executorMemory = executorMemory;
  }

  
  /**
   **/
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   **/
  @JsonProperty("clusters")
  public List<Cluster> getClusters() {
    return clusters;
  }
  public void setClusters(List<Cluster> clusters) {
    this.clusters = clusters;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Framework {\n");
    
    sb.append("  bindAddress: ").append(bindAddress).append("\n");
    sb.append("  httpPort: ").append(httpPort).append("\n");
    sb.append("  mesosMaster: ").append(mesosMaster).append("\n");
    sb.append("  frameworkName: ").append(frameworkName).append("\n");
    sb.append("  executorMemory: ").append(executorMemory).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  id: ").append(id).append("\n");
    sb.append("  clusters: ").append(clusters).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
