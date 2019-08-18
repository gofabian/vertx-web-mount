package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.RouteDefinitionInvokerImpl;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.Future;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class RouteDefinitionInvokerImplTest {

    private RouteDefinitionInvokerImpl invoker = new RouteDefinitionInvokerImpl();

    @Test
    public void noFutureReturnType() throws Exception {
        Method method = getClass().getMethod("noFuture");
        RouteDefinition routeDefinition = new RouteDefinition().setContext(method);
        Future<Object> future = invoker.invoke(this, routeDefinition, null, new Object[0]);
        assertEquals("noFuture", future.result());
    }

    @Test
    public void futureReturnType() throws Exception {
        Method method = getClass().getMethod("future");
        RouteDefinition routeDefinition = new RouteDefinition().setContext(method);
        Future<Object> future = invoker.invoke(this, routeDefinition, null, new Object[0]);
        assertEquals("future", future.result());
    }

    @Test
    public void throwingError() throws Exception {
        Method method = getClass().getMethod("throwing");
        RouteDefinition routeDefinition = new RouteDefinition().setContext(method);
        Future<Object> future = invoker.invoke(this, routeDefinition, null, new Object[0]);
        assertEquals(IllegalArgumentException.class, future.cause().getClass());
        assertEquals("dummy", future.cause().getMessage());
    }

    public String noFuture() {
        return "noFuture";
    }

    public Future<String> future() {
        return Future.succeededFuture("future");
    }

    public String throwing() {
        throw new IllegalArgumentException("dummy");
    }

}