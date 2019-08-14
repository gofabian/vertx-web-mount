package gofabian.vertx.web.mount.param;

import io.vertx.ext.web.RoutingContext;

public interface ParamProvider {

    Object provide(RoutingContext context);

}
