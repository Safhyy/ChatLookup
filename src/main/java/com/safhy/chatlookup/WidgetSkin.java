package com.safhy.chatlookup;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}

public final class WidgetSkin {
    public static final int ACCENT = 0xFFFFD24A;
    public static final int BORDER_IDLE = 0xFF4A4A54;
    public static final int BORDER_HOVER = 0xFF9A9AA6;
    public static final int LABEL_IDLE = 0xFFB0B0B8;

    //? if >=26.1 {
    public static void drawPanel(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int background, int border) {
    //?} else {
    /*public static void drawPanel(GuiGraphics context, int x1, int y1, int x2, int y2, int background, int border) {
    *///?}
        context.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, background);
        context.fill(x1 + 2, y1, x2 - 2, y1 + 1, border);
        context.fill(x1 + 2, y2 - 1, x2 - 2, y2, border);
        context.fill(x1, y1 + 2, x1 + 1, y2 - 2, border);
        context.fill(x2 - 1, y1 + 2, x2, y2 - 2, border);
        context.fill(x1 + 1, y1 + 1, x1 + 2, y1 + 2, border);
        context.fill(x2 - 2, y1 + 1, x2 - 1, y1 + 2, border);
        context.fill(x1 + 1, y2 - 2, x1 + 2, y2 - 1, border);
        context.fill(x2 - 2, y2 - 2, x2 - 1, y2 - 1, border);
    }

    //? if >=26.1 {
    public static void fillRounded(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int color) {
    //?} else {
    /*public static void fillRounded(GuiGraphics context, int x1, int y1, int x2, int y2, int color) {
    *///?}
        context.fill(x1 + 1, y1, x2 - 1, y1 + 1, color);
        context.fill(x1, y1 + 1, x2, y2 - 1, color);
        context.fill(x1 + 1, y2 - 1, x2 - 1, y2, color);
    }

    //? if >=26.1 {
    public static void text(GuiGraphicsExtractor context, Font font, Component message, int x, int y, int color, boolean shadow) {
        context.text(font, message, x, y, color, shadow);
    }
    //?} else {
    /*public static void text(GuiGraphics context, Font font, Component message, int x, int y, int color, boolean shadow) {
        context.drawString(font, message, x, y, color, shadow);
    }
    *///?}

    public static int lerpColor(float t, int from, int to) {
        float clamped = t < 0 ? 0 : Math.min(t, 1);
        int a = (int) (((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * clamped);
        int r = (int) (((from >>> 16) & 0xFF) + (((to >>> 16) & 0xFF) - ((from >>> 16) & 0xFF)) * clamped);
        int g = (int) (((from >>> 8) & 0xFF) + (((to >>> 8) & 0xFF) - ((from >>> 8) & 0xFF)) * clamped);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * clamped);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private WidgetSkin() {
    }
}
