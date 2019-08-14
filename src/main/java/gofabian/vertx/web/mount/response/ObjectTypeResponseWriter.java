package gofabian.vertx.web.mount.response;

import io.vertx.ext.web.RoutingContext;

public class ObjectTypeResponseWriter implements ResponseWriter {
    @Override
    public boolean write(RoutingContext context, Object result, ResponseWriter delegate) {
        if (result == null) {
            return false;
        }

        context.response().end(result.toString());
        return true;
    }
}
