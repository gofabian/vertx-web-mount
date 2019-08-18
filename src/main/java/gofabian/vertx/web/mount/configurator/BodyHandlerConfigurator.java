package gofabian.vertx.web.mount.configurator;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.handler.BodyHandler;

public class BodyHandlerConfigurator implements RouteConfigurator {
    @Override
    public void configure(RouteDefinition routeDefinition, Route route) {
        // read request body
        route.handler(BodyHandler.create());
    }
}
