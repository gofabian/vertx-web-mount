package gofabian.vertx.web.mount.security;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import gofabian.vertx.web.mount.parser.RouteParser;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SecurityParser implements RouteParser {

    static final String KEY_IS_AUTHENTICATION_REQUIRED = "is_authentication_required";
    static final String KEY_ALLOWED_AUTHORITIES = "allowed_authorities";
    static final String KEY_REQUIRED_AUTHORITIES = "required_authorities";

    @Override
    public void visitClass(Class<?> clazz, RouteDefinition routeDefinition, MountOptions options) {
        visitAnnotations(clazz, routeDefinition);
    }

    @Override
    public void visitMethod(Method method, RouteDefinition routeDefinition, MountOptions options) {
        visitAnnotations(method, routeDefinition);
    }

    private void visitAnnotations(AnnotatedElement annotatedElement, RouteDefinition routeDefinition) {
        Map<String, Object> attributes = routeDefinition.getAttributes();

        if (annotatedElement.isAnnotationPresent(Authenticated.class)) {
            attributes.put(KEY_IS_AUTHENTICATION_REQUIRED, true);
        }
        if (annotatedElement.isAnnotationPresent(NotAuthenticated.class)) {
            attributes.put(KEY_IS_AUTHENTICATION_REQUIRED, false);
        }

        AuthoritiesAllowed authoritiesAllowed = annotatedElement.getAnnotation(AuthoritiesAllowed.class);
        if (authoritiesAllowed != null) {
            attributes.put(KEY_ALLOWED_AUTHORITIES, Arrays.asList(authoritiesAllowed.value()));
        }

        AuthoritiesRequired authoritiesRequired = annotatedElement.getAnnotation(AuthoritiesRequired.class);
        if (authoritiesRequired != null) {
            attributes.put(KEY_REQUIRED_AUTHORITIES, Arrays.asList(authoritiesRequired.value()));
        }
    }

    @Override
    public void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result, MountOptions options) {
        Boolean isAuthenticationRequired = mergeAttribute(KEY_IS_AUTHENTICATION_REQUIRED, parent, child);
        if (isAuthenticationRequired == null) {
            isAuthenticationRequired = options.isAuthenticationRequired();
        }
        if (isAuthenticationRequired) {
            result.getRouteHandlers().add(new AuthenticationRequiredHandler());
        }

        List<String> allowedAuthorities = mergeAttribute(KEY_ALLOWED_AUTHORITIES, parent, child);
        if (allowedAuthorities != null && !allowedAuthorities.isEmpty()) {
            result.getRouteHandlers().add(new AllowedAuthoritiesHandler(allowedAuthorities));
        }

        List<String> requiredAuthorities = mergeAttribute(KEY_REQUIRED_AUTHORITIES, parent, child);
        if (requiredAuthorities != null && !requiredAuthorities.isEmpty()) {
            result.getRouteHandlers().add(new RequiredAuthoritiesHandler(requiredAuthorities));
        }
    }

    private <T> T mergeAttribute(String name, RouteDefinition parent, RouteDefinition child) {
        if (child.getAttributes().containsKey(name)) {
            return child.getAttribute(name);
        }
        if (parent.getAttributes().containsKey(name)) {
            return parent.getAttribute(name);
        }
        return null;
    }

    @Override
    public void visitParameter(Parameter parameter, ParamDefinition paramDefinition, MountOptions options) {
    }
}
