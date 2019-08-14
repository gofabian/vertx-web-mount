package gofabian.vertx.web.mount.response;

import io.vertx.ext.web.RoutingContext;

public class ResponseEntityTypeResponseWriter implements ResponseWriter {
    @Override
    public boolean write(RoutingContext context, Object result, ResponseWriter delegate) {
        if (!(result instanceof ResponseEntity)) {
            return false;
        }

        ResponseEntity responseEntity = (ResponseEntity) result;

        context.response()
                .setStatusCode(responseEntity.getStatus())
                .headers().addAll(responseEntity.getHeaders());

        return delegate.write(context, responseEntity.getBody(), delegate);
    }
}
