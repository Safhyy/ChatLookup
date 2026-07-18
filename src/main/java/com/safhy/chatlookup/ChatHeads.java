package com.safhy.chatlookup;

import com.safhy.chatlookup.mixin.ChatHudAccessor;
//? if <1.21.2 {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
*///?}
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?} else {
/*import net.minecraft.client.GuiMessage;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.ChatVisiblity;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public final class ChatHeads {
    public static final int HEAD_SIZE = 8;
    private static final int LEFT_MARGIN = 3;
    private static final int CHAT_OFFSET_FROM_BOTTOM = 40;
    private static final int VANILLA_VISIBLE_TICKS = 200;

    private static final Map<GuiMessage, PlayerInfo> OWNERS = new IdentityHashMap<>();
    private static PlayerInfo pendingLineOwner;

    public static PlayerInfo detect(Component content) {
        if (!ChatLookup.isHeadsEnabled()) {
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft != null ? minecraft.getConnection() : null;
        if (connection == null) {
            return null;
        }
        String stripped = ChatFormatting.stripFormatting(content.getString());
        if (stripped == null || stripped.isEmpty()) {
            return null;
        }

        Map<String, PlayerInfo> online = new HashMap<>();
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            //? if >=1.21.9 {
            online.put(info.getProfile().name().toLowerCase(Locale.ROOT), info);
            //?} else {
            /*online.put(info.getProfile().getName().toLowerCase(Locale.ROOT), info);
            *///?}
        }
        if (online.isEmpty()) {
            return null;
        }

        int length = stripped.length();
        int i = 0;
        while (i < length) {
            if (isNameChar(stripped.charAt(i))) {
                int start = i;
                while (i < length && isNameChar(stripped.charAt(i))) {
                    i++;
                }
                if (i - start >= 3 && i - start <= 16) {
                    PlayerInfo match = online.get(stripped.substring(start, i).toLowerCase(Locale.ROOT));
                    if (match != null) {
                        return match;
                    }
                }
            } else {
                i++;
            }
        }
        return null;
    }

    private static boolean isNameChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }

    public static void remember(GuiMessage message, PlayerInfo owner) {
        if (owner == null) {
            return;
        }
        if (OWNERS.size() > ChatLookup.HISTORY_LIMIT * 2) {
            OWNERS.clear();
        }
        OWNERS.put(message, owner);
    }

    public static PlayerInfo owner(GuiMessage message) {
        return OWNERS.get(message);
    }

    public static void beginLines(GuiMessage message) {
        pendingLineOwner = OWNERS.get(message);
    }

    public static void endLines() {
        pendingLineOwner = null;
    }

    public static PlayerInfo claimLineOwner() {
        PlayerInfo owner = pendingLineOwner;
        pendingLineOwner = null;
        return owner;
    }

    //? if >=26.1 {
    public static void render(GuiGraphicsExtractor graphics, ChatComponent chatHud, Minecraft minecraft) {
    //?} else {
    /*public static void render(GuiGraphics graphics, ChatComponent chatHud, Minecraft minecraft) {
    *///?}
        if (!ChatLookup.isHeadsEnabled() || minecraft == null || minecraft.options == null
                || minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN) {
            return;
        }
        ChatHudAccessor accessor = (ChatHudAccessor) chatHud;
        List<GuiMessage.Line> lines = accessor.chatlookup$getVisibleMessages();
        if (lines.isEmpty()) {
            return;
        }

        boolean focused = chatHud.isChatFocused();
        int guiTicks = currentGuiTicks(minecraft);
        double scale = accessor.chatlookup$getChatScale();
        int lineHeight = accessor.chatlookup$getLineHeight();
        int scrolled = accessor.chatlookup$getScrolledLines();
        double spacing = minecraft.options.chatLineSpacing().get();
        int textOffset = (int) Math.round(8.0 * (spacing + 1.0) - 4.0 * spacing);
        int chatBottom = (int) Math.floor((minecraft.getWindow().getGuiScaledHeight() - CHAT_OFFSET_FROM_BOTTOM) / scale);
        int slots = Math.min(chatHud.getLinesPerPage(), lines.size() - scrolled);

        boolean pushed = false;
        for (int slot = 0; slot < slots; slot++) {
            GuiMessage.Line line = lines.get(slot + scrolled);
            PlayerInfo owner = ((HeadedLine) (Object) line).chatlookup$headOwner();
            if (owner == null) {
                continue;
            }
            float alpha = 1.0f;
            if (!focused) {
                int age = guiTicks - line.addedTime();
                if (age >= VANILLA_VISIBLE_TICKS) {
                    continue;
                }
                alpha = timeFactor(age);
            }
            alpha *= (float) ChatAnimator.easeLineOpacity(1.0, line);
            if (alpha < 0.02f) {
                continue;
            }
            if (!pushed) {
                pushed = true;
                //? if >=1.21.6 {
                graphics.pose().pushMatrix();
                graphics.pose().scale((float) scale, (float) scale);
                //?} else {
                /*graphics.pose().pushPose();
                graphics.pose().scale((float) scale, (float) scale, 1.0f);
                *///?}
            }
            int y = chatBottom - slot * lineHeight - textOffset;
            drawFace(graphics, owner, LEFT_MARGIN, y, alpha);
        }
        if (pushed) {
            //? if >=1.21.6 {
            graphics.pose().popMatrix();
            //?} else {
            /*graphics.pose().popPose();
            *///?}
        }
    }

    //? if >=26.1 {
    private static void drawFace(GuiGraphicsExtractor graphics, PlayerInfo owner, int x, int y, float alpha) {
        int color = ((int) (alpha * 255.0f) << 24) | 0xFFFFFF;
        PlayerFaceExtractor.extractRenderState(graphics, owner.getSkin(), x, y, HEAD_SIZE, color);
    }
    //?}
    //? if >=1.21.2 && <26.1 {
    /*private static void drawFace(GuiGraphics graphics, PlayerInfo owner, int x, int y, float alpha) {
        int color = ((int) (alpha * 255.0f) << 24) | 0xFFFFFF;
        PlayerFaceRenderer.draw(graphics, owner.getSkin(), x, y, HEAD_SIZE, color);
    }
    *///?}
    //? if <1.21.2 {
    /*private static void drawFace(GuiGraphics graphics, PlayerInfo owner, int x, int y, float alpha) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        PlayerFaceRenderer.draw(graphics, owner.getSkin(), x, y, HEAD_SIZE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    *///?}

    private static int currentGuiTicks(Minecraft minecraft) {
        //? if >=26.2 {
        return minecraft.gui.hud.getGuiTicks();
        //?} else {
        /*return minecraft.gui.getGuiTicks();
        *///?}
    }

    /** Vanilla unfocused-chat fade curve for a line of the given tick age. */
    private static float timeFactor(int age) {
        double t = 1.0 - age / (double) VANILLA_VISIBLE_TICKS;
        t = Math.clamp(t * 10.0, 0.0, 1.0);
        return (float) (t * t);
    }

    private ChatHeads() {
    }
}
