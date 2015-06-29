package aredee.mesos.frameworks.accumulo.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProcessConfiguration {

    private String maxMemory = "128m";
    private String minMemory = "16m";
    private File dir = null;
    private File libDir;
    private File libExtDir;
    private File logDir = null;
    private File confDir = null;
    private File zooKeeperDir = null;
    private File accumuloDir = null;
    private File walogDir = null;
    private File hadoopConfDir = null;
    private File clientConfFile = null;

    private List<String> classpathItems = new ArrayList<>();
    private List<String> nativeLibPaths = new ArrayList<>();

    private Map<String,String> systemProperties = new HashMap<>();

    private Map<ServerType,Long> serverMemory = new HashMap<>();

    public ProcessConfiguration(){}

    public String getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(String maxMemory) {
        this.maxMemory = maxMemory;
    }

    public String getMinMemory() {
        return minMemory;
    }

    public void setMinMemory(String minMemory) {
        this.minMemory = minMemory;
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public File getLibDir() {
        return libDir;
    }

    public void setLibDir(File libDir) {
        this.libDir = libDir;
    }

    public File getLibExtDir() {
        return libExtDir;
    }

    public void setLibExtDir(File libExtDir) {
        this.libExtDir = libExtDir;
    }

    public File getLogDir() {
        return logDir;
    }

    public void setLogDir(File logDir) {
        this.logDir = logDir;
    }

    public File getConfDir() {
        return confDir;
    }

    public void setConfDir(File confDir) {
        this.confDir = confDir;
    }

    public File getZooKeeperDir() {
        return zooKeeperDir;
    }

    public void setZooKeeperDir(File zooKeeperDir) {
        this.zooKeeperDir = zooKeeperDir;
    }

    public File getAccumuloDir() {
        return accumuloDir;
    }

    public void setAccumuloDir(File accumuloDir) {
        this.accumuloDir = accumuloDir;
    }

    public File getWalogDir() {
        return walogDir;
    }

    public void setWalogDir(File walogDir) {
        this.walogDir = walogDir;
    }

    public File getHadoopConfDir() {
        return hadoopConfDir;
    }

    public void setHadoopConfDir(File hadoopConfDir) {
        this.hadoopConfDir = hadoopConfDir;
    }

    public File getClientConfFile() {
        return clientConfFile;
    }

    public void setClientConfFile(File clientConfFile) {
        this.clientConfFile = clientConfFile;
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

    public Map<ServerType, Long> getServerMemory() {
        return serverMemory;
    }

    public void setServerMemory(Map<ServerType, Long> serverMemory) {
        this.serverMemory = serverMemory;
    }



}
