package gofabian.vertx.web.mount.security;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
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

        List<Future> futures = new ArrayList<>();
        for (String authority : allowedAuthorities) {
            Future<Boolean> future = Future.future();
            context.user().isAuthorized(authority, future);
            futures.add(future);
        }

        CompositeFuture.all(futures).setHandler(ar -> {
            if (ar.failed()) {
                context.response().setStatusCode(500).end("Internal Server Error");
                return;
            }

            for (Future future : futures) {
                if (Boolean.TRUE.equals(future.result())) {
                    context.next();
                    return;
                }
            }

            context.response().setStatusCode(403).end("Forbidden");
        });
    }
}
