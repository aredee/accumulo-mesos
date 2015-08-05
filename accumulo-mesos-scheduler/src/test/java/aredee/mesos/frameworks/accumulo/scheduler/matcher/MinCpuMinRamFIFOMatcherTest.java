package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import aredee.mesos.frameworks.accumulo.configuration.cluster.BaseClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.cluster.ClusterConfiguration;
import aredee.mesos.frameworks.accumulo.configuration.ServerType;
import aredee.mesos.frameworks.accumulo.scheduler.server.AccumuloServer;
import aredee.mesos.frameworks.accumulo.scheduler.server.ServerUtils;
import org.apache.mesos.Protos;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.Parameter;

import java.util.*;

import static org.junit.Assert.*;


//@RunWith(Parameterized.class)
public class MinCpuMinRamFIFOMatcherTest {

/*
    private Matcher matcher = null;

    private static class MatcherTestClusterConfiguration extends BaseClusterConfiguration {

    }

    private static class TestParameter {

        private ClusterConfiguration config;
        private Set<AccumuloServer> servers;
        private List<Protos.Offer> offers;
        private List<Match> matches;

        public TestParameter(ClusterConfiguration config){
            this.config = config;
            this.servers = new HashSet<>();
            this.offers = new ArrayList<>();
            this.matches = new ArrayList<>();
        }

        public TestParameter addMaster(){
            this.servers.add(ServerUtils.newServer(ServerType.MASTER));
            return this;
        }

        public TestParameter addTabletServer(){
            this.servers.add(ServerUtils.newServer(ServerType.TABLET_SERVER));
            return this;
        }

        public TestParameter addTabletServers(int numServers){
            for( int ii = 0; ii < numServers; ii++ ){
                this.addTabletServer();
            }
            return this;
        }

        public TestParameter addSimpleOffer(double cpus, double ram){
            this.offers.add(makeSimpleOffer(cpus, ram));
            return this;
        }

    }

    private static final TestParameter param0 =
            new TestParameter(new MatcherTestClusterConfiguration());

    static {

        param0.config.setMinTservers(3);
        param0.config.setTserverCpus(2);
        param0.config.setMinTserverMem(1024);
        param0.config.setMinMasterCpus(2);
        param0.config.setMinMasterMem(1024);

        param0.addMaster()
                .addTabletServers(3)
                .addSimpleOffer(2.0, 10240.0)
                .addSimpleOffer(3.0 ,10240.0)
                .addSimpleOffer(4.0 ,10240.0)
                .addSimpleOffer(5.0 ,10240.0);


        // TODO create class to package config, servers, offers, matches
        matches0 = Arrays.asList(new Match[] {
           new Match(Servers)
        });

    }



    @Parameters(name = "{index}: {4}")
    public static Collection<Object[]> data(){

        return Arrays.asList(new Object[][]{
                {config0, servers0, offers0, matches0, "Simple Test Setup"}
        });
    }

    @Parameter
    public ClusterConfiguration inputConfig;

    @Parameter
    public Set<AccumuloServer> inputServers;

    @Parameter
    public List<Protos.Offer> inputOffers;

    @Parameter
    public List<Match> expectedMatches;

    @Parameter
    public String testText;

    @Before
    public void setup() {
        // Auto-setup of number of tservers
        int tservers = 0;
        for( AccumuloServer server : inputServers){
            if( server.getType() == ServerType.TABLET_SERVER){
                tservers++;
            }
        }
        inputConfig.setMinTservers(tservers);
    }

    @Test
    public void testMatchOffers() throws Exception {
        matcher = new MinCpuMinRamFIFOMatcher(inputConfig);
        List<Match> actualMatches = matcher.matchOffers(inputServers, inputOffers);
        for( Match match : expectedMatches ){
            assertTrue(testText, actualMatches.contains(match));
        }
    }

    private static Protos.Resource makeScalarResource(String name, Double value){
        return Protos.Resource.newBuilder()
                .setName(name)
                .setScalar(Protos.Value.Scalar.newBuilder()
                                .setValue(value)
                                .build()
                )
                .build();
    }

    private static Protos.Offer makeSimpleOffer(Double cpus, Double mem){
        return Protos.Offer.newBuilder()
                .addResources(makeScalarResource("cpus", cpus))
                .addResources(makeScalarResource("mem", mem))
                .build();
    }
*/

}