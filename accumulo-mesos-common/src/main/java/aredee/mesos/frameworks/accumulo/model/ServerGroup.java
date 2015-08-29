package aredee.mesos.frameworks.accumulo.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ServerGroup {

    private Integer count = 1;
    private ServerProfile profile = null;


    /**
     * Number of instances of this profile to launch\n
     **/
    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }


    /**
     * Server profile to launch\n
     **/
    @JsonProperty("profile")
    public ServerProfile getProfile() {
        return profile;
    }
    public void setProfile(ServerProfile profile) {
        this.profile = profile;
    }


    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();
        sb.append("class Server {\n");

        sb.append("  count: ").append(count).append("\n");
        sb.append("  profile: ").append(profile).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

}
