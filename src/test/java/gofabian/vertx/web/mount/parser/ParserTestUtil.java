package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ParserTestUtil {

    private final RouteParser parser;
    private final MountOptions options;

    public ParserTestUtil(RouteParser parser, MountOptions options) {
        this.parser = parser;
        this.options = options;
    }

    public RouteDefinition parseMethod(Class<?> clazz, String methodName) {
        Method method = getMethod(clazz, methodName);
        RouteDefinition parent = new RouteDefinition();
        parser.visitClass(clazz, parent, options);
        RouteDefinition child = new RouteDefinition();
        parser.visitMethod(method, child, options);
        RouteDefinition result = new RouteDefinition();
        parser.merge(parent, child, result, options);
        return result;
    }

    public ParamDefinition parseParameter(String methodName) {
        Method method = getMethod(JaxRsParserTest.ParameterExample.class, methodName);
        Parameter parameter = method.getParameters()[0];
        ParamDefinition definition = new ParamDefinition();
        parser.visitParameter(parameter, definition, options);
        return definition;
    }

    private Method getMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown method: " + methodName);
    }

}
