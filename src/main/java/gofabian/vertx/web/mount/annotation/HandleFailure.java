package gofabian.vertx.web.mount.annotation;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(HandleFailureN.class)
@Documented
public @interface HandleFailure {
    Class<? extends Handler<RoutingContext>> value();
}
