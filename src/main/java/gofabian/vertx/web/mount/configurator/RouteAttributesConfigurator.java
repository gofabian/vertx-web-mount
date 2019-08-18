package gofabian.vertx.web.mount.configurator;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.ext.web.Route;

public class RouteAttributesConfigurator implements RouteConfigurator {
    @Override
    public void configure(RouteDefinition routeDefinition, Route route) {
        route.path(routeDefinition.getPath());
        routeDefinition.getMethods().forEach(m -> route.method(m));
        routeDefinition.getConsumes().forEach(route::consumes);
        routeDefinition.getProduces().forEach(route::produces);
    }
}
