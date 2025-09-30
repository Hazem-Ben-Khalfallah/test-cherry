package com.blacknebula.testcherry.util;

public final class CommentStringEscaper {
    private CommentStringEscaper() {}

    // Build a Java string literal for use in source (assumes input originates from JavaDoc comments).
    // Since JavaDoc typically won't include raw backslash-u Unicode escape sequences, we can simply escape and wrap once.
    public static String toJavaStringConstantExpression(String s) {
        if (s == null || s.isEmpty()) return "\"\""; // empty literal ""
        return "\"" + escapeForJavaString(s) + "\"";
    }

    // Minimal, robust escaping for Java string literals used in source code
    public static String escapeForJavaString(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\r': sb.append("\\r"); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    // Escape ISO control chars (0x00-0x1F, 0x7F-0x9F) and Unicode LS/PS if present
                    if (Character.isISOControl(c) || c == '\u2028' || c == '\u2029') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
