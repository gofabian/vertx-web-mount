package gofabian.vertx.web.mount.parser;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface RouteHandlerFactory {

    Handler<RoutingContext> createHandler(Class<? extends Handler<RoutingContext>> handlerClass, String handlerName);

}
