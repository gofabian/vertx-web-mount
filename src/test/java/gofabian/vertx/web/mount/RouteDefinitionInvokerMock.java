package gofabian.vertx.web.mount;

import io.vertx.core.Future;

import java.lang.reflect.Method;

public class RouteDefinitionInvokerMock implements RouteDefinitionInvoker {
    private RouteDefinitionInvoker delegate = (apiSpec, method, args) -> null;

    @Override
    public Future<?> invoke(Object apiDefinition, Method method, Object[] args) throws Exception {
        return delegate.invoke(apiDefinition, method, args);
    }

    public void mockInvoke(RouteDefinitionInvoker delegate) {
        this.delegate = delegate;
    }
}
