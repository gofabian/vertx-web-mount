package gofabian.vertx.web.mount.request;

import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Type;

public class FallbackRequestReader implements RequestReader {

    private final StringReader stringReader = new StringReader();

    @Override
    public boolean supports(RoutingContext context, Type type) {
        return true;
    }

    @Override
    public Object read(RoutingContext context, Type type, RequestReader delegate) {
        String string = context.getBodyAsString();
        return stringReader.read(string, type);
    }

}
