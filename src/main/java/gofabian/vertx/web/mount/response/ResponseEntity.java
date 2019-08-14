package gofabian.vertx.web.mount.response;

import io.vertx.core.MultiMap;

public class ResponseEntity<T> {

    // todo: add content type?

    private final int status;
    private final Object body;
    private final MultiMap headers = MultiMap.caseInsensitiveMultiMap();

    private ResponseEntity(int status, Object body) {
        this.status = status;
        this.body = body;
    }

    public static <T> ResponseEntity<T> success(int status, T body) {
        return new ResponseEntity<>(status, body);
    }

    public static <T> ResponseEntity<T> error(int status, Object body) {
        return new ResponseEntity<>(status, body);
    }

    public static <T> ResponseEntity<T> ok(T body) {
        return success(200, body);
    }

    public static <T> ResponseEntity<T> created(T body) {
        return success(201, body);
    }

    public static <T> ResponseEntity<T> accepted(T body) {
        return success(202, body);
    }

    public static <T> ResponseEntity<T> noContent() {
        return success(204, null);
    }

    public static <T> ResponseEntity<T> badRequest(Object body) {
        return error(400, body);
    }

    public static <T> ResponseEntity<T> unauthorized(Object body) {
        return error(401, body);
    }

    public static <T> ResponseEntity<T> forbidden(Object body) {
        return error(403, body);
    }

    public static <T> ResponseEntity<T> notFound(Object body) {
        return error(404, body);
    }

    public static <T> ResponseEntity<T> serverError(Object body) {
        return error(500, body);
    }

    public ResponseEntity<T> addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public MultiMap getHeaders() {
        return headers;
    }

    public int getStatus() {
        return status;
    }

    public Object getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ResponseEntity{" +
                "status=" + status +
                ", body=" + body +
                ", headers=" + headers +
                '}';
    }
}
