package gofabian.vertx.web.mount.parser;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface HandleN {
    Handle[] value();
}