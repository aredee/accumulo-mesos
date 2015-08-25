package aredee.mesos.frameworks.accumulo.framework.api;

import javax.ws.rs.core.Response;

public abstract class ClusterApiService {
  
      public abstract Response clusterKillPost()
      throws NotFoundException;
  
      public abstract Response clusterMasterReprovisionPost()
      throws NotFoundException;
  
      public abstract Response clusterMasterRestartPost()
      throws NotFoundException;
  
      public abstract Response clusterMonitorGet()
      throws NotFoundException;
  
      public abstract Response clusterStartPost()
      throws NotFoundException;
  
      public abstract Response clusterStopPost()
      throws NotFoundException;
  
      public abstract Response clusterTserverReprovisionPost(String id)
      throws NotFoundException;
  
      public abstract Response clusterTserverRestartPost(String id)
      throws NotFoundException;
  
      public abstract Response clusterTserverRollingrestartPost(Boolean master,Integer group,Boolean reprovision)
      throws NotFoundException;
  
}
