package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.annotation.Order;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class OrderParser implements RouteParser {
    @Override
    public void visitClass(Class<?> clazz, RouteDefinition routeDefinition, MountOptions options) {
        visitAnnotatedElement(clazz, routeDefinition);
    }

    @Override
    public void visitMethod(Method method, RouteDefinition routeDefinition, MountOptions options) {
        visitAnnotatedElement(method, routeDefinition);
    }

    private void visitAnnotatedElement(AnnotatedElement element, RouteDefinition routeDefinition) {
        Order annotation = element.getAnnotation(Order.class);
        if (annotation != null) {
            routeDefinition.setOrder(annotation.value());
        }
    }

    @Override
    public void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result, MountOptions options) {
        if (child.getOrder() != null) {
            result.setOrder(child.getOrder());
        } else {
            result.setOrder(parent.getOrder());
        }
    }

    @Override
    public void visitParameter(Parameter parameter, ParamDefinition paramDefinition, MountOptions options) {
    }
}
