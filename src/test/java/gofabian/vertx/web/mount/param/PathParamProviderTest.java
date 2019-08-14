package gofabian.vertx.web.mount.param;

import gofabian.vertx.web.mount.definition.ParamCategory;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import io.vertx.ext.web.RoutingContext;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PathParamProviderTest {

    private final PathParamProviderFactory factory = new PathParamProviderFactory();

    @Test
    public void testSupportsPath() {
        assertTrue(factory.supports(new ParamDefinition().setCategory(ParamCategory.PATH)));
        assertFalse(factory.supports(new ParamDefinition().setCategory(ParamCategory.BODY)));
    }

    @Test(expected = Exception.class)
    public void testRequiredName() {
        ParamDefinition definition = new ParamDefinition()
                .setName(null)
                .setType(String.class);

        factory.createParamProvider(definition);
    }

    @Test(expected = Exception.class)
    public void testRequiredType() {
        ParamDefinition definition = new ParamDefinition()
                .setName("name")
                .setType(null);

        factory.createParamProvider(definition);
    }

    @Test(expected = Exception.class)
    public void testMissingDefaultValue() {
        ParamDefinition definition = new ParamDefinition()
                .setName("name")
                .setType(String.class)
                .setMandatory(false)
                .setDefaultValue(null);

        factory.createParamProvider(definition);
    }

    @Test(expected = Exception.class)
    public void testWrongDefaultValueType() {
        ParamDefinition definition = new ParamDefinition()
                .setName("name")
                .setType(String.class)
                .setMandatory(false)
                .setDefaultValue(Long.class);

        factory.createParamProvider(definition);
    }

    @Test
    public void testReadPathParameter() {
        ParamDefinition definition = new ParamDefinition()
                .setName("id")
                .setType(int.class);

        ParamProvider provider = factory.createParamProvider(definition);

        RoutingContext context = mock(RoutingContext.class);
        when(context.pathParam("id")).thenReturn("42");

        assertEquals(42, provider.provide(context));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConversionError() {
        ParamProvider provider = null;
        try {
            ParamDefinition definition = new ParamDefinition()
                    .setName("id")
                    .setType(int.class);
            
            provider = factory.createParamProvider(definition);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            fail("Caught exception: " + e.getMessage());
        }

        RoutingContext context = mock(RoutingContext.class);
        when(context.pathParam("id")).thenReturn("no-integer");

        assertEquals(42, provider.provide(context));
    }

    @Test
    public void testDefaultValue() {
        ParamDefinition definition = new ParamDefinition()
                .setName("id")
                .setType(int.class)
                .setMandatory(false)
                .setDefaultValue("1337");

        ParamProvider provider = factory.createParamProvider(definition);

        RoutingContext context = mock(RoutingContext.class);
        when(context.pathParam("id")).thenReturn(null);

        assertEquals(1337, provider.provide(context));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultValueConversionError() {
        ParamProvider provider = null;
        try {
            ParamDefinition definition = new ParamDefinition()
                    .setName("id")
                    .setType(int.class)
                    .setMandatory(false)
                    .setDefaultValue("no-int");

            provider = factory.createParamProvider(definition);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            fail("Caught exception: " + e.getMessage());
        }

        RoutingContext context = mock(RoutingContext.class);
        when(context.pathParam("id")).thenReturn(null);

        provider.provide(context);
    }

}
