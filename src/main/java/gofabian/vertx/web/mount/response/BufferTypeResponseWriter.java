package gofabian.vertx.web.mount.response;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

public class BufferTypeResponseWriter implements ResponseWriter {
    @Override
    public boolean write(RoutingContext context, Object result, ResponseWriter delegate) {
        if (result instanceof Buffer) {
            context.response().end((Buffer) result);
            return true;
        }
        return false;
    }
}
