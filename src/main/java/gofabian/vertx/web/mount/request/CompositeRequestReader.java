package gofabian.vertx.web.mount.request;

import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class CompositeRequestReader implements RequestReader {
    private final List<RequestReader> requestReaders;

    public CompositeRequestReader(List<RequestReader> requestReaders) {
        this.requestReaders = Objects.requireNonNull(requestReaders);
    }

    @Override
    public boolean supports(RoutingContext context, Type type) {
        for (RequestReader reader : requestReaders) {
            if (reader.supports(context, type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object read(RoutingContext context, Type type, RequestReader delegate) {
        for (RequestReader reader : requestReaders) {
            if (reader.supports(context, type)) {
                return reader.read(context, type, delegate);
            }
        }
        throw new IllegalArgumentException("Unsupported request");
    }
}
