package aredee.mesos.frameworks.accumulo.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Slave  {
  
  private String hostname = null;
  private String ID = null;

  
  /**
   **/
  @JsonProperty("hostname")
  public String getHostname() {
    return hostname;
  }
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  
  /**
   **/
  @JsonProperty("ID")
  public String getID() {
    return ID;
  }
  public void setID(String ID) {
    this.ID = ID;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Slave {\n");
    
    sb.append("  hostname: ").append(hostname).append("\n");
    sb.append("  ID: ").append(ID).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
