
# Vert.x Web Mounter

[![](https://jitpack.io/v/gofabian/vertx-web-mount.svg)](https://jitpack.io/#gofabian/vertx-web-mount)
[![](https://jitci.com/gh/gofabian/vertx-web-mount/svg)](https://jitci.com/gh/gofabian/vertx-web-mount)

This library enables you to define [Vert.x] web routes with [JAX-RS annotations][JAX-RS] like this:

```java
public class HelloWorldApi {
    @GET
    @Path("/hello")
    @Produces("text/plain")
    public String helloWorld() {
        return "Hello World!";
    }
}
```


## Vert.x and Annotations? WTH!

A Vert.x application consists of explicit code without annotations. That's great! But [Spring MVC] and [JAX-RS] show that annotations can be quite useful to define web APIs.

This library is intended for developers who want...

- Vert.x web routes defined with annotations
- standardized JAX-RS annotations
- support for all Vert.x web features
- a reactive code style
- a highly customizable library
- to implement a parser for route definitions with or even without annotations

It is not...

- not a full-featured implementation of JAX-RS
- not using blocking handlers by default
- not based on dependency injection (but support can be added easily)


## Getting Started with Maven

**Step 1:** Configure the Jitpack repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```


**Step 2:** Add the library to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.gofabian</groupId>
    <artifactId>vertx-web-mount</artifactId>
    <version>0.1.0</version>
</dependency>
```


## Usage

Mount your API definition to a given router:

```java
Vertx vertx = Vertx.vertx();
Router router = Router.router(vertx);

new VertxWebMounter()
    .addApiDefinition(new HelloWorldApi())
    .mount(router);

vertx.createHttpServer()
        .requestHandler(router)
        .listen(8080);
```

### Minimal route definition

A minimal route definition consists of the following parts:

- route path: `@Path("/path/to/endpoint")`
- HTTP method: at least one of `@GET`, `@POST`, `@PUT`, `@DELETE`, `@PATCH`, `@HEAD`, `@OPTIONS`

```java
@GET
@Path("/")
public void minimalDefinition() {
}
```

The `@Path` annotations from class and method are combined:

```java
@Path("/user")
class MinmalApi {
    @GET
    public void pathFromClass() {
        // -> GET /user
    }

    @GET
    @Path("/picture")
    public void pathCombination() {
        // -> GET /user/picture
    }
}
```


### Asynchronous route handlers

We advice you to use a `Future` as a return type:

```java
@GET
@Path("/good")
public Future<String> nonBlocking() {
    Future<String> future = nonBlockingCallToDatabase();
    return future;
}
```

You should never block the route handler. DON'T do stuff like this!!!

```java
@GET
@Path("/bad")
public String blocking() throws InterruptedException {
    String result = blockingCallToDatabase();   // bad!!!
    Thread.sleep(1000);                         // bad!!!
    return result;
}
```


### Request body

The request body is provided as a method parameter without an annotation.

Define accepted content types with `@Consumes`:

```java
@POST
@Path("/upload")
@Consumes("text/plain")
public void text(String requestBody) {
}
```

Supported request body readers ordered by priority:

1. by content type `application/json` with [Jackson]
2. by type `Buffer` for raw access
3. by Jackson value conversion (fallback)

As a fallback we try to convert the request body according to the parameter type with [Jackson] value conversion like this:

```java
JavaType javaType = Json.mapper.constructType(paramType);
T paramValue = Json.mapper.convertValue(string, javaType);
```


### Response body

The method return value is written into the HTTP response body.

Use `@Produces` to define the content type:

```java
@GET
@Path("/text")
@Produces("text/plain", "text/poetry")
public String text() {
    // -> Response body: 'some text'
    return "some text";
}
```

We use the official Vert.x [ResponseContentTypeHandler] to negotiate the content type according to the `Accept` header.

Supported response body writers ordered by priority:

1. by return type `Buffer` for raw access
2. by content type `application/json` with [Jackson]
3. by content type `text/*` and type `String`
4. fallback with `toString()` conversion

If the response body is empty the HTTP status will be `204 No Content`.


### JSON support

JSON content is converted with [Jackson].

Example:

```java
class User {
    String name = "george";
}

@POST
@Path("/json")
@Consumes("application/json")
@Produces("application/json")
public User text(User user) {
    // -> Response body: '{ "name": "tom" }'
    user.name = "tom";
    return user;
}
```


### Request parameters

Access path parameters with `@PathParam`:

```java
@GET
@Path("/users/:id")
public void getUser(@PathParam("id") int id) {
}
```

Access query parameters with `@QueryParam`. You can access the first occurrence of the query parameter or all occurrences as a `List`:

```java
@GET
@Path("/query")
public void text(@QueryParam("name") String name, 
                 @QueryParam("name") List<String> names) {
    // example: GET /query?name=foo&name=bar
}
```

Request parameters are mandatory by default. Make them optional with `@DefaultValue`:

```java
@GET
@Path("/query")
public void text(@QueryParam("name") @DefaultValue("smith") String name) {
}
```

The default value of `@QueryParam` may be a comma separated list.

We try to convert the request parameters according to the parameter type with [Jackson] value conversion like this:

```java
JavaType javaType = Json.mapper.constructType(paramType);
T paramValue = Json.mapper.convertValue(string, javaType);
```


### Access RoutingContext

You can access the `RoutingContext` directly. The `@Context` annotation is optional:

```java
@GET
@Path("/context")
public void context(@Context RoutingContext context) {
}
```

Additionally you can access the following context parameters:

```java
@GET
@Path("/context")
public void context(@Context Vertx vertx,
                    @Context HttpServerRequest request,
                    @Context HttpServerResponse response,
                    @Context User user) {
}
```

The parameter values are taken from the `RoutingContext` instance:

- `Vertx` from `RoutingContext::vertx()`
- `HttpServerRequest` from `RoutingContext::request()`
- `HttpServerResponse` from `RoutingContext::response()`
- `User` from `RoutingContext.user()`



## Customization

Vert.x Web Mounter is fully customizable:

```java
new VertxWebMounter()
    // support special request types
    .addRequestReader(new MyRequestReader())
    // support special response types
    .addResponseWriter(new MyResponseWriter())
    // support custom method parameters
    .addParamProviderFactory(new MyParamProviderFactory())

    // your own route definition parser
    .setRouteDefinitionFactory(new MyRouteDefinitionFactory())
    // support for Kotlin coroutines etc...
    .setRouteDefinitionInvoker(new MyRouteDefinitionInvoker())

    .addApiDefinition(new JaxRsApi())
    .mount(router);
```


### Custom request body reader

You can add a custom `RequestReader` that reads the request and provides the result as a body parameter. 

This example adds support for XML content with

- content type `application/xml` and
- method parameters of type `String`

```java
@POST
@Path("/")
@Consumes("application/xml")
public void route(String body) {
}

public class XmlRequestReader implements RequestReader {
    @Override
    public boolean supports(RoutingContext context, Type type) {
        String contentType = context.request().getHeader("content-type");
        return "application/xml".equals(contentType) && type == String.class;
    }

    @Override
    public Object read(RoutingContext context, Type type, RequestReader delegate) {
        return context.getBodyAsString();
    }
}
```

The `read` method has a delegate `RequestReader` that is a composite of all available request readers. The delegate can be helpful if you want to support wrapper classes:

```java
@POST
@Path("/")
@Consumes("application/json")
public void route(MyRequest<Person> request) {
}

public class MyRequestReader implements RequestReader {
    @Override
    public boolean supports(RoutingContext context, Type type) {
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getRawType() == MyRequest.class;
        }
        return false;
    }

    @Override
    public Object read(RoutingContext context, Type type, RequestReader delegate) {
        // MyRequest<T>  with  T == genericType
        Type genericType = ((ParameterizedType) type).getActualTypeArguments()[0];
        Object value = delegate.read(context, genericType, delegate);
        return new MyRequest(value);
    }
}
```

Have a look at the built-in sub classes of `RequestReader` for more examples.


### Custom response body writer

You can add a custom `ResponseWriter` that writes the route result to the response body:

```java
@GET
@Path("/")
@Produces("text/awesome")
public String route() {
}

public class AwesomeResponseWriter implements ResponseWriter {
    @Override
    public boolean write(RoutingContext context, Object result, ResponseWriter delegate) {
        String contentType = context.getAcceptableContentType();
        if (!"text/awesome".equals(contentType)) {
            return false;
        }

        context.response()
                .putHeader("content-type", "text/awesome")
                .end("awesome: " + result);
        return true;
    }
}
```

Have a look at the built-in sub classes of `ResponseWriter` for more examples.


### Custom parameter provider

With a custom `ParamProviderFactory` you can implement a provider for every type of method parameters e. g. body, path, query or context parameter. Or you can extend the behaviour of an existing parameter provider.

A simple provider for the `Vertx` instance:

```java
public class VertxParamProviderFactory implements ParamProviderFactory {
    @Override
    public boolean supports(ParamDefinition paramDefinition) {
        return paramDefinition.getType() == Vertx.class;
    }

    @Override
    public ParamProvider createParamProvider(ParamDefinition paramDefinition) {
        return context -> context.vertx();
    }
}
```

Have a look at the built-in sub classes of `ParamProviderFactory` for more examples.


### Implement your own parser

Interface: `RouteDefinitionFactory`

Built-in implementation: `JaxRsRouteDefinitionFactory`


### Implement your own method invoker

Interface: `RouteDefinitionInvoker`

Built-in implementation: `MethodRouteDefinitionInvoker`


## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details


[Spring MVC]: https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-controller
[JAX-RS]: https://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services
[Vert.x]: https://vertx.io/
[ResponseContentTypeHandler]: https://vertx.io/docs/apidocs/io/vertx/ext/web/handler/ResponseContentTypeHandler.html
[Jackson]: https://github.com/FasterXML/jackson
