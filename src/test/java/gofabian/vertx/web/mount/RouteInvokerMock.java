package gofabian.vertx.web.mount;

import io.vertx.core.Future;

import java.lang.reflect.Method;

public class RouteInvokerMock implements RouteInvoker {
    private RouteInvoker delegate = (apiSpec, method, args) -> null;

    @Override
    public Future<?> invoke(Object apiDefinition, Method method, Object[] args) throws Exception {
        return delegate.invoke(apiDefinition, method, args);
    }

    public void mockInvoke(RouteInvoker delegate) {
        this.delegate = delegate;
    }
}
