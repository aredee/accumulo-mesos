package aredee.mesos.frameworks.accumulo.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class FrameworkTest {

    private static final ObjectMapper mapper = new ObjectMapper();



    @Test
    public void testFrameworkOnly() throws Exception {
        byte[] jsonBytes = readJsonResource("/model/FrameworkOnly.json");
        Framework framework = mapper.readValue(jsonBytes, Framework.class);

        assertFalse(framework.hasCluster());
        assertFalse(framework.hasId());
        assertTrue(framework.hasName());
        assertTrue(framework.hasTarballUri());

        verifyFramework(framework);
    }

    @Test
    public void testAccumuloOnly() throws Exception {
        byte[] jsonBytes = readJsonResource("/model/AccumuloOnly.json");
        Accumulo cluster = mapper.readValue(jsonBytes, Accumulo.class);

        assertFalse(cluster.hasSiteXml());

        verifyCluster(cluster);

    }

    @Test
    public void testFrameworkAndAccumulo() throws Exception {
        byte[] jsonBytes = readJsonResource("/model/FrameworkAndAccumulo.json");
        Framework framework = mapper.readValue(jsonBytes, Framework.class);

        assertTrue(framework.hasCluster());

        verifyFramework(framework);
        verifyCluster(framework.getCluster());

    }

    private void verifyFramework(Framework framework){
        assertEquals("1.1.1.1", framework.getBindAddress());
        assertEquals(new Integer(1234), framework.getHttpPort());
        assertEquals("1.2.3.4:5150", framework.getMesosMaster());
        assertEquals("accumulo-mesos-test", framework.getName());
        assertEquals("hdfs://localhost:9000/data/accumulo-mesos.tar.gz", framework.getTarballUri());
        assertEquals("server1:2181,server2:2181,server3:2181", framework.getZkServers());
    }

    private void verifyCluster(Accumulo cluster){
        assertEquals("testInstance", cluster.getInstance());
        assertEquals("jimbo", cluster.getRootUser());
        assertEquals("jimbopassword", cluster.getRootPassword());
        assertEquals("srvA:2181", cluster.getZkServers());
        assertEquals(new Integer(1024), cluster.getExecutorMemory());
        assertEquals("hdfs://localhost:9000/data/accumulo.tar.gz", cluster.getTarballUri());
        assertEquals("hdfs://localhost:9000/accumulo-mesos", cluster.getHdfsUri());
        assertEquals(4, cluster.getServerGroups().size());

        verifyServerGroup(getServerGroupByProfileType(cluster.getServerGroups(), "tserver"),
                5,
                "BasicTserver",
                "Basic Tserver setup",
                "tserver",
                8.0,
                8192,
                "accumulo"
        );

        verifyServerGroup(getServerGroupByProfileType(cluster.getServerGroups(), "master"),
                1,
                "BasicMaster",
                "Basic Master setup",
                "master",
                2.0,
                2048,
                "accumulomaster"
        );

        verifyServerGroup(getServerGroupByProfileType(cluster.getServerGroups(), "gc"),
                1,
                "BasicGC",
                "Basic Garbage Collector setup",
                "gc",
                2.0,
                512,
                "accumulogc"
        );

        verifyServerGroup(getServerGroupByProfileType(cluster.getServerGroups(), "monitor"),
                2,
                "Monitor",
                "Basic Monitor setup",
                "monitor",
                1.0,
                512,
                "accumulomon"
        );

    }

    private void verifyServerGroup(ServerGroup group,
                                   int count,
                                   String name,
                                   String description,
                                   String type,
                                   double cpus,
                                   int mem,
                                   String user
                                   ){

        assertEquals(new Integer(count), group.getCount());
        ServerProfile profile = group.getProfile();
        assertEquals(name, profile.getName());
        assertEquals(description, profile.getDescription());
        assertEquals(type, profile.getType().name());
        assertTrue( cpus == profile.getCpus().doubleValue() );
        assertEquals(new Integer(mem), profile.getMemory());
        assertEquals(user, profile.getUser());
    }


    private ServerGroup getServerGroupByProfileType(List<ServerGroup> groups, String type){
        for( ServerGroup group : groups ){
            String profileType = group.getProfile().getType().name();
            if( profileType.equals(type)){
                return group;
            }
        }
        return null;
    }

    private byte[] readJsonResource(String resource) throws IOException {
        URL url = this.getClass().getResource(resource);
        File jsonFile = new File(url.getFile());
        byte [] jsonBytes = IOUtils.toByteArray(new FileInputStream(jsonFile));
        return jsonBytes;
    }
}