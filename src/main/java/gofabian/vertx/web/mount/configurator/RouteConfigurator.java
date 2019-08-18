package gofabian.vertx.web.mount.configurator;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.ext.web.Route;

public interface RouteConfigurator {

    void configure(RouteDefinition routeDefinition, Route route);

}
