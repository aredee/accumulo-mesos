package aredee.mesos.frameworks.accumulo.model;

import aredee.mesos.frameworks.accumulo.configuration.Defaults;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Objects;


public class Framework  {

  private String bindAddress = Defaults.BIND_ADDRESS;
  private Integer httpPort = Defaults.HTTP_PORT;
  private String mesosMaster = Defaults.MESOS_MASTER;
  private String name;
  private String id;
  private String tarballUri;
  private String dockerImage;
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
  public boolean hasName(){ return !Strings.isNullOrEmpty(name); }

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
  public boolean hasId(){ return !Strings.isNullOrEmpty(id); }

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
  public boolean hasTarballUri(){ return !Strings.isNullOrEmpty(tarballUri);  }

  /**
  * Name of docker container to use for deployment
  *
  * @return
  */
  @JsonProperty("dockerInfo")
  public String getDockerImage() { return dockerImage; }
  public void setDockerImage(String dockerImage){ this.dockerImage = dockerImage; }
  public boolean hasDockerImage() { return !Strings.isNullOrEmpty(dockerImage);  }

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
    return isNotNullEmpty(cluster);
  }

  private static boolean isNotNullEmpty(Object thing){
    boolean hasThing = false;
    if( thing != null ){
      hasThing = true;
      if( thing instanceof String){
        if( ((String) thing).trim().isEmpty() ){
          hasThing = false;
        }
      }
    }
    return hasThing;
  }

    public void merge(Framework other){
        if(!this.hasName() && other.hasName()) this.setName(other.getName());
        if(!this.hasId() && other.hasId()) this.setId(other.getId());
        if(!this.hasDockerImage() && other.hasDockerImage()) this.setDockerImage(other.getDockerImage());
        if(!this.hasTarballUri() && other.hasTarballUri()) this.setTarballUri(other.getTarballUri());
        if(!this.hasCluster() && other.hasCluster()) this.setCluster(other.getCluster());
    }

  @Override
  public String toString()  {

    MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
            helper
            .add("bindAddress", bindAddress)
            .add("httpPort", httpPort)
            .add("dockerImage", this.dockerImage)
            .add("id", this.id)
            .add("mesosMaster", this.mesosMaster)
            .add("name", this.name)
            .add("tarballUri", this.tarballUri)
            .add("zkServers", this.zkServers);
        if( this.hasCluster() ){
            helper.add("cluster", this.cluster.toString());
        } else {
            helper.add("cluster", "");
        }

    return helper.toString();

  }

  @Override
  public int hashCode(){
    return Objects.hash(bindAddress,
            httpPort,
            cluster,
            dockerImage,
            id,
            mesosMaster,
            name,
            tarballUri,
            zkServers);
  }
}
