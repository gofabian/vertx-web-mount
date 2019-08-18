package gofabian.vertx.web.mount.security;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import gofabian.vertx.web.mount.parser.ParseOptions;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SecurityParserTest {

    private final SecurityParser parser = new SecurityParser();
    private final ParseOptions options = new ParseOptions();

    @Test
    public void visitClass() {
        @Authenticated
        @AuthoritiesAllowed({"role:admin", "role:user"})
        @AuthoritiesRequired("right:listUsers")
        class Api {
            public void route() {
            }
        }

        RouteDefinition routeDefinition = new RouteDefinition();
        parser.visitClass(Api.class, routeDefinition, options);

        assertTrue(routeDefinition.isAuthenticationRequired());
        assertEquals(Arrays.asList("role:admin", "role:user"), routeDefinition.getAllowedAuthorities());
        assertEquals(Arrays.asList("right:listUsers"), routeDefinition.getRequiredAuthorities());
    }

    @Test
    public void visitMethod() throws NoSuchMethodException {
        class Api {
            @Authenticated
            @AuthoritiesAllowed("role:developer")
            @AuthoritiesRequired("right:write")
            public void route() {
            }
        }

        RouteDefinition routeDefinition = new RouteDefinition();
        parser.visitMethod(Api.class.getMethod("route"), routeDefinition, options);

        assertTrue(routeDefinition.isAuthenticationRequired());
        assertEquals(Arrays.asList("role:developer"), routeDefinition.getAllowedAuthorities());
        assertEquals(Arrays.asList("right:write"), routeDefinition.getRequiredAuthorities());
    }

    @Test
    public void merge() {
        RouteDefinition classDefinition = new RouteDefinition()
                .setAuthenticationRequired(false)
                .setAllowedAuthorities(Arrays.asList("role:fisher"))
                .setRequiredAuthorities(Arrays.asList("right:eat"));
        RouteDefinition methodDefinition = new RouteDefinition()
                .setAuthenticationRequired(true)
                .setAllowedAuthorities(Arrays.asList("role:haunter"))
                .setRequiredAuthorities(Arrays.asList("right:haunt"));

        RouteDefinition routeDefinition = new RouteDefinition();
        parser.merge(classDefinition, methodDefinition, routeDefinition);

        assertTrue(routeDefinition.isAuthenticationRequired());
        assertEquals(Arrays.asList("role:haunter"), routeDefinition.getAllowedAuthorities());
        assertEquals(Arrays.asList("right:haunt"), routeDefinition.getRequiredAuthorities());
    }

}
