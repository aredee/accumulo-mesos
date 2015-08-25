package aredee.mesos.frameworks.accumulo.framework.api;

import javax.ws.rs.core.Response;

public abstract class ConfigApiService {
  
      public abstract Response configGet()
      throws NotFoundException;
  
}
