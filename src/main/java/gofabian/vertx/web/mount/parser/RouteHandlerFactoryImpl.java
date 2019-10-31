package gofabian.vertx.web.mount.parser;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class RouteHandlerFactoryImpl implements RouteHandlerFactory {

    public Handler<RoutingContext> createHandler(Class<? extends Handler<RoutingContext>> handlerClass, String handlerName) {
        try {
            return handlerClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Constructor without parameter required", e);
        }
    }

}
