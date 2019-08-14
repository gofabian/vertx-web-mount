package gofabian.vertx.web.mount;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

public interface RouteDefinitionInvoker {

    Future<Object> invoke(Object apiDefinition, RouteDefinition routeDefinition, RoutingContext context, Object[] args) throws Exception;

}
