package gofabian.vertx.web.mount.request;

import com.fasterxml.jackson.databind.JavaType;
import io.vertx.core.json.Json;

import java.lang.reflect.Type;

public class StringReader {

    public Object read(String string, Type type) {
        JavaType javaType = Json.mapper.constructType(type);
        return Json.mapper.convertValue(string, javaType);
    }

}
