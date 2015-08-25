package aredee.mesos.frameworks.accumulo.framework.api;

import javax.ws.rs.core.Response;

public abstract class StatusApiService {
  
      public abstract Response statusGet()
      throws NotFoundException;
  
}
