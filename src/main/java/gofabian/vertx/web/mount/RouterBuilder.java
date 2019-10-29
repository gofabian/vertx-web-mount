package gofabian.vertx.web.mount;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import gofabian.vertx.web.mount.invoker.RouteInvoker;
import gofabian.vertx.web.mount.invoker.RouteInvokerImpl;
import gofabian.vertx.web.mount.jaxrs.JaxrsParser;
import gofabian.vertx.web.mount.param.*;
import gofabian.vertx.web.mount.parser.*;
import gofabian.vertx.web.mount.request.*;
import gofabian.vertx.web.mount.response.*;
import gofabian.vertx.web.mount.security.SecurityParser;
import gofabian.vertx.web.mount.validation.ValidatingRouteInvoker;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RouterBuilder {

    private RouteDefinitionFactory routeDefinitionFactory = new RouteDefinitionFactoryImpl();
    private RouteInvoker routeInvoker = new RouteInvokerImpl();

    private final ClassAccessList<RequestReader> requestReaders;
    private final ClassAccessList<ResponseWriter> responseWriters;
    private final ClassAccessList<ParamProviderFactory> paramProviderFactories;
    private final ClassAccessList<RouteParser> routeParsers;
    private final ClassAccessList<Handler<RoutingContext>> routeHandlers;

    private final List<Object> apiDefinitions = new ArrayList<>();
    private MountOptions options = new MountOptions();
    private Validator validator;

    public RouterBuilder() {
        requestReaders = new ClassAccessList<>(Arrays.asList(
                new JsonRequestReader(),
                new BufferRequestReader(),
                new FallbackRequestReader()
        ));
        responseWriters = new ClassAccessList<>(Arrays.asList(
                new ResponseEntityTypeResponseWriter(),
                new BufferTypeResponseWriter(),
                new JsonResponseWriter(),
                new TextResponseWriter(),
                new ObjectTypeResponseWriter()
        ));
        RequestReader compositeRequestReader = new CompositeRequestReader(requestReaders.getList());
        paramProviderFactories = new ClassAccessList<>(Arrays.asList(
                new RoutingContextParamProviderFactory(),
                new VertxParamProviderFactory(),
                new HttpRequestParamProviderFactory(),
                new HttpResponseParamProviderFactory(),
                new UserParamProviderFactory(),
                new PathParamProviderFactory(),
                new QueryParamProviderFactory(),
                new BodyParamProviderFactory(compositeRequestReader)
        ));
        routeParsers = new ClassAccessList<>(Arrays.asList(
                new ReturnTypeParser(),
                new JaxrsParser(),
                new SecurityParser(),
                new HandleParser()
        ));
        routeHandlers = new ClassAccessList<>(Arrays.asList(
                BodyHandler.create(),
                ResponseContentTypeHandler.create(),
                new NoContentHandler()
        ));
    }

    public RouterBuilder setRouteDefinitionFactory(RouteDefinitionFactory routeDefinitionFactory) {
        Objects.requireNonNull(routeDefinitionFactory);
        this.routeDefinitionFactory = routeDefinitionFactory;
        return this;
    }

    public RouterBuilder setRouteInvoker(RouteInvoker routeInvoker) {
        Objects.requireNonNull(routeInvoker);
        this.routeInvoker = routeInvoker;
        return this;
    }

    public RouterBuilder addRequestReader(RequestReader requestReader) {
        Objects.requireNonNull(requestReader);
        requestReaders.addFirst(requestReader);
        return this;
    }

    public ClassAccessList<RequestReader> getRequestReaders() {
        return requestReaders;
    }

    public RouterBuilder addResponseWriter(ResponseWriter responseWriter) {
        Objects.requireNonNull(responseWriter);
        responseWriters.addFirst(responseWriter);
        return this;
    }

    public ClassAccessList<ResponseWriter> getResponseWriters() {
        return responseWriters;
    }

    public RouterBuilder addParamProviderFactory(ParamProviderFactory paramProviderFactory) {
        Objects.requireNonNull(paramProviderFactory);
        paramProviderFactories.addFirst(paramProviderFactory);
        return this;
    }

    public ClassAccessList<ParamProviderFactory> getParamProviderFactories() {
        return paramProviderFactories;
    }

    public RouterBuilder addRouteParser(RouteParser routeParser) {
        Objects.requireNonNull(routeParser);
        routeParsers.addFirst(routeParser);
        return this;
    }

    public ClassAccessList<RouteParser> getRouteParsers() {
        return routeParsers;
    }

    public RouterBuilder addRouteHandler(Handler<RoutingContext> routeHandler) {
        Objects.requireNonNull(routeHandler);
        routeHandlers.addFirst(routeHandler);
        return this;
    }

    public ClassAccessList<Handler<RoutingContext>> getRouteHandlers() {
        return routeHandlers;
    }

    public RouterBuilder addApiDefinition(Object apiDefinition) {
        Objects.requireNonNull(apiDefinition);
        this.apiDefinitions.add(apiDefinition);
        return this;
    }

    public void setOptions(MountOptions options) {
        Objects.requireNonNull(options);
        this.options = options;
    }

    public MountOptions getOptions() {
        return options;
    }

    public void setValidator(Validator validator) {
        Objects.requireNonNull(validator);
        this.validator = validator;
    }

    public Router build(Vertx vertx) {
        Objects.requireNonNull(vertx);

        RouteInvoker finalInvoker = (validator == null) ? routeInvoker : new ValidatingRouteInvoker(routeInvoker, validator);
        ResponseWriter compositeResponseWriter = new CompositeResponseWriter(responseWriters.getList());
        RouteMounter routeMounter = new RouteMounter(finalInvoker, compositeResponseWriter,
                paramProviderFactories.getList(), routeHandlers.getList());
        RouteParser compositeRouteParser = new CompositeRouteParser(routeParsers.getList());

        Router router = Router.router(vertx);
        for (Object apiDefinition : apiDefinitions) {
            List<RouteDefinition> routeDefinitions = routeDefinitionFactory.create(apiDefinition, compositeRouteParser, options);
            if (routeDefinitions.isEmpty()) {
                throw new IllegalArgumentException("Given api instance has no route definitions: " + apiDefinition);
            }
            routeDefinitions.forEach(routeDefinition -> routeMounter.mountRoute(router, apiDefinition, routeDefinition));
        }

        return router;
    }

}
