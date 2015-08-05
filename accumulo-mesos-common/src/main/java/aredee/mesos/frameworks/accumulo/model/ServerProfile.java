package aredee.mesos.frameworks.accumulo.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ServerProfile  {
  
  private String name = null;
  private String description = null;
  public enum TypeEnum {
     master,  tserver,  gc,  tracer,  monitor, 
  };
  private TypeEnum type = null;
  private Integer memory = null;
  private BigDecimal cpus = null;
  private String launcher = null;
  private String user = null;

  
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
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  
  /**
   **/
  @JsonProperty("memory")
  public Integer getMemory() {
    return memory;
  }
  public void setMemory(Integer memory) {
    this.memory = memory;
  }

  
  /**
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
  @JsonProperty("launcher")
  public String getLauncher() {
    return launcher;
  }
  public void setLauncher(String launcher) {
    this.launcher = launcher;
  }

  
  /**
   **/
  @JsonProperty("user")
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
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
    sb.append("}\n");
    return sb.toString();
  }
}
