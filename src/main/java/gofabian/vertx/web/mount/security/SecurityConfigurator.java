package gofabian.vertx.web.mount.security;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.configurator.RouteConfigurator;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.ext.web.Route;

public class SecurityConfigurator implements RouteConfigurator {
    @Override
    public void configure(RouteDefinition routeDefinition, Route route, MountOptions options) {
        if (options.isAuthenticationRequired()) {
            if (!Boolean.FALSE.equals(routeDefinition.isAuthenticationRequired())) {
                // authentication required by default + required flag not explicitly unset
                route.handler(new AuthenticationRequiredHandler());
            }
        } else if (Boolean.TRUE.equals(routeDefinition.isAuthenticationRequired())) {
            // default: no authentication required + required flag explicitly set
            route.handler(new AuthenticationRequiredHandler());
        }

        if (routeDefinition.isAuthenticationRequired() != null && routeDefinition.isAuthenticationRequired()) {
            route.handler(new AuthenticationRequiredHandler());
        }
        if (!routeDefinition.getAllowedAuthorities().isEmpty()) {
            route.handler(new AllowedAuthoritiesHandler(routeDefinition.getAllowedAuthorities()));
        }
        if (!routeDefinition.getRequiredAuthorities().isEmpty()) {
            route.handler(new RequiredAuthoritiesHandler(routeDefinition.getRequiredAuthorities()));
        }
    }
}
