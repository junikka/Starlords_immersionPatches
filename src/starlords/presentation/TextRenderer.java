package starlords.presentation;

import java.util.*;
import java.util.regex.*;

/** Literal, single-pass formatting. Inserted values are never parsed again. */
public final class TextRenderer {
    private static final Pattern NAMED_TOKEN = Pattern.compile("\\{[A-Za-z][A-Za-z0-9_.]*\\}");
    private TextRenderer() {}

    /** Inspect the template, never inserted names or other literal values. */
    public static Set<String> missingNamedTokens(String template, Map<String, String> values) {
        Set<String> missing = new LinkedHashSet<>();
        Matcher matcher = NAMED_TOKEN.matcher(Objects.toString(template, ""));
        while (matcher.find()) {
            String token = matcher.group();
            if (!values.containsKey(token) || values.get(token) == null) missing.add(token);
        }
        return missing;
    }

    public static Set<String> namedTokens(String template) {
        return missingNamedTokens(template, Map.of());
    }

    public static String render(String template, Map<String, String> values) {
        if (template == null) return "";
        List<String> keys = new ArrayList<>();
        for (String key : values.keySet()) if (key != null && !key.isEmpty()) keys.add(key);
        keys.sort(Comparator.comparingInt(String::length).reversed().thenComparing(String::compareTo));
        StringBuilder out = new StringBuilder();
        for (int pos = 0; pos < template.length();) {
            String match = null;
            for (String key : keys) {
                if (!template.startsWith(key, pos)) continue;
                int end = pos + key.length();
                // A legacy marker is a whole token: %NAME cannot consume
                // %NAME_FIRST, nor %c1 consume %c10 when that key is absent.
                if (key.startsWith("%") && word(key.charAt(key.length() - 1))
                        && end < template.length() && word(template.charAt(end))) continue;
                match = key;
                break;
            }
            if (match == null) out.append(template.charAt(pos++));
            else {
                out.append(Objects.toString(values.get(match), ""));
                pos += match.length();
            }
        }
        return out.toString();
    }

    private static boolean word(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    public static String positional(String template, String... values) {
        if (template == null) return null;
        StringBuilder out = new StringBuilder();
        int start = 0, value = 0, found;
        while ((found = template.indexOf("$string", start)) >= 0) {
            out.append(template, start, found);
            out.append(value < values.length ? Objects.toString(values[value++], "unavailable") : "unavailable");
            start = found + "$string".length();
        }
        return out.append(template, start, template.length()).toString();
    }
}
