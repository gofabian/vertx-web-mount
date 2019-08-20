package gofabian.vertx.web.mount;

import gofabian.vertx.web.mount.configurator.RouteConfigurator;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import gofabian.vertx.web.mount.invoker.RouteInvoker;
import gofabian.vertx.web.mount.param.ParamProvider;
import gofabian.vertx.web.mount.param.ParamProviderFactory;
import gofabian.vertx.web.mount.response.ResponseWriter;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class RouteMounter {

    private static final Logger log = LogManager.getLogger();

    private final RouteInvoker routeInvoker;
    private final List<ParamProviderFactory> parameterProviderFactories;
    private final ResponseWriter responseWriter;
    private final List<RouteConfigurator> routeConfigurators;
    private final MountOptions options;

    public RouteMounter(RouteInvoker routeInvoker,
                        ResponseWriter responseWriter,
                        List<ParamProviderFactory> parameterProviderFactories,
                        List<RouteConfigurator> routeConfigurators,
                        MountOptions options) {
        this.routeInvoker = routeInvoker;
        this.parameterProviderFactories = parameterProviderFactories;
        this.responseWriter = responseWriter;
        this.routeConfigurators = routeConfigurators;
        this.options = options;
    }

    public Route mountRoute(Router router, Object apiDefinition, RouteDefinition routeDefinition) {
        log.info("Mount route " + routeDefinition);

        Route vertxRoute = router.route();
        routeConfigurators.forEach(configurator -> configurator.configure(routeDefinition, vertxRoute, options));
        Handler<RoutingContext> routeHandler = createRouteHandler(apiDefinition, routeDefinition);
        vertxRoute.handler(routeHandler);
        return vertxRoute;
    }

    private Handler<RoutingContext> createRouteHandler(Object apiDefinition, RouteDefinition routeDefinition) {
        if (!(routeDefinition.getContext() instanceof Method)) {
            throw new IllegalArgumentException("Context of RouteDefinition is not a " + Method.class + ":" +
                    routeDefinition.getContext());
        }
        Method method = (Method) routeDefinition.getContext();

        List<ParamProvider> paramProviders = routeDefinition.getParams()
                .stream()
                .map(this::createParameterProvider)
                .collect(Collectors.toList());

        return context -> {
            try {
                handleRoute(apiDefinition, method, paramProviders, context);
            } catch (Exception e) {
                context.fail(e);
            }
        };
    }

    private ParamProvider createParameterProvider(ParamDefinition paramDefinition) {
        for (ParamProviderFactory factory : parameterProviderFactories) {
            if (factory.supports(paramDefinition)) {
                return factory.createParamProvider(paramDefinition);
            }
        }
        throw new IllegalArgumentException("No ParameterProvider for param definition: " + paramDefinition);
    }

    private void handleRoute(Object apiDefinition, Method method, List<ParamProvider> paramProviders,
                             RoutingContext context) throws Exception {
        Object[] args = paramProviders.stream()
                .map(p -> p.provide(context))
                .toArray();

        routeInvoker.invoke(apiDefinition, method, args)
                .map(result -> {
                    handleResult(context, result);
                    return null;
                })
                .otherwise(e -> {
                    context.fail(e);
                    return null;
                });
    }

    private void handleResult(RoutingContext context, Object result) {
        HttpServerResponse response = context.response();
        if (response.ended()) {
            if (result != null) {
                log.warn("HTTP response ended before result could be written: " + result);
            }
            return;
        }

        boolean written = responseWriter.write(context, result, responseWriter);

        if (!written && result != null) {
            throw new IllegalArgumentException("Missing writer for response: " + result);
        }

        if (!response.ended()) {
            if (result != null) {
                log.warn("Response written successfully but RoutingContext not ended");
            }
            response.end();
        }
    }

}