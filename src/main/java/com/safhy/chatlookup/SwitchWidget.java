package com.safhy.chatlookup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Util;
//?} else {
/*import net.minecraft.Util;
*///?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;

public class SwitchWidget extends AbstractWidget {
    public static final int WIDTH = 24;
    public static final int HEIGHT = 12;

    private static final int ANIM_MS = 120;
    private static final int KNOB = 8;

    private static final int TRACK_OFF = 0xFF33333C;
    private static final int TRACK_ON = 0xFF6D5A12;
    private static final int BORDER_ON = WidgetSkin.ACCENT;
    private static final int KNOB_OFF = 0xFF8A8A94;
    private static final int KNOB_ON = 0xFFFFE14D;

    private final BooleanSupplier state;
    private final Runnable onToggle;

    private float anim;
    private long lastFrameMs;
    private boolean animInitialized;

    public SwitchWidget(int x, int y, Component label, BooleanSupplier state, Runnable onToggle) {
        super(x, y, WIDTH, HEIGHT, label);
        this.state = state;
        this.onToggle = onToggle;
    }

    @Override
    //? if >=1.21.9 {
    public void onClick(MouseButtonEvent click, boolean doubled) {
    //?} else {
    /*public void onClick(double mouseX, double mouseY) {
    *///?}
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        this.onToggle.run();
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
        boolean on = this.state.getAsBoolean();
        long now = Util.getMillis();
        if (!this.animInitialized) {
            this.animInitialized = true;
            this.anim = on ? 1.0f : 0.0f;
        } else {
            float step = (now - this.lastFrameMs) / (float) ANIM_MS;
            this.anim = on ? Math.min(this.anim + step, 1.0f) : Math.max(this.anim - step, 0.0f);
        }
        this.lastFrameMs = now;
        float t = this.anim * this.anim * (3.0f - 2.0f * this.anim);

        int x1 = this.getX();
        int y1 = this.getY();
        int x2 = x1 + this.width;
        int y2 = y1 + this.height;

        int track = WidgetSkin.lerpColor(t, TRACK_OFF, TRACK_ON);
        int border = WidgetSkin.lerpColor(t,
                this.isHovered() ? WidgetSkin.BORDER_HOVER : WidgetSkin.BORDER_IDLE, BORDER_ON);
        WidgetSkin.drawPanel(context, x1, y1, x2, y2, track, border);

        int travel = this.width - KNOB - 4;
        int knobX = x1 + 2 + Math.round(t * travel);
        int knob = WidgetSkin.lerpColor(t, KNOB_OFF, KNOB_ON);
        WidgetSkin.fillRounded(context, knobX, y1 + 2, knobX + KNOB, y2 - 2, knob);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        this.defaultButtonNarrationText(builder);
    }
}
