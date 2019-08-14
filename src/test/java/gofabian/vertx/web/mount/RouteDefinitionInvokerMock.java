package gofabian.vertx.web.mount;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

public class RouteDefinitionInvokerMock implements RouteDefinitionInvoker {
    private RouteDefinitionInvoker delegate = (apiSpec, routeSpec, context, args) -> null;

    @Override
    public Future<Object> invoke(Object apiDefinition, RouteDefinition routeDefinition, RoutingContext context, Object[] args) throws Exception {
        return delegate.invoke(apiDefinition, routeDefinition, context, args);
    }

    public void mockInvoke(RouteDefinitionInvoker delegate) {
        this.delegate = delegate;
    }
}
