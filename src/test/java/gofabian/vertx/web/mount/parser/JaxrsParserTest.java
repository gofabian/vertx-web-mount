package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.jaxrs.JaxrsParser;
import gofabian.vertx.web.mount.definition.ParamCategory;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class JaxrsParserTest {

    @Path("/auth")
    @Consumes("application/json")
    @Produces("application/json")
    interface Api {
        @POST
        @PUT
        @Path("/login")
        @Consumes("text/plain")
        @Produces("text/plain")
        String login(@DefaultValue("unsupported") String body);

        void context(@Context @DefaultValue("unsupported") Api context);

        void path(@PathParam("id") @DefaultValue("42") long id);

        void query(@QueryParam("name") @DefaultValue("foo,bar") List<String> names);

        void queryEmptyDefaultValue(@QueryParam("name") @DefaultValue(" ") String name);
    }

    private final JaxrsParser parser = new JaxrsParser();
    private final ParseOptions options = new ParseOptions();

    @Test
    public void visitClass() {
        RouteDefinition routeDefinition = new RouteDefinition();
        parser.visitClass(Api.class, routeDefinition, options);

        assertTrue(routeDefinition.getMethods().isEmpty());
        assertEquals("/auth", routeDefinition.getPath());
        assertEquals(Arrays.asList("application/json"), routeDefinition.getProduces());
        assertEquals(Arrays.asList("application/json"), routeDefinition.getConsumes());
    }

    @Test
    public void visitMethod() throws NoSuchMethodException {
        RouteDefinition routeDefinition = new RouteDefinition();
        parser.visitMethod(Api.class.getMethod("login", String.class), routeDefinition, options);

        assertEquals(Arrays.asList(HttpMethod.POST, HttpMethod.PUT), routeDefinition.getMethods());
        assertEquals("/login", routeDefinition.getPath());
        assertEquals(Arrays.asList("text/plain"), routeDefinition.getProduces());
        assertEquals(Arrays.asList("text/plain"), routeDefinition.getConsumes());
    }

    @Test
    public void mergeOverwriteClass() {
        RouteDefinition classDefinition = new RouteDefinition()
                .setPath("/foo")
                .setProduces(Arrays.asList("text/plain"))
                .setConsumes(Arrays.asList("text/xml"));
        RouteDefinition methodDefinition = new RouteDefinition()
                .setMethods(Arrays.asList(HttpMethod.DELETE))
                .setPath("/bar")
                .setProduces(Arrays.asList("text/json"))
                .setConsumes(Arrays.asList("text/json"));

        RouteDefinition routeDefinition = new RouteDefinition();
        parser.merge(classDefinition, methodDefinition, routeDefinition);

        assertEquals(Arrays.asList(HttpMethod.DELETE), routeDefinition.getMethods());
        assertEquals("/foo/bar", routeDefinition.getPath());
        assertEquals(Arrays.asList("text/json"), routeDefinition.getProduces());
        assertEquals(Arrays.asList("text/json"), routeDefinition.getConsumes());
    }

    @Test
    public void mergeNoOverwrite() {
        RouteDefinition classDefinition = new RouteDefinition()
                .setPath("/foo")
                .setProduces(Arrays.asList("text/plain"))
                .setConsumes(Arrays.asList("text/xml"));
        RouteDefinition methodDefinition = new RouteDefinition()
                .setMethods(Arrays.asList(HttpMethod.DELETE));

        RouteDefinition routeDefinition = new RouteDefinition();
        parser.merge(classDefinition, methodDefinition, routeDefinition);

        assertEquals(Arrays.asList(HttpMethod.DELETE), routeDefinition.getMethods());
        assertEquals("/foo", routeDefinition.getPath());
        assertEquals(Arrays.asList("text/plain"), routeDefinition.getProduces());
        assertEquals(Arrays.asList("text/xml"), routeDefinition.getConsumes());
    }

    @Test
    public void mergeEmptyClassDefinition() {
        RouteDefinition classDefinition = new RouteDefinition();
        RouteDefinition methodDefinition = new RouteDefinition()
                .setMethods(Arrays.asList(HttpMethod.DELETE))
                .setPath("/bar")
                .setProduces(Arrays.asList("text/json"))
                .setConsumes(Arrays.asList("text/json"));

        RouteDefinition routeDefinition = new RouteDefinition();
        parser.merge(classDefinition, methodDefinition, routeDefinition);

        assertEquals(Arrays.asList(HttpMethod.DELETE), routeDefinition.getMethods());
        assertEquals("/bar", routeDefinition.getPath());
        assertEquals(Arrays.asList("text/json"), routeDefinition.getProduces());
        assertEquals(Arrays.asList("text/json"), routeDefinition.getConsumes());
    }

    @Test
    public void visitBodyParameter() throws NoSuchMethodException {
        Parameter parameter = Api.class.getMethod("login", String.class).getParameters()[0];
        ParamDefinition paramDefinition = new ParamDefinition();
        parser.visitParameter(parameter, paramDefinition, options);

        assertEquals(ParamCategory.BODY, paramDefinition.getCategory());
        assertEquals(String.class, paramDefinition.getType());
        assertNull(paramDefinition.getName());
        assertNull(paramDefinition.getDefaultValue());
        assertTrue(paramDefinition.isMandatory());
    }

    @Test
    public void visitContextParameter() throws NoSuchMethodException {
        Parameter parameter = Api.class.getMethod("context", Api.class).getParameters()[0];
        ParamDefinition paramDefinition = new ParamDefinition();
        parser.visitParameter(parameter, paramDefinition, options);

        assertEquals(ParamCategory.CONTEXT, paramDefinition.getCategory());
        assertEquals(Api.class, paramDefinition.getType());
        assertNull(paramDefinition.getName());
        assertNull(paramDefinition.getDefaultValue());
        assertTrue(paramDefinition.isMandatory());
    }

    @Test
    public void visitPathParameter() throws NoSuchMethodException {
        Parameter parameter = Api.class.getMethod("path", long.class).getParameters()[0];
        ParamDefinition paramDefinition = new ParamDefinition();
        parser.visitParameter(parameter, paramDefinition, options);

        assertEquals(ParamCategory.PATH, paramDefinition.getCategory());
        assertEquals(long.class, paramDefinition.getType());
        assertEquals("id", paramDefinition.getName());
        assertEquals("42", paramDefinition.getDefaultValue());
        assertFalse(paramDefinition.isMandatory());
    }

    @Test
    public void visitQueryParameter() throws NoSuchMethodException {
        Parameter parameter = Api.class.getMethod("query", List.class).getParameters()[0];
        ParamDefinition paramDefinition = new ParamDefinition();
        parser.visitParameter(parameter, paramDefinition, options);

        assertEquals(ParamCategory.QUERY, paramDefinition.getCategory());
        assertTrue(paramDefinition.getType() instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType) paramDefinition.getType()).getRawType());
        assertEquals(String.class, ((ParameterizedType) paramDefinition.getType()).getActualTypeArguments()[0]);
        assertEquals("name", paramDefinition.getName());
        assertEquals(Arrays.asList("foo", "bar"), paramDefinition.getDefaultValue());
        assertFalse(paramDefinition.isMandatory());
    }

    @Test
    public void visitQueryParameterWithEmptyDefaultValue() throws NoSuchMethodException {
        Parameter parameter = Api.class.getMethod("queryEmptyDefaultValue", String.class).getParameters()[0];
        ParamDefinition paramDefinition = new ParamDefinition();
        parser.visitParameter(parameter, paramDefinition, options);

        assertEquals(ParamCategory.QUERY, paramDefinition.getCategory());
        assertEquals(String.class, paramDefinition.getType());
        assertEquals("name", paramDefinition.getName());
        assertTrue(((List) paramDefinition.getDefaultValue()).isEmpty());
        assertFalse(paramDefinition.isMandatory());
    }

}
