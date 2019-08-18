package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class ReturnTypeParser implements RouteParser {

    @Override
    public void visitClass(Class<?> clazz, RouteDefinition routeDefinition, MountOptions options) {
    }

    @Override
    public void visitMethod(Method method, RouteDefinition routeDefinition, MountOptions options) {
        Type returnType = method.getGenericReturnType();
        if (returnType != Void.TYPE && returnType != Void.class) {
            routeDefinition.setResponseType(returnType);
        }
    }

    @Override
    public void visitParameter(Parameter parameter, ParamDefinition paramDefinition, MountOptions options) {
    }

    @Override
    public void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result) {
        result.setResponseType(child.getResponseType());
    }

}
