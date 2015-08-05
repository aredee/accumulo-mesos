package aredee.mesos.frameworks.accumulo.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Task  {
  
  private Executor executor = null;
  private Slave slave = null;
  private String ID = null;

  
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
    sb.append("class Task {\n");
    
    sb.append("  executor: ").append(executor).append("\n");
    sb.append("  slave: ").append(slave).append("\n");
    sb.append("  ID: ").append(ID).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
