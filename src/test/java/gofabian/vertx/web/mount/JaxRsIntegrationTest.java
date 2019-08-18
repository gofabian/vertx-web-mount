package gofabian.vertx.web.mount;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.handler.LoggerHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class JaxRsIntegrationTest {

    public static class Entity {
        @SuppressWarnings("WeakerAccess")
        public String name;
    }

    @Path("/")
    public static class JaxRsApi {
        @GET
        public void getNoContent() {
        }

        @GET
        @Path("/text")
        @Produces("text/plain")
        public String getText() {
            return "awesome text";
        }

        @GET
        @Path("/accept")
        @Produces({"text/html", "text/plain"})
        public String accept() {
            return "plain text";
        }

        @GET
        @Path("/no-type")
        public String noType() {
            return "cannot be converted";
        }

        @POST
        @Path("/post")
        @Produces("application/json")
        public Entity post(Entity entity) {
            Entity response = new Entity();
            response.name = entity.name + " comes back";
            return response;
        }
    }


    private Vertx vertx = Vertx.vertx();
    private Router router = Router.router(vertx);
    private int port;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        router = Router.router(vertx);
        router.route().handler(LoggerHandler.create());

        new VertxWebMounter().addApiDefinition(new JaxRsApi()).mount(router);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(0, context.asyncAssertSuccess(httpServer -> {
                    port = httpServer.actualPort();
                }));
    }

    @After
    public void after() {
        vertx.close();
    }

    @Test
    public void getNoContent(TestContext context) {
        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/")
                .expect(ResponsePredicate.SC_NO_CONTENT)
                .send(context.asyncAssertSuccess());
    }

    @Test
    public void getText(TestContext context) {
        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/text")
                .expect(ResponsePredicate.SC_OK)
                .expect(ResponsePredicate.contentType("text/plain"))
                .send(context.asyncAssertSuccess(response -> {
                    assertEquals("awesome text", response.bodyAsString());
                }));
    }

    @Test
    public void acceptHeader(TestContext context) {
        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/accept")
                .putHeader("accept", "text/plain")
                .expect(ResponsePredicate.SC_OK)
                .expect(ResponsePredicate.contentType("text/plain"))
                .send(context.asyncAssertSuccess(response -> {
                    assertEquals("plain text", response.bodyAsString());
                }));
    }

    @Test
    public void responseWithoutContentType(TestContext context) {
        WebClient.create(vertx)
                .get(port, "127.0.0.1", "/no-type")
                .expect(ResponsePredicate.SC_OK)
                .send(context.asyncAssertSuccess());
    }

    @Test
    public void post(TestContext context) {
        Entity entity = new Entity();
        entity.name = "foo";
        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/post")
                .expect(ResponsePredicate.contentType("application/json"))
                .expect(ResponsePredicate.SC_OK)
                .sendJson(entity, context.asyncAssertSuccess(response -> {
                    assertEquals(new JsonObject().put("name", "foo comes back"), response.bodyAsJsonObject());
                }));
    }

    @Test
    public void postWithoutContentType(TestContext context) {
        Entity entity = new Entity();
        entity.name = "foo";
        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/post")
                .expect(ResponsePredicate.SC_INTERNAL_SERVER_ERROR)
                .sendBuffer(Json.encodeToBuffer(entity), context.asyncAssertSuccess());
    }

}
