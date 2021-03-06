package gofabian.vertx.web.mount.invoker;

import io.vertx.core.Future;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RouteInvokerImpl implements RouteInvoker {
    @Override
    public Future<?> invoke(Object apiDefinition, Method method, Object[] args) throws Exception {
        Object result;
        try {
            result = method.invoke(apiDefinition, args);
        } catch (InvocationTargetException e) {
            return Future.failedFuture(e.getCause());
        }

        if (result instanceof Future) {
            return (Future) result;
        } else {
            return Future.succeededFuture(result);
        }
    }
}
