package com.safhy.chatlookup;

import net.minecraft.client.gui.DrawContext;

public final class WidgetSkin {
    public static final int ACCENT = 0xFFFFD24A;
    public static final int BORDER_IDLE = 0xFF4A4A54;
    public static final int BORDER_HOVER = 0xFF9A9AA6;
    public static final int LABEL_IDLE = 0xFFB0B0B8;

    public static void drawPanel(DrawContext context, int x1, int y1, int x2, int y2, int background, int border) {
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

    public static void fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1 + 1, y1, x2 - 1, y1 + 1, color);
        context.fill(x1, y1 + 1, x2, y2 - 1, color);
        context.fill(x1 + 1, y2 - 1, x2 - 1, y2, color);
    }

    private WidgetSkin() {
    }
}
