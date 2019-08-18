package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RouteDefinitionFactoryImplTest {

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
        public abstract void abstractMethod();
    }

    interface ApiInterface {
        void interfaceMethod();
    }

    static class ObjectApi {
        @Override
        public String toString() {
            return super.toString();
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    private final RouteDefinitionFactoryImpl factory = new RouteDefinitionFactoryImpl();
    private final MountOptions options = new MountOptions();

    private final RouteParser parser = new RouteParser() {
        @Override
        public void visitClass(Class<?> clazz, RouteDefinition routeDefinition, MountOptions options) {
            routeDefinition.setPath("/" + clazz.getSimpleName());
        }

        @Override
        public void visitMethod(Method method, RouteDefinition routeDefinition, MountOptions options) {
            routeDefinition.setPath("/" + method.getName());
            routeDefinition.getMethods().add(HttpMethod.POST);
        }

        @Override
        public void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result) {
            result.setPath(parent.getPath() + child.getPath());
            result.setMethods(child.getMethods());
            result.setParams(child.getParams());
        }

        @Override
        public void visitParameter(Parameter parameter, ParamDefinition paramDefinition, MountOptions options) {
            paramDefinition.setName(parameter.getType().getSimpleName());
        }
    };

    @Test
    public void isMethodSupported() throws NoSuchMethodException {
        assertTrue(factory.isMethodSupported(ApiClass.class.getDeclaredMethod("publicMethod")));
        assertFalse(factory.isMethodSupported(ApiClass.class.getDeclaredMethod("privateMethod")));
        assertFalse(factory.isMethodSupported(ApiClass.class.getDeclaredMethod("nativeMethod")));
        assertFalse(factory.isMethodSupported(ApiClass.class.getDeclaredMethod("staticMethod")));
        assertFalse(factory.isMethodSupported(AbstractApiClass.class.getDeclaredMethod("abstractMethod")));
        assertFalse(factory.isMethodSupported(ApiInterface.class.getDeclaredMethod("interfaceMethod")));
    }

    @Test
    public void skipObjectMethods() throws NoSuchMethodException {
        assertFalse(factory.isMethodSupported(ObjectApi.class.getMethod("toString")));
        assertFalse(factory.isMethodSupported(ObjectApi.class.getMethod("hashCode")));
        assertFalse(factory.isMethodSupported(ObjectApi.class.getMethod("equals", Object.class)));
    }

    @Test
    public void parseClass() {
        RouteDefinition classDefinition = factory.parseClass(new ApiClass(), parser, options);
        assertEquals("/ApiClass", classDefinition.getPath());
    }

    @Test
    public void parseMethod() throws NoSuchMethodException {
        class Api {
            public void route(String p1, long p2) {
            }
        }

        Method method = Api.class.getMethod("route", String.class, long.class);
        RouteDefinition methodDefinition = factory.parseMethod(method, parser, options);

        assertEquals("/route", methodDefinition.getPath());
        assertEquals(2, methodDefinition.getParams().size());
        assertEquals("String", methodDefinition.getParams().get(0).getName());
        assertEquals("long", methodDefinition.getParams().get(1).getName());
    }

    @Test
    public void isValidRouteDefinition() {
        RouteDefinition valid = new RouteDefinition().setMethods(Arrays.asList(HttpMethod.OPTIONS));
        assertTrue(factory.isValidRouteDefinition(valid));

        RouteDefinition invalid = new RouteDefinition();
        assertFalse(factory.isValidRouteDefinition(invalid));
    }

    @Test
    public void createRouteDefinitions() {
        class Api {
            public void route(Object body) {
            }

            protected void noRoute(Object noBody) {
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }

        List<RouteDefinition> list = factory.create(new Api(), parser, options);

        assertEquals(1, list.size());
        assertEquals("/Api/route", list.get(0).getPath());
        assertEquals(1, list.get(0).getParams().size());
        assertEquals("Object", list.get(0).getParams().get(0).getName());
    }

}
