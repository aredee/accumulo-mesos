package aredee.mesos.frameworks.accumulo.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Cluster  {
  
  private Integer minTservers = null;
  private Integer numTservers = null;
  private String tarballUri = null;
  private String accumuloRootPassword = null;
  private List<Object> servers = new ArrayList<Object>();
  private String name = null;
  private String id = null;
  private String description = null;
  private String tarballURI = null;
  private Accumulo accumulo = null;

  
  /**
   **/
  @JsonProperty("minTservers")
  public Integer getMinTservers() {
    return minTservers;
  }
  public void setMinTservers(Integer minTservers) {
    this.minTservers = minTservers;
  }

  
  /**
   **/
  @JsonProperty("numTservers")
  public Integer getNumTservers() {
    return numTservers;
  }
  public void setNumTservers(Integer numTservers) {
    this.numTservers = numTservers;
  }

  
  /**
   **/
  @JsonProperty("tarballUri")
  public String getTarballUri() {
    return tarballUri;
  }
  public void setTarballUri(String tarballUri) {
    this.tarballUri = tarballUri;
  }

  
  /**
   **/
  @JsonProperty("accumuloRootPassword")
  public String getAccumuloRootPassword() {
    return accumuloRootPassword;
  }
  public void setAccumuloRootPassword(String accumuloRootPassword) {
    this.accumuloRootPassword = accumuloRootPassword;
  }

  
  /**
   **/
  @JsonProperty("servers")
  public List<Object> getServers() {
    return servers;
  }
  public void setServers(List<Object> servers) {
    this.servers = servers;
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
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @JsonProperty("tarballURI")
  public String getTarballURI() {
    return tarballURI;
  }
  public void setTarballURI(String tarballURI) {
    this.tarballURI = tarballURI;
  }

  
  /**
   **/
  @JsonProperty("accumulo")
  public Accumulo getAccumulo() {
    return accumulo;
  }
  public void setAccumulo(Accumulo accumulo) {
    this.accumulo = accumulo;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Cluster {\n");
    
    sb.append("  minTservers: ").append(minTservers).append("\n");
    sb.append("  numTservers: ").append(numTservers).append("\n");
    sb.append("  tarballUri: ").append(tarballUri).append("\n");
    sb.append("  accumuloRootPassword: ").append(accumuloRootPassword).append("\n");
    sb.append("  servers: ").append(servers).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  id: ").append(id).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  tarballURI: ").append(tarballURI).append("\n");
    sb.append("  accumulo: ").append(accumulo).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
