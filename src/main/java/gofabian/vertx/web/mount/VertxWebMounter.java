package gofabian.vertx.web.mount;

import gofabian.vertx.web.mount.configurator.*;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import gofabian.vertx.web.mount.invoker.RouteInvoker;
import gofabian.vertx.web.mount.invoker.RouteInvokerImpl;
import gofabian.vertx.web.mount.jaxrs.JaxrsParser;
import gofabian.vertx.web.mount.param.*;
import gofabian.vertx.web.mount.parser.*;
import gofabian.vertx.web.mount.request.*;
import gofabian.vertx.web.mount.response.*;
import gofabian.vertx.web.mount.security.SecurityConfigurator;
import gofabian.vertx.web.mount.security.SecurityParser;
import io.vertx.ext.web.Router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VertxWebMounter {

    private RouteDefinitionFactory routeDefinitionFactory = new RouteDefinitionFactoryImpl();
    private RouteInvoker routeInvoker = new RouteInvokerImpl();

    private final ClassAccessList<RequestReader> requestReaders;
    private final ClassAccessList<ResponseWriter> responseWriters;
    private final ClassAccessList<ParamProviderFactory> paramProviderFactories;
    private final ClassAccessList<RouteParser> routeParsers;
    private final ClassAccessList<RouteConfigurator> routeConfigurators;

    private final List<Object> apiDefinitions = new ArrayList<>();
    private MountOptions options = new MountOptions();

    public VertxWebMounter() {
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
                new SecurityParser()
        ));
        routeConfigurators = new ClassAccessList<>(Arrays.asList(
                new RouteAttributesConfigurator(),
                new BodyHandlerConfigurator(),
                new SecurityConfigurator(),
                new NegotiationConfigurator(),
                new NoContentConfigurator()
        ));
    }

    public VertxWebMounter setRouteDefinitionFactory(RouteDefinitionFactory routeDefinitionFactory) {
        Objects.requireNonNull(routeDefinitionFactory);
        this.routeDefinitionFactory = routeDefinitionFactory;
        return this;
    }

    public VertxWebMounter setRouteInvoker(RouteInvoker routeInvoker) {
        Objects.requireNonNull(routeInvoker);
        this.routeInvoker = routeInvoker;
        return this;
    }

    public VertxWebMounter addRequestReader(RequestReader requestReader) {
        Objects.requireNonNull(requestReader);
        requestReaders.addFirst(requestReader);
        return this;
    }

    public ClassAccessList<RequestReader> getRequestReaders() {
        return requestReaders;
    }

    public VertxWebMounter addResponseWriter(ResponseWriter responseWriter) {
        Objects.requireNonNull(responseWriter);
        responseWriters.addFirst(responseWriter);
        return this;
    }

    public ClassAccessList<ResponseWriter> getResponseWriters() {
        return responseWriters;
    }

    public VertxWebMounter addParamProviderFactory(ParamProviderFactory paramProviderFactory) {
        Objects.requireNonNull(paramProviderFactory);
        paramProviderFactories.addFirst(paramProviderFactory);
        return this;
    }

    public ClassAccessList<ParamProviderFactory> getParamProviderFactories() {
        return paramProviderFactories;
    }

    public VertxWebMounter addRouteParser(RouteParser routeParser) {
        Objects.requireNonNull(routeParser);
        routeParsers.addFirst(routeParser);
        return this;
    }

    public ClassAccessList<RouteParser> getRouteParsers() {
        return routeParsers;
    }

    public VertxWebMounter addRouteConfigurator(RouteConfigurator routeConfigurator) {
        Objects.requireNonNull(routeConfigurator);
        routeConfigurators.addFirst(routeConfigurator);
        return this;
    }

    public ClassAccessList<RouteConfigurator> getRouteConfigurators() {
        return routeConfigurators;
    }

    public VertxWebMounter addApiDefinition(Object apiDefinition) {
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

    public void mount(Router router) {
        Objects.requireNonNull(router);

        ResponseWriter compositeResponseWriter = new CompositeResponseWriter(responseWriters.getList());

        RouteMounter routeMounter = new RouteMounter(routeInvoker,
                compositeResponseWriter, paramProviderFactories.getList(), routeConfigurators.getList(), options);

        RouteParser compositeRouteParser = new CompositeRouteParser(routeParsers.getList());

        for (Object apiDefinition : apiDefinitions) {
            List<RouteDefinition> routeDefinitions = routeDefinitionFactory.create(apiDefinition, compositeRouteParser,
                    null);

            if (routeDefinitions.isEmpty()) {
                throw new IllegalArgumentException("Given api instance has no route definitions: " + apiDefinition);
            }

            routeDefinitions.forEach(routeDefinition -> routeMounter.mountRoute(router, apiDefinition, routeDefinition));
        }
    }

}
