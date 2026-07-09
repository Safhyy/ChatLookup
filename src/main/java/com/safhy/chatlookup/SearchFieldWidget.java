package com.safhy.chatlookup;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class SearchFieldWidget extends TextFieldWidget {
    public SearchFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
        this.setTextShadow(false);
    }


    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return null;
    }

    @Override
    public boolean drawsBackground() {
        return false;
    }

    @Override
    public int getInnerWidth() {
        return this.getWidth() - 8;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int x1 = this.getX();
        int y1 = this.getY();
        int x2 = x1 + this.getWidth();
        int y2 = y1 + this.getHeight();
        int background = this.isFocused() ? 0xF0161610 : (this.isHovered() ? 0xE81C1C22 : 0xDC121216);
        int border = this.isFocused() ? WidgetSkin.ACCENT
                : (this.isHovered() ? WidgetSkin.BORDER_HOVER : WidgetSkin.BORDER_IDLE);
        WidgetSkin.drawPanel(context, x1, y1, x2, y2, background, border);
        super.renderWidget(context, mouseX, mouseY, delta);
    }
}
