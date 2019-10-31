package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.annotation.Handle;
import gofabian.vertx.web.mount.annotation.HandleFailure;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.BlockingHandlerDecorator;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class HandleParser implements RouteParser {

    private final RouteHandlerFactory routeHandlerFactory;

    public HandleParser(RouteHandlerFactory routeHandlerFactory) {
        this.routeHandlerFactory = routeHandlerFactory;
    }

    @Override
    public void visitClass(Class<?> clazz, RouteDefinition routeDefinition, MountOptions options) {
        visitAnnotatedElement(clazz, routeDefinition);
    }

    @Override
    public void visitMethod(Method method, RouteDefinition routeDefinition, MountOptions options) {
        visitAnnotatedElement(method, routeDefinition);
    }

    private void visitAnnotatedElement(AnnotatedElement element, RouteDefinition routeDefinition) {
        Handle[] handleAnnotations = element.getAnnotationsByType(Handle.class);
        for (Handle annotation : handleAnnotations) {
            Class<? extends Handler<RoutingContext>> handlerClass = annotation.value();
            if (handlerClass == Handle.NoHandler.class) {
                handlerClass = null;
            }
            String handlerName = annotation.name();
            if ("".equals(handlerName)) {
                handlerName = null;
            }
            Handler<RoutingContext> handler = routeHandlerFactory.createHandler(handlerClass, handlerName);

            if (annotation.blocking()) {
                handler = new BlockingHandlerDecorator(handler, annotation.ordered());
            }
            routeDefinition.getRouteHandlers().add(handler);
        }

        HandleFailure[] handleFailureAnnotations = element.getAnnotationsByType(HandleFailure.class);
        for (HandleFailure annotation : handleFailureAnnotations) {
            Class<? extends Handler<RoutingContext>> handlerClass = annotation.value();
            if (handlerClass == Handle.NoHandler.class) {
                handlerClass = null;
            }
            String handlerName = annotation.name();
            if ("".equals(handlerName)) {
                handlerName = null;
            }
            Handler<RoutingContext> handler = routeHandlerFactory.createHandler(handlerClass, handlerName);
            routeDefinition.getFailureHandlers().add(handler);
        }
    }


    @Override
    public void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result, MountOptions options) {
        result.getRouteHandlers().addAll(parent.getRouteHandlers());
        result.getRouteHandlers().addAll(child.getRouteHandlers());
        result.getFailureHandlers().addAll(parent.getFailureHandlers());
        result.getFailureHandlers().addAll(child.getFailureHandlers());
    }

    @Override
    public void visitParameter(Parameter parameter, ParamDefinition paramDefinition, MountOptions options) {
    }
}
