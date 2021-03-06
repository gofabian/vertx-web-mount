package gofabian.vertx.web.mount.invoker;

import io.vertx.core.Future;

import java.lang.reflect.Method;

public interface RouteInvoker {
    Future<?> invoke(Object apiDefinition, Method method, Object[] args) throws Exception;
}
