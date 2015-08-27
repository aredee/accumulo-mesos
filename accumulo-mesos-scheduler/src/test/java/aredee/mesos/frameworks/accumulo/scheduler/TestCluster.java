package aredee.mesos.frameworks.accumulo.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.SlaveID;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.mesos.Protos.Value.Scalar;
import org.apache.mesos.Protos.Value.Type;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.junit.Test;

import aredee.mesos.frameworks.accumulo.initialize.AccumuloInitializer;
import aredee.mesos.frameworks.accumulo.initialize.TestAccumuloInitializer;
import mockit.Mock;
import mockit.MockUp;

public class TestCluster {

    
    Map <String, TaskInfo> launchedTasks = new HashMap<String, TaskInfo>();
    
    /**
     * Stub out the Mesos Driver, check what is passed in is what is expected.
     */
    SchedulerDriver mesosDriver = new MockUp<SchedulerDriver>() {
        @Mock
        Status declineOffer(OfferID offerId) {
            System.out.println("SchedulerDriver: declineOffer ");
           
            //TODO: add additional checks
            assertNotNull(offerId);
         
            return null;
        }
        
        @Mock
        Status reconcileTasks(java.util.Collection<TaskStatus> statuses) {
            System.out.println("SchedulerDriver: reconcileTasks ");
          
            // TODO: add additional checks.
            assertNotNull(statuses);
            
            return null;
        }
        
        @Mock
        Status launchTasks(java.util.Collection<OfferID> offerIds, java.util.Collection<TaskInfo> tasks) {
            
            //System.out.println("SchedulerDriver: launchTask: \nofferIds? " + offerIds + " \ntasks? " + tasks);
            System.out.println("SchedulerDriver: launchTask ");
          
            // TODO: add additional checks.
            assertNotNull(offerIds);
            assertNotNull(tasks);
            
            assertTrue(offerIds.iterator().hasNext());
            
            OfferID id = offerIds.iterator().next();
            
            assertTrue(tasks.iterator().hasNext());
            
            // Save off the tasks mesos thinks it launched
            TaskInfo tinfo = tasks.iterator().next();
            
            launchedTasks.put(id.getValue(), tinfo);
     
            return Status.DRIVER_RUNNING;
        }
    }.getMockInstance();
    
    
  
    @Test
    public void testCluster() {
        
        
        // Setup mocks for the initializer
        TestAccumuloInitializer.setupMocks();
        
        // Create initializer 
        AccumuloInitializer initializer = TestAccumuloInitializer.getGoodAccumuloInitializer();
        
        Cluster cluster = new Cluster(initializer);
        
        // Since initially there are no offers then there should not be anything running
        assertNothingRunning(cluster);        
        
        // Simulate offers coming in
        //
        // The following will represent 5 offers for the same slave but different offer ids
        // so all the non-tservers should start but no more than 1 tservers. This generally the 
        // scenario if you have a huge server, there is one offer at a time until the resources 
        // are used.
        //
        List<Protos.Offer> offers = null;
        List<Protos.Offer> totalOffers = new ArrayList<Protos.Offer>();
        
        for (int i=0; i<5; i++) {
            offers = createOffers(1, ""+i, 50000.0, 20.0, "Accumulo-Mesos-default-instance");
            totalOffers.addAll(offers);   
            cluster.handleOffers(mesosDriver, offers);           
        }
        
        // Inform the scheduler all launched tasks are running
        postTaskStatus(cluster, totalOffers, TaskState.TASK_RUNNING);
      
        printClusterRunning(cluster);
        
        assertTrue(cluster.numTserversRunning() == 1);
        
        assertTrue(cluster.isGCRunning() && cluster.isMasterRunning() 
                && cluster.isMonitorRunning() && cluster.isTracerRunning());       
  
        // Inform the scheduler all launched tasks where killed
        postTaskStatus(cluster, totalOffers, TaskState.TASK_KILLED);
        
        printClusterRunning(cluster);
       
        assertNothingRunning(cluster);        
        
        // Set in MockScehdulerDriver#launchedTask()
        launchedTasks.clear();
        totalOffers.clear();
        
        // The next test will be five offers from 5 slaves. We will not know which were started where.
        offers = createOffers(5, null, 50000.0, 20.0, "Accumulo-Mesos-default-instance");
        
        totalOffers.addAll(offers);   
        cluster.handleOffers(mesosDriver, offers);
        
        // Inform the scheduler all launched tasks are running
        postTaskStatus(cluster, totalOffers, TaskState.TASK_RUNNING);       
        printClusterRunning(cluster);
        
        // There should be 5 servers running
        int running = cluster.numTserversRunning();
        if (cluster.isGCRunning())
            running++;
        if (cluster.isMasterRunning())
            running++;        
        if (cluster.isMonitorRunning())
            running++;
        if (cluster.isTracerRunning())
            running++;
        
        assertEquals(5,running);
        
        // Inform the scheduler all launched tasks where lost
        postTaskStatus(cluster, totalOffers, TaskState.TASK_LOST);
        
        printClusterRunning(cluster);      
        
        assertNothingRunning(cluster);
        
        
    }
    
