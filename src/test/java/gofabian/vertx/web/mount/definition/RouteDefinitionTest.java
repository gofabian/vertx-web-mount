package gofabian.vertx.web.mount.definition;

import org.junit.Test;

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

}
