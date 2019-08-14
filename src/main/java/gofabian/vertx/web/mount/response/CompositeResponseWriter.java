package gofabian.vertx.web.mount.response;

import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class CompositeResponseWriter implements ResponseWriter {

    private final List<ResponseWriter> writers;

    public CompositeResponseWriter(List<ResponseWriter> writers) {
        this.writers = writers;
    }

    @Override
    public boolean write(RoutingContext context, Object result, ResponseWriter delegate) {
        for (ResponseWriter writer : writers) {
            boolean written = writer.write(context, result, delegate);
            if (written) {
                return true;
            }
        }
        return false;
    }

}
