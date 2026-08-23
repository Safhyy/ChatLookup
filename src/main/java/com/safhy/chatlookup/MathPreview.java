package com.safhy.chatlookup;

import com.safhy.chatlookup.mixin.EditBoxAccessor;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public final class MathPreview {
    private static final int PANEL_HEIGHT = 13;
    private static final int PADDING = 5;
    private static final int RESULT_COLOR = 0xFFFFDE5C;
    private static final int HINT_COLOR = 0xFF7A7A86;
    //? if <1.21.6 {
    /*private static final float OVERLAY_DEPTH = 400.0f;
    *///?}

    private static MathEvaluator.Expression expressionOf(EditBox input) {
        if (!ChatLookup.isMathPreviewEnabled() || input == null) {
            return null;
        }
        String text = input.getValue();
        if (text.startsWith("/")) {
            return null;
        }
        return MathEvaluator.expressionAt(text, input.getCursorPosition());
    }

    public static boolean applyResult(EditBox input) {
        MathEvaluator.Expression expression = expressionOf(input);
        if (expression == null) {
            return false;
        }
        String text = input.getValue();
        String replacement = MathEvaluator.format(expression.value());
        input.setValue(text.substring(0, expression.start()) + replacement + text.substring(expression.end()));
        input.moveCursorTo(expression.start() + replacement.length(), false);
        return true;
    }

    //? if >=26.1 {
    public static void render(GuiGraphicsExtractor context, Font font, int screenWidth, EditBox input) {
    //?} else {
    /*public static void render(GuiGraphics context, Font font, int screenWidth, EditBox input) {
    *///?}
        MathEvaluator.Expression expression = expressionOf(input);
        if (expression == null) {
            return;
        }
        Component result = Component.literal("= " + MathEvaluator.format(expression.value()));
        Component hint = Component.literal("Tab");
        int resultWidth = font.width(result);
        int hintWidth = font.width(hint);
        int panelWidth = PADDING + resultWidth + PADDING + hintWidth + PADDING;

        String text = input.getValue();
        int innerX = input.isBordered() ? input.getX() + 4 : input.getX();
        int displayPos = Math.max(0, Math.min(((EditBoxAccessor) input).chatlookup$getDisplayPos(), text.length()));
        int from = Math.min(Math.max(expression.start(), displayPos), text.length());
        int anchorX = innerX + font.width(text.substring(displayPos, from));

        int x1 = Math.max(2, Math.min(anchorX - PADDING, screenWidth - 2 - panelWidth));
        int x2 = x1 + panelWidth;
        int y2 = input.getY() - 2;
        int y1 = y2 - PANEL_HEIGHT;

        //? if <1.21.6 {
        /*context.pose().pushPose();
        context.pose().translate(0.0f, 0.0f, OVERLAY_DEPTH);
        *///?}
        WidgetSkin.drawPanel(context, x1, y1, x2, y2, 0xF0121216, WidgetSkin.ACCENT);
        context.fill(x1 + 3, y2, x1 + 5, y2 + 2, WidgetSkin.ACCENT);
        WidgetSkin.text(context, font, result, x1 + PADDING, y1 + 3, RESULT_COLOR, false);
        WidgetSkin.text(context, font, hint, x1 + PADDING + resultWidth + PADDING, y1 + 3, HINT_COLOR, false);
        //? if <1.21.6 {
        /*context.pose().popPose();
        *///?}
    }

    private MathPreview() {
    }
}
