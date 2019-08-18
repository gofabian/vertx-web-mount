package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface RouteParser {

    void visitClass(Class<?> clazz, RouteDefinition routeDefinition, MountOptions options);

    void visitMethod(Method method, RouteDefinition routeDefinition, MountOptions options);

    void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result);

    void visitParameter(Parameter parameter, ParamDefinition paramDefinition, MountOptions options);

}
