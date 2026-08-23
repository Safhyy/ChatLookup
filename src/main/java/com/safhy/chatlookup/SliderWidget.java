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
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

public class SliderWidget extends AbstractWidget {
    public static final int HEIGHT = 12;
    public static final int TRACK_W = 64;

    private static final int GAP = 5;
    private static final int TRACK_H = 3;
    private static final int KNOB_W = 4;
    private static final int TRACK_BG = 0xFF2B2B33;
    private static final int TRACK_FILL = 0xFF6D5A12;
    private static final int KNOB_IDLE = 0xFFD8C24A;

    private final int valueWidth;
    private final int min;
    private final int max;
    private final int step;
    private final IntSupplier value;
    private final IntConsumer onChange;
    private final Runnable onCommit;
    private final IntFunction<Component> format;

    private boolean dragging;

    public SliderWidget(int x, int y, int valueWidth, Component name, Component tooltip,
                        int min, int max, int step,
                        IntSupplier value, IntConsumer onChange, Runnable onCommit,
                        IntFunction<Component> format) {
        super(x, y, width(valueWidth), HEIGHT, name);
        this.valueWidth = valueWidth;
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;
        this.onChange = onChange;
        this.onCommit = onCommit;
        this.format = format;
        if (tooltip != null) {
            this.setTooltip(Tooltip.create(tooltip));
        }
    }

    public static int width(int valueWidth) {
        return valueWidth + GAP + TRACK_W;
    }

    private int trackX() {
        return this.getX() + this.valueWidth + GAP;
    }

    private int knobX() {
        float progress = (this.value.getAsInt() - this.min) / (float) (this.max - this.min);
        return trackX() + Math.round(Mth.clamp(progress, 0.0f, 1.0f) * (TRACK_W - KNOB_W));
    }

    @Override
    //? if >=1.21.9 {
    public void onClick(MouseButtonEvent click, boolean doubled) {
        double mx = click.x();
    //?} else {
    /*public void onClick(double mx, double my) {
    *///?}
        if (mx < trackX() - GAP) {
            return;
        }
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        this.dragging = true;
        applyFromMouse(mx);
    }

    @Override
    //? if >=1.21.9 {
    protected void onDrag(MouseButtonEvent click, double dragX, double dragY) {
        double mx = click.x();
    //?} else {
    /*protected void onDrag(double mx, double my, double dragX, double dragY) {
    *///?}
        if (this.dragging) {
            applyFromMouse(mx);
        }
    }

    @Override
    //? if >=1.21.9 {
    public void onRelease(MouseButtonEvent click) {
    //?} else {
    /*public void onRelease(double mouseX, double mouseY) {
    *///?}
        if (this.dragging) {
            this.dragging = false;
            this.onCommit.run();
        }
    }

    private void applyFromMouse(double mx) {
        float travel = TRACK_W - KNOB_W;
        float progress = Mth.clamp((float) (mx - trackX() - KNOB_W / 2.0) / travel, 0.0f, 1.0f);
        int raw = this.min + Math.round(progress * (this.max - this.min));
        int snapped = this.min + Math.round((raw - this.min) / (float) this.step) * this.step;
        snapped = Mth.clamp(snapped, this.min, this.max);
        if (snapped != this.value.getAsInt()) {
            this.onChange.accept(snapped);
        }
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
        int current = this.value.getAsInt();
        boolean lit = this.active && (this.isHovered() || this.dragging);
        int trackX = trackX();
        int knobX = knobX();
        int trackY = this.getY() + (HEIGHT - TRACK_H) / 2;

        WidgetSkin.fillRounded(context, trackX, trackY, trackX + TRACK_W, trackY + TRACK_H,
                this.active ? TRACK_BG : 0xFF1E1E24);
        int fillEnd = knobX + KNOB_W / 2;
        if (fillEnd > trackX + 1) {
            WidgetSkin.fillRounded(context, trackX, trackY, fillEnd, trackY + TRACK_H,
                    this.active ? TRACK_FILL : 0xFF34343C);
        }
        int knobColor = !this.active ? 0xFF5A5A64 : lit ? WidgetSkin.ACCENT : KNOB_IDLE;
        WidgetSkin.fillRounded(context, knobX, this.getY(), knobX + KNOB_W, this.getY() + HEIGHT, knobColor);

        Component text = this.format.apply(current);
        Minecraft client = Minecraft.getInstance();
        int textColor = !this.active ? 0xFF5A5A64
                : current > this.min ? 0xFFFFDE5C : lit ? 0xFFFFFFFF : WidgetSkin.LABEL_IDLE;
        WidgetSkin.text(context, client.font, text,
                this.getX() + this.valueWidth - client.font.width(text),
                this.getY() + (HEIGHT - 8) / 2 + 1, textColor, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        this.defaultButtonNarrationText(builder);
    }
}
