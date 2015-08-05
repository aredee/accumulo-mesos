package aredee.mesos.frameworks.accumulo.model;


import com.fasterxml.jackson.annotation.JsonProperty;


public class Executor  {
  
  private String executorID = null;
  private String frameworkID = null;
  private String name = null;

  
  /**
   **/
  @JsonProperty("executorID")
  public String getExecutorID() {
    return executorID;
  }
  public void setExecutorID(String executorID) {
    this.executorID = executorID;
  }

  
  /**
   **/
  @JsonProperty("frameworkID")
  public String getFrameworkID() {
    return frameworkID;
  }
  public void setFrameworkID(String frameworkID) {
    this.frameworkID = frameworkID;
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

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Executor {\n");
    
    sb.append("  executorID: ").append(executorID).append("\n");
    sb.append("  frameworkID: ").append(frameworkID).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
