package com.safhy.chatlookup;

import com.safhy.chatlookup.mixin.ChatHudAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;

import java.util.List;

public final class ChatMessageCopier {
    private static final int SNIPPET_LENGTH = 40;
    private static final int CHAT_OFFSET_FROM_BOTTOM = 40;

    private static final long POPUP_DURATION_MS = 2200;
    private static final long POPUP_FADE_IN_MS = 120;
    private static final long POPUP_FADE_OUT_MS = 300;

    private static long popupShownAt = Long.MIN_VALUE;
    private static String popupSnippet = "";

    public static boolean copyMessageAt(Minecraft minecraft, double mouseX, double mouseY, int windowHeight) {
        ChatComponent chatHud = ChatLookup.getChat(minecraft);
        ChatHudAccessor hud = (ChatHudAccessor) chatHud;
        if (minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN) {
            return false;
        }

        double scale = hud.chatlookup$getChatScale();
        double localX = mouseX / scale - 4.0;
        double localY = mouseY / scale;

        int width = Mth.ceil(hud.chatlookup$getWidth() / scale);
        if (localX < -4.0 || localX > width + 8.0) {
            return false;
        }

        int chatBottom = Mth.floor((windowHeight - CHAT_OFFSET_FROM_BOTTOM) / scale);
        double fromBottom = chatBottom - localY;
        if (fromBottom < 0.0) {
            return false;
        }
        int slot = (int) (fromBottom / hud.chatlookup$getLineHeight());

        List<GuiMessage.Line> visible = hud.chatlookup$getVisibleMessages();
        int scrolled = hud.chatlookup$getScrolledLines();
        if (slot >= chatHud.getLinesPerPage() || slot >= visible.size() - scrolled) {
            return false;
        }
        int lineIndex = slot + scrolled;

        int ordinal = -1;
        for (int i = 0; i <= lineIndex; i++) {
            if (visible.get(i).endOfEntry()) {
                ordinal++;
            }
        }
        if (ordinal < 0) {
            return false;
        }

        int seen = -1;
        for (GuiMessage line : hud.chatlookup$getMessages()) {
            if (ChatLookup.matches(line)) {
                seen++;
                if (seen == ordinal) {
                    return copy(minecraft, line);
                }
            }
        }
        return false;
    }

    private static boolean copy(Minecraft minecraft, GuiMessage line) {
        String stripped = ChatFormatting.stripFormatting(line.content().getString());
        String text = stripped == null ? "" : stripped;
        if (text.isBlank()) {
            return false;
        }
        minecraft.keyboardHandler.setClipboard(text);

        String snippet = text.replace('\n', ' ');
        if (snippet.length() > SNIPPET_LENGTH) {
            snippet = snippet.substring(0, SNIPPET_LENGTH - 1).stripTrailing() + "…";
        }
        popupSnippet = snippet;
        popupShownAt = now();
        return true;
    }

    private static long now() {
        return System.nanoTime() / 1_000_000L;
    }

    //? if >=26.1 {
    public static void renderCopyPopup(GuiGraphicsExtractor context, Font font, int screenWidth, int screenHeight) {
    //?} else {
    /*public static void renderCopyPopup(GuiGraphics context, Font font, int screenWidth, int screenHeight) {
    *///?}
        long elapsed = now() - popupShownAt;
        if (elapsed < 0 || elapsed >= POPUP_DURATION_MS) {
            return;
        }

        float alpha;
        float slide = 0.0F;
        if (elapsed < POPUP_FADE_IN_MS) {
            float t = elapsed / (float) POPUP_FADE_IN_MS;
            alpha = t * t * (3.0F - 2.0F * t);
            slide = (1.0F - alpha) * 6.0F;
        } else if (elapsed > POPUP_DURATION_MS - POPUP_FADE_OUT_MS) {
            float t = (POPUP_DURATION_MS - elapsed) / (float) POPUP_FADE_OUT_MS;
            alpha = t * t;
        } else {
            alpha = 1.0F;
        }
        if (alpha < 0.05F) {
            return;
        }

        Component title = Component.translatable("chatlookup.copied.title");
        int textWidth = Math.max(font.width(title), font.width(popupSnippet));
        int panelWidth = textWidth + 13;
        int panelHeight = 5 + 9 + 3 + 9 + 5;

        int x2 = screenWidth - 6;
        int x1 = x2 - panelWidth;
        int y2 = screenHeight - 36 + Math.round(slide);
        int y1 = y2 - panelHeight;

        int backgroundColor = (int) (alpha * 0xF0) << 24 | 0x100010;
        int borderColor = (int) (alpha * 0x80) << 24 | 0x55FF55;
        int accentColor = (int) (alpha * 0xFF) << 24 | 0x55FF55;
        int titleColor = (int) (alpha * 0xFF) << 24 | 0xFFFFFF;
        int snippetColor = (int) (alpha * 0xFF) << 24 | 0xAAAAAA;

        context.fill(x1, y1, x2, y2, backgroundColor);
        context.fill(x1, y1 - 1, x2, y1, borderColor);
        context.fill(x1, y2, x2, y2 + 1, borderColor);
        context.fill(x1 - 1, y1 - 1, x1, y2 + 1, borderColor);
        context.fill(x2, y1 - 1, x2 + 1, y2 + 1, borderColor);
        context.fill(x1, y1, x1 + 3, y2, accentColor);

        int textX = x1 + 7;
        WidgetSkin.text(context, font, title, textX, y1 + 5, titleColor, true);
        WidgetSkin.text(context, font, Component.literal(popupSnippet), textX, y1 + 5 + 9 + 3, snippetColor, true);
    }

    private ChatMessageCopier() {
    }
}
