package com.safhy.chatlookup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;

public class ToggleButton extends ClickableWidget {
    public static final int SIZE = 12;

    private final BooleanSupplier state;
    private final Runnable onToggle;
    private final boolean markerIcon;

    public ToggleButton(int x, int y, Text label, Text tooltip, boolean markerIcon,
                        BooleanSupplier state, Runnable onToggle) {
        super(x, y, SIZE, SIZE, label);
        this.state = state;
        this.onToggle = onToggle;
        this.markerIcon = markerIcon;
        this.setTooltip(Tooltip.of(tooltip));
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        this.playDownSound(MinecraftClient.getInstance().getSoundManager());
        this.onToggle.run();
    }

    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return null;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
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

        MinecraftClient client = MinecraftClient.getInstance();
        
        int glyphWidth = client.textRenderer.getWidth(this.getMessage()) - 1;
        int textX = x1 + (this.width - glyphWidth) / 2;
        int textY = y1 + (this.height - 8) / 2 + 1;
        context.drawText(client.textRenderer, this.getMessage(), textX, textY, labelColor, false);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}
