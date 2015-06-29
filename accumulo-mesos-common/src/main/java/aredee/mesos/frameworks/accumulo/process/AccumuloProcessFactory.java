package aredee.mesos.frameworks.accumulo.process;

import aredee.mesos.frameworks.accumulo.configuration.ProcessConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import com.google.common.base.Joiner;
import org.apache.accumulo.server.util.time.SimpleTimer;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.VFSClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class AccumuloProcessFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloProcessFactory.class);

    private final ProcessConfiguration config;
    private List<LogWriter> logWriters = new ArrayList<>();
    private List<Process> cleanup = new ArrayList<>();

    public static class LogWriter extends Thread {
        private BufferedReader in;
        private BufferedWriter out;

        public LogWriter(InputStream stream, File logFile) throws IOException {

            this.in = new BufferedReader(new InputStreamReader(stream));
            this.out = new BufferedWriter(new FileWriter(logFile));

        SimpleTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    flush();
                } catch (IOException e) {
                    LOGGER.error("Exception while attempting to flush.", e);
                }
            }
        }, 1000, 1000);
    }

    public synchronized void flush() throws IOException {
        if (out != null)
            out.flush();
    }

    @Override
    public void run() {
        String line;

        try {
            while ((line = in.readLine()) != null) {
                out.append(line);
                out.append("\n");
            }

            synchronized (this) {
                out.close();
                out = null;
                in.close();
            }

        } catch (IOException e) {}
    }
}

    public AccumuloProcessFactory(ProcessConfiguration config){
        this.config = config;
    }

    public Process exec(Class<?> clazz, List<String> jvmArgs, String... args) throws IOException {

        ArrayList<String> jvmArgs2 = new ArrayList<>(2 + (jvmArgs == null ? 0 : jvmArgs.size()));
        jvmArgs2.add("-Xmx" + config.getMaxMemory());
        jvmArgs2.add("-Xms" + config.getMinMemory());
        if (jvmArgs != null)
            jvmArgs2.addAll(jvmArgs);
        Process proc = _exec(clazz, jvmArgs2, args);

        cleanup.add(proc);

        return proc;
    }

    private Process _exec(Class<?> clazz, List<String> extraJvmOpts, String... args) throws IOException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = getClasspath();

        String className = clazz.getName();

        ArrayList<String> argList = new ArrayList<>();
        argList.addAll(Arrays.asList(javaBin, "-Dproc=" + clazz.getSimpleName(), "-cp", classpath));
        argList.addAll(extraJvmOpts);
        for (Map.Entry<String,String> sysProp : config.getSystemProperties().entrySet()) {
            argList.add(String.format("-D%s=%s", sysProp.getKey(), sysProp.getValue()));
        }
        // @formatter:off
        argList.addAll(Arrays.asList(
                "-XX:+UseConcMarkSweepGC",
                "-XX:CMSInitiatingOccupancyFraction=75",
                "-Dapple.awt.UIElement=true",
                "-Djava.net.preferIPv4Stack=true",
                "-XX:+PerfDisableSharedMem",
                "-XX:+AlwaysPreTouch",
                org.apache.accumulo.start.Main.class.getName(), className));
        // @formatter:on
        argList.addAll(Arrays.asList(args));

        ProcessBuilder builder = new ProcessBuilder(argList);

        builder.environment().put("ACCUMULO_HOME", config.getDir().getAbsolutePath());
        builder.environment().put("ACCUMULO_LOG_DIR", config.getLogDir().getAbsolutePath());
        builder.environment().put("ACCUMULO_CLIENT_CONF_PATH", config.getClientConfFile().getAbsolutePath());
        String ldLibraryPath = Joiner.on(File.pathSeparator).join(config.getNativeLibPaths());
        builder.environment().put("LD_LIBRARY_PATH", ldLibraryPath);
        builder.environment().put("DYLD_LIBRARY_PATH", ldLibraryPath);

        // if we're running under accumulo.start, we forward these env vars
        String env = System.getenv("HADOOP_PREFIX");
        if (env != null)
            builder.environment().put("HADOOP_PREFIX", env);
        env = System.getenv("ZOOKEEPER_HOME");
        if (env != null)
            builder.environment().put("ZOOKEEPER_HOME", env);
        builder.environment().put("ACCUMULO_CONF_DIR", config.getConfDir().getAbsolutePath());
        // hadoop-2.2 puts error messages in the logs if this is not set
        builder.environment().put("HADOOP_HOME", config.getDir().getAbsolutePath());
        if (config.getHadoopConfDir() != null)
            builder.environment().put("HADOOP_CONF_DIR", config.getHadoopConfDir().getAbsolutePath());

        Process process = builder.start();

        LogWriter lw;
        lw = new LogWriter(process.getErrorStream(), new File(config.getLogDir(), clazz.getSimpleName() + "_" + process.hashCode() + ".err"));
        lw.setDaemon(true);
        logWriters.add(lw);

        lw.start();
        lw = new LogWriter(process.getInputStream(), new File(config.getLogDir(), clazz.getSimpleName() + "_" + process.hashCode() + ".out"));
        lw.setDaemon(true);
        logWriters.add(lw);
        lw.start();

        return process;
    }

    private String getClasspath() throws IOException {

        try {
            ArrayList<ClassLoader> classloaders = new ArrayList<ClassLoader>();

            ClassLoader cl = this.getClass().getClassLoader();

            while (cl != null) {
                classloaders.add(cl);
                cl = cl.getParent();
            }

            Collections.reverse(classloaders);

            StringBuilder classpathBuilder = new StringBuilder();
            classpathBuilder.append(config.getConfDir().getAbsolutePath());

            if (config.getHadoopConfDir() != null)
                classpathBuilder.append(File.pathSeparator).append(config.getHadoopConfDir().getAbsolutePath());

            if (config.getClasspathItems() == null) {

                // assume 0 is the system classloader and skip it
                for (int i = 1; i < classloaders.size(); i++) {
                    ClassLoader classLoader = classloaders.get(i);

                    if (classLoader instanceof URLClassLoader) {

                        for (URL u : ((URLClassLoader) classLoader).getURLs()) {
                            append(classpathBuilder, u);
                        }

                    } else if (classLoader instanceof VFSClassLoader) {

                        VFSClassLoader vcl = (VFSClassLoader) classLoader;
                        for (FileObject f : vcl.getFileObjects()) {
                            append(classpathBuilder, f.getURL());
                        }
                    } else {
                        throw new IllegalArgumentException("Unknown classloader type : " + classLoader.getClass().getName());
                    }
                }
            } else {
                for (String s : config.getClasspathItems())
                    classpathBuilder.append(File.pathSeparator).append(s);
            }

            return classpathBuilder.toString();

        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private void append(StringBuilder classpathBuilder, URL url) throws URISyntaxException {
        File file = new File(url.toURI());
        // do not include dirs containing hadoop or accumulo site files
        if (!containsSiteFile(file))
            classpathBuilder.append(File.pathSeparator).append(file.getAbsolutePath());
    }

    private boolean containsSiteFile(File f) {
        return f.isDirectory() && f.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("site.xml");
            }
        }).length > 0;
    }

    /*
    private List<String> buildRemoteDebugParams(int port) {
        return Arrays.asList(new String[] {"-Xdebug", String.format("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=%d", port)});
    }
    */
}
