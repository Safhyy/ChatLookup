package com.safhy.chatlookup;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class HighlightRule {
    public static final int DEFAULT_COLOR = 0xFFE14D;

    private String text = "";
    private String lowerText = "";
    private int color = DEFAULT_COLOR;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private boolean strikethrough;
    private boolean regex;
    private boolean sound;

    private Pattern compiled;
    private boolean compileDirty = true;

    public String text() {
        return this.text;
    }

    public String lowerText() {
        return this.lowerText;
    }

    public void setText(String value) {
        this.text = value == null ? "" : value;
        this.lowerText = this.text.toLowerCase(Locale.ROOT);
        this.compileDirty = true;
    }

    public int color() {
        return this.color;
    }

    public void setColor(int value) {
        this.color = value & 0xFFFFFF;
    }

    public boolean bold() {
        return this.bold;
    }

    public void setBold(boolean value) {
        this.bold = value;
    }

    public boolean italic() {
        return this.italic;
    }

    public void setItalic(boolean value) {
        this.italic = value;
    }

    public boolean underline() {
        return this.underline;
    }

    public void setUnderline(boolean value) {
        this.underline = value;
    }

    public boolean strikethrough() {
        return this.strikethrough;
    }

    public void setStrikethrough(boolean value) {
        this.strikethrough = value;
    }

    public boolean regex() {
        return this.regex;
    }

    public void setRegex(boolean value) {
        this.regex = value;
        this.compileDirty = true;
    }

    public boolean sound() {
        return this.sound;
    }

    public void setSound(boolean value) {
        this.sound = value;
    }

    public boolean isUsable() {
        return !this.text.isBlank() && (!this.regex || pattern() != null);
    }

    public boolean isBroken() {
        return this.regex && !this.text.isBlank() && pattern() == null;
    }

    public Pattern pattern() {
        if (this.compileDirty) {
            this.compileDirty = false;
            this.compiled = null;
            if (this.regex && !this.text.isBlank()) {
                try {
                    this.compiled = Pattern.compile(this.text, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                } catch (PatternSyntaxException e) {
                    this.compiled = null;
                }
            }
        }
        return this.compiled;
    }

    public Style decorate(Style base) {
        Style styled = base.withColor(TextColor.fromRgb(this.color));
        if (this.bold) {
            styled = styled.withBold(true);
        }
        if (this.italic) {
            styled = styled.withItalic(true);
        }
        if (this.underline) {
            styled = styled.withUnderlined(true);
        }
        if (this.strikethrough) {
            styled = styled.withStrikethrough(true);
        }
        return styled;
    }
}
