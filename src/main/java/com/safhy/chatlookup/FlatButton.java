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

public class FlatButton extends AbstractWidget {
    private final Runnable onPress;
    private final boolean accent;
    private final String[] icon;

    public FlatButton(int x, int y, int width, int height, Component label, Component tooltip,
                      boolean accent, Runnable onPress) {
        this(x, y, width, height, null, label, tooltip, accent, onPress);
    }

    /** Icon variant: draws the pixel icon instead of text; {@code label} is kept for narration. */
    public FlatButton(int x, int y, int width, int height, String[] icon, Component label, Component tooltip,
                      boolean accent, Runnable onPress) {
        super(x, y, width, height, label);
        this.icon = icon;
        this.onPress = onPress;
        this.accent = accent;
        if (tooltip != null) {
            this.setTooltip(Tooltip.create(tooltip));
        }
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

        int background;
        int border;
        int labelColor;
        if (this.accent) {
            background = this.isHovered() ? 0xF03A3008 : 0xF02A2306;
            border = WidgetSkin.ACCENT;
            labelColor = 0xFFFFDE5C;
        } else {
            background = this.isHovered() ? 0xE81C1C22 : 0xDC121216;
            border = this.isHovered() ? WidgetSkin.BORDER_HOVER : WidgetSkin.BORDER_IDLE;
            labelColor = this.isHovered() ? 0xFFFFFFFF : WidgetSkin.LABEL_IDLE;
        }
        WidgetSkin.drawPanel(context, x1, y1, x2, y2, background, border);

        if (this.icon != null) {
            Icons.drawCentered(context, this.icon, x1, y1, this.width, this.height, labelColor);
            return;
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
