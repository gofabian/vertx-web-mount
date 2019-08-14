package gofabian.vertx.web.mount.jaxrs;

import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MethodBasedRouteDefinitionFactoryTest {

    static class TestRouteDefinitionFactory extends MethodBasedRouteDefinitionFactory {
        @Override
        protected RouteDefinition createRouteDefinitionFromClass(Class<?> clazz) {
            return new RouteDefinition();
        }

        @Override
        protected RouteDefinition createRouteDefinitionFromMethod(Method method) {
            return new RouteDefinition()
                    .setPath("/")
                    .setMethods(Arrays.asList(HttpMethod.POST));
        }
    }

    static class ApiClass {
        public void publicMethod() {
        }

        private void privateMethod() {
        }

        native public void nativeMethod();

        static public void staticMethod() {
        }
    }

    static abstract class AbstractApiClass {
        @PostConstruct
        public abstract void abstractMethod();
    }

    interface ApiInterface {
        @PostConstruct
        void interfaceMethod();
    }

    private MethodBasedRouteDefinitionFactory routeFactory = new TestRouteDefinitionFactory();

    @Test
    public void isApiMethod() throws NoSuchMethodException {
        assertTrue(routeFactory.isSupportedMethod(ApiClass.class.getDeclaredMethod("publicMethod")));
        assertFalse(routeFactory.isSupportedMethod(ApiClass.class.getDeclaredMethod("privateMethod")));
        assertFalse(routeFactory.isSupportedMethod(ApiClass.class.getDeclaredMethod("nativeMethod")));
        assertFalse(routeFactory.isSupportedMethod(ApiClass.class.getDeclaredMethod("staticMethod")));
        assertFalse(routeFactory.isSupportedMethod(ApiInterface.class.getDeclaredMethod("interfaceMethod")));
        assertFalse(routeFactory.isSupportedMethod(AbstractApiClass.class.getDeclaredMethod("abstractMethod")));
    }

    @Test
    public void createRoutes() {
        List<RouteDefinition> routeDefinitions = routeFactory.createRouteDefinitions(new ApiClass());

        assertEquals(1, routeDefinitions.size());
        RouteDefinition routeDefinition = routeDefinitions.get(0);
        assertEquals(Collections.singletonList(HttpMethod.POST), routeDefinition.getMethods());
    }

}
