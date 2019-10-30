package gofabian.vertx.web.mount.security;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.annotation.Authenticated;
import gofabian.vertx.web.mount.annotation.AuthoritiesAllowed;
import gofabian.vertx.web.mount.annotation.AuthoritiesRequired;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gofabian.vertx.web.mount.security.SecurityParser.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SecurityParserTest {

    private final SecurityParser parser = new SecurityParser();
    private final MountOptions options = new MountOptions();

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

        assertTrue(routeDefinition.getAttribute(KEY_IS_AUTHENTICATION_REQUIRED));
        assertEquals(Arrays.asList("role:admin", "role:user"), routeDefinition.getAttribute(KEY_ALLOWED_AUTHORITIES));
        assertEquals(Arrays.asList("right:listUsers"), routeDefinition.getAttribute(KEY_REQUIRED_AUTHORITIES));
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

        assertTrue(routeDefinition.getAttribute(KEY_IS_AUTHENTICATION_REQUIRED));
        assertEquals(Arrays.asList("role:developer"), routeDefinition.getAttribute(KEY_ALLOWED_AUTHORITIES));
        assertEquals(Arrays.asList("right:write"), routeDefinition.getAttribute(KEY_REQUIRED_AUTHORITIES));
    }

    @Test
    public void merge() {
        RouteDefinition classDefinition = new RouteDefinition()
                .putAttribute(KEY_IS_AUTHENTICATION_REQUIRED, false)
                .putAttribute(KEY_ALLOWED_AUTHORITIES, Arrays.asList("role:fisher"))
                .putAttribute(KEY_REQUIRED_AUTHORITIES, Arrays.asList("right:eat"));
        RouteDefinition methodDefinition = new RouteDefinition()
                .putAttribute(KEY_IS_AUTHENTICATION_REQUIRED, true)
                .putAttribute(KEY_ALLOWED_AUTHORITIES, Arrays.asList("role:haunter"))
                .putAttribute(KEY_REQUIRED_AUTHORITIES, Arrays.asList("right:haunt"));

        RouteDefinition routeDefinition = new RouteDefinition();
        parser.merge(classDefinition, methodDefinition, routeDefinition, options);

        List<Class<?>> classes = routeDefinition.getRouteHandlers().stream()
                .map(Object::getClass).collect(Collectors.toList());
        assertTrue(classes.contains(AuthenticationRequiredHandler.class));
        assertTrue(classes.contains(AllowedAuthoritiesHandler.class));
        assertTrue(classes.contains(RequiredAuthoritiesHandler.class));
    }

}
