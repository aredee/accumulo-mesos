package aredee.mesos.frameworks.accumulo.scheduler.launcher;

import aredee.mesos.frameworks.accumulo.configuration.Constants;
import aredee.mesos.frameworks.accumulo.configuration.Environment;
import aredee.mesos.frameworks.accumulo.model.Framework;
import aredee.mesos.frameworks.accumulo.model.ServerProfile;
import aredee.mesos.frameworks.accumulo.model.Task;
import aredee.mesos.frameworks.accumulo.scheduler.matcher.Match;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.Value.Scalar;
import org.apache.mesos.Protos.Value.Type;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Launches an Executor process that starts Accumulo Servers using the accumulo-start jar.
 */
public class AccumuloStartExecutorLauncher implements Launcher {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumuloStartExecutorLauncher.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String CPUS = "cpus";
    private static final String MEM = "mem";

    private Framework config;
    private long executorCount = 0;
    private final Lock executorCountLock = new ReentrantLock();

    public AccumuloStartExecutorLauncher(Framework config){
        this.config = config;
    }

    /**
     * Interface used to launch Accumulo Server tasks.
     *
     * @param driver Mesos interface to use to launch a server
     * @param match AccumuloServer and Offer to launch
     */
    public Protos.TaskInfo launch(SchedulerDriver driver, Match match){
        Task task = match.getTask();
        Protos.Offer offer = match.getOffer();

        // get cpu and memory from offer
        double offerCpus = 0.0;
        double offerMem = 0.0;
        for( Resource resource : offer.getResourcesList()){
            if( resource.hasName() && resource.hasScalar() ) {
                switch (resource.getName()) {
                    case CPUS:
                        offerCpus = resource.getScalar().getValue();
                        break;
                    case MEM:
                        offerMem = resource.getScalar().getValue();
                        break;
                }
            }
        }

        List<Protos.CommandInfo.URI> uris = new ArrayList<>();
        Protos.CommandInfo.URI frameworkTarballUri = Protos.CommandInfo.URI.newBuilder()
                .setValue(this.config.getTarballUri())
                .setExtract(true)
                .setExecutable(false)
                .build();
        Protos.CommandInfo.URI accumuloTarballUri = Protos.CommandInfo.URI.newBuilder()
                .setValue(this.config.getCluster().getTarballUri())
                .setExtract(true)
                .setExecutable(false)
                .build();
        uris.add(frameworkTarballUri);
        uris.add(accumuloTarballUri);

        if( config.getCluster().hasNativeLibUri() ) {
            Protos.CommandInfo.URI accumuloNativeUri = Protos.CommandInfo.URI.newBuilder()
                    .setValue(this.config.getCluster().getNativeLibUri())
                    .setExtract(false)  // it's just a .so file
                    .setExecutable(false)
                    .build();
            uris.add(accumuloNativeUri);
        }

        // TODO get java -XX stuff from config
        // TODO get executor jar name from URI
        // The "m" is hard coded here to get the cluster up...should be handled in another manner, maybe another method that
        // leveraged off the *ExecutorMemory, or if this is not being used to check against offers then just make it the
        // correct string....
        
        // Since JAVA_HOME is usually installed here...hard code it for now. Should we pass it in or instead
        // of launching it directly use a script that checks the local server(environment) for JAVA_HOME...and
        // the rest of the environment var?
        String keyword = getAccumuloProcessKeyword(task.getType());
        // TODO JAVA_HOME is not defined on current mesos slaves
        String commandLine = new StringBuilder()
                .append("export ACCUMULO_HOME=$MESOS_DIRECTORY/").append(getAccumuloDirectory(this.config.getCluster().getTarballUri())).append(";")
                .append("export ACCUMULO_LOG_DIR=$ACCUMULO_HOME/logs").append(";")
                .append("export JAVA_HOME=").append(System.getProperty("java.home")).append(";")
                .append("export HADOOP_PREFIX=").append(System.getenv(Environment.HADOOP_PREFIX)).append(";")
                .append("export ZOOKEEPER_HOME=").append(System.getenv(Environment.ZOOKEEPER_HOME)).append(";")
                .append("$ACCUMULO_HOME/bin/build_native_library.sh").append(";")
                .append("env").append(";")
                .append("/usr/bin/java")
                .append(" -Dserver=").append(keyword)  // this is just candy to see what's running using jps or ps
                .append(" -Xmx").append(this.config.getCluster().getExecutorMemory() + "m")
                .append(" -jar $MESOS_DIRECTORY/").append(Constants.ACCUMULO_MESOS_DISTRO)
                .append("/").append(Constants.EXECUTOR_JAR)
                .toString();

        // Build Command
        Protos.CommandInfo.Builder commandBuilder = Protos.CommandInfo.newBuilder()
                .setValue(commandLine)
                .addAllUris(uris);
        if(match.getTask().getServerProfile().hasUser()){
            commandBuilder.setUser(match.getTask().getServerProfile().getUser());
        }
        Protos.CommandInfo commandInfo = commandBuilder.build();

        // Json to pass to task on executor
        ServerProfile profile = match.getTask().getServerProfile();
        profile.setSiteXml(config.getCluster().getSiteXml());
        if( config.getCluster().hasNativeLibUri() ){
            profile.setUseNativemaps(true);
        }
        byte[] profileBytes = null;
        try {
            profileBytes = mapper.writeValueAsBytes(profile);
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not serialize ServerProfile to send to executor");
            LOGGER.error("serverProfile ? {}", profile);

            // TODO what do here?
            e.printStackTrace();
        }


        // TODO why not UUID instead of a counter here?
        String executorId;
        executorCountLock.lock();
        try {
            executorId = "accumuloExecutor-" + keyword + "-" + executorCount++;
        } finally {
            executorCountLock.unlock();
        }

        LOGGER.info("Creating executor {}", executorId);
        LOGGER.info("Executor command {}", commandInfo.getValue());

        List<Resource> resources = Arrays.asList(
                Resource.newBuilder()
                        .setName(CPUS)
                        .setType(Type.SCALAR)
                        .setScalar(Scalar.newBuilder().setValue(offerCpus))
                        .setRole("*")
                        .build(),
                Resource.newBuilder()
                        .setName(MEM)
                        .setType(Type.SCALAR)
                        .setScalar(Scalar.newBuilder().setValue(offerMem))
                        .setRole("*")
                        .build()
        );

        Protos.ExecutorInfo executorInfo = Protos.ExecutorInfo.newBuilder()
                .setFrameworkId(offer.getFrameworkId())
                .setExecutorId(ExecutorID.newBuilder().setValue(executorId))
                .setCommand(commandInfo)
                .setName(executorId)
                //.addAllResources(resources)  // TODO Should this be here and in TaskInfo???
                .build();


        Protos.TaskInfo.Builder taskInfo = Protos.TaskInfo.newBuilder()
                .setName(keyword)
                .setTaskId(Protos.TaskID.newBuilder().setValue(task.getTaskId()))
                .setSlaveId(offer.getSlaveId())
                .setData(ByteString.copyFrom(profileBytes))
                .setExecutor(executorInfo)
                .addAllResources(resources);

        if( config.hasDockerImage()){
            // TODO this is totally untested
            Protos.ContainerInfo.DockerInfo dockerInfo = Protos.ContainerInfo.DockerInfo.newBuilder()
                    .setImage(config.getDockerImage())
                    .build();

            Protos.ContainerInfo containerInfo = Protos.ContainerInfo.newBuilder()
                    .setDocker(dockerInfo)
                    .build();

            taskInfo.setContainer(containerInfo);
        }

        // TODO handle driver Status
        LOGGER.info("Launching task {} offer {} slave {}", taskInfo.getTaskId(), offer.getId(), taskInfo.getSlaveId().getValue());


        Protos.Status status = driver.launchTasks(Arrays.asList(new Protos.OfferID[]{offer.getId()}),
                Arrays.asList(new Protos.TaskInfo[]{taskInfo.build()}));

        LOGGER.info("Launched task. status ? ", status.toString());

        return taskInfo.build();
    }

    private String getAccumuloProcessKeyword(ServerProfile.TypeEnum type){
        String server = "";
        switch( type ){
            case master:
                server = "master";
                break;
            case tserver:
                server = "tserver";
                break;
            case gc:
                server = "gc";
                break;
            case tracer:
                server = "tracer";
                break;
            case monitor:
                server = "monitor";
                break;
            case init:
                server = "init";
                break;
        }
        return server;
    }

    public static String getAccumuloDirectory(String uri){
        // hdfs://localhost:9000/test/accumulo-1.7.0-bin.tar.gz
        // TODO make a test case!

        List<String> parts = Splitter.on("/").splitToList(uri);
        // get last item only
        String tarball = parts.get(parts.size()-1);
        List<String> pieces = Splitter.on("-").splitToList(tarball);

        String ret = Joiner.on("-").join(pieces.subList(0, pieces.size()-1));

        return ret;
    }


}
