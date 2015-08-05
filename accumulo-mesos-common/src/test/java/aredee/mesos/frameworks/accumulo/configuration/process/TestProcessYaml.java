package aredee.mesos.frameworks.accumulo.configuration.process;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


public class TestProcessYaml {

    // TODO get test working
    //@Test
    public void loadJson() {
        loadJsonConfig();    
    }

    // TODO get test working

    //@Test
    public void writeJson() {
        ServerProcessConfiguration config = new ServerProcessConfiguration();
        
        Map props = System.getProperties();
        
        config.setSystemProperties((Map<String,String>)props);
        config.setAccumuloDir(new File("/usr/local/accumulo"));
        GsonBuilder gbld = new GsonBuilder();
        gbld.registerTypeAdapter(File.class, new FileTypeAdapter());       
        System.out.println(gbld.setPrettyPrinting().create().toJson(config));
        
     }

    // TODO get test working

    //@Test
    public void loadYaml() {
       // loadConfig();
        assertTrue(true);
    }
    
    protected void loadConfig() {
        InputStream input = null;
        Yaml y = new Yaml();
        try {
            
            System.out.println(new File("./").getAbsolutePath());
            
            input = new FileInputStream(Paths.get("./", "/accumulo-mesos-executor/src/main/resources/accumulo.yaml").toFile());
            
            ServerProcessConfiguration config = y.loadAs(input,ServerProcessConfiguration.class);
            

            System.out.println(config);
            
         } catch (Exception e) {
             e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(input);
        }
    }    
    
    public void loadJsonConfig() {
        InputStream input = null;
       try {
            
            System.out.println(new File("./").getAbsolutePath());
            
            File f = Paths.get("./", "/accumulo-mesos-executor/src/main/resources/accumulo.json").toFile();
            
            GsonBuilder gbld = new GsonBuilder();
            gbld.registerTypeAdapter(File.class, new FileTypeAdapter());
            
            ServerProcessConfiguration config = gbld.create().fromJson(new FileReader(f), ServerProcessConfiguration.class);
            
            
            if (config.getZooKeeperDir() != null) {
                System.out.println(config.getZooKeeperDir().getAbsolutePath());
            }           
            System.out.println(config);
            
         } catch (Exception e) {
             e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(input);
        }     
    }
    
    public static class FileTypeAdapter extends TypeAdapter<File> {

        @Override
        public void write(final JsonWriter out, final File value)
                throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.getAbsolutePath());
            }
        }

        @Override
        public File read(final JsonReader in) throws IOException {
            if (in.hasNext()) {
                final String name = in.nextString();
                return name != null ? new File(name) : null;
            }
            return null;
        }
    }}
