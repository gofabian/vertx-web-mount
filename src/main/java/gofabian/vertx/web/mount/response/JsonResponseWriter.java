package gofabian.vertx.web.mount.response;

import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

/**
 * Writes responses with content-type "application/json".
 */
public class JsonResponseWriter implements ResponseWriter {
    @Override
    public boolean write(RoutingContext context, Object result, ResponseWriter delegate) {
        if (result == null) {
            return false;
        }

        String contentType = context.getAcceptableContentType();
        if (!"application/json".equals(contentType)) {
            return false;
        }

        String json;
        try {
            json = Json.encodePrettily(result);
        } catch (EncodeException e) {
            throw new IllegalArgumentException("JSON encode error: " + e.getMessage());
        }

        context.response().putHeader("content-type", "application/json").end(json);
        return true;
    }
}
