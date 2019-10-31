package gofabian.vertx.web.mount.annotation;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(HandleN.class)
@Documented
public @interface Handle {
    Class<? extends Handler<RoutingContext>> value() default NoHandler.class;

    String name() default "";

    boolean blocking() default false;

    boolean ordered() default true;

    interface NoHandler extends Handler<RoutingContext> {
    }
}
