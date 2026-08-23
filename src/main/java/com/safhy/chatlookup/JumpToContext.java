package com.safhy.chatlookup;

import com.safhy.chatlookup.mixin.ChatHudAccessor;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?} else {
/*import net.minecraft.client.GuiMessage;
*///?}
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
//? if >=1.21.6 {
import org.joml.Matrix3x2fStack;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
import net.minecraft.world.entity.player.ChatVisiblity;

import java.util.List;

public final class JumpToContext {
    private static final int CHAT_OFFSET_FROM_BOTTOM = 40;
    private static final int BUTTON_X = -4;
    private static final int CONTEXT_MARGIN = 16;
    private static final int CONTENT_SHIFT = 8;

    public static int contentShift() {
        return ChatLookup.isJumpButtonEnabled() && ChatLookup.isFiltering() ? CONTENT_SHIFT : 0;
    }

    private record Layout(ChatComponent chat, ChatHudAccessor hud, List<GuiMessage.Line> visible,
                          int scrolled, int onScreen, float scale, int chatBottom, int lineHeight,
                          int textOffset) {
    }

    private static Layout layout(Minecraft minecraft, int screenHeight) {
        if (minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN) {
            return null;
        }
        ChatComponent chat = ChatLookup.getChat(minecraft);
        ChatHudAccessor hud = (ChatHudAccessor) chat;
        float scale = (float) hud.chatlookup$getChatScale();
        if (scale <= 0.0f) {
            return null;
        }
        List<GuiMessage.Line> visible = hud.chatlookup$getVisibleMessages();
        int scrolled = hud.chatlookup$getScrolledLines();
        int onScreen = Math.min(visible.size() - scrolled, chat.getLinesPerPage());
        if (onScreen <= 0) {
            return null;
        }
        int lineHeight = hud.chatlookup$getLineHeight();
        double spacing = minecraft.options.chatLineSpacing().get();
        int textOffset = (int) Math.round(8.0 * (spacing + 1.0) - 4.0 * spacing);
        return new Layout(chat, hud, visible, scrolled, onScreen, scale,
                Mth.floor((screenHeight - CHAT_OFFSET_FROM_BOTTOM) / scale), lineHeight, textOffset);
    }

    private static int buttonWidth() {
        return Icons.width(Icons.JUMP) + 4;
    }

    private static int buttonHeight() {
        return Icons.height(Icons.JUMP) + 2;
    }

    private static boolean startsEntry(List<GuiMessage.Line> lines, int index) {
        return index + 1 >= lines.size() || lines.get(index + 1).endOfEntry();
    }

    private static int buttonY(Layout layout, int slot) {
        return layout.chatBottom() - slot * layout.lineHeight() - layout.textOffset() - 1;
    }

    //? if >=26.1 {
    public static void render(GuiGraphicsExtractor context, Minecraft minecraft, int screenHeight,
                              int mouseX, int mouseY) {
    //?} else {
    /*public static void render(GuiGraphics context, Minecraft minecraft, int screenHeight,
                              int mouseX, int mouseY) {
    *///?}
        if (!ChatLookup.isJumpButtonEnabled() || !ChatLookup.isFiltering()) {
            return;
        }
        Layout layout = layout(minecraft, screenHeight);
        if (layout == null) {
            return;
        }
        int width = buttonWidth();
        int height = buttonHeight();
        double localMouseX = mouseX / layout.scale() - 4.0;
        double localMouseY = mouseY / layout.scale();
        int gutterAlpha = (int) (255.0 * minecraft.options.textBackgroundOpacity().get());

        float slide = ChatAnimator.chatDisplacement(layout.chat());
        //? if >=1.21.6 {
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(0.0F, slide);
        matrices.scale(layout.scale(), layout.scale());
        matrices.translate(4.0F, 0.0F);
        //?} else {
        /*PoseStack matrices = context.pose();
        matrices.pushPose();
        matrices.translate(0.0F, slide, 0.0F);
        matrices.scale(layout.scale(), layout.scale(), 1.0F);
        matrices.translate(4.0F, 0.0F, 0.0F);
        *///?}
        if (gutterAlpha > 0) {
            context.fill(BUTTON_X, buttonY(layout, layout.onScreen() - 1) - 1,
                    BUTTON_X + CONTENT_SHIFT, buttonY(layout, 0) + 9, gutterAlpha << 24);
        }
        boolean anyHovered = false;
        for (int slot = 0; slot < layout.onScreen(); slot++) {
            if (!startsEntry(layout.visible(), slot + layout.scrolled())) {
                continue;
            }
            int x = BUTTON_X;
            int y = buttonY(layout, slot);
            boolean hovered = localMouseX >= x && localMouseX < x + width
                    && localMouseY >= y && localMouseY < y + height;
            anyHovered |= hovered;
            WidgetSkin.drawPanel(context, x, y, x + width, y + height,
                    hovered ? 0xF02A2306 : 0xC8121216,
                    hovered ? WidgetSkin.ACCENT : WidgetSkin.BORDER_IDLE);
            Icons.drawCentered(context, Icons.JUMP, x, y, width, height,
                    hovered ? 0xFFFFDE5C : WidgetSkin.LABEL_IDLE);
        }
        //? if >=1.21.6 {
        matrices.popMatrix();
        //?} else {
        /*matrices.popPose();
        *///?}
        if (anyHovered) {
            renderTooltip(context, minecraft, mouseX, mouseY);
        }
    }

    //? if >=26.1 {
    private static void renderTooltip(GuiGraphicsExtractor context, Minecraft minecraft, int mouseX, int mouseY) {
    //?} else {
    /*private static void renderTooltip(GuiGraphics context, Minecraft minecraft, int mouseX, int mouseY) {
    *///?}
        Component label = Component.literal("Jump to message");
        //? if >=1.21.6 {
        context.setTooltipForNextFrame(minecraft.font, label, mouseX, mouseY);
        //?} else {
        /*context.renderTooltip(minecraft.font, label, mouseX, mouseY);
        *///?}
    }

    public static boolean click(Minecraft minecraft, double mouseX, double mouseY, int screenHeight,
                                EditBox searchField) {
        if (!ChatLookup.isJumpButtonEnabled() || !ChatLookup.isFiltering()) {
            return false;
        }
        Layout layout = layout(minecraft, screenHeight);
        if (layout == null) {
            return false;
        }
        double localX = mouseX / layout.scale() - 4.0;
        double localY = mouseY / layout.scale();
        double fromBottom = layout.chatBottom() - localY;
        if (fromBottom < 0.0) {
            return false;
        }
        int slot = (int) (fromBottom / layout.lineHeight());
        if (slot >= layout.onScreen()) {
            return false;
        }
        int lineIndex = slot + layout.scrolled();
        if (!startsEntry(layout.visible(), lineIndex)) {
            return false;
        }
        int y = buttonY(layout, slot);
        if (localX < BUTTON_X || localX >= BUTTON_X + buttonWidth() || localY < y || localY >= y + buttonHeight()) {
            return false;
        }

        int ordinal = -1;
        for (int i = 0; i <= lineIndex; i++) {
            if (layout.visible().get(i).endOfEntry()) {
                ordinal++;
            }
        }
        if (ordinal < 0) {
            return false;
        }
        List<GuiMessage> messages = layout.hud().chatlookup$getMessages();
        int index = -1;
        int seen = -1;
        for (int i = 0; i < messages.size(); i++) {
            if (ChatLookup.matches(messages.get(i))) {
                seen++;
                if (seen == ordinal) {
                    index = i;
                    break;
                }
            }
        }
        if (index < 0) {
            return false;
        }

        ChatComponent chat = layout.chat();
        JumpFlash.mark(messages.get(index));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        ChatLookup.requestRebuildDepth(index + chat.getLinesPerPage() + CONTEXT_MARGIN);
        if (searchField != null) {
            searchField.setValue("");
        }
        ChatLookup.setQuery("");
        if (ChatLookup.hasPendingRebuildDepth()) {
            ChatLookup.budgetedRefresh(chat);
        }
        scrollTo(chat, index);
        return true;
    }

    private static void scrollTo(ChatComponent chat, int index) {
        ChatHudAccessor hud = (ChatHudAccessor) chat;
        List<GuiMessage.Line> lines = hud.chatlookup$getVisibleMessages();
        int start = -1;
        int seen = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).endOfEntry()) {
                seen++;
                if (seen == index) {
                    start = i;
                    break;
                }
            }
        }
        if (start < 0) {
            return;
        }
        int page = chat.getLinesPerPage();
        int scroll = Mth.clamp(start - page / 2, 0, Math.max(0, lines.size() - page));
        chat.scrollChat(scroll - hud.chatlookup$getScrolledLines());
    }

    private JumpToContext() {
    }
}
