package gofabian.vertx.web.mount.parser;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ParseHelperTest {

    @Test
    public void combinePath() {
        assertNull(ParseHelper.combinePath(null, null));
        assertEquals("/path", ParseHelper.combinePath(null, "/path"));
        assertEquals("/path", ParseHelper.combinePath("/path", null));
        assertEquals("/path/sub", ParseHelper.combinePath("/path", "/sub"));
        assertEquals("/sub", ParseHelper.combinePath("/", "/sub"));
        assertEquals("/path", ParseHelper.combinePath("/path", "/"));
    }

    @Test
    public void splitStringByComma() {
        List<String> target = new ArrayList<>();
        ParseHelper.splitByComma("good, old, text  ,,yes", target);
        assertEquals(Arrays.asList("good", "old", "text", "", "yes"), target);
    }

    @Test
    public void splitStringsByComma() {
        List<String> target = new ArrayList<>();
        ParseHelper.splitByComma(new String[]{"yes,no", "maybe"}, target);
        assertEquals(Arrays.asList("yes", "no", "maybe"), target);
    }

}
