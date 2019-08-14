package gofabian.vertx.web.mount.response;

import io.vertx.ext.web.RoutingContext;

public interface ResponseWriter {

    boolean write(RoutingContext context, Object result, ResponseWriter delegate);

}
