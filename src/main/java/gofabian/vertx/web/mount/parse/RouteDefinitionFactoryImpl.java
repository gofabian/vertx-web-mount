package gofabian.vertx.web.mount.parse;

import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RouteDefinitionFactoryImpl implements RouteDefinitionFactory {

    public List<RouteDefinition> create(Object api, RouteParser parser, ParseOptions options) {
        Class<?> clazz = api.getClass();

        RouteDefinition classDefinition = parseClass(api, parser, options);

        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (!isMethodSupported(method)) {
                continue;
            }

            RouteDefinition methodDefinition = parseMethod(method, parser, options);
            RouteDefinition routeDefinition = mergeDefinitions(classDefinition, methodDefinition, parser);

            if (isValidRouteDefinition(routeDefinition)) {
                routeDefinitions.add(routeDefinition);
            }
        }

        return routeDefinitions;
    }

    protected boolean isMethodSupported(Method method) {
        if (isObjectMethod(method)) {
            return false;
        }

        int mandatoryModifiers = Modifier.PUBLIC;
        if ((method.getModifiers() & mandatoryModifiers) == 0) {
            return false;
        }

        int prohibitedModifiers = Modifier.ABSTRACT | Modifier.INTERFACE | Modifier.NATIVE | Modifier.STATIC;
        return (method.getModifiers() & prohibitedModifiers) == 0;
    }

    protected boolean isObjectMethod(Method method) {
        for (Method objectMethod : Object.class.getDeclaredMethods()) {
            if (method.getName().equals(objectMethod.getName())) {
                List<Type> types1 = Arrays.asList(method.getGenericParameterTypes());
                List<Type> types2 = Arrays.asList(objectMethod.getGenericParameterTypes());
                if (types1.equals(types2)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected RouteDefinition parseClass(Object api, RouteParser parser, ParseOptions options) {
        RouteDefinition classDefinition = new RouteDefinition().setContext(api);
        parser.visitClass(api.getClass(), classDefinition, options);
        return classDefinition;
    }

    protected RouteDefinition parseMethod(Method method, RouteParser parser, ParseOptions options) {
        RouteDefinition methodDefinition = new RouteDefinition().setContext(method);
        parser.visitMethod(method, methodDefinition, options);

        for (Parameter parameter : method.getParameters()) {
            ParamDefinition paramDefinition = parseParameter(parameter, parser, options);
            methodDefinition.getParams().add(paramDefinition);
        }

        return methodDefinition;
    }

    protected ParamDefinition parseParameter(Parameter parameter, RouteParser parser, ParseOptions options) {
        ParamDefinition paramDefinition = new ParamDefinition();
        parser.visitParameter(parameter, paramDefinition, options);
        return paramDefinition;
    }

    protected RouteDefinition mergeDefinitions(RouteDefinition parentDefinition, RouteDefinition childDefinition,
                                               RouteParser parser) {
        RouteDefinition routeDefinition = new RouteDefinition().setContext(childDefinition.getContext());
        parser.merge(parentDefinition, childDefinition, routeDefinition);
        return routeDefinition;
    }

    protected boolean isValidRouteDefinition(RouteDefinition routeDefinition) {
        return !routeDefinition.getPath().isEmpty() && !routeDefinition.getMethods().isEmpty();
    }

}
