package com.safhy.chatlookup;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;

public class SearchFieldWidget extends EditBox {
    public SearchFieldWidget(Font font, int x, int y, int width, int height, Component text) {
        super(font, x, y, width, height, text);
        //? if >=1.21.6 {
        this.setTextShadow(false);
        //?}
    }


    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent navigation) {
        return null;
    }

    @Override
    public boolean isBordered() {
        return false;
    }

    @Override
    public int getInnerWidth() {
        return this.getWidth() - 8;
    }

    @Override
    //? if >=26.1 {
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    //?} else {
    /*public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
    *///?}
        int x1 = this.getX();
        int y1 = this.getY();
        int x2 = x1 + this.getWidth();
        int y2 = y1 + this.getHeight();
        int background = this.isFocused() ? 0xF0161610 : (this.isHovered() ? 0xE81C1C22 : 0xDC121216);
        int border = this.isFocused() ? WidgetSkin.ACCENT
                : (this.isHovered() ? WidgetSkin.BORDER_HOVER : WidgetSkin.BORDER_IDLE);
        WidgetSkin.drawPanel(context, x1, y1, x2, y2, background, border);
        //? if >=26.1 {
        super.extractWidgetRenderState(context, mouseX, mouseY, delta);
        //?} else {
        /*super.renderWidget(context, mouseX, mouseY, delta);
        *///?}
    }
}
