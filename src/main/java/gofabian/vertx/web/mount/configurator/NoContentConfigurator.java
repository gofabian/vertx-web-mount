package gofabian.vertx.web.mount.configurator;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.ext.web.Route;

import static io.vertx.core.http.HttpHeaders.CONTENT_LENGTH;

public class NoContentConfigurator implements RouteConfigurator {
    @Override
    public void configure(RouteDefinition routeDefinition, Route route) {
        // status 200 + no response body -> status 204
        route.handler(context -> {
            context.addHeadersEndHandler(x -> {
                if ("0".equals(context.response().headers().get(CONTENT_LENGTH))
                        && context.response().getStatusCode() == 200) {
                    context.response().setStatusCode(204);
                }
            });
            context.next();
        });
    }
}
