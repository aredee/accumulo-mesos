package aredee.mesos.frameworks.accumulo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import java.util.List;


public class IdRegistry {

    public static class RegistryPair {

        private String name;
        private String id;

        public RegistryPair(String name, String id){
            this.name = name;
            this.id = id;
        }

        @JsonProperty("id")
        public String getId(){ return this.id; }
        public void setId(String id){ this.id = id; }

        @JsonProperty("name")
        public String getName(){ return this.name; }
        public void setName(String name){ this.name = name; }


    }

    private List<RegistryPair> pairs = Lists.newArrayList();

    public IdRegistry(){}

    @JsonProperty("registry")
    public List<RegistryPair> getRegistry(){ return this.pairs;}
    public void setRegistry(List<RegistryPair> pairs){ this.pairs = pairs; }

    public void addItem(String name, String id){
        pairs.add(new RegistryPair(name, id));
    }
}
