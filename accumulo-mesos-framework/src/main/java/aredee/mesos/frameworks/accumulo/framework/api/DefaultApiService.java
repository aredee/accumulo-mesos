package aredee.mesos.frameworks.accumulo.framework.api;

import javax.ws.rs.core.Response;

public abstract class DefaultApiService {
  
      public abstract Response rootGet()
      throws NotFoundException;
  
}
