package com.safhy.chatlookup;

import com.safhy.chatlookup.mixin.ChatHudAccessor;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.ChatComponent;
//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?} else {
/*import net.minecraft.client.GuiMessage;
*///?}
import net.minecraft.util.Mth;
//? if >=1.21.6 {
import org.joml.Matrix3x2fStack;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
import net.minecraft.world.entity.player.ChatVisiblity;

import java.util.List;

public final class JumpFlash {
    private static final long DURATION_MS = 10_000L;
    private static final long FADE_OUT_MS = 1_500L;
    private static final float PULSE_MS = 1_400.0f;
    private static final float PEAK_ALPHA = 0x4C;
    private static final float FLOOR = 0.25f;
    private static final int CHAT_OFFSET_FROM_BOTTOM = 40;

    private static GuiMessage target;
    private static long startMs;

    public static void mark(GuiMessage message) {
        if (!ChatLookup.isJumpFlashEnabled()) {
            return;
        }
        target = message;
        startMs = now();
    }

    public static void clear() {
        target = null;
    }

    private static long now() {
        return System.nanoTime() / 1_000_000L;
    }

    //? if >=26.1 {
    public static void render(GuiGraphicsExtractor context, ChatComponent chatHud, Minecraft minecraft) {
    //?} else {
    /*public static void render(GuiGraphics context, ChatComponent chatHud, Minecraft minecraft) {
    *///?}
        if (target == null || !ChatLookup.isJumpFlashEnabled() || minecraft == null || minecraft.options == null
                || minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN || !chatHud.isChatFocused()) {
            return;
        }
        long elapsed = now() - startMs;
        if (elapsed < 0 || elapsed >= DURATION_MS) {
            target = null;
            return;
        }

        ChatHudAccessor hud = (ChatHudAccessor) chatHud;
        float scale = (float) hud.chatlookup$getChatScale();
        if (scale <= 0.0f) {
            return;
        }
        List<GuiMessage.Line> visible = hud.chatlookup$getVisibleMessages();
        int scrolled = hud.chatlookup$getScrolledLines();
        int onScreen = Math.min(visible.size() - scrolled, chatHud.getLinesPerPage());
        if (onScreen <= 0) {
            return;
        }

        int ordinal = -1;
        boolean found = false;
        for (GuiMessage message : hud.chatlookup$getMessages()) {
            if (!ChatLookup.matches(message)) {
                if (message == target) {
                    return;
                }
                continue;
            }
            ordinal++;
            if (message == target) {
                found = true;
                break;
            }
            if (ordinal >= visible.size()) {
                return;
            }
        }
        if (!found) {
            target = null;
            return;
        }

        int start = -1;
        int seen = -1;
        for (int i = 0; i < visible.size(); i++) {
            if (visible.get(i).endOfEntry()) {
                seen++;
                if (seen == ordinal) {
                    start = i;
                    break;
                }
            }
        }
        if (start < 0) {
            return;
        }
        int end = start + 1;
        while (end < visible.size() && !visible.get(end).endOfEntry()) {
            end++;
        }

        float fade = elapsed > DURATION_MS - FADE_OUT_MS
                ? (DURATION_MS - elapsed) / (float) FADE_OUT_MS
                : 1.0f;
        float wave = 0.5f + 0.5f * (float) Math.cos(elapsed / PULSE_MS * Math.PI * 2.0);
        int alpha = (int) (PEAK_ALPHA * (FLOOR + (1.0f - FLOOR) * wave) * fade);
        if (alpha <= 2) {
            return;
        }
        int color = (alpha << 24) | ChatLookup.getHighlightColor();

        int lineHeight = hud.chatlookup$getLineHeight();
        double spacing = minecraft.options.chatLineSpacing().get();
        int textOffset = (int) Math.round(8.0 * (spacing + 1.0) - 4.0 * spacing);
        int chatBottom = Mth.floor(
                (minecraft.getWindow().getGuiScaledHeight() - CHAT_OFFSET_FROM_BOTTOM) / scale);
        int right = Mth.ceil(hud.chatlookup$getWidth() / scale) + 4;

        //? if >=1.21.6 {
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.scale(scale, scale);
        matrices.translate(4.0F, 0.0F);
        //?} else {
        /*PoseStack matrices = context.pose();
        matrices.pushPose();
        matrices.scale(scale, scale, 1.0F);
        matrices.translate(4.0F, 0.0F, 0.0F);
        *///?}
        for (int index = start; index < end; index++) {
            int slot = index - scrolled;
            if (slot < 0 || slot >= onScreen) {
                continue;
            }
            int top = chatBottom - slot * lineHeight - textOffset;
            context.fill(-4, top - 1, right, top + 9, color);
        }
        //? if >=1.21.6 {
        matrices.popMatrix();
        //?} else {
        /*matrices.popPose();
        *///?}
    }

    private JumpFlash() {
    }
}
