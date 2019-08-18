package gofabian.vertx.web.mount;

import gofabian.vertx.web.mount.security.AuthoritiesAllowed;
import gofabian.vertx.web.mount.security.AuthoritiesRequired;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class AuthorityIntegrationTest {

    private Vertx vertx = Vertx.vertx();
    private Router router = Router.router(vertx);
    private int port;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        router = Router.router(vertx);
        router.route().handler(LoggerHandler.create());

        router.route().handler(routingContext -> {
            List<String> authorities = routingContext.queryParam("authority");
            routingContext.setUser(new AuthUser(authorities));
            routingContext.next();
        });

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
    public void allowedAuthorities(TestContext context) {
        class Api {
            @POST
            @Path("/")
            @AuthoritiesAllowed("role:user")
            public void route() {
            }
        }
        new VertxWebMounter().addApiDefinition(new Api()).mount(router);

        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/?authority=role:user")
                .expect(ResponsePredicate.SC_NO_CONTENT)
                .send(context.asyncAssertSuccess());

        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/?authority=role:admin")
                .expect(ResponsePredicate.SC_FORBIDDEN)
                .send(context.asyncAssertSuccess());
    }

    @Test
    public void requiredAuthorities(TestContext context) {
        @AuthoritiesRequired("right:read")
        class Api {
            @POST
            @Path("/")
            public void route() {
            }
        }
        new VertxWebMounter().addApiDefinition(new Api()).mount(router);

        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/?authority=right:read")
                .expect(ResponsePredicate.SC_NO_CONTENT)
                .send(context.asyncAssertSuccess());

        WebClient.create(vertx)
                .post(port, "127.0.0.1", "/?authority=right:write")
                .expect(ResponsePredicate.SC_FORBIDDEN)
                .send(context.asyncAssertSuccess());
    }

    static class AuthUser implements User {
        private final List<String> authorities;

        AuthUser(List<String> authorities) {
            this.authorities = authorities;
        }

        @Override
        public User isAuthorized(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
            boolean isAuthorized = authorities.contains(authority);
            resultHandler.handle(Future.succeededFuture(isAuthorized));
            return this;
        }

        @Override
        public User clearCache() {
            return this;
        }

        @Override
        public JsonObject principal() {
            return new JsonObject();
        }

        @Override
        public void setAuthProvider(AuthProvider authProvider) {
        }
    }

}
