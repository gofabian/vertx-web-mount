package gofabian.vertx.web.mount.security;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class AllowedAuthoritiesHandler implements Handler<RoutingContext> {
    private final List<String> allowedAuthorities;

    public AllowedAuthoritiesHandler(List<String> allowedAuthorities) {
        this.allowedAuthorities = allowedAuthorities;
    }

    @Override
    public void handle(RoutingContext context) {
        if (context.user() == null) {
            context.response().setStatusCode(401).end("Not Authorized");
            return;
        }

        AuthoritiesHelper.hasUserAnyAuthority(context.user(), allowedAuthorities).setHandler(ar -> {
            if (ar.failed()) {
                context.response().setStatusCode(500).end("Internal Server Error");
                return;
            }

            boolean isAuthorized = ar.result();
            if (isAuthorized) {
                context.next();
            } else {
                context.response().setStatusCode(403).end("Forbidden");
            }
        });
    }
}
