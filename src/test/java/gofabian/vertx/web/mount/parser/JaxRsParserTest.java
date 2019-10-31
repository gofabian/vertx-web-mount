package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.definition.ParamCategory;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class JaxRsParserTest {

    private RouteParser jaxRsParser = new JaxRsParser();
    private ParserTestUtil parserTestUtil = new ParserTestUtil(jaxRsParser, new MountOptions());

    @Test
    public void parseContentType() {
        {
            RouteDefinition definition = parserTestUtil.parseMethod(Example.class, "maximal");
            assertEquals(Arrays.asList("application/json"), definition.getConsumes());
        }
        {
            RouteDefinition definition = parserTestUtil.parseMethod(MergeExample.class, "merged");
            assertEquals(Arrays.asList("application/json"), definition.getConsumes());
        }
        {
            RouteDefinition definition = parserTestUtil.parseMethod(MergeExample.class, "inherited");
            assertEquals(Arrays.asList("text/plain"), definition.getConsumes());
        }
    }

    @Test
    public void parseAcceptHeader() {
        {
            RouteDefinition definition = parserTestUtil.parseMethod(Example.class, "maximal");
            assertEquals(Arrays.asList("text/plain"), definition.getProduces());
        }
        {
            RouteDefinition definition = parserTestUtil.parseMethod(MergeExample.class, "merged");
            assertEquals(Arrays.asList("text/xml"), definition.getProduces());
        }
        {
            RouteDefinition definition = parserTestUtil.parseMethod(MergeExample.class, "inherited");
            assertEquals(Arrays.asList("text/json"), definition.getProduces());
        }
    }

    @Test
    public void parsePath() {
        {
            RouteDefinition definition = parserTestUtil.parseMethod(Example.class, "maximal");
            assertEquals("/json", definition.getPath());
        }
        {
            RouteDefinition definition = parserTestUtil.parseMethod(MergeExample.class, "merged");
            assertEquals("/top/json", definition.getPath());
        }
        {
            RouteDefinition definition = parserTestUtil.parseMethod(MergeExample.class, "inherited");
            assertEquals("/top", definition.getPath());
        }
    }

    @Test
    public void parseHttpMethod() {
        {
            RouteDefinition definition = parserTestUtil.parseMethod(MergeExample.class, "merged");
            assertEquals(Arrays.asList(HttpMethod.GET), definition.getMethods());
        }
        {
            RouteDefinition definition = parserTestUtil.parseMethod(MergeExample.class, "inherited");
            assertEquals(
                    new HashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE)),
                    new HashSet<>(definition.getMethods())
            );
        }
    }

    interface Example {
        @GET
        @Path("/json")
        @Consumes("application/json")
        @Produces("text/plain")
        void maximal();
    }

    @Path("/top")
    @Consumes("text/plain")
    @Produces("text/json")
    interface MergeExample {
        @GET
        @Path("/json")
        @Consumes("application/json")
        @Produces("text/xml")
        void merged();

        @PUT
        @DELETE
        void inherited();
    }


    @Test
    public void parseBodyParameter() {
        ParamDefinition definition = parserTestUtil.parseParameter("body");
        assertEquals(ParamCategory.BODY, definition.getCategory());
        assertEquals(byte[].class, definition.getType());
        assertTrue(definition.isMandatory());
    }

    @Test
    public void parseContextParameter() {
        ParamDefinition definition = parserTestUtil.parseParameter("context");
        assertEquals(ParamCategory.CONTEXT, definition.getCategory());
        assertEquals(Long.class, definition.getType());
        assertTrue(definition.isMandatory());
    }

    @Test
    public void parseQueryParameter() {
        ParamDefinition definition = parserTestUtil.parseParameter("query");
        assertEquals(ParamCategory.QUERY, definition.getCategory());
        assertEquals(String.class, definition.getType());
        assertTrue(definition.isMandatory());
        assertEquals("key", definition.getName());
    }

    @Test
    public void parseQueryListParameter() {
        ParamDefinition definition = parserTestUtil.parseParameter("queryList");
        assertTrue(definition.getType() instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType) definition.getType()).getRawType());
        assertEquals(Boolean.class, ((ParameterizedType) definition.getType()).getActualTypeArguments()[0]);
    }

    @Test
    public void parseHeaderParameter() {
        ParamDefinition definition = parserTestUtil.parseParameter("header");
        assertEquals(ParamCategory.HEADER, definition.getCategory());
        assertEquals(short.class, definition.getType());
        assertTrue(definition.isMandatory());
        assertEquals("short", definition.getName());
    }

    @Test
    public void parseFormParameter() {
        ParamDefinition definition = parserTestUtil.parseParameter("form");
        assertEquals(ParamCategory.FORM, definition.getCategory());
        assertEquals(String.class, definition.getType());
        assertTrue(definition.isMandatory());
        assertEquals("form", definition.getName());
    }

    @Test
    public void parsePathParameter() {
        ParamDefinition definition = parserTestUtil.parseParameter("path");
        assertEquals(ParamCategory.PATH, definition.getCategory());
        assertEquals(float.class, definition.getType());
        assertTrue(definition.isMandatory());
        assertEquals("path", definition.getName());
    }

    @Test
    public void parseDefaultValue() {
        ParamDefinition definition = parserTestUtil.parseParameter("defaultValue");
        assertFalse(definition.isMandatory());
        assertEquals("42", definition.getDefaultValue());
    }

    @Test
    public void parseDefaultValueList() {
        ParamDefinition definition = parserTestUtil.parseParameter("defaultValueList");
        assertFalse(definition.isMandatory());
        assertEquals(Arrays.asList("42", "value"), definition.getDefaultValue());
    }

    @SuppressWarnings("unused")
    interface ParameterExample {
        void body(byte[] a);

        void context(@Context Long a);

        void query(@QueryParam("key") String a);

        void queryList(@QueryParam("list") List<Boolean> a);

        void header(@HeaderParam("short") short a);

        void form(@FormParam("form") String a);

        void path(@PathParam("path") float a);

        void defaultValue(@PathParam("d") @DefaultValue("42") int a);

        void defaultValueList(@QueryParam("d") @DefaultValue("42,value") int a);
    }

}
