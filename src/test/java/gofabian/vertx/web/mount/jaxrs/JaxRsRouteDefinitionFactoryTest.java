package gofabian.vertx.web.mount.jaxrs;

import gofabian.vertx.web.mount.definition.ParamCategory;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class JaxRsRouteDefinitionFactoryTest {

    private JaxRsRouteDefinitionFactory routeFactory = new JaxRsRouteDefinitionFactory();

    @Path("/")
    static class SupportedMethodsDefinition {
        public void noAnnotation() {
        }

        @GET
        public void withHttpMethod() {
        }

        @Path("/path")
        public void noHttpMethod() {
        }

        @GET
        public static void staticMethod() {
        }

        @GET
        void nonPublic() {
        }
    }

    @Test
    public void testSupportedMethods() throws NoSuchMethodException {
        List<RouteDefinition> routeDefinitions = routeFactory.createRouteDefinitions(new SupportedMethodsDefinition());

        assertEquals(1, routeDefinitions.size());
        Method expectedMethod = SupportedMethodsDefinition.class.getMethod("withHttpMethod");
        assertEquals(expectedMethod, routeDefinitions.get(0).getContext());
    }

    @Test
    public void testRouteAttributes() {
        List<RouteDefinition> routeDefinitions = routeFactory.createRouteDefinitions(new Object() {
            @POST
            @PUT
            @Path("/foo")
            @Consumes({"text/plain   ,  text/poetry"})
            @Produces({" application/json  ", "application/xml"})
            public void test() {
            }
        });

        assertEquals(1, routeDefinitions.size());
        assertEquals(
                new HashSet<>(Arrays.asList(HttpMethod.POST, HttpMethod.PUT)),
                new HashSet<>(routeDefinitions.get(0).getMethods())
        );
        assertEquals("/foo", routeDefinitions.get(0).getPath());
        assertEquals(
                new HashSet<>(Arrays.asList("text/plain", "text/poetry")),
                new HashSet<>(routeDefinitions.get(0).getConsumes())
        );
        assertEquals(
                new HashSet<>(Arrays.asList("application/json", "application/xml")),
                new HashSet<>(routeDefinitions.get(0).getProduces())
        );
    }

    @Test
    public void testBodyParameter() {
        List<RouteDefinition> routeDefinitions = routeFactory.createRouteDefinitions(new Object() {
            @PATCH
            @Path("/foo")
            public void test(@DefaultValue("dropped") String body) {
            }
        });

        assertEquals(1, routeDefinitions.size());
        assertEquals(1, routeDefinitions.get(0).getParams().size());
        ParamDefinition paramDefinition = routeDefinitions.get(0).getParams().get(0);
        assertEquals(ParamCategory.BODY, paramDefinition.getCategory());
        assertEquals(String.class, paramDefinition.getType());
        assertTrue(paramDefinition.isMandatory());
        assertNull(paramDefinition.getDefaultValue());
    }

    @Test
    public void testContextParameter() {
        List<RouteDefinition> routeDefinitions = routeFactory.createRouteDefinitions(new Object() {
            @PATCH
            @Path("/foo")
            public void test(@DefaultValue("dropped") @Context int context) {
            }
        });

        assertEquals(1, routeDefinitions.size());
        assertEquals(1, routeDefinitions.get(0).getParams().size());
        ParamDefinition paramDefinition = routeDefinitions.get(0).getParams().get(0);
        assertEquals(ParamCategory.CONTEXT, paramDefinition.getCategory());
        assertEquals(int.class, paramDefinition.getType());
        assertTrue(paramDefinition.isMandatory());
        assertNull(paramDefinition.getDefaultValue());
    }

    @Test
    public void testQueryParameter() {
        List<RouteDefinition> routeDefinitions = routeFactory.createRouteDefinitions(new Object() {
            @PATCH
            @Path("/foo")
            public void test(@QueryParam("p1") byte[] queryParameter) {
            }
        });

        assertEquals(1, routeDefinitions.size());
        assertEquals(1, routeDefinitions.get(0).getParams().size());
        ParamDefinition paramDefinition = routeDefinitions.get(0).getParams().get(0);
        assertEquals(ParamCategory.QUERY, paramDefinition.getCategory());
        assertEquals(byte[].class, paramDefinition.getType());
        assertEquals("p1", paramDefinition.getName());
        assertTrue(paramDefinition.isMandatory());
        assertNull(paramDefinition.getDefaultValue());
    }

    @Test
    public void testQueryParameterWithDefaultValue() {
        List<RouteDefinition> routeDefinitions = routeFactory.createRouteDefinitions(new Object() {
            @PATCH
            @Path("/foo")
            public void test(@QueryParam("p1") @DefaultValue("zip,zap") String p1,
                             @QueryParam("p2") @DefaultValue("single") String p2,
                             @QueryParam("p3") @DefaultValue("") String p3) {
            }
        });

        List<ParamDefinition> paramDefinitions = routeDefinitions.get(0).getParams();
        assertEquals(3, paramDefinitions.size());
        assertFalse(paramDefinitions.get(0).isMandatory());
        assertEquals(Arrays.asList("zip", "zap"), paramDefinitions.get(0).getDefaultValue());
        assertFalse(paramDefinitions.get(1).isMandatory());
        assertEquals(Arrays.asList("single"), paramDefinitions.get(1).getDefaultValue());
        assertFalse(paramDefinitions.get(2).isMandatory());
        assertTrue(paramDefinitions.get(2).getDefaultValue() instanceof List);
        assertTrue(((List) paramDefinitions.get(2).getDefaultValue()).isEmpty());
    }

    @Test
    public void testPathParameter() {
        List<RouteDefinition> routeDefinitions = routeFactory.createRouteDefinitions(new Object() {
            @PATCH
            @Path("/foo")
            public void test(@PathParam("foo") Long parameter) {
            }
        });

        assertEquals(1, routeDefinitions.size());
        assertEquals(1, routeDefinitions.get(0).getParams().size());
        ParamDefinition paramDefinition = routeDefinitions.get(0).getParams().get(0);
        assertEquals(ParamCategory.PATH, paramDefinition.getCategory());
        assertEquals("foo", paramDefinition.getName());
        assertEquals(Long.class, paramDefinition.getType());
        assertTrue(paramDefinition.isMandatory());
        assertNull(paramDefinition.getDefaultValue());
    }

    @Test
    public void testPathParameterWithDefaultValue() {
        List<RouteDefinition> routeDefinitions = routeFactory.createRouteDefinitions(new Object() {
            @PATCH
            @Path("/foo")
            public void test(@PathParam("foo") @DefaultValue("apple") String p1,
                             @PathParam("bar") @DefaultValue("beef") String p2) {
            }
        });

        List<ParamDefinition> paramDefinitions = routeDefinitions.get(0).getParams();
        assertEquals(2, paramDefinitions.size());
        assertFalse(paramDefinitions.get(0).isMandatory());
        assertEquals("apple", paramDefinitions.get(0).getDefaultValue());
        assertFalse(paramDefinitions.get(1).isMandatory());
        assertEquals("beef", paramDefinitions.get(1).getDefaultValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPathParameterWithEmptyName() {
        routeFactory.createRouteDefinitions(new Object() {
            @DELETE
            @Path("/bar")
            public void test(@PathParam("") String p) {
            }
        });
    }

    @Test
    public void testReturnType() {
        List<RouteDefinition> routeDefinitions;

        routeDefinitions = routeFactory.createRouteDefinitions(new Object() {
            @OPTIONS
            @Path("/foo")
            public String test() {
                return null;
            }
        });
        System.out.println(routeDefinitions.get(0).getResponseType());
        assertEquals(String.class, routeDefinitions.get(0).getResponseType());

        routeDefinitions = routeFactory.createRouteDefinitions(new Object() {
            @OPTIONS
            @Path("/foo")
            public void test() {
            }

            @GET
            @Path("/bar")
            public Void test2() {
                return null;
            }
        });
        assertNull(routeDefinitions.get(0).getResponseType());
    }


    // no path (class + method)

}
