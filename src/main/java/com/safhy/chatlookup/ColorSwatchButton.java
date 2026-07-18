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
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;

import java.util.function.IntSupplier;

public class ColorSwatchButton extends AbstractWidget {
    public static final int WIDTH = 24;
    public static final int HEIGHT = 12;

    private final IntSupplier color;
    private final Runnable onPress;

    public ColorSwatchButton(int x, int y, Component label, IntSupplier color, Runnable onPress) {
        super(x, y, WIDTH, HEIGHT, label);
        this.color = color;
        this.onPress = onPress;
    }

    @Override
    //? if >=1.21.9 {
    public void onClick(MouseButtonEvent click, boolean doubled) {
    //?} else {
    /*public void onClick(double mouseX, double mouseY) {
    *///?}
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        this.onPress.run();
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
        int x1 = this.getX();
        int y1 = this.getY();
        int x2 = x1 + this.width;
        int y2 = y1 + this.height;
        int border = this.isHovered() ? WidgetSkin.BORDER_HOVER : WidgetSkin.BORDER_IDLE;
        WidgetSkin.drawPanel(context, x1, y1, x2, y2, 0xFF000000, border);
        context.fill(x1 + 2, y1 + 2, x2 - 2, y2 - 2, 0xFF000000 | this.color.getAsInt());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        this.defaultButtonNarrationText(builder);
    }
}
