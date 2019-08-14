package gofabian.vertx.web.mount.definition;

import java.util.List;

public interface RouteDefinitionFactory {

    List<RouteDefinition> createRouteDefinitions(Object apiDefinition);

}
