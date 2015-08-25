package aredee.mesos.frameworks.accumulo.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Task  {

  private Executor executor = null;
  private Slave slave = null;
  private String id = null;


  /**
   **/
  @JsonProperty("executor")
  public Executor getExecutor() {
    return executor;
  }
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }


  /**
   **/
  @JsonProperty("slave")
  public Slave getSlave() {
    return slave;
  }
  public void setSlave(Slave slave) {
    this.slave = slave;
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



  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Task {\n");

    sb.append("  executor: ").append(executor).append("\n");
    sb.append("  slave: ").append(slave).append("\n");
    sb.append("  id: ").append(id).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
