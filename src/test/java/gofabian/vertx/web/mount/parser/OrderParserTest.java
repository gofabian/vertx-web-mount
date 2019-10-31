package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.annotation.Order;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrderParserTest {

    private RouteParser orderParser = new OrderParser();
    private ParserTestUtil parserTestUtil = new ParserTestUtil(orderParser, new MountOptions());

    @Test
    public void parseOrder() {
        RouteDefinition definition = parserTestUtil.parseMethod(Example.class, "method");
        assertEquals(1337, definition.getOrder().intValue());
    }

    @SuppressWarnings("unused")
    interface Example {
        @Order(1337)
        void method();
    }

}
