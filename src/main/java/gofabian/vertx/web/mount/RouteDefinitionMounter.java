package gofabian.vertx.web.mount;

import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import gofabian.vertx.web.mount.param.ParamProvider;
import gofabian.vertx.web.mount.param.ParamProviderFactory;
import gofabian.vertx.web.mount.response.CompositeResponseWriter;
import gofabian.vertx.web.mount.response.ResponseWriter;
import gofabian.vertx.web.mount.security.AllowedAuthoritiesHandler;
import gofabian.vertx.web.mount.security.RequiredAuthoritiesHandler;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

import static io.vertx.core.http.HttpHeaders.CONTENT_LENGTH;

public class RouteDefinitionMounter {

    private static final Logger log = LogManager.getLogger();

    private final RouteDefinitionInvoker routeDefinitionInvoker;
    private final List<ParamProviderFactory> parameterProviderFactories;
    private final ResponseWriter responseWriter;

    public RouteDefinitionMounter(RouteDefinitionInvoker routeDefinitionInvoker,
                                  List<ParamProviderFactory> parameterProviderFactories,
                                  List<ResponseWriter> responseWriters) {
        this.routeDefinitionInvoker = routeDefinitionInvoker;
        this.parameterProviderFactories = parameterProviderFactories;
        this.responseWriter = new CompositeResponseWriter(responseWriters);
    }

    public Route mountRoute(Router router, Object apiDefinition, RouteDefinition routeDefinition) {
        log.info("Mount route " + routeDefinition);

        Route vertxRoute = router.route(routeDefinition.getPath());
        routeDefinition.getMethods().forEach(m -> vertxRoute.method(m));
        routeDefinition.getConsumes().forEach(vertxRoute::consumes);
        routeDefinition.getProduces().forEach(vertxRoute::produces);

        addIntermediateRouteHandlers(routeDefinition, vertxRoute);

        Handler<RoutingContext> routeHandler = createRouteHandler(apiDefinition, routeDefinition);
        vertxRoute.handler(routeHandler);
        return vertxRoute;
    }

    private void addIntermediateRouteHandlers(RouteDefinition routeDefinition, Route route) {
        // read request body
        route.handler(BodyHandler.create());

        // set negotiated content-type
        route.handler(ResponseContentTypeHandler.create());

        // set acceptable content-type fallback
        if (!routeDefinition.getProduces().isEmpty()) {
            String fallbackContentType = routeDefinition.getProduces().get(0);
            route.handler(context -> {
                if (context.getAcceptableContentType() == null) {
                    log.debug("No content negotiation, use content-type fallback: " + fallbackContentType);
                    context.setAcceptableContentType(fallbackContentType);
                }
                context.next();
            });
        }

        // status 200 + no response body -> status 204
        route.handler(context -> {
            context.addHeadersEndHandler(x -> {
                if ("0".equals(context.response().headers().get(CONTENT_LENGTH))
                        && context.response().getStatusCode() == 200) {
                    context.response().setStatusCode(204);
                }
            });
            context.next();
        });

        // security
        if (!routeDefinition.getAllowedAuthorities().isEmpty()) {
            route.handler(new AllowedAuthoritiesHandler(routeDefinition.getAllowedAuthorities()));
        }
        if (!routeDefinition.getRequiredAuthorities().isEmpty()) {
            route.handler(new RequiredAuthoritiesHandler(routeDefinition.getRequiredAuthorities()));
        }
    }

    private Handler<RoutingContext> createRouteHandler(Object apiDefinition, RouteDefinition routeDefinition) {
        List<ParamProvider> paramProviders = routeDefinition.getParams()
                .stream()
                .map(this::createParameterProvider)
                .collect(Collectors.toList());

        return context -> {
            try {
                handleRoute(apiDefinition, routeDefinition, paramProviders, context);
            } catch (Exception e) {
                handleException(context, e);
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

    private void handleRoute(Object apiDefinition, RouteDefinition routeDefinition, List<ParamProvider> paramProviders,
                             RoutingContext context) throws Exception {
        Object[] args = paramProviders.stream()
                .map(p -> p.provide(context))
                .toArray();

        routeDefinitionInvoker.invoke(apiDefinition, routeDefinition, context, args)
                .map(result -> {
                    handleResult(context, result);
                    return null;
                })
                .otherwise(e -> {
                    handleException(context, e);
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

    private void handleException(RoutingContext context, Throwable e) {
        log.error("Unexpected exception", e);

        HttpServerResponse response = context.response();
        if (!response.ended()) {
            String message = e.getClass().getName() + ": " + e.getMessage();

            response.putHeader("content-type", "text/plain")
                    .setStatusCode(500)
                    .end(message);
        }
    }

}
