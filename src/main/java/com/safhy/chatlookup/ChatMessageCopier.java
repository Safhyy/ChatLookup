package com.safhy.chatlookup;

import com.mojang.blaze3d.platform.InputConstants;
import com.safhy.chatlookup.mixin.ChatHudAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
//? if >=1.21.9 {
import net.minecraft.client.input.InputQuirks;
//?} else {
/*import net.minecraft.client.gui.screens.Screen;
*///?}
//? if >=1.21.6 {
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
//?}
import net.minecraft.client.gui.components.ChatComponent;
//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?} else {
/*import net.minecraft.client.GuiMessage;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
//? if >=1.21.6 {
import org.joml.Matrix3x2fStack;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
import net.minecraft.world.entity.player.ChatVisiblity;

import java.util.List;

public final class ChatMessageCopier {
    private static final int SNIPPET_LENGTH = 40;
    private static final int CHAT_OFFSET_FROM_BOTTOM = 40;

    private static final long POPUP_DURATION_MS = 2200;
    private static final long POPUP_FADE_IN_MS = 120;
    private static final long POPUP_FADE_OUT_MS = 300;
    private static final long HINT_COPIED_MS = 500;

    private static GuiMessage copiedMessage;
    private static long copiedAt = Long.MIN_VALUE;
    private static long popupShownAt = Long.MIN_VALUE;
    private static String popupSnippet = "";
    private static Component popupTitle = Component.literal("Message copied!");

    private static GuiMessage anchor;

    public static void clearAnchor() {
        anchor = null;
    }

    public static boolean hasAnchor() {
        return anchor != null;
    }

    public static boolean copyMessageAt(Minecraft minecraft, double mouseX, double mouseY, int windowHeight,
                                        boolean extend) {
        if (!ChatLookup.isCopyEnabled()) {
            return false;
        }
        GuiMessage target = messageAt(minecraft, mouseX, mouseY, windowHeight);
        if (target == null) {
            return false;
        }
        boolean copied;
        if (extend && ChatLookup.isMultiCopyEnabled() && anchor != null && anchor != target) {
            copied = copyRange(minecraft, anchor, target);
        } else {
            copied = copySingle(minecraft, target);
            anchor = copied && ChatLookup.isMultiCopyEnabled() ? target : null;
        }
        copiedMessage = copied ? target : null;
        return copied;
    }

    private static GuiMessage messageAt(Minecraft minecraft, double mouseX, double mouseY, int windowHeight) {
        ChatComponent chatHud = ChatLookup.getChat(minecraft);
        ChatHudAccessor hud = (ChatHudAccessor) chatHud;
        if (minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN) {
            return null;
        }

        double scale = hud.chatlookup$getChatScale();
        double localX = mouseX / scale - 4.0 - JumpToContext.contentShift();
        double localY = mouseY / scale;

        int width = Mth.ceil(hud.chatlookup$getWidth() / scale);
        if (localX < -4.0 || localX > width + 8.0) {
            return null;
        }

        int chatBottom = Mth.floor((windowHeight - CHAT_OFFSET_FROM_BOTTOM) / scale);
        double fromBottom = chatBottom - localY;
        if (fromBottom < 0.0) {
            return null;
        }
        int slot = (int) (fromBottom / hud.chatlookup$getLineHeight());

        List<GuiMessage.Line> visible = hud.chatlookup$getVisibleMessages();
        int scrolled = hud.chatlookup$getScrolledLines();
        if (slot >= chatHud.getLinesPerPage() || slot >= visible.size() - scrolled) {
            return null;
        }
        int lineIndex = slot + scrolled;

        int ordinal = -1;
        for (int i = 0; i <= lineIndex; i++) {
            if (visible.get(i).endOfEntry()) {
                ordinal++;
            }
        }
        if (ordinal < 0) {
            return null;
        }

        int seen = -1;
        for (GuiMessage line : hud.chatlookup$getMessages()) {
            if (ChatLookup.matches(line)) {
                seen++;
                if (seen == ordinal) {
                    return line;
                }
            }
        }
        return null;
    }

    private static boolean copySingle(Minecraft minecraft, GuiMessage line) {
        String text = plainOf(line);
        if (text.isBlank()) {
            return false;
        }
        minecraft.keyboardHandler.setClipboard(text);
        popupTitle = Component.literal("Message copied!");
        popupSnippet = snippet(text);
        popupShownAt = now();
        copiedAt = popupShownAt;
        return true;
    }

    private static boolean copyRange(Minecraft minecraft, GuiMessage from, GuiMessage to) {
        List<GuiMessage> messages = ((ChatHudAccessor) ChatLookup.getChat(minecraft)).chatlookup$getMessages();
        int first = indexOfIdentity(messages, from);
        int second = indexOfIdentity(messages, to);
        if (first < 0 || second < 0) {
            anchor = null;
            return false;
        }
        int newest = Math.min(first, second);
        int oldest = Math.max(first, second);

        StringBuilder out = new StringBuilder();
        String firstText = "";
        int copied = 0;
        for (int i = oldest; i >= newest; i--) {
            GuiMessage message = messages.get(i);
            if (!ChatLookup.matches(message)) {
                continue;
            }
            String text = plainOf(message);
            if (text.isBlank()) {
                continue;
            }
            if (copied > 0) {
                out.append('\n');
            } else {
                firstText = text;
            }
            out.append(text);
            copied++;
        }
        if (copied == 0) {
            return false;
        }
        minecraft.keyboardHandler.setClipboard(out.toString());
        popupTitle = Component.literal(copied + " messages copied!");
        popupSnippet = snippet(firstText);
        popupShownAt = now();
        copiedAt = popupShownAt;
        anchor = null;
        return true;
    }

    private static String plainOf(GuiMessage line) {
        String stripped = ChatFormatting.stripFormatting(MessageDecorator.stripForCopy(line.content()).getString());
        return stripped == null ? "" : stripped.strip();
    }

    private static String snippet(String text) {
        String snippet = text.replace('\n', ' ');
        if (snippet.length() > SNIPPET_LENGTH) {
            snippet = snippet.substring(0, SNIPPET_LENGTH - 1).stripTrailing() + "…";
        }
        return snippet;
    }

    private static int indexOfIdentity(List<GuiMessage> messages, GuiMessage target) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    private static long now() {
        return System.nanoTime() / 1_000_000L;
    }

    private static boolean copyModifierDown(Minecraft minecraft) {
        //? if >=1.21.9 {
        if (keyDown(minecraft, InputConstants.KEY_LCONTROL) || keyDown(minecraft, InputConstants.KEY_RCONTROL)) {
            return true;
        }
        //? if >=26.3 {
        return InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY
                && (keyDown(minecraft, InputConstants.KEY_LGUI) || keyDown(minecraft, InputConstants.KEY_RGUI));
        //?} else {
        /*return InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY
                && (keyDown(minecraft, InputConstants.KEY_LSUPER) || keyDown(minecraft, InputConstants.KEY_RSUPER));
        *///?}
        //?} else {
        /*return Screen.hasControlDown();
        *///?}
    }

    //? if >=1.21.9 {
    private static boolean keyDown(Minecraft minecraft, int key) {
        //? if >=26.3 {
        return InputConstants.isKeyDown(key);
        //?} else {
        /*return InputConstants.isKeyDown(minecraft.getWindow(), key);
        *///?}
    }
    //?}

    //? if >=26.1 {
    public static void renderHint(GuiGraphicsExtractor context, Minecraft minecraft, Font font,
                                  int mouseX, int mouseY, int windowHeight) {
    //?} else {
    /*public static void renderHint(GuiGraphics context, Minecraft minecraft, Font font,
                                  int mouseX, int mouseY, int windowHeight) {
    *///?}
        Component label = hintLabel(minecraft, mouseX, mouseY, windowHeight);
        if (label == null) {
            return;
        }
        //? if >=1.21.6 {
        context.setTooltipForNextFrame(font, List.of(label.getVisualOrderText()),
                DefaultTooltipPositioner.INSTANCE, mouseX, mouseY, true);
        //?} else {
        /*context.renderTooltip(font, label, mouseX, mouseY);
        *///?}
    }

    public static boolean isHintVisible(Minecraft minecraft, int mouseX, int mouseY, int windowHeight) {
        return hintLabel(minecraft, mouseX, mouseY, windowHeight) != null;
    }

    private static Component hintLabel(Minecraft minecraft, int mouseX, int mouseY, int windowHeight) {
        if (!ChatLookup.isCopyEnabled() || !ChatLookup.isCopyHintEnabled()) {
            return null;
        }
        GuiMessage hovered = messageAt(minecraft, mouseX, mouseY, windowHeight);
        if (hovered == null) {
            return null;
        }
        boolean copied = hovered == copiedMessage && now() - copiedAt < HINT_COPIED_MS;
        if (!copied && !copyModifierDown(minecraft)) {
            return null;
        }
        return copied
                ? Component.literal("Copied!")
                : Component.literal("Left-click to copy");
    }

    //? if >=26.1 {
    public static void renderAnchor(GuiGraphicsExtractor context, Minecraft minecraft, int windowHeight) {
    //?} else {
    /*public static void renderAnchor(GuiGraphics context, Minecraft minecraft, int windowHeight) {
    *///?}
        if (anchor == null || !ChatLookup.isCopyEnabled() || !ChatLookup.isMultiCopyEnabled()) {
            return;
        }
        ChatComponent chatHud = ChatLookup.getChat(minecraft);
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
                if (message == anchor) {
                    return;
                }
                continue;
            }
            ordinal++;
            if (message == anchor) {
                found = true;
                break;
            }
            if (ordinal >= visible.size()) {
                return;
            }
        }
        if (!found || ordinal < 0) {
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

        int lineHeight = hud.chatlookup$getLineHeight();
        double spacing = minecraft.options.chatLineSpacing().get();
        int textOffset = (int) Math.round(8.0 * (spacing + 1.0) - 4.0 * spacing);
        int chatBottom = Mth.floor((windowHeight - CHAT_OFFSET_FROM_BOTTOM) / scale);
        int color = 0xFF000000 | ChatLookup.getCopyBorderColor();

        float slide = ChatAnimator.chatDisplacement(chatHud);
        //? if >=1.21.6 {
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(0.0F, slide);
        matrices.scale(scale, scale);
        matrices.translate(4.0F, 0.0F);
        //?} else {
        /*PoseStack matrices = context.pose();
        matrices.pushPose();
        matrices.translate(0.0F, slide, 0.0F);
        matrices.scale(scale, scale, 1.0F);
        matrices.translate(4.0F, 0.0F, 0.0F);
        *///?}
        int barX = JumpToContext.contentShift();
        for (int index = start; index < end; index++) {
            int slot = index - scrolled;
            if (slot < 0 || slot >= onScreen) {
                continue;
            }
            int top = chatBottom - slot * lineHeight - textOffset;
            context.fill(barX - 3, top - 1, barX - 1, top + 9, color);
        }
        //? if >=1.21.6 {
        matrices.popMatrix();
        //?} else {
        /*matrices.popPose();
        *///?}
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

        Component title = popupTitle;
        int textWidth = Math.max(font.width(title), font.width(popupSnippet));
        int panelWidth = textWidth + 13;
        int panelHeight = 5 + 9 + 3 + 9 + 5;

        int x2 = screenWidth - 6;
        int x1 = x2 - panelWidth;
        int y2 = screenHeight - 36 + Math.round(slide);
        int y1 = y2 - panelHeight;

        int borderRgb = ChatLookup.getCopyBorderColor();
        int backgroundColor = (int) (alpha * 0xF0) << 24 | 0x100010;
        int borderColor = (int) (alpha * 0x80) << 24 | borderRgb;
        int accentColor = (int) (alpha * 0xFF) << 24 | borderRgb;
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
