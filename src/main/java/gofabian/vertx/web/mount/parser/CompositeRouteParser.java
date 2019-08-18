package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public class CompositeRouteParser implements RouteParser {

    private final List<RouteParser> parsers;

    public CompositeRouteParser(List<RouteParser> parsers) {
        this.parsers = parsers;
    }

    @Override
    public void visitClass(Class<?> clazz, RouteDefinition routeDefinition, ParseOptions options) {
        parsers.forEach(p -> p.visitClass(clazz, routeDefinition, options));
    }

    @Override
    public void visitMethod(Method method, RouteDefinition routeDefinition, ParseOptions options) {
        parsers.forEach(p -> p.visitMethod(method, routeDefinition, options));
    }

    @Override
    public void visitParameter(Parameter parameter, ParamDefinition paramDefinition, ParseOptions options) {
        parsers.forEach(p -> p.visitParameter(parameter, paramDefinition, options));
    }

    @Override
    public void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result) {
        parsers.forEach(p -> p.merge(parent, child, result));
    }

}