    public void assertNothingRunning(Cluster cluster) {
        
        assertTrue(cluster.numTserversRunning() == 0);
        
        assertFalse(cluster.isGCRunning() || cluster.isMasterRunning() 
                || cluster.isMonitorRunning() || cluster.isTracerRunning());         
    }
    
    public void postTaskStatus(Cluster cluster, List<Protos.Offer> offers, TaskState state) {
        
      //  System.out.println("Offers ? " + offers);
      //  System.out.println("Launched ? " + launchedTasks);
     
        for (Protos.Offer offer : offers) {
            String id = offer.getId().getValue();
            if (launchedTasks.containsKey(id)) {
                TaskInfo tinfo = launchedTasks.get(id);
                TaskStatus taskStatus = TaskStatus.newBuilder()
                        .setSlaveId(offer.getSlaveId())
                        .setTaskId(tinfo.getTaskId())
                        .setState(state)
                        .build();
                
                // Simulate the executor firing a running message back to mesos which would cause this action to occur and
                // then update the cluster.
                cluster.updateTaskStatus(taskStatus);               
            }
        }
        
    }
    public void printClusterRunning(Cluster cluster) {
        
        System.out.println("\nCluster Status !!! ");
        System.out.println("TServers running ? " + cluster.numTserversRunning());
        System.out.println("Master running ? " + cluster.isMasterRunning());
        System.out.println("Monitor running ? " + cluster.isMonitorRunning());
        System.out.println("GC running ? " + cluster.isGCRunning());
        System.out.println("Tracer running ? " + cluster.isTracerRunning());
            
    }
    
    /**
     * If there is only 1 offer then use the same slave everytime. Otherwise create a new slaveId
     * 
     * @param count
     * @param offerId
     * @param mem
     * @param cpus
     * @param fwId
     * @return
     */
    public List<Protos.Offer> createOffers(int count, String offerId, double mem, double cpus, String fwId) {
          
          List<Protos.Offer> offers = new ArrayList<Protos.Offer>(count);
          
          if (count == 1) {
              Offer offer = Offer.newBuilder()
                      .setHostname("localhost")
                      .setSlaveId(SlaveID.newBuilder().setValue("slave-1"))
                      .setId(OfferID.newBuilder().setValue(offerId))
                      .setFrameworkId(FrameworkID.newBuilder().setValue(fwId))
                      .addResources(Resource.newBuilder()
                              .setName("cpus")
                              .setType(Type.SCALAR)
                              .setScalar(Scalar.newBuilder().setValue(cpus))
                              .setRole("*"))
                      .addResources(Resource.newBuilder().setName("mem")
                              .setType(Type.SCALAR)
                              .setScalar(Scalar.newBuilder().setValue(mem))
                              .setRole("*"))
                      .build();
              
              offers.add(offer);            
          } else {
              for (int i = 0; i<count; i++) {
              
                  Offer offer = Offer.newBuilder()
                          .setHostname("localhost")
                          .setSlaveId(SlaveID.newBuilder().setValue("slave-"+i))
                          .setId(OfferID.newBuilder().setValue(""+i))
                          .setFrameworkId(FrameworkID.newBuilder().setValue(fwId))
                          .addResources(Resource.newBuilder()
                                  .setName("cpus")
                                  .setType(Type.SCALAR)
                                  .setScalar(Scalar.newBuilder().setValue(cpus))
                                  .setRole("*"))
                          .addResources(Resource.newBuilder().setName("mem")
                                  .setType(Type.SCALAR)
                                  .setScalar(Scalar.newBuilder().setValue(mem))
                                  .setRole("*"))
                          .build();
                  
                  offers.add(offer);
              }
          }
          
          return offers;
      }
}
