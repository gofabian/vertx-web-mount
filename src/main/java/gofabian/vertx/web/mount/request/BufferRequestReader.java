package gofabian.vertx.web.mount.request;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Type;

public class BufferRequestReader implements RequestReader {
    @Override
    public boolean supports(RoutingContext context, Type type) {
        return type == Buffer.class;
    }

    @Override
    public Object read(RoutingContext context, Type type, RequestReader delegate) {
        return context.getBody();
    }
}
