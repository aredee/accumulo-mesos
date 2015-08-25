package aredee.mesos.frameworks.accumulo.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Slave  {

    private String hostname = null;
    private String id = null;


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
        sb.append("class Slave {\n");

        sb.append("  hostname: ").append(hostname).append("\n");
        sb.append("  id: ").append(id).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
