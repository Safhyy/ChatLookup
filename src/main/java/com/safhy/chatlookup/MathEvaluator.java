package com.safhy.chatlookup;

import java.util.Locale;

public final class MathEvaluator {
    private static final int MAX_LENGTH = 128;

    public record Expression(int start, int end, double value) {
    }

    private final String source;
    private int position;

    private MathEvaluator(String source) {
        this.source = source;
    }

    public static Expression expressionAt(String text, int caret) {
        if (text == null || caret <= 0 || caret > text.length()) {
            return null;
        }
        char last = text.charAt(caret - 1);
        if (!isDigit(last) && last != ')') {
            return null;
        }
        int scan = caret - 1;
        while (scan > 0 && isExpressionChar(text.charAt(scan - 1))) {
            scan--;
        }
        while (scan < caret && text.charAt(scan) == ' ') {
            scan++;
        }
        for (int start = scan; start < caret; start++) {
            if (text.charAt(start) == ' ') {
                continue;
            }
            Double value = evaluate(text.substring(start, caret));
            if (value != null) {
                return new Expression(start, caret, value);
            }
        }
        return null;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isExpressionChar(char c) {
        return isDigit(c) || c == '.' || c == ' '
                || c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')';
    }

    public static Double evaluate(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH || !isSupported(trimmed) || !hasOperator(trimmed)) {
            return null;
        }
        MathEvaluator parser = new MathEvaluator(trimmed);
        Double value = parser.expression();
        if (value == null) {
            return null;
        }
        parser.skipSpaces();
        if (parser.position != trimmed.length() || value.isNaN() || value.isInfinite()) {
            return null;
        }
        return value;
    }

    public static String format(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1.0E15) {
            return Long.toString((long) value);
        }
        String text = String.format(Locale.ROOT, "%.6f", value);
        int end = text.length();
        while (end > 0 && text.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0 && text.charAt(end - 1) == '.') {
            end--;
        }
        return text.substring(0, end);
    }

    private static boolean isSupported(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!isExpressionChar(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasOperator(String text) {
        boolean digitSeen = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                digitSeen = true;
            } else if (digitSeen && (c == '+' || c == '-' || c == '*' || c == '/')) {
                return true;
            }
        }
        return false;
    }

    private Double expression() {
        Double left = term();
        if (left == null) {
            return null;
        }
        while (true) {
            skipSpaces();
            char operator = peek();
            if (operator != '+' && operator != '-') {
                return left;
            }
            this.position++;
            Double right = term();
            if (right == null) {
                return null;
            }
            left = operator == '+' ? left + right : left - right;
        }
    }

    private Double term() {
        Double left = factor();
        if (left == null) {
            return null;
        }
        while (true) {
            skipSpaces();
            char operator = peek();
            if (operator != '*' && operator != '/') {
                return left;
            }
            this.position++;
            Double right = factor();
            if (right == null) {
                return null;
            }
            if (operator == '/' && right == 0.0) {
                return null;
            }
            left = operator == '*' ? left * right : left / right;
        }
    }

    private Double factor() {
        skipSpaces();
        char c = peek();
        if (c == '+' || c == '-') {
            this.position++;
            Double value = factor();
            if (value == null) {
                return null;
            }
            return c == '-' ? -value : value;
        }
        return primary();
    }

    private Double primary() {
        skipSpaces();
        if (peek() == '(') {
            this.position++;
            Double value = expression();
            if (value == null) {
                return null;
            }
            skipSpaces();
            if (peek() != ')') {
                return null;
            }
            this.position++;
            return value;
        }
        return number();
    }

    private Double number() {
        int start = this.position;
        boolean dot = false;
        while (this.position < this.source.length()) {
            char c = this.source.charAt(this.position);
            if (c >= '0' && c <= '9') {
                this.position++;
            } else if (c == '.' && !dot) {
                dot = true;
                this.position++;
            } else {
                break;
            }
        }
        if (this.position == start) {
            return null;
        }
        try {
            return Double.parseDouble(this.source.substring(start, this.position));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private char peek() {
        return this.position < this.source.length() ? this.source.charAt(this.position) : '\0';
    }

    private void skipSpaces() {
        while (this.position < this.source.length() && this.source.charAt(this.position) == ' ') {
            this.position++;
        }
    }
}
