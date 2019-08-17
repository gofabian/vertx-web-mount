package gofabian.vertx.web.mount.parse;

import gofabian.vertx.web.mount.definition.RouteDefinition;

import java.util.List;

public interface RouteDefinitionFactory {

    List<RouteDefinition> create(Object api, RouteParser parser, ParseOptions options);

}
