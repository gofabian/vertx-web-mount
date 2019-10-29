package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class HandleParser implements RouteParser {
    @Override
    public void visitClass(Class<?> clazz, RouteDefinition routeDefinition, MountOptions options) {
        visitAnnotatedElement(clazz, routeDefinition);
    }

    @Override
    public void visitMethod(Method method, RouteDefinition routeDefinition, MountOptions options) {
        visitAnnotatedElement(method, routeDefinition);
    }

    private void visitAnnotatedElement(AnnotatedElement element, RouteDefinition routeDefinition) {
        Handle[] annotations = element.getAnnotationsByType(Handle.class);
        for (Handle annotation : annotations) {
            Class<? extends Handler<RoutingContext>> handlerClass = ((Handle) annotation).value();
            Handler<RoutingContext> handler = createInstance(handlerClass);
            routeDefinition.getRouteHandlers().add(handler);
        }
    }

    private <T> T createInstance(Class<? extends T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Public constructor without parameter required", e);
        }
    }

    @Override
    public void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result, MountOptions options) {
        result.getRouteHandlers().addAll(parent.getRouteHandlers());
        result.getRouteHandlers().addAll(child.getRouteHandlers());
    }

    @Override
    public void visitParameter(Parameter parameter, ParamDefinition paramDefinition, MountOptions options) {
    }
}
