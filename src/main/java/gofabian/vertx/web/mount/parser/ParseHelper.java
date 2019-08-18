package gofabian.vertx.web.mount.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParseHelper {

    public static String combinePath(String parent, String child) {
        if (parent == null) return child;
        if (child == null) return parent;

        // parent + child != null
        if ("/".equals(parent)) return child;
        if ("/".equals(child)) return parent;

        // parent + child != "/"
        return parent + child;
    }

    public static void splitByComma(String[] texts, List<String> target) {
        for (String consumes : texts) {
            splitByComma(consumes, target);
        }
    }

    public static void splitByComma(String text, List<String> target) {
        String[] parts = text.trim().split("\\s*,\\s*");
        target.addAll(Arrays.asList(parts));
    }

    public static <T> List<T> combineList(List<T> a, List<T> b) {
        List<T> list = new ArrayList<>(a);
        list.addAll(b);
        return list;
    }

}
