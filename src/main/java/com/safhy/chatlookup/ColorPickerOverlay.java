package com.safhy.chatlookup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Locale;
import java.util.function.IntConsumer;

public class ColorPickerOverlay extends AbstractWidget {
    private static final int PANEL_W = 130;
    private static final int PANEL_H = 108;
    private static final int SV_W = 96;
    private static final int SV_H = 64;
    private static final int HUE_W = 10;
    private static final int BTN = 12;
    public static final int HEX_W = 56;

    private static final int ZONE_NONE = 0;
    private static final int ZONE_SV = 1;
    private static final int ZONE_HUE = 2;

    private final EditBox hexField;
    private final Runnable onClosed;

    private Component title = Component.empty();
    private IntConsumer apply = rgb -> { };
    private int defaultColor;
    private float hue;
    private float sat;
    private float val;
    private int dragZone = ZONE_NONE;
    private boolean updatingHexField;

    public ColorPickerOverlay(int screenWidth, int screenHeight, EditBox hexField, Runnable onClosed) {
        super(0, 0, screenWidth, screenHeight, Component.empty());
        this.hexField = hexField;
        this.onClosed = onClosed;
        this.visible = false;
        hexField.visible = false;
        hexField.setMaxLength(7);
        hexField.setResponder(this::onHexEdited);
    }

    public boolean isOpen() {
        return this.visible;
    }

    public void open(Component pickerTitle, int currentColor, int resetColor, IntConsumer applyColor) {
        this.title = pickerTitle;
        this.apply = applyColor;
        this.defaultColor = resetColor;
        setHsvFromRgb(currentColor);
        this.visible = true;
        this.hexField.visible = true;
        this.hexField.setX(hexX());
        this.hexField.setY(bottomRowY());
        pushHexText(currentColor);
    }

    public void close() {
        if (!this.visible) {
            return;
        }
        this.visible = false;
        this.hexField.visible = false;
        this.hexField.setFocused(false);
        this.dragZone = ZONE_NONE;
        ChatLookupConfig.save();
        this.onClosed.run();
    }

    private int panelX() {
        return (this.width - PANEL_W) / 2;
    }

    private int panelY() {
        return (this.height - PANEL_H) / 2;
    }

    private int svX() {
        return panelX() + 8;
    }

    private int svY() {
        return panelY() + 16;
    }

    private int hueX() {
        return svX() + SV_W + 6;
    }

    private int bottomRowY() {
        return svY() + SV_H + 8;
    }

    private int previewX() {
        return panelX() + 8;
    }

    private int hexX() {
        return previewX() + BTN + 4;
    }

    private int resetX() {
        return hexX() + HEX_W + 4;
    }

    private int closeX() {
        return resetX() + BTN + 4;
    }

    private int currentRgb() {
        return Mth.hsvToRgb(Math.min(this.hue, 0.9999f), this.sat, this.val) & 0xFFFFFF;
    }

    private void setHsvFromRgb(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float h;
        if (delta == 0) {
            h = 0;
        } else if (max == r) {
            h = ((g - b) / delta % 6.0f + 6.0f) % 6.0f / 6.0f;
        } else if (max == g) {
            h = ((b - r) / delta + 2.0f) / 6.0f;
        } else {
            h = ((r - g) / delta + 4.0f) / 6.0f;
        }
        this.hue = h;
        this.sat = max == 0 ? 0 : delta / max;
        this.val = max;
    }

    private void colorChanged() {
        int rgb = currentRgb();
        this.apply.accept(rgb);
        pushHexText(rgb);
    }

    private void pushHexText(int rgb) {
        this.updatingHexField = true;
        try {
            this.hexField.setValue(String.format("#%06X", rgb & 0xFFFFFF));
        } finally {
            this.updatingHexField = false;
        }
    }

    private void onHexEdited(String text) {
        if (this.updatingHexField || !this.visible) {
            return;
        }
        String hex = text.startsWith("#") ? text.substring(1) : text;
        if (hex.length() != 6) {
            return;
        }
        int rgb;
        try {
            rgb = Integer.parseInt(hex.toLowerCase(Locale.ROOT), 16);
        } catch (NumberFormatException e) {
            return;
        }
        setHsvFromRgb(rgb);
        this.apply.accept(rgb);
    }

    private boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    public boolean isOverHexField(double mx, double my) {
        return inRect(mx, my, hexX(), bottomRowY(), HEX_W, BTN);
    }

