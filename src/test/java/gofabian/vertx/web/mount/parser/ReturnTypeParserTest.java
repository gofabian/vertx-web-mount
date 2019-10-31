package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ReturnTypeParserTest {

    private RouteParser returnTypeParser = new ReturnTypeParser();
    private ParserTestUtil parserTestUtil = new ParserTestUtil(returnTypeParser, new MountOptions());

    @Test
    public void parseVoid() {
        {
            RouteDefinition definition = parserTestUtil.parseMethod(Example.class, "voidPrimitive");
            assertNull(definition.getResponseType());
        }
        {
            RouteDefinition definition = parserTestUtil.parseMethod(Example.class, "voidType");
            assertNull(definition.getResponseType());
        }
    }

    @Test
    public void parseType() {
        RouteDefinition definition = parserTestUtil.parseMethod(Example.class, "type");
        assertEquals(String.class, definition.getResponseType());
    }

    @SuppressWarnings("unused")
    interface Example {
        void voidPrimitive();

        Void voidType();

        String type();
    }

}
