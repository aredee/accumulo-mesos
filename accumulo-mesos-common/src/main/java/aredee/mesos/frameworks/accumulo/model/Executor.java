package aredee.mesos.frameworks.accumulo.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Executor  {

  private String executorId = null;
  private String frameworkId = null;
  private String name = null;


  /**
   **/
  @JsonProperty("executorId")
  public String getExecutorId() {
    return executorId;
  }
  public void setExecutorId(String executorId) {
    this.executorId = executorId;
  }


  /**
   **/
  @JsonProperty("frameworkId")
  public String getFrameworkId() {
    return frameworkId;
  }
  public void setFrameworkId(String frameworkId) {
    this.frameworkId = frameworkId;
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

    sb.append("  executorId: ").append(executorId).append("\n");
    sb.append("  frameworkId: ").append(frameworkId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}