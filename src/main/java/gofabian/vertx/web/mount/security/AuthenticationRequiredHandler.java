package gofabian.vertx.web.mount.security;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class AuthenticationRequiredHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        if (context.user() == null) {
            context.response().setStatusCode(401).end("Not Authorized");
        } else {
            context.next();
        }
    }
}
