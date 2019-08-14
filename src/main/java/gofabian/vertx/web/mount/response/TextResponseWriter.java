package gofabian.vertx.web.mount.response;

import io.vertx.ext.web.RoutingContext;

public class TextResponseWriter implements ResponseWriter {
    @Override
    public boolean write(RoutingContext context, Object result, ResponseWriter delegate) {
        String contentType = context.getAcceptableContentType();

        if (result instanceof String && contentType != null && contentType.startsWith("text/")) {

            if (contentType.equals("text/*")) contentType = "text/plain";

            context.response()
                    .putHeader("content-type", contentType)
                    .end((String) result);

            return true;
        }
        return false;
    }
}
