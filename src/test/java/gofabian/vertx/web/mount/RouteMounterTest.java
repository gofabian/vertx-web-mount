package gofabian.vertx.web.mount;

import gofabian.vertx.web.mount.definition.ParamCategory;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import gofabian.vertx.web.mount.param.*;
import gofabian.vertx.web.mount.request.*;
import gofabian.vertx.web.mount.response.*;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(VertxUnitRunner.class)
@SuppressWarnings("BeforeOrAfterWithIncorrectSignature")
public class RouteMounterTest {

    private static Method runMethod;

    static {
        try {
            runMethod = Runnable.class.getMethod("run");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private Vertx vertx;
    private int port;
    private Router router;
    private RouteInvokerMock apiInvokerMock;
    private RouteMounter routeMounter;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        router = Router.router(vertx);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(0, context.asyncAssertSuccess(httpServer -> {
                    port = httpServer.actualPort();
                }));

        apiInvokerMock = new RouteInvokerMock();

        RequestReader requestReader = new CompositeRequestReader(Arrays.asList(
                new JsonRequestReader(),
                new BufferRequestReader(),
                new FallbackRequestReader()
        ));
        List<ParamProviderFactory> paramProviderFactories = Arrays.asList(
                new VertxParamProviderFactory(),
                new RoutingContextParamProviderFactory(),
                new PathParamProviderFactory(),
                new ListParamProviderFactory(),
                new BodyParamProviderFactory(requestReader)
        );
        ResponseWriter responseWriter = new CompositeResponseWriter(Arrays.asList(
                new ResponseEntityTypeResponseWriter(),
                new BufferTypeResponseWriter(),
                new JsonResponseWriter(),
                new TextResponseWriter(),
                new ObjectTypeResponseWriter()
        ));
        List<Handler<RoutingContext>> routeHandlers = Arrays.asList(
                BodyHandler.create(),
                ResponseContentTypeHandler.create()
        );
        routeMounter = new RouteMounter(apiInvokerMock, responseWriter, paramProviderFactories,
                routeHandlers);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testApiInvoker(TestContext context) {
        ParamDefinition paramDefinition = new ParamDefinition()
                .setCategory(ParamCategory.BODY)
                .setType(String.class);
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.POST))
                .setPath("/post")
                .setParams(Arrays.asList(paramDefinition))
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture(apiSpec + "+" + method.getName() + "+" + args[0]);
        });

        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/post")
                .expect(ResponsePredicate.SC_OK)
                .sendBuffer(Buffer.buffer("zero"), context.asyncAssertSuccess(response -> {
                    assertEquals("api-spec+run+zero", response.bodyAsString());
                }));
    }

    @Test
    public void testApiInvokerThrowsException(TestContext context) {
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/get")
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            throw new Exception("severe");
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/get")
                .expect(ResponsePredicate.SC_INTERNAL_SERVER_ERROR)
                .send(context.asyncAssertSuccess());
    }

    @Test
    public void testApiInvokerFails(TestContext context) {
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.PUT))
                .setPath("/put")
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.failedFuture("fail");
        });

        WebClient.create(vertx)
                .put(port, "127.0.0.1", "/put")
                .expect(ResponsePredicate.SC_INTERNAL_SERVER_ERROR)
                .send(context.asyncAssertSuccess());
    }

    @Test
    public void testToStringResponseWriter(TestContext context) {
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/")
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        Object result = new Object();
        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture(result);
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/")
                .expect(ResponsePredicate.SC_OK)
                .send(context.asyncAssertSuccess(response -> {
                    assertEquals(result.toString(), response.bodyAsString());
                }));
    }

    @Test
    public void testNullResult(TestContext context) {
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/")
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture("");
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .send(context.asyncAssertSuccess(response -> {
                    assertNull(response.body());
                }));
    }

    @Test
    public void testRoutingContextParameter(TestContext context) {
        ParamDefinition paramDefinition = new ParamDefinition().setType(RoutingContext.class);
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/")
                .setParams(Arrays.asList(paramDefinition))
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            assertTrue(args[0] instanceof RoutingContext);
            return Future.succeededFuture();
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .send(context.asyncAssertSuccess());
    }

    @Test
    public void testReadStringTypeWithTextContentType(TestContext context) {
        ParamDefinition paramDefinition = new ParamDefinition()
                .setCategory(ParamCategory.BODY)
                .setType(String.class);
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.POST))
                .setPath("/")
                .setParams(Arrays.asList(paramDefinition))
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture(args[0]);
        });

        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/")
                .putHeader("content-type", "text/awesome")
                .expect(ResponsePredicate.SC_SUCCESS)
                .sendBuffer(Buffer.buffer("tixt"), context.asyncAssertSuccess(response -> {
                    assertEquals("tixt", response.bodyAsString());
                }));
    }

    @Test
    public void testReadJsonContentType(TestContext context) {
        ParamDefinition paramDefinition = new ParamDefinition()
                .setCategory(ParamCategory.BODY)
                .setType(Map.class);
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.POST))
                .setPath("/")
                .setParams(Arrays.asList(paramDefinition))
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture(((Map) args[0]).get("k"));
        });

        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/")
                .putHeader("content-type", "application/json")
                .expect(ResponsePredicate.SC_SUCCESS)
                .sendJsonObject(new JsonObject().put("k", "v"), context.asyncAssertSuccess(response -> {
                    assertEquals("v", response.bodyAsString());
                }));
    }

    @Test
    public void testReadStringType(TestContext context) {
        ParamDefinition paramDefinition = new ParamDefinition()
                .setCategory(ParamCategory.BODY)
                .setType(String.class);
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.POST))
                .setPath("/")
                .setParams(Arrays.asList(paramDefinition))
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture(args[0]);
        });

        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/")
                .putHeader("content-type", "unknown/unknown")
                .expect(ResponsePredicate.SC_SUCCESS)
                .sendBuffer(Buffer.buffer("pafff"), context.asyncAssertSuccess(response -> {
                    assertEquals("pafff", response.bodyAsString());
                }));
    }

    @Test
    public void testWriteBufferType(TestContext context) {
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/")
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture(Buffer.buffer("peng"));
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .send(context.asyncAssertSuccess(response -> {
                    assertEquals("peng", response.bodyAsString());
                }));
    }

    @Test
    public void testWriteJsonContentType(TestContext context) {
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/")
                .setProduces(Arrays.asList("application/json"))
                .setResponseType(Map.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture(Collections.singletonMap("a", "b"));
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/")
                .putHeader("accept", "application/json")
                .expect(ResponsePredicate.SC_SUCCESS)
                .expect(ResponsePredicate.contentType("application/json"))
                .send(context.asyncAssertSuccess(response -> {
                    assertEquals(new JsonObject().put("a", "b"), response.bodyAsJsonObject());
                }));
    }

    @Test
    public void testWriteTextContentType(TestContext context) {
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/")
                .setProduces(Arrays.asList("text/ugly"))
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture("ugly text");
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/")
                .putHeader("accept", "text/ugly")
                .expect(ResponsePredicate.SC_SUCCESS)
                .send(context.asyncAssertSuccess(response -> {
                    assertEquals("ugly text", response.bodyAsString());
                }));
    }

    @Test
    public void testWriteObjectType(TestContext context) {
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/")
                .setResponseType(Object.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        Object result = new Object();
        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture(result);
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .send(context.asyncAssertSuccess(response -> {
                    assertEquals(result.toString(), response.bodyAsString());
                }));
    }

    @Test
    public void testWriteResponseEntityType(TestContext context) {
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/")
                .setResponseType(ResponseEntity.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture(ResponseEntity.created("42"));
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/")
                .expect(ResponsePredicate.SC_CREATED)
                .send(context.asyncAssertSuccess(response -> {
                    assertEquals("42", response.bodyAsString());
                }));
    }

    @Test
    public void testNegotiationFallback(TestContext context) {
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/")
                .setProduces(Arrays.asList("text/zonk"))
                .setResponseType(ResponseEntity.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture("response");
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/")
                .putHeader("Accept", "")
                .expect(ResponsePredicate.SC_SUCCESS)
                .expect(ResponsePredicate.contentType("text/zonk"))
                .send(context.asyncAssertSuccess(response -> {
                    assertEquals("response", response.bodyAsString());
                }));
    }

    @Test
    public void testReadNumberType(TestContext context) {
        List<ParamDefinition> paramDefinitions = Arrays.asList(
                new ParamDefinition().setCategory(ParamCategory.BODY).setType(int.class),
                new ParamDefinition().setCategory(ParamCategory.BODY).setType(Integer.class),
                new ParamDefinition().setCategory(ParamCategory.BODY).setType(short.class),
                new ParamDefinition().setCategory(ParamCategory.BODY).setType(Short.class),
                new ParamDefinition().setCategory(ParamCategory.BODY).setType(long.class),
                new ParamDefinition().setCategory(ParamCategory.BODY).setType(Long.class),
                new ParamDefinition().setCategory(ParamCategory.BODY).setType(double.class),
                new ParamDefinition().setCategory(ParamCategory.BODY).setType(Double.class),
                new ParamDefinition().setCategory(ParamCategory.BODY).setType(float.class),
                new ParamDefinition().setCategory(ParamCategory.BODY).setType(Float.class)
        );
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.POST))
                .setPath("/")
                .setParams(paramDefinitions)
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            List<String> strings = Arrays.stream(args)
                    .map(a -> a.getClass().getSimpleName() + "=" + ((Number) a).intValue())
                    .collect(Collectors.toList());
            return Future.succeededFuture(strings);
        });

        List<String> expectedBody = Arrays.asList("Integer=42", "Integer=42", "Short=42", "Short=42",
                "Long=42", "Long=42", "Double=42", "Double=42", "Float=42", "Float=42");

        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .sendBuffer(Buffer.buffer("42"), context.asyncAssertSuccess(response -> {
                    assertEquals(expectedBody.toString(), response.bodyAsString());
                }));
    }

    @Test
    public void testReadBufferType(TestContext context) {
        ParamDefinition paramDefinition = new ParamDefinition()
                .setCategory(ParamCategory.BODY)
                .setType(Buffer.class);
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.POST))
                .setPath("/")
                .setParams(Arrays.asList(paramDefinition))
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture(args[0].toString());
        });

        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .sendBuffer(Buffer.buffer("test"), context.asyncAssertSuccess(response -> {
                    assertEquals("test", response.bodyAsString());
                }));
    }

    @Test
    public void testReadBooleanType(TestContext context) {
        ParamDefinition paramDefinition1 = new ParamDefinition()
                .setCategory(ParamCategory.BODY)
                .setType(boolean.class);
        ParamDefinition paramDefinition2 = new ParamDefinition()
                .setCategory(ParamCategory.BODY)
                .setType(Boolean.class);
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.POST))
                .setPath("/")
                .setParams(Arrays.asList(paramDefinition1, paramDefinition2))
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            boolean b1 = (boolean) args[0];
            boolean b2 = (boolean) args[1];
            return Future.succeededFuture("=" + (b1 && b2));
        });

        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .sendBuffer(Buffer.buffer("true"), context.asyncAssertSuccess(response -> {
                    assertEquals("=true", response.bodyAsString());
                }));
    }

    @Test
    public void testPathParameterWithStringType(TestContext context) {
        ParamDefinition paramDefinition = new ParamDefinition()
                .setCategory(ParamCategory.PATH)
                .setType(String.class)
                .setName("name");
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/users/:name")
                .setParams(Arrays.asList(paramDefinition))
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            return Future.succeededFuture(args[0]);
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/users/julia")
                .expect(ResponsePredicate.SC_SUCCESS)
                .send(context.asyncAssertSuccess(response -> {
                    assertEquals("julia", response.bodyAsString());
                }));
    }

    @Test
    public void testPathParameterWithOtherType(TestContext context) {
        ParamDefinition paramDefinition = new ParamDefinition()
                .setCategory(ParamCategory.PATH)
                .setType(int.class)
                .setName("id");
        RouteDefinition routeDefinition = new RouteDefinition()
                .setContext(runMethod)
                .setMethods(Arrays.asList(HttpMethod.GET))
                .setPath("/users/:id")
                .setParams(Arrays.asList(paramDefinition))
                .setResponseType(String.class);
        routeMounter.mountRoute(router, "api-spec", routeDefinition);

        apiInvokerMock.mockInvoke((apiSpec, method, args) -> {
            int id = (int) args[0];
            return Future.succeededFuture(String.valueOf(id));
        });

        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/users/1337")
                .expect(ResponsePredicate.SC_SUCCESS)
                .send(context.asyncAssertSuccess(response -> {
                    assertEquals("1337", response.bodyAsString());
                }));
    }

}
