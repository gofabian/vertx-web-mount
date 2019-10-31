package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.annotation.Handle;
import gofabian.vertx.web.mount.annotation.HandleFailure;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.BlockingHandlerDecorator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HandleParserTest {

    private RouteParser handleParser = new HandleParser();
    private ParserTestUtil parserTestUtil = new ParserTestUtil(handleParser, new MountOptions());

    @Test
    public void routeHandlers() {
        RouteDefinition definition = parserTestUtil.parseMethod(Example.class, "method");
        assertEquals(Handler1.class, definition.getRouteHandlers().get(0).getClass());
        assertEquals(Handler2.class, definition.getRouteHandlers().get(1).getClass());
    }

    @Test
    public void failureHandlers() {
        RouteDefinition definition = parserTestUtil.parseMethod(Example.class, "method");
        assertEquals(Handler3.class, definition.getFailureHandlers().get(0).getClass());
        assertEquals(Handler4.class, definition.getFailureHandlers().get(1).getClass());
    }

    @Test
    public void mergeHandlers() {
        RouteDefinition definition = parserTestUtil.parseMethod(MergeExample.class, "method");
        assertEquals(Handler1.class, definition.getRouteHandlers().get(0).getClass());
        assertEquals(Handler3.class, definition.getRouteHandlers().get(1).getClass());
        assertEquals(Handler2.class, definition.getFailureHandlers().get(0).getClass());
        assertEquals(Handler4.class, definition.getFailureHandlers().get(1).getClass());
    }

    @Test
    public void blockingHandler() {
        RouteDefinition definition = parserTestUtil.parseMethod(Example.class, "blocking");
        assertEquals(BlockingHandlerDecorator.class, definition.getRouteHandlers().get(0).getClass());
    }

    interface Example {
        @Handle(Handler1.class)
        @Handle(Handler2.class)
        @HandleFailure(Handler3.class)
        @HandleFailure(Handler4.class)
        void method();

        @Handle(value = Handler1.class, blocking = true)
        void blocking();
    }

    @Handle(Handler1.class)
    @HandleFailure(Handler2.class)
    interface MergeExample {
        @Handle(Handler3.class)
        @HandleFailure(Handler4.class)
        void method();
    }

    static class Handler1 implements Handler<RoutingContext> {
        @Override
        public void handle(RoutingContext event) {
        }
    }

    static class Handler2 implements Handler<RoutingContext> {
        @Override
        public void handle(RoutingContext event) {
        }
    }

    static class Handler3 implements Handler<RoutingContext> {
        @Override
        public void handle(RoutingContext event) {
        }
    }

    static class Handler4 implements Handler<RoutingContext> {
        @Override
        public void handle(RoutingContext event) {
        }
    }


}
