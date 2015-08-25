package aredee.mesos.frameworks.accumulo.model;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Error message\n
 **/
public class Error {
  
  private Integer code = null;
  private String message = null;

  
  /**
   * Numeric error code
   **/
  @JsonProperty("code")
  public Integer getCode() {
    return code;
  }
  public void setCode(Integer code) {
    this.code = code;
  }

  
  /**
   * Description of error
   **/
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error {\n");
    
    sb.append("  code: ").append(code).append("\n");
    sb.append("  message: ").append(message).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
