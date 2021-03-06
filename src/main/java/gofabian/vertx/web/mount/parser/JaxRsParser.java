package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.definition.ParamCategory;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.http.HttpMethod;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class JaxRsParser implements RouteParser {

    @Override
    public void visitClass(Class<?> clazz, RouteDefinition routeDefinition, MountOptions options) {
        for (Annotation annotation : clazz.getAnnotations()) {
            visitAnnotation(annotation, routeDefinition);
        }
    }

    @Override
    public void visitMethod(Method method, RouteDefinition routeDefinition, MountOptions options) {
        for (Annotation annotation : method.getAnnotations()) {
            visitAnnotation(annotation, routeDefinition);
        }
    }

    protected void visitAnnotation(Annotation annotation, RouteDefinition routeDefinition) {
        javax.ws.rs.HttpMethod httpMethod = annotation.annotationType().getAnnotation(javax.ws.rs.HttpMethod.class);
        if (httpMethod != null) {
            HttpMethod vertxMethod = convertHttpMethod(httpMethod);
            routeDefinition.getMethods().add(vertxMethod);
        }

        if (annotation instanceof Path) {
            String path = ((Path) annotation).value();
            routeDefinition.setPath(path);
            return;
        }

        if (annotation instanceof Produces) {
            String[] producesN = ((Produces) annotation).value();
            ParseHelper.splitByComma(producesN, routeDefinition.getProduces());
            return;
        }

        if (annotation instanceof Consumes) {
            String[] consumesN = ((Consumes) annotation).value();
            ParseHelper.splitByComma(consumesN, routeDefinition.getConsumes());
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

    @Override
    public void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result, MountOptions options) {
        result.setContext(child.getContext());
        result.setMethods(child.getMethods());
        result.setPath(ParseHelper.combinePath(parent.getPath(), child.getPath()));
        result.setParams(child.getParams());

        if (child.getConsumes().isEmpty()) {
            result.setConsumes(parent.getConsumes());
        } else {
            result.setConsumes(child.getConsumes());
        }

        if (child.getProduces().isEmpty()) {
            result.setProduces(parent.getProduces());
        } else {
            result.setProduces(child.getProduces());
        }
    }

    @Override
    public void visitParameter(Parameter parameter, ParamDefinition paramDefinition, MountOptions options) {
        paramDefinition
                .setCategory(ParamCategory.BODY)
                .setType(parameter.getParameterizedType());

        for (Annotation annotation : parameter.getAnnotations()) {
            visitParameterAnnotation(annotation, paramDefinition);
        }

        ParamCategory category = paramDefinition.getCategory();
        switch (category) {
            case BODY:
            case CONTEXT:
                paramDefinition.setMandatory(true);
                paramDefinition.setDefaultValue(null);
                break;
            case QUERY:
            case HEADER:
            case FORM:
                // convert default value string to list
                List<String> values = new ArrayList<>();
                if (paramDefinition.getDefaultValue() != null) {
                    String rawValues = (String) paramDefinition.getDefaultValue();
                    if (!rawValues.trim().equals("")) {
                        ParseHelper.splitByComma(rawValues, values);
                    }
                }
                paramDefinition.setDefaultValue(values);
                break;
        }
    }

    protected void visitParameterAnnotation(Annotation annotation, ParamDefinition paramDefinition) {
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
            return;
        }

        if (annotation instanceof QueryParam) {
            paramDefinition.setCategory(ParamCategory.QUERY);
            paramDefinition.setName(((QueryParam) annotation).value());
            return;
        }

        if (annotation instanceof HeaderParam) {
            paramDefinition.setCategory(ParamCategory.HEADER);
            paramDefinition.setName(((HeaderParam) annotation).value());
            return;
        }

        if (annotation instanceof FormParam) {
            paramDefinition.setCategory(ParamCategory.FORM);
            paramDefinition.setName(((FormParam) annotation).value());
        }
    }

}
