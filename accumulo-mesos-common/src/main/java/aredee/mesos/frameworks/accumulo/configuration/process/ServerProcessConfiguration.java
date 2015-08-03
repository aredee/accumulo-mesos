package aredee.mesos.frameworks.accumulo.configuration.process;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import com.google.gson.Gson;


public class ServerProcessConfiguration extends BaseProcessConfiguration {
    
    private File executorDir = null;
    private File accumuloLibDir;
    private File accumuloLibExtDir;
    private File accumuloLogDir = null;
    private File accumuloConfDir = null;
    private File accumuloDir = null;
    private File accumuloClientConfFile = null;
   
    private File zooKeeperDir = null;
    private File walogDir = null;
    private File hadoopHomeDir = null;
    private File hadoopConfDir = null;

    private List<String> classpathItems = new ArrayList<String>();
    private List<String> nativeLibPaths = new ArrayList<String>(5);

    private Map<String,String> systemProperties = new HashMap<String,String>();
    private Map<ServerType,Integer> serverMemory = new HashMap<ServerType,Integer>();

    public ServerProcessConfiguration(){
        super();
    }

    public File getExecutorDir() {
        return executorDir;
    }

    public void setExecutorDir(File dir) {
        executorDir = dir;
    }
    public File getAccumuloDir() {
        return accumuloDir;
    }

    public void setAccumuloDir(File accumuloDir) {
        this.accumuloDir = accumuloDir;
    }
    public File getAccumuloLibDir() {
        return accumuloLibDir;
    }

    public void setAccumuloLibDir(File libDir) {
        this.accumuloLibDir = libDir;
    }

    public File getAccumuloLibExtDir() {
        return accumuloLibExtDir;
    }

    public void setAccumuloLibExtDir(File libExtDir) {
        this.accumuloLibExtDir = libExtDir;
    }

    public File getAccumuloLogDir() {
        return accumuloLogDir;
    }

    public void setAccumuloLogDir(File logDir) {
        this.accumuloLogDir = logDir;
    }

    public File getAccumuloConfDir() {
        return accumuloConfDir;
    }

    public void setAccumuloConfDir(File confDir) {
        this.accumuloConfDir = confDir;
    }

    public File getZooKeeperDir() {
        return zooKeeperDir;
    }

    public void setZooKeeperDir(File zooKeeperDir) {
        this.zooKeeperDir = zooKeeperDir;
    }
 
    public File getWalogDir() {
        return walogDir;
    }

    public void setWalogDir(File walogDir) {
        this.walogDir = walogDir;
    }

    public void setHadoopHomeDir(File dir) {
        hadoopHomeDir = dir;
    }
    public File getHadoopHomeDir() {
        return hadoopHomeDir;
    }
    public File getHadoopConfDir() {
        return hadoopConfDir;
    }

    public void setHadoopConfDir(File hadoopConfDir) {
        this.hadoopConfDir = hadoopConfDir;
    }

    public File getAccumuloClientConfFile() {
        return accumuloClientConfFile;
    }

    public void setAccumuloClientConfFile(File clientConfFile) {
        this.accumuloClientConfFile = clientConfFile;
    }

    public List<String> getClasspathItems() {
        return classpathItems;
    }

    public void setClasspathItems(List<String> classpathItems) {
        this.classpathItems = classpathItems;
    }

    public List<String> getNativeLibPaths() {
        return nativeLibPaths;
    }

    public void setNativeLibPaths(List<String> nativeLibPaths) {
        this.nativeLibPaths = nativeLibPaths;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    public Map<ServerType, Integer> getServerMemory() {
        return serverMemory;
    }

    public void setServerMemory(Map<ServerType, Integer> serverMemory) {
        this.serverMemory = serverMemory;
    }


    public String toString() {
        return new Gson().toJson(this);
    }

}
