package gofabian.vertx.web.mount.security;

import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import gofabian.vertx.web.mount.parser.ParseOptions;
import gofabian.vertx.web.mount.parser.RouteParser;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class SecurityParser implements RouteParser {
    @Override
    public void visitClass(Class<?> clazz, RouteDefinition routeDefinition, ParseOptions options) {
        visitAnnotations(clazz, routeDefinition);
    }

    @Override
    public void visitMethod(Method method, RouteDefinition routeDefinition, ParseOptions options) {
        visitAnnotations(method, routeDefinition);
    }

    private void visitAnnotations(AnnotatedElement annotatedElement, RouteDefinition routeDefinition) {
        if (annotatedElement.isAnnotationPresent(Authenticated.class)) {
            routeDefinition.setAuthenticationRequired(true);
        }

        AuthoritiesAllowed authoritiesAllowed = annotatedElement.getAnnotation(AuthoritiesAllowed.class);
        if (authoritiesAllowed != null) {
            List<String> list = Arrays.asList(authoritiesAllowed.value());
            routeDefinition.getAllowedAuthorities().addAll(list);
        }

        AuthoritiesRequired authoritiesRequired = annotatedElement.getAnnotation(AuthoritiesRequired.class);
        if (authoritiesRequired != null) {
            List<String> list = Arrays.asList(authoritiesRequired.value());
            routeDefinition.getRequiredAuthorities().addAll(list);
        }
    }

    @Override
    public void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result) {
        if (child.isAuthenticationRequired() == null) {
            result.setAuthenticationRequired(parent.isAuthenticationRequired());
        } else {
            result.setAuthenticationRequired(child.isAuthenticationRequired());
        }

        if (child.getAllowedAuthorities().isEmpty()) {
            result.getAllowedAuthorities().addAll(parent.getAllowedAuthorities());
        } else {
            result.getAllowedAuthorities().addAll(child.getAllowedAuthorities());
        }

        if (child.getRequiredAuthorities().isEmpty()) {
            result.getRequiredAuthorities().addAll(parent.getRequiredAuthorities());
        } else {
            result.getRequiredAuthorities().addAll(child.getRequiredAuthorities());
        }
    }

    @Override
    public void visitParameter(Parameter parameter, ParamDefinition paramDefinition, ParseOptions options) {
    }
}
