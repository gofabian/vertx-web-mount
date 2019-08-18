package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface RouteParser {

    void visitClass(Class<?> clazz, RouteDefinition routeDefinition, ParseOptions options);

    void visitMethod(Method method, RouteDefinition routeDefinition, ParseOptions options);

    void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result);

    void visitParameter(Parameter parameter, ParamDefinition paramDefinition, ParseOptions options);

}
