package aredee.mesos.frameworks.accumulo.model;


import com.fasterxml.jackson.annotation.JsonProperty;


public class Accumulo  {
  
  private String instance = null;
  private String rootUser = null;
  private String rootPassword = null;
  private String initLocation = null;
  private String zkServers = null;

  
  /**
   **/
  @JsonProperty("instance")
  public String getInstance() {
    return instance;
  }
  public void setInstance(String instance) {
    this.instance = instance;
  }

  
  /**
   **/
  @JsonProperty("rootUser")
  public String getRootUser() {
    return rootUser;
  }
  public void setRootUser(String rootUser) {
    this.rootUser = rootUser;
  }

  
  /**
   **/
  @JsonProperty("rootPassword")
  public String getRootPassword() {
    return rootPassword;
  }
  public void setRootPassword(String rootPassword) {
    this.rootPassword = rootPassword;
  }

  
  /**
   * URI of the rfiles for this instance\n
   **/
  @JsonProperty("initLocation")
  public String getInitLocation() {
    return initLocation;
  }
  public void setInitLocation(String initLocation) {
    this.initLocation = initLocation;
  }

  
  /**
   **/
  @JsonProperty("zkServers")
  public String getZkServers() {
    return zkServers;
  }
  public void setZkServers(String zkServers) {
    this.zkServers = zkServers;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Accumulo {\n");
    
    sb.append("  instance: ").append(instance).append("\n");
    sb.append("  rootUser: ").append(rootUser).append("\n");
    sb.append("  rootPassword: ").append(rootPassword).append("\n");
    sb.append("  initLocation: ").append(initLocation).append("\n");
    sb.append("  zkServers: ").append(zkServers).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}