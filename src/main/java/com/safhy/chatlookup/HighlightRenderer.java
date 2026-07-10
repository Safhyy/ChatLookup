package com.safhy.chatlookup;

import com.safhy.chatlookup.mixin.ChatHudAccessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.ChatComponent;
//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?} else {
/*import net.minecraft.client.GuiMessage;
*///?}
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
//? if >=1.21.6 {
import org.joml.Matrix3x2fStack;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class HighlightRenderer {
    private static final int HIGHLIGHT_COLOR = 0x5FFFE14D;
    private static final int CACHE_LIMIT = 4096;
    private static final int[] NO_RANGES = new int[0];

    private static final Map<GuiMessage.Line, int[]> PIXEL_CACHE = new IdentityHashMap<>();

    //? if >=26.1 {
    public static void render(GuiGraphicsExtractor context, Font font, Minecraft minecraft, int windowHeight) {
    //?} else {
    /*public static void render(GuiGraphics context, Font font, Minecraft minecraft, int windowHeight) {
    *///?}
        ChatComponent chatHud = ChatLookup.getChat(minecraft);
        ChatHudAccessor hud = (ChatHudAccessor) chatHud;
        List<GuiMessage.Line> visible = hud.chatlookup$getVisibleMessages();
        int scrolled = hud.chatlookup$getScrolledLines();
        int onScreen = Math.min(visible.size() - scrolled, chatHud.getLinesPerPage());
        if (onScreen <= 0) {
            return;
        }

        float scale = (float) hud.chatlookup$getChatScale();
        int chatBottom = Mth.floor((windowHeight - 40) / scale);
        int lineHeight = hud.chatlookup$getLineHeight();
        double spacing = minecraft.options.chatLineSpacing().get();
        int textOffset = (int) Math.round(8.0 * (spacing + 1.0) - 4.0 * spacing);

        //? if >=1.21.6 {
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.scale(scale, scale);
        matrices.translate(4.0F, 0.0F);
        //?} else {
        /*PoseStack matrices = context.pose();
        matrices.pushPose();
        matrices.scale(scale, scale, 1.0F);
        matrices.translate(4.0F, 0.0F, 0.0F);
        *///?}
        for (int slot = 0; slot < onScreen; slot++) {
            GuiMessage.Line line = visible.get(slot + scrolled);
            int[] ranges = pixelRanges(line, font);
            if (ranges.length == 0) {
                continue;
            }
            int top = chatBottom - slot * lineHeight - textOffset;
            for (int i = 0; i < ranges.length; i += 2) {
                context.fill(ranges[i], top - 1, ranges[i + 1] + 1, top + 9, HIGHLIGHT_COLOR);
            }
        }
        //? if >=1.21.6 {
        matrices.popMatrix();
        //?} else {
        /*matrices.popPose();
        *///?}
    }

    private static int[] pixelRanges(GuiMessage.Line line, Font font) {
        int[] cached = PIXEL_CACHE.get(line);
        if (cached != null) {
            return cached;
        }
        if (PIXEL_CACHE.size() > CACHE_LIMIT) {
            PIXEL_CACHE.clear();
        }
        int[] result = computePixelRanges(line.content(), font);
        PIXEL_CACHE.put(line, result);
        return result;
    }

    private static int[] computePixelRanges(FormattedCharSequence content, Font font) {
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
            int x1 = width(font, styles, codePoints, 0, fromGlyph);
            int x2 = x1 + width(font, styles, codePoints, fromGlyph, toGlyph);
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

    private static int width(Font font, List<Style> styles, IntArrayList codePoints, int from, int to) {
        if (to <= from) {
            return 0;
        }
        FormattedCharSequence part = visitor -> {
            for (int i = from; i < to; i++) {
                if (!visitor.accept(i - from, styles.get(i), codePoints.getInt(i))) {
                    return false;
                }
            }
            return true;
        };
        return font.width(part);
    }

    private HighlightRenderer() {
    }
}
