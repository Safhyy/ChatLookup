package com.safhy.chatlookup;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}

public final class Icons {
    public static final String[] GEAR = {
            "...##...",
            ".#.##.#.",
            ".######.",
            "###..###",
            "###..###",
            ".######.",
            ".#.##.#.",
            "...##...",
    };

    public static final String[] RESET = {
            "..####..",
            ".#....#.",
            "###....#",
            ".#.....#",
            ".......#",
            ".......#",
            ".#....#.",
            "..####..",
    };

    public static final String[] HIGHLIGHT = {
            ".####.",
            "##..##",
            "##..##",
            "######",
            "##..##",
            "##..##",
    };

    public static final String[] CHECK = {
            ".......#",
            "......##",
            "#....##.",
            "##..##..",
            ".####...",
            "..##....",
    };

    public static int width(String[] icon) {
        return icon[0].length();
    }

    public static int height(String[] icon) {
        return icon.length;
    }

    //? if >=26.1 {
    public static void drawCentered(GuiGraphicsExtractor context, String[] icon, int x, int y, int w, int h, int color) {
    //?} else {
    /*public static void drawCentered(GuiGraphics context, String[] icon, int x, int y, int w, int h, int color) {
    *///?}
        draw(context, icon, x + (w - width(icon)) / 2, y + (h - height(icon)) / 2, color);
    }

    //? if >=26.1 {
    public static void draw(GuiGraphicsExtractor context, String[] icon, int x, int y, int color) {
    //?} else {
    /*public static void draw(GuiGraphics context, String[] icon, int x, int y, int color) {
    *///?}
        for (int row = 0; row < icon.length; row++) {
            String line = icon[row];
            int col = 0;
            while (col < line.length()) {
                if (line.charAt(col) == '#') {
                    int start = col;
                    while (col < line.length() && line.charAt(col) == '#') {
                        col++;
                    }
                    context.fill(x + start, y + row, x + col, y + row + 1, color);
                } else {
                    col++;
                }
            }
        }
    }

    private Icons() {
    }
}
