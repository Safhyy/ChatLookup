package com.safhy.chatlookup;

import com.safhy.chatlookup.mixin.ChatHudAccessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class HighlightRenderer {
    private static final int HIGHLIGHT_COLOR = 0x5FFFE14D;
    private static final int CACHE_LIMIT = 4096;
    private static final int[] NO_RANGES = new int[0];

    private static final Map<ChatHudLine.Visible, int[]> PIXEL_CACHE = new IdentityHashMap<>();

    public static void render(DrawContext context, TextRenderer textRenderer, MinecraftClient client, int windowHeight) {
        ChatHud chatHud = client.inGameHud.getChatHud();
        ChatHudAccessor hud = (ChatHudAccessor) chatHud;
        List<ChatHudLine.Visible> visible = hud.chatlookup$getVisibleMessages();
        int scrolled = hud.chatlookup$getScrolledLines();
        int onScreen = Math.min(visible.size() - scrolled, chatHud.getVisibleLineCount());
        if (onScreen <= 0) {
            return;
        }

        // Mirrors ChatHud.render's geometry: scale(f, f) then translate(4, 0);
        // slot s has its text top at chatBottom - s * lineHeight - textOffset.
        float scale = (float) hud.chatlookup$getChatScale();
        int chatBottom = MathHelper.floor((windowHeight - 40) / scale);
        int lineHeight = hud.chatlookup$getLineHeight();
        double spacing = client.options.getChatLineSpacing().getValue();
        int textOffset = (int) Math.round(8.0 * (spacing + 1.0) - 4.0 * spacing);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(scale, scale);
        matrices.translate(4.0F, 0.0F);
        for (int slot = 0; slot < onScreen; slot++) {
            ChatHudLine.Visible line = visible.get(slot + scrolled);
            int[] ranges = pixelRanges(line, textRenderer);
            if (ranges.length == 0) {
                continue;
            }
            int top = chatBottom - slot * lineHeight - textOffset;
            for (int i = 0; i < ranges.length; i += 2) {
                context.fill(ranges[i], top - 1, ranges[i + 1] + 1, top + 9, HIGHLIGHT_COLOR);
            }
        }
        matrices.popMatrix();
    }

    private static int[] pixelRanges(ChatHudLine.Visible line, TextRenderer textRenderer) {
        int[] cached = PIXEL_CACHE.get(line);
        if (cached != null) {
            return cached;
        }
        if (PIXEL_CACHE.size() > CACHE_LIMIT) {
            PIXEL_CACHE.clear();
        }
        int[] result = computePixelRanges(line.content(), textRenderer);
        PIXEL_CACHE.put(line, result);
        return result;
    }

    private static int[] computePixelRanges(OrderedText content, TextRenderer textRenderer) {
        List<Style> styles = new ArrayList<>();
        IntArrayList codePoints = new IntArrayList();
        IntArrayList charStarts = new IntArrayList();
        StringBuilder flattened = new StringBuilder();
        content.accept((index, style, codePoint) -> {
            styles.add(style);
            codePoints.add(codePoint);
            charStarts.add(flattened.length());
            flattened.appendCodePoint(codePoint);
            return true;
        });

        int[] charRanges = ChatLookup.findMatches(flattened.toString());
        if (charRanges.length == 0) {
            return NO_RANGES;
        }

        IntArrayList pixels = new IntArrayList(charRanges.length);
        for (int i = 0; i < charRanges.length; i += 2) {
            int fromGlyph = glyphIndexAt(charStarts, charRanges[i]);
            int toGlyph = glyphIndexAt(charStarts, charRanges[i + 1]);
            if (toGlyph <= fromGlyph) {
                continue;
            }
            int x1 = width(textRenderer, styles, codePoints, 0, fromGlyph);
            int x2 = x1 + width(textRenderer, styles, codePoints, fromGlyph, toGlyph);
            pixels.add(x1);
            pixels.add(x2);
        }
        return pixels.toIntArray();
    }

    private static int glyphIndexAt(IntArrayList charStarts, int charIndex) {
        for (int g = 0; g < charStarts.size(); g++) {
            if (charStarts.getInt(g) >= charIndex) {
                return g;
            }
        }
        return charStarts.size();
    }

    private static int width(TextRenderer textRenderer, List<Style> styles, IntArrayList codePoints, int from, int to) {
        if (to <= from) {
            return 0;
        }
        OrderedText part = visitor -> {
            for (int i = from; i < to; i++) {
                if (!visitor.accept(i - from, styles.get(i), codePoints.getInt(i))) {
                    return false;
                }
            }
            return true;
        };
        return textRenderer.getWidth(part);
    }

    private HighlightRenderer() {
    }
}
