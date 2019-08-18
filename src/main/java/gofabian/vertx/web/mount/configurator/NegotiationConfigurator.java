package gofabian.vertx.web.mount.configurator;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

public class NegotiationConfigurator implements RouteConfigurator {
    @Override
    public void configure(RouteDefinition routeDefinition, Route route, MountOptions options) {
        // set negotiated content-type
        route.handler(ResponseContentTypeHandler.create());

        // set acceptable content-type fallback
        if (!routeDefinition.getProduces().isEmpty()) {
            String fallbackContentType = routeDefinition.getProduces().get(0);
            route.handler(context -> {
                if (context.getAcceptableContentType() == null) {
                    context.setAcceptableContentType(fallbackContentType);
                }
                context.next();
            });
        }
    }
}
