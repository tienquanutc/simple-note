package utils;


import java.util.regex.Pattern;

public class UrlSlugger {

    private final static Pattern PATTERN_NORMALIZE_HYPHEN_SEPARATOR = Pattern.compile("[[\\p{Punct}]\\s+]+");
    private final static Pattern PATTERN_NORMALIZE_TRIM_DASH = Pattern.compile("^-|-$");

    private final static String EMPTY = "";
    private static final String HYPHEN = "-";

    public static String slug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        input = PATTERN_NORMALIZE_HYPHEN_SEPARATOR.matcher(input).replaceAll(HYPHEN);
        input = PATTERN_NORMALIZE_TRIM_DASH.matcher(input).replaceAll(EMPTY);
        return input.toLowerCase();
    }

}
