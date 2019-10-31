package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.annotation.Blocking;
import gofabian.vertx.web.mount.definition.BlockingType;
import gofabian.vertx.web.mount.definition.RouteDefinition;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BlockingParserTest {

    private RouteParser blockingParser = new BlockingParser();
    private ParserTestUtil parserTestUtil = new ParserTestUtil(blockingParser, new MountOptions());

    @Test
    public void parseOrderedBlockingRoute() {
        RouteDefinition definition = parserTestUtil.parseMethod(Example.class, "blocking");
        assertEquals(BlockingType.BLOCKING_ORDERED, definition.getBlockingType());
    }

    @Test
    public void parseNoneBlockingRoute() {
        RouteDefinition definition = parserTestUtil.parseMethod(MergeExample.class, "merged");
        assertEquals(BlockingType.NONE_BLOCKING, definition.getBlockingType());
    }

    @Test
    public void parseUnorderedBlockingRoute() {
        RouteDefinition definition = parserTestUtil.parseMethod(MergeExample.class, "inherited");
        assertEquals(BlockingType.BLOCKING_UNORDERED, definition.getBlockingType());
    }

    @Test
    public void parseDefaultFallback() {
        MountOptions options = new MountOptions().setBlockingType(BlockingType.BLOCKING_ORDERED);
        ParserTestUtil parserTestUtil = new ParserTestUtil(blockingParser, options);
        RouteDefinition definition = parserTestUtil.parseMethod(DefaultExample.class, "none");
        assertEquals(BlockingType.BLOCKING_ORDERED, definition.getBlockingType());
    }

    @SuppressWarnings("unused")
    interface Example {
        @Blocking
        void blocking();
    }

    @SuppressWarnings("unused")
    @Blocking(ordered = false)
    interface MergeExample {
        @Blocking(false)
        void merged();

        void inherited();
    }

    @SuppressWarnings("unused")
    interface DefaultExample {
        void none();
    }
}
