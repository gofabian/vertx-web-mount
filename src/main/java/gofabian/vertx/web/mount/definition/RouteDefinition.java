package gofabian.vertx.web.mount.definition;

import io.vertx.core.http.HttpMethod;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RouteDefinition {

    private Object context;

    private List<HttpMethod> methods = new ArrayList<>();
    private String path = "/";
    private List<String> consumes = new ArrayList<>();
    private List<String> produces = new ArrayList<>();
    private List<ParamDefinition> params = new ArrayList<>();
    private Type responseType;

    private boolean isAuthenticationRequired;
    private List<String> allowedAuthorities = new ArrayList<>();
    private List<String> requiredAuthorities = new ArrayList<>();

    public Object getContext() {
        return context;
    }

    public RouteDefinition setContext(Object context) {
        this.context = context;
        return this;
    }

    public List<HttpMethod> getMethods() {
        return methods;
    }

    public RouteDefinition setMethods(List<HttpMethod> methods) {
        this.methods = Objects.requireNonNull(methods);
        return this;
    }

    public String getPath() {
        return path;
    }

    public RouteDefinition setPath(String path) {
        this.path = formatPath(path);
        return this;
    }

    private String formatPath(String path) {
        path = path.trim();
        if (!path.startsWith("/")) {
            // add leading slash
            path = "/" + path;
        }
        if ((path.length() > 1) && path.endsWith("/")) {
            // remove trailing slash
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public RouteDefinition setConsumes(List<String> consumes) {
        this.consumes = Objects.requireNonNull(consumes);
        return this;
    }

    public List<String> getProduces() {
        return produces;
    }

    public RouteDefinition setProduces(List<String> produces) {
        this.produces = Objects.requireNonNull(produces);
        return this;
    }

    public List<ParamDefinition> getParams() {
        return params;
    }

    public RouteDefinition setParams(List<ParamDefinition> params) {
        this.params = Objects.requireNonNull(params);
        return this;
    }

    public Type getResponseType() {
        return responseType;
    }

    public RouteDefinition setResponseType(Type responseType) {
        this.responseType = responseType;
        return this;
    }

    public boolean isAuthenticationRequired() {
        return isAuthenticationRequired;
    }

    public RouteDefinition setAuthenticationRequired(boolean authenticationRequired) {
        isAuthenticationRequired = authenticationRequired;
        return this;
    }

    public List<String> getAllowedAuthorities() {
        return allowedAuthorities;
    }

    public RouteDefinition setAllowedAuthorities(List<String> allowedAuthorities) {
        this.allowedAuthorities = allowedAuthorities;
        return this;
    }

    public List<String> getRequiredAuthorities() {
        return requiredAuthorities;
    }

    public RouteDefinition setRequiredAuthorities(List<String> requiredAuthorities) {
        this.requiredAuthorities = requiredAuthorities;
        return this;
    }

    @Override
    public String toString() {
        return "RouteDefinition{" +
                "context=" + context +
                ", methods=" + methods +
                ", path='" + path + '\'' +
                ", consumes=" + consumes +
                ", produces=" + produces +
                ", params=" + params +
                ", responseType=" + responseType +
                ", isAuthenticationRequired=" + isAuthenticationRequired +
                ", allowedAuthorities=" + allowedAuthorities +
                ", requiredAuthorities=" + requiredAuthorities +
                '}';
    }
}
