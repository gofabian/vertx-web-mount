package gofabian.vertx.web.mount.validation;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import javax.validation.ConstraintViolationException;

public class ConstraintViolationHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        Throwable t = context.failure();

        if (t instanceof ConstraintViolationException) {
            context.response()
                    .setStatusCode(400)
                    .end(t.getMessage());
        } else {
            context.next();
        }
    }
}
