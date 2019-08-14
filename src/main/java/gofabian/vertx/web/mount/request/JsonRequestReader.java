package gofabian.vertx.web.mount.request;

import com.fasterxml.jackson.databind.JavaType;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Handles all incoming requests with content-type "application/json".
 */
public class JsonRequestReader implements RequestReader {
    @Override
    public boolean supports(RoutingContext context, Type type) {
        String contentType = context.request().getHeader("content-type");
        return contentType != null && contentType.startsWith("application/json");
    }

    @Override
    public Object read(RoutingContext context, Type type, RequestReader delegate) {
        String body = context.getBodyAsString();

        JavaType javaType = Json.mapper.constructType(type);

        try {
            return Json.mapper.readValue(body, javaType);
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON decode error", e);
        }
    }

}
