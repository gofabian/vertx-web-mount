package gofabian.vertx.web.mount.request;

import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Type;

public interface RequestReader {

    boolean supports(RoutingContext context, Type type);

    Object read(RoutingContext context, Type type, RequestReader delegate);

}
