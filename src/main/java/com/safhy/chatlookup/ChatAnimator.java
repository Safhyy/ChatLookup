package com.safhy.chatlookup;

import com.safhy.chatlookup.mixin.ChatHudAccessor;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.ChatComponent;
//? if >=1.21.9 {
import net.minecraft.util.Util;
//?} else {
/*import net.minecraft.Util;
*///?}

public final class ChatAnimator {
    private static final int MESSAGE_FADE_MS = 150;
    private static final int FIELD_FADE_MS = 170;
    private static final float FIELD_OFFSET = 8.0f;
    private static final float SLIDE_TAU_MS = 55.0f;
    private static final float MAX_SLIDE_LINES = 5.0f;

    private static float slideLines;
    private static long slideStampMs;
    private static boolean countingLines;
    private static int newLines;
    private static boolean chatPosePushed;
    private static boolean screenOpen;
    private static long screenOpenedMs;

    public static void beginMessageAdd() {
        countingLines = true;
        newLines = 0;
    }

    public static void onLineCreated() {
        if (countingLines) {
            newLines++;
        }
    }

    public static void endMessageAdd() {
        if (!countingLines) {
            return;
        }
        countingLines = false;
        if (newLines > 0) {
            slideLines = Math.min(currentSlideLines() + newLines, MAX_SLIDE_LINES);
            slideStampMs = Util.getMillis();
        }
    }

    private static float currentSlideLines() {
        long age = Util.getMillis() - slideStampMs;
        if (age < 0) {
            return 0;
        }
        float remaining = slideLines * (float) Math.exp(-age / SLIDE_TAU_MS);
        return remaining < 0.01f ? 0 : remaining;
    }

    public static void onChatScreenClosed() {
        screenOpen = false;
    }

    //? if >=26.1 {
    public static void pushChatPose(GuiGraphicsExtractor graphics, ChatComponent chatHud) {
    //?} else {
    /*public static void pushChatPose(GuiGraphics graphics, ChatComponent chatHud) {
    *///?}
        float displacement = chatDisplacement(chatHud);
        chatPosePushed = displacement != 0;
        if (chatPosePushed) {
            //? if >=1.21.6 {
            graphics.pose().pushMatrix();
            graphics.pose().translate(0, displacement);
            //?} else {
            /*graphics.pose().pushPose();
            graphics.pose().translate(0, displacement, 0);
            *///?}
        }
    }

    //? if >=26.1 {
    public static void popChatPose(GuiGraphicsExtractor graphics) {
    //?} else {
    /*public static void popChatPose(GuiGraphics graphics) {
    *///?}
        if (chatPosePushed) {
            chatPosePushed = false;
            //? if >=1.21.6 {
            graphics.pose().popMatrix();
            //?} else {
            /*graphics.pose().popPose();
            *///?}
        }
    }

    private static float chatDisplacement(ChatComponent chatHud) {
        if (!ChatLookup.isAnimationEnabled()) {
            return 0;
        }
        ChatHudAccessor accessor = (ChatHudAccessor) chatHud;
        if (accessor.chatlookup$getScrolledLines() != 0) {
            return 0;
        }
        float lines = currentSlideLines();
        if (lines == 0) {
            return 0;
        }
        return (float) (accessor.chatlookup$getLineHeight() * accessor.chatlookup$getChatScale()) * lines;
    }

    public static double easeLineOpacity(double original, Object line) {
        if (!ChatLookup.isAnimationEnabled()) {
            return original;
        }
        long age = Util.getMillis() - ((AnimatedLine) line).chatlookup$addedMs();
        double factor = Math.min(age / (double) MESSAGE_FADE_MS, 1.0);
        double alpha = original * factor;
        return 0.5 - Math.cos(alpha * Math.PI) / 2.0;
    }

    //? if >=26.1 {
    public static void wrapField(GuiGraphicsExtractor graphics, Minecraft minecraft, Runnable draw) {
    //?} else {
    /*public static void wrapField(GuiGraphics graphics, Minecraft minecraft, Runnable draw) {
    *///?}
        float displacement = fieldDisplacement(minecraft);
        if (displacement == 0) {
            draw.run();
            return;
        }
        //? if >=1.21.6 {
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, displacement);
        draw.run();
        graphics.pose().popMatrix();
        //?} else {
        /*graphics.pose().pushPose();
        graphics.pose().translate(0, displacement, 0);
        draw.run();
        graphics.pose().popPose();
        *///?}
    }

    private static float fieldDisplacement(Minecraft minecraft) {
        if (!ChatLookup.isAnimationEnabled() || minecraft == null) {
            return 0;
        }
        if (minecraft.player != null && !screenOpen && !minecraft.player.isSleeping()) {
            screenOpen = true;
            screenOpenedMs = Util.getMillis();
        }
        float elapsed = Math.min(Util.getMillis() - screenOpenedMs, (float) FIELD_FADE_MS);
        float alpha = 1.0f - elapsed / FIELD_FADE_MS;
        float c1 = 1.70158f;
        float eased = (c1 + 1) * alpha * alpha * alpha - c1 * alpha * alpha;
        return eased * FIELD_OFFSET * (minecraft.getWindow().getHeight() / 1080.0f);
    }

    private ChatAnimator() {
    }
}
