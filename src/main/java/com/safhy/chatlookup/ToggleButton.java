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

import java.util.function.BooleanSupplier;

public class ToggleButton extends AbstractWidget {
    public static final int SIZE = 12;

    private final BooleanSupplier state;
    private final Runnable onToggle;
    private final boolean markerIcon;

    public ToggleButton(int x, int y, Component label, Component tooltip, boolean markerIcon,
                        BooleanSupplier state, Runnable onToggle) {
        super(x, y, SIZE, SIZE, label);
        this.state = state;
        this.onToggle = onToggle;
        this.markerIcon = markerIcon;
        this.setTooltip(Tooltip.create(tooltip));
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
        boolean active = this.state.getAsBoolean();
        int x1 = this.getX();
        int y1 = this.getY();
        int x2 = x1 + this.width;
        int y2 = y1 + this.height;

        int background = active ? 0xF02A2306 : (this.isHovered() ? 0xE81C1C22 : 0xDC121216);
        int border = active ? WidgetSkin.ACCENT : (this.isHovered() ? WidgetSkin.BORDER_HOVER : WidgetSkin.BORDER_IDLE);
        WidgetSkin.drawPanel(context, x1, y1, x2, y2, background, border);

        int labelColor;
        if (this.markerIcon) {
            WidgetSkin.fillRounded(context, x1 + 2, y1 + 2, x2 - 2, y2 - 2, active ? 0xB4FFE14D : 0x34FFE14D);
            labelColor = active ? 0xFF1E1A05 : WidgetSkin.LABEL_IDLE;
        } else {
            labelColor = active ? 0xFFFFDE5C : (this.isHovered() ? 0xFFFFFFFF : WidgetSkin.LABEL_IDLE);
        }

        Minecraft client = Minecraft.getInstance();

        int glyphWidth = client.font.width(this.getMessage()) - 1;
        int textX = x1 + (this.width - glyphWidth) / 2;
        int textY = y1 + (this.height - 8) / 2 + 1;
        WidgetSkin.text(context, client.font, this.getMessage(), textX, textY, labelColor, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        this.defaultButtonNarrationText(builder);
    }
}
