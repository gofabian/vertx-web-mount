package gofabian.vertx.web.mount.jaxrs;

import gofabian.vertx.web.mount.definition.ParamCategory;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JaxRsRouteDefinitionFactory extends MethodBasedRouteDefinitionFactory {

    @Override
    protected boolean isSupportedMethod(Method method) {
        if (!super.isSupportedMethod(method)) {
            return false;
        }

        for (Annotation annotation : method.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(HttpMethod.class)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected RouteDefinition createRouteDefinitionFromClass(Class<?> clazz) {
        RouteDefinition routeDefinition = new RouteDefinition().withContext(clazz);
        parseRouteAnnotations(clazz.getAnnotations(), routeDefinition);
        return routeDefinition;
    }

    @Override
    protected RouteDefinition createRouteDefinitionFromMethod(Method method) {
        RouteDefinition routeDefinition = new RouteDefinition().withContext(method);
        parseRouteAnnotations(method.getAnnotations(), routeDefinition);
        parseParameters(method, routeDefinition.getParams());
        parseReturnType(method, routeDefinition);
        return routeDefinition;
    }

    protected void parseRouteAnnotations(Annotation[] annotations, RouteDefinition routeDefinition) {
        for (Annotation annotation : annotations) {
            parseRouteAnnotation(annotation, routeDefinition);
        }
    }

    protected void parseRouteAnnotation(Annotation annotation, RouteDefinition routeDefinition) {
        javax.ws.rs.HttpMethod httpMethod = annotation.annotationType().getAnnotation(javax.ws.rs.HttpMethod.class);
        if (httpMethod != null) {
            routeDefinition.getMethods().add(convertHttpMethod(httpMethod));
        }

        if (annotation instanceof Path) {
            String path = ((Path) annotation).value();
            routeDefinition.setPath(path);
            return;
        }

        if (annotation instanceof Produces) {
            String[] producesN = ((Produces) annotation).value();
            splitByComma(producesN, routeDefinition.getProduces());
            return;
        }

        if (annotation instanceof Consumes) {
            String[] consumesN = ((Consumes) annotation).value();
            splitByComma(consumesN, routeDefinition.getConsumes());
        }
    }

    private io.vertx.core.http.HttpMethod convertHttpMethod(javax.ws.rs.HttpMethod jaxRsHttpMethod) {
        String text = jaxRsHttpMethod.value();
        try {
            return io.vertx.core.http.HttpMethod.valueOf(text);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown HTTP method: " + text, e);
        }
    }

    protected void parseParameters(Method method, List<ParamDefinition> target) {
        for (Parameter parameter : method.getParameters()) {
            parseParameter(parameter, target);
        }
    }

    protected void parseParameter(Parameter parameter, List<ParamDefinition> target) {
        ParamDefinition paramDefinition = new ParamDefinition()
                .setCategory(ParamCategory.BODY)
                .setType(parameter.getParameterizedType());
        target.add(paramDefinition);

        for (Annotation annotation : parameter.getAnnotations()) {
            parseParameterAnnotation(annotation, paramDefinition);
        }

        ParamCategory category = paramDefinition.getCategory();
        if (category == ParamCategory.BODY || category == ParamCategory.CONTEXT) {
            paramDefinition.setMandatory(true);
            paramDefinition.setDefaultValue(null);
        }

        if (!paramDefinition.isMandatory() && category == ParamCategory.QUERY) {
            List<String> values = new ArrayList<>();
            String rawValues = (String) paramDefinition.getDefaultValue();
            if (!rawValues.trim().equals("")) {
                splitByComma(rawValues, values);
            }
            paramDefinition.setDefaultValue(values);
        }
    }

    protected void parseParameterAnnotation(Annotation annotation, ParamDefinition paramDefinition) {
        if (annotation instanceof Context) {
            paramDefinition.setCategory(ParamCategory.CONTEXT);
            return;
        }
        if (annotation instanceof DefaultValue) {
            paramDefinition.setMandatory(false);
            paramDefinition.setDefaultValue(((DefaultValue) annotation).value());
            return;
        }
        if (annotation instanceof PathParam) {
            paramDefinition.setCategory(ParamCategory.PATH);
            paramDefinition.setName(((PathParam) annotation).value());
            if (paramDefinition.getName().equals("")) {
                throw new IllegalArgumentException("Found @PathParm without a name: " + annotation);
            }
            return;
        }
        if (annotation instanceof QueryParam) {
            paramDefinition.setCategory(ParamCategory.QUERY);
            paramDefinition.setName(((QueryParam) annotation).value());
        }
    }

    protected void parseReturnType(Method method, RouteDefinition routeDefinition) {
        Type returnType = method.getGenericReturnType();
        if (returnType != Void.TYPE && returnType != Void.class) {
            routeDefinition.setResponseType(returnType);
        }
    }

    private void splitByComma(String[] texts, List<String> target) {
        for (String consumes : texts) {
            splitByComma(consumes, target);
        }
    }

    private void splitByComma(String text, List<String> target) {
        String[] parts = text.trim().split("\\s*,\\s*");
        target.addAll(Arrays.asList(parts));
    }

}
