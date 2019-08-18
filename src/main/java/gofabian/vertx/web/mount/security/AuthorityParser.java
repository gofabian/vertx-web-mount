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

public class AuthorityParser implements RouteParser {
    @Override
    public void visitClass(Class<?> clazz, RouteDefinition routeDefinition, ParseOptions options) {
        visitAnnotations(clazz, routeDefinition);
    }

    @Override
    public void visitMethod(Method method, RouteDefinition routeDefinition, ParseOptions options) {
        visitAnnotations(method, routeDefinition);
    }

    private void visitAnnotations(AnnotatedElement annotatedElement, RouteDefinition routeDefinition) {
        AuthoritiesAllowed annotation = annotatedElement.getAnnotation(AuthoritiesAllowed.class);
        if (annotation != null) {
            List<String> list = Arrays.asList(annotation.value());
            routeDefinition.getAllowedAuthorities().addAll(list);
        }
    }

    @Override
    public void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result) {
        if (child.getAllowedAuthorities().isEmpty()) {
            result.setAllowedAuthorities(parent.getAllowedAuthorities());
        } else {
            result.setAllowedAuthorities(child.getAllowedAuthorities());
        }
    }

    @Override
    public void visitParameter(Parameter parameter, ParamDefinition paramDefinition, ParseOptions options) {
    }
}
