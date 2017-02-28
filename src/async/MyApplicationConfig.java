package async;

import javax.websocket.*;
import javax.websocket.server.*;
import java.util.*;



public class MyApplicationConfig implements ServerApplicationConfig {

  @Override
  public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> set) {
    return new HashSet<ServerEndpointConfig>() {
      {
        add(ServerEndpointConfig.Builder
            .create(MyWsHandler.class, "/websocket")
            .build());
      }
    };
  }

  @Override
  public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> set) {
    return Collections.emptySet();
  }
}