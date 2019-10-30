package gofabian.vertx.web.mount.definition;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Type;
import java.util.*;

public class RouteDefinition {

    private Object context;

    private List<HttpMethod> methods = new ArrayList<>();
    private String path = "/";
    private List<String> consumes = new ArrayList<>();
    private List<String> produces = new ArrayList<>();
    private List<ParamDefinition> params = new ArrayList<>();
    private Type responseType;
    private List<Handler<RoutingContext>> routeHandlers = new ArrayList<>();
    private List<Handler<RoutingContext>> failureHandlers = new ArrayList<>();
    private BlockingType blockingType;
    private Integer order;

    private final Map<String, Object> attributes = new HashMap<>();

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

    public List<Handler<RoutingContext>> getRouteHandlers() {
        return routeHandlers;
    }

    public RouteDefinition setRouteHandlers(List<Handler<RoutingContext>> routeHandlers) {
        this.routeHandlers = Objects.requireNonNull(routeHandlers);
        return this;
    }

    public List<Handler<RoutingContext>> getFailureHandlers() {
        return failureHandlers;
    }

    public void setFailureHandlers(List<Handler<RoutingContext>> failureHandlers) {
        this.failureHandlers = failureHandlers;
    }

    public BlockingType getBlockingType() {
        return blockingType;
    }

    public RouteDefinition setBlockingType(BlockingType blockingType) {
        this.blockingType = blockingType;
        return this;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }

    public RouteDefinition putAttribute(String name, Object attribute) {
        attributes.put(name, attribute);
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
                ", routeHandlers=" + routeHandlers +
                ", failureHandlers=" + failureHandlers +
                ", blockingType=" + blockingType +
                ", order=" + order +
                ", attributes=" + attributes +
                '}';
    }
}
