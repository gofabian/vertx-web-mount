package gofabian.vertx.web.mount;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import static io.vertx.core.http.HttpHeaders.CONTENT_LENGTH;

public class NoContentHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        context.addHeadersEndHandler(x -> {
            if ("0".equals(context.response().headers().get(CONTENT_LENGTH))
                    && context.response().getStatusCode() == 200) {
                context.response().setStatusCode(204);
            }
        });
        context.next();
    }
}
