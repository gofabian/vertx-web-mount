package gofabian.vertx.web.mount.jaxrs;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinitionFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class MethodBasedRouteDefinitionFactory implements RouteDefinitionFactory {

    @Override
    public List<RouteDefinition> createRouteDefinitions(Object apiDefinition) {
        Class<?> clazz = apiDefinition.getClass();
        RouteDefinition classRouteDefinition = createRouteDefinitionFromClass(clazz);

        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (isSupportedMethod(method)) {
                RouteDefinition methodRouteDefinition = createRouteDefinitionFromMethod(method);
                RouteDefinition routeDefinition = classRouteDefinition.withSubRouteDefinition(methodRouteDefinition);
                validateRouteDefinition(routeDefinition);
                routeDefinitions.add(routeDefinition);
            }
        }
        return routeDefinitions;
    }

    protected boolean isSupportedMethod(Method method) {
        if (method.getDeclaringClass() == Object.class) {
            return false;
        }

        int modifiers = method.getModifiers();

        int mandatoryModifiers = Modifier.PUBLIC;
        if ((modifiers & mandatoryModifiers) == 0) {
            return false;
        }

        int prohibitedModifiers = Modifier.ABSTRACT | Modifier.INTERFACE | Modifier.NATIVE | Modifier.STATIC;
        return (modifiers & prohibitedModifiers) == 0;
    }

    protected void validateRouteDefinition(RouteDefinition routeDefinition) {
        if (routeDefinition.getPath() == null || routeDefinition.getPath().isEmpty()) {
            throw new IllegalArgumentException("Missing path in route definition: " + routeDefinition);
        }
        if (routeDefinition.getMethods() == null || routeDefinition.getMethods().isEmpty()) {
            throw new IllegalArgumentException("Missing method in route definition: " + routeDefinition);
        }
    }

    abstract protected RouteDefinition createRouteDefinitionFromClass(Class<?> clazz);

    abstract protected RouteDefinition createRouteDefinitionFromMethod(Method method);

}
