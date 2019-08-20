package gofabian.vertx.web.mount.validation;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.configurator.RouteConfigurator;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.ext.web.Route;

public class ConstraintViolationHandlerConfigurator implements RouteConfigurator {
    @Override
    public void configure(RouteDefinition routeDefinition, Route route, MountOptions options) {
        route.failureHandler(new ConstraintViolationHandler());
    }
}
