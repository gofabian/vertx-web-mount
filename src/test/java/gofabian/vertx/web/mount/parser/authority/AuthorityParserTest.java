package gofabian.vertx.web.mount.parser.authority;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import gofabian.vertx.web.mount.parser.ParseOptions;
import gofabian.vertx.web.mount.security.AuthoritiesAllowed;
import gofabian.vertx.web.mount.security.AuthorityParser;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class AuthorityParserTest {

    private final AuthorityParser parser = new AuthorityParser();
    private final ParseOptions options = new ParseOptions();

    @Test
    public void visitClass() {
        @AuthoritiesAllowed({"role:admin", "role:user"})
        class Api {
            public void route() {
            }
        }

        RouteDefinition routeDefinition = new RouteDefinition();
        parser.visitClass(Api.class, routeDefinition, options);

        assertEquals(Arrays.asList("role:admin", "role:user"), routeDefinition.getAllowedAuthorities());
    }

    @Test
    public void visitMethod() throws NoSuchMethodException {
        class Api {
            @AuthoritiesAllowed("role:developer")
            public void route() {
            }
        }

        RouteDefinition routeDefinition = new RouteDefinition();
        parser.visitMethod(Api.class.getMethod("route"), routeDefinition, options);

        assertEquals(Arrays.asList("role:developer"), routeDefinition.getAllowedAuthorities());
    }

    @Test
    public void merge() {
        RouteDefinition classDefinition = new RouteDefinition().setAllowedAuthorities(Arrays.asList("role:fisher"));
        RouteDefinition methodDefinition = new RouteDefinition().setAllowedAuthorities(Arrays.asList("role:haunter"));

        RouteDefinition routeDefinition = new RouteDefinition();
        parser.merge(classDefinition, methodDefinition, routeDefinition);

        assertEquals(Arrays.asList("role:haunter"), routeDefinition.getAllowedAuthorities());
    }

}
