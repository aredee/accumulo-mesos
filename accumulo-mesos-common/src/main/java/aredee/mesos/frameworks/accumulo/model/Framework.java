package aredee.mesos.frameworks.accumulo.model;

import aredee.mesos.frameworks.accumulo.configuration.Defaults;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Framework  {

  private String bindAddress = Defaults.BIND_ADDRESS;
  private Integer httpPort = Defaults.HTTP_PORT;
  private String mesosMaster = Defaults.MESOS_MASTER;
  private String name = null;
  private String id = null;
  private String tarballUri = null;
  private String zkServers = Defaults.ZK_SERVERS;
  private Accumulo cluster = null;


  /**
   * IP address to bind framework webserver to.
   **/
  @JsonProperty("bindAddress")
  public String getBindAddress() {
    return bindAddress;
  }
  public void setBindAddress(String bindAddress) {
    this.bindAddress = bindAddress;
  }

  /**
   * Port to bind framework webserver to.
   **/
  @JsonProperty("httpPort")
  public Integer getHttpPort() {
    return httpPort;
  }
  public void setHttpPort(Integer httpPort) {
    this.httpPort = httpPort;
  }


  /**
   * IP and port of Mesos Master node to register with.
   **/
  @JsonProperty("mesosMaster")
  public String getMesosMaster() {
    return mesosMaster;
  }
  public void setMesosMaster(String mesosMaster) {
    this.mesosMaster = mesosMaster;
  }


  /**
   * Descriptive name for this framework.
   **/
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public boolean hasName(){
    return name == null;
  }

  /**
   * Unique ID that references this framework
   **/
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public boolean hasId(){
    return id == null;
  }

  /**
   * URI of tarball containing framework distribution
   **/
  @JsonProperty("tarballUri")
  public String getTarballUri() {
    return tarballUri;
  }
  public void setTarballUri(String tarballUri) {
    this.tarballUri = tarballUri;
  }
  public boolean hasTarballUri(){
    return tarballUri == null;
  }

  /**
   * List of Zookeeper servers to store Framework state on. Does not have\nto be the same ZK servers used for the Accumulo cluster
   **/
  @JsonProperty("zkServers")
  public String getZkServers() {
    return zkServers;
  }
  public void setZkServers(String zkServers) {
    this.zkServers = zkServers;
  }


  /**
   * Accumulo cluster definition
   **/
  @JsonProperty("cluster")
  public Accumulo getCluster() {
    return cluster;
  }
  public void setCluster(Accumulo cluster) {
    this.cluster = cluster;
  }
  public boolean hasCluster(){
    return cluster == null;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Framework {\n");

    sb.append("  bindAddress: ").append(bindAddress).append("\n");
    sb.append("  httpPort: ").append(httpPort).append("\n");
    sb.append("  mesosMaster: ").append(mesosMaster).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  id: ").append(id).append("\n");
    sb.append("  tarballUri: ").append(tarballUri).append("\n");
    sb.append("  zkServers: ").append(zkServers).append("\n");
    sb.append("  cluster: ").append(cluster).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
