package gofabian.vertx.web.mount;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinitionFactory;
import gofabian.vertx.web.mount.jaxrs.JaxRsRouteDefinitionFactory;
import gofabian.vertx.web.mount.jaxrs.MethodRouteDefinitionInvoker;
import gofabian.vertx.web.mount.param.*;
import gofabian.vertx.web.mount.request.*;
import gofabian.vertx.web.mount.response.*;
import io.vertx.ext.web.Router;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VertxWebMounter {

    private RouteDefinitionFactory routeDefinitionFactory = new JaxRsRouteDefinitionFactory();
    private RouteDefinitionInvoker routeDefinitionInvoker = new MethodRouteDefinitionInvoker();
    private List<Object> apiDefinitions = new ArrayList<>();
    private List<ParamProviderFactory> paramProviderFactories = new ArrayList<>();
    private List<RequestReader> requestReaders = new ArrayList<>();
    private List<ResponseWriter> responseWriters = new ArrayList<>();

    public VertxWebMounter setRouteDefinitionFactory(JaxRsRouteDefinitionFactory routeDefinitionFactory) {
        this.routeDefinitionFactory = routeDefinitionFactory;
        return this;
    }

    public VertxWebMounter setRouteDefinitionInvoker(RouteDefinitionInvoker routeDefinitionInvoker) {
        this.routeDefinitionInvoker = routeDefinitionInvoker;
        return this;
    }

    public VertxWebMounter addParamProviderFactory(ParamProviderFactory paramProviderFactory) {
        paramProviderFactories.add(paramProviderFactory);
        return this;
    }

    public VertxWebMounter addRequestReader(RequestReader requestReader) {
        requestReaders.add(requestReader);
        return this;
    }

    public VertxWebMounter addResponseWriter(ResponseWriter responseWriter) {
        responseWriters.add(responseWriter);
        return this;
    }

    public VertxWebMounter addApiDefinition(Object apiDefinition) {
        Objects.requireNonNull(apiDefinition, "API definition must not be null!");
        this.apiDefinitions.add(apiDefinition);
        return this;
    }

    public void mount(Router router) {
        List<RequestReader> requestReaders = new ArrayList<>(this.requestReaders);
        requestReaders.add(new JsonRequestReader());
        requestReaders.add(new BufferRequestReader());
        requestReaders.add(new FallbackRequestReader());
        RequestReader globalRequestReader = new CompositeRequestReader(requestReaders);

        List<ParamProviderFactory> paramProviderFactories = new ArrayList<>(this.paramProviderFactories);
        paramProviderFactories.add(new RoutingContextParamProviderFactory());
        paramProviderFactories.add(new VertxParamProviderFactory());
        paramProviderFactories.add(new HttpRequestParamProviderFactory());
        paramProviderFactories.add(new HttpResponseParamProviderFactory());
        paramProviderFactories.add(new UserParamProviderFactory());
        paramProviderFactories.add(new PathParamProviderFactory());
        paramProviderFactories.add(new QueryParamProviderFactory());
        paramProviderFactories.add(new BodyParamProviderFactory(globalRequestReader));

        List<ResponseWriter> responseWriters = new ArrayList<>(this.responseWriters);
        responseWriters.add(new ResponseEntityTypeResponseWriter());
        responseWriters.add(new BufferTypeResponseWriter());
        responseWriters.add(new JsonResponseWriter());
        responseWriters.add(new TextResponseWriter());
        responseWriters.add(new ObjectTypeResponseWriter());

        RouteDefinitionMounter routeDefinitionMounter = new RouteDefinitionMounter(routeDefinitionInvoker, paramProviderFactories, responseWriters);

        for (Object apiDefinition : apiDefinitions) {
            List<RouteDefinition> routeDefinitions = routeDefinitionFactory.createRouteDefinitions(apiDefinition);

            if (routeDefinitions.isEmpty()) {
                throw new IllegalArgumentException("Given api instance has no route definitions: " + apiDefinition);
            }

            routeDefinitions.forEach(routeDefinition -> routeDefinitionMounter.mountRoute(router, apiDefinition, routeDefinition));
        }
    }

}
