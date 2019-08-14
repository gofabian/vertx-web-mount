package gofabian.vertx.web.mount.jaxrs;

import gofabian.vertx.web.mount.RouteDefinitionInvoker;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodRouteDefinitionInvoker implements RouteDefinitionInvoker {

    @Override
    public Future<Object> invoke(Object apiDefinition, RouteDefinition routeDefinition, RoutingContext context, Object[] args) throws Exception {
        assert routeDefinition.getContext() instanceof Method;
        Method method = (Method) routeDefinition.getContext();

        Object result;
        try {
            result = method.invoke(apiDefinition, args);
        } catch (InvocationTargetException e) {
            return Future.failedFuture(e.getCause());
        }

        if (result instanceof Future) {
            //noinspection unchecked
            return (Future<Object>) result;
        } else {
            return Future.succeededFuture(result);
        }
    }

}
