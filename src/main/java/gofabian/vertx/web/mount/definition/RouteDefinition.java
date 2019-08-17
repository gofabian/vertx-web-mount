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

    public RouteDefinition withSubRouteDefinition(RouteDefinition subRouteDefinition) {
        RouteDefinition routeDefinition = new RouteDefinition().setContext(subRouteDefinition.context);

        routeDefinition.methods = combineList(methods, subRouteDefinition.methods);
        routeDefinition.path = combinePath(path, subRouteDefinition.path);
        routeDefinition.consumes = subRouteDefinition.consumes.isEmpty() ? consumes : subRouteDefinition.consumes;
        routeDefinition.produces = subRouteDefinition.produces.isEmpty() ? produces : subRouteDefinition.produces;
        routeDefinition.params = combineList(params, subRouteDefinition.params);
        routeDefinition.responseType = subRouteDefinition.responseType;

        return routeDefinition;
    }

    private <T> List<T> combineList(List<T> a, List<T> b) {
        List<T> list = new ArrayList<>(a);
        list.addAll(b);
        return list;
    }

    private String combinePath(String parent, String child) {
        if (parent == null) return child;
        if (child == null) return parent;

        // parent + child != null
        if ("/".equals(parent)) return child;
        if ("/".equals(child)) return parent;

        // parent + child != "/"
        return parent + child;
    }

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
                '}';
    }
}