    @Override
    //? if >=1.21.9 {
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.visible && isOverHexField(click.x(), click.y())) {
            return false;
        }
        return super.mouseClicked(click, doubled);
    }
    //?} else {
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.visible && isOverHexField(mouseX, mouseY)) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    *///?}

    @Override
    //? if >=1.21.9 {
    public void onClick(MouseButtonEvent click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
    //?} else {
    /*public void onClick(double mx, double my) {
    *///?}
        if (inRect(mx, my, svX(), svY(), SV_W, SV_H)) {
            this.dragZone = ZONE_SV;
            updateSv(mx, my);
        } else if (inRect(mx, my, hueX() - 1, svY() - 1, HUE_W + 2, SV_H + 2)) {
            this.dragZone = ZONE_HUE;
            updateHue(my);
        } else if (inRect(mx, my, resetX(), bottomRowY(), BTN, BTN)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            setHsvFromRgb(this.defaultColor);
            colorChanged();
        } else if (inRect(mx, my, closeX(), bottomRowY(), BTN, BTN)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            close();
        } else if (!inRect(mx, my, panelX(), panelY(), PANEL_W, PANEL_H)) {
            close();
        }
    }

    @Override
    //? if >=1.21.9 {
    protected void onDrag(MouseButtonEvent click, double dragX, double dragY) {
        double mx = click.x();
        double my = click.y();
    //?} else {
    /*protected void onDrag(double mx, double my, double dragX, double dragY) {
    *///?}
        if (this.dragZone == ZONE_SV) {
            updateSv(mx, my);
        } else if (this.dragZone == ZONE_HUE) {
            updateHue(my);
        }
    }

    @Override
    //? if >=1.21.9 {
    public void onRelease(MouseButtonEvent click) {
    //?} else {
    /*public void onRelease(double mouseX, double mouseY) {
    *///?}
        this.dragZone = ZONE_NONE;
    }

    private void updateSv(double mx, double my) {
        this.sat = Mth.clamp((float) (mx - svX()) / (SV_W - 1), 0.0f, 1.0f);
        this.val = 1.0f - Mth.clamp((float) (my - svY()) / (SV_H - 1), 0.0f, 1.0f);
        colorChanged();
    }

    private void updateHue(double my) {
        this.hue = Mth.clamp((float) (my - svY()) / (SV_H - 1), 0.0f, 1.0f);
        colorChanged();
    }

    private static int hueRgb(float h) {
        return 0xFF000000 | Mth.hsvToRgb(Math.min(h, 0.9999f), 1.0f, 1.0f);
    }

    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent navigation) {
        return null;
    }

    @Override
    //? if >=26.1 {
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    //?} else {
    /*protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
    *///?}
        Minecraft client = Minecraft.getInstance();
        int px = panelX();
        int py = panelY();

        context.fill(0, 0, this.width, this.height, 0x90000000);
        WidgetSkin.drawPanel(context, px, py, px + PANEL_W, py + PANEL_H, 0xF60F0F13, WidgetSkin.BORDER_HOVER);
        WidgetSkin.text(context, client.font, this.title, px + 8, py + 5, 0xFFE8E8F0, false);

        int sx = svX();
        int sy = svY();
        int hueColor = hueRgb(this.hue);
        for (int i = 0; i < SV_W; i++) {
            int top = WidgetSkin.lerpColor(i / (float) (SV_W - 1), 0xFFFFFFFF, hueColor);
            context.fillGradient(sx + i, sy, sx + i + 1, sy + SV_H, top, 0xFF000000);
        }
        frame(context, sx, sy, SV_W, SV_H);

        int hx = hueX();
        for (int i = 0; i < 6; i++) {
            int y1 = sy + i * SV_H / 6;
            int y2 = sy + (i + 1) * SV_H / 6;
            context.fillGradient(hx, y1, hx + HUE_W, y2, hueRgb(i / 6.0f), hueRgb((i + 1) / 6.0f));
        }
        frame(context, hx, sy, HUE_W, SV_H);

        int rgb = 0xFF000000 | currentRgb();
        int cx = sx + Math.round(this.sat * (SV_W - 1));
        int cy = sy + Math.round((1.0f - this.val) * (SV_H - 1));
        context.fill(cx - 2, cy - 2, cx + 3, cy + 3, 0xFFFFFFFF);
        context.fill(cx - 1, cy - 1, cx + 2, cy + 2, rgb);

        int hy = sy + Math.round(this.hue * (SV_H - 1));
        context.fill(hx - 2, hy - 1, hx + HUE_W + 2, hy + 1, 0xFFFFFFFF);

        int rowY = bottomRowY();
        WidgetSkin.drawPanel(context, previewX(), rowY, previewX() + BTN, rowY + BTN, rgb, WidgetSkin.BORDER_HOVER);

        boolean resetHover = inRect(mouseX, mouseY, resetX(), rowY, BTN, BTN);
        WidgetSkin.drawPanel(context, resetX(), rowY, resetX() + BTN, rowY + BTN,
                resetHover ? 0xE81C1C22 : 0xDC121216,
                resetHover ? WidgetSkin.BORDER_HOVER : WidgetSkin.BORDER_IDLE);
        Icons.drawCentered(context, Icons.RESET, resetX(), rowY, BTN, BTN,
                resetHover ? 0xFFFFFFFF : WidgetSkin.LABEL_IDLE);

        boolean closeHover = inRect(mouseX, mouseY, closeX(), rowY, BTN, BTN);
        WidgetSkin.drawPanel(context, closeX(), rowY, closeX() + BTN, rowY + BTN,
                closeHover ? 0xF03A3008 : 0xF02A2306, WidgetSkin.ACCENT);
        Icons.drawCentered(context, Icons.CHECK, closeX(), rowY, BTN, BTN, 0xFFFFDE5C);
    }

    //? if >=26.1 {
    private static void frame(GuiGraphicsExtractor context, int x, int y, int w, int h) {
    //?} else {
    /*private static void frame(GuiGraphics context, int x, int y, int w, int h) {
    *///?}
        context.fill(x - 1, y - 1, x + w + 1, y, WidgetSkin.BORDER_IDLE);
        context.fill(x - 1, y + h, x + w + 1, y + h + 1, WidgetSkin.BORDER_IDLE);
        context.fill(x - 1, y, x, y + h, WidgetSkin.BORDER_IDLE);
        context.fill(x + w, y, x + w + 1, y + h, WidgetSkin.BORDER_IDLE);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }
}
