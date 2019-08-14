package gofabian.vertx.web.mount.definition;

import io.vertx.core.http.HttpMethod;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class RouteDefinitionTest {

    @Test
    public void harmonizePath() {
        RouteDefinition routeDefinition = new RouteDefinition();

        assertEquals("/te/st", routeDefinition.setPath("te/st/").getPath());
        assertEquals("/te/st", routeDefinition.setPath("/te/st").getPath());
        assertEquals("/x", routeDefinition.setPath("/x/").getPath());
        assertEquals("/", routeDefinition.setPath("/").getPath());
        assertEquals("/", routeDefinition.setPath("").getPath());
    }

    @Test
    public void withOverwrites() {
        RouteDefinition parent = new RouteDefinition()
                .setMethods(Collections.singletonList(HttpMethod.DELETE))
                .setPath("/parent")
                .setProduces(Collections.singletonList("application/apple"))
                .setConsumes(Collections.singletonList("application/banana"));
        RouteDefinition child = new RouteDefinition()
                .setMethods(Collections.singletonList(HttpMethod.POST))
                .setPath("/child")
                .setProduces(Collections.singletonList("text/apple"))
                .setConsumes(Collections.singletonList("text/banana"));

        RouteDefinition result = parent.withSubRouteDefinition(child);
        assertEquals(Arrays.asList(HttpMethod.DELETE, HttpMethod.POST), result.getMethods());
        assertEquals("/parent/child", result.getPath());
        assertEquals(Collections.singletonList("text/apple"), result.getProduces());
        assertEquals(Collections.singletonList("text/banana"), result.getConsumes());
    }

    @Test
    public void withRootPath() {
        RouteDefinition root = new RouteDefinition().setPath("/");
        RouteDefinition noRoot = new RouteDefinition().setPath("/noroot");

        assertEquals("/noroot", root.withSubRouteDefinition(noRoot).getPath());
        assertEquals("/noroot", noRoot.withSubRouteDefinition(root).getPath());
        assertEquals("/", root.withSubRouteDefinition(root).getPath());
    }

    @Test
    public void withNoOverwrites() {
        RouteDefinition parent = new RouteDefinition()
                .setProduces(Collections.singletonList("application/apple"))
                .setConsumes(Collections.singletonList("application/banana"));
        RouteDefinition child = new RouteDefinition();

        RouteDefinition result = parent.withSubRouteDefinition(child);
        assertEquals(Collections.singletonList("application/apple"), result.getProduces());
        assertEquals(Collections.singletonList("application/banana"), result.getConsumes());
    }

}
