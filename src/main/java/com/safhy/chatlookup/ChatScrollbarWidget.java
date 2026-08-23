package com.safhy.chatlookup;

import com.safhy.chatlookup.mixin.ChatHudAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ChatScrollbarWidget extends AbstractWidget {
    private static final int MIN_THUMB = 10;
    private static final int TRACK_COLOR = 0x60000000;
    private static final int THUMB_IDLE = 0xB0808090;
    private static final int THUMB_ACTIVE = WidgetSkin.ACCENT;

    private final Minecraft minecraft;
    private final Runnable focusRestore;

    private int totalLines;
    private int historyLines;
    private int pageLines;
    private boolean dragging;
    private double grabOffset;

    public ChatScrollbarWidget(Minecraft minecraft, Runnable focusRestore) {
        super(0, 0, 3, 1, Component.literal("Chat scrollbar"));
        this.minecraft = minecraft;
        this.focusRestore = focusRestore;
        this.visible = false;
        this.updateBounds();
    }

    public void updateBounds() {
        ChatComponent chat = ChatLookup.getChat(this.minecraft);
        ChatHudAccessor hud = (ChatHudAccessor) chat;
        double scale = hud.chatlookup$getChatScale();
        if (scale <= 0.0) {
            this.visible = false;
            return;
        }
        this.pageLines = chat.getLinesPerPage();
        this.totalLines = hud.chatlookup$getVisibleMessages().size();
        this.historyLines = estimateHistoryLines(chat, this.totalLines);

        int screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        int bottom = (int) Math.round(Mth.floor((screenHeight - 40) / scale) * scale);
        int trackHeight = (int) Math.round(this.pageLines * hud.chatlookup$getLineHeight() * scale);
        int barWidth = Math.max(3, (int) Math.round(3.0 * scale));
        int x = (int) Math.round((4.0 + JumpToContext.contentShift()) * scale) + hud.chatlookup$getWidth() + 2;

        this.setX(x);
        this.setY(bottom - trackHeight);
        this.setSize(barWidth, trackHeight);
        this.visible = ChatLookup.isScrollbarEnabled() && this.pageLines > 0 && this.historyLines > this.pageLines;
        this.active = this.visible;
        if (!this.visible) {
            this.dragging = false;
        }
    }

    private static int estimateHistoryLines(ChatComponent chat, int loadedLines) {
        int unloaded = ChatLookup.getUnloadedCount();
        int loadedMessages = ChatLookup.getLoadedMessageCount(chat);
        if (unloaded <= 0 || loadedMessages <= 0 || loadedLines <= 0) {
            return loadedLines;
        }
        long estimate = (long) loadedLines * (loadedMessages + unloaded) / loadedMessages;
        return (int) Math.max(loadedLines, Math.min(Integer.MAX_VALUE, estimate));
    }

    private int maxScroll() {
        return Math.max(1, this.historyLines - this.pageLines);
    }

    private int thumbHeight() {
        int track = Math.max(1, this.height);
        int shortest = Math.min(MIN_THUMB, track);
        return Mth.clamp((int) ((long) track * this.pageLines / Math.max(1, this.historyLines)), shortest, track);
    }

    private int thumbTop() {
        int travel = this.height - this.thumbHeight();
        int scrolled = ((ChatHudAccessor) ChatLookup.getChat(this.minecraft)).chatlookup$getScrolledLines();
        float fraction = Mth.clamp(scrolled / (float) this.maxScroll(), 0.0f, 1.0f);
        return this.getY() + travel - Math.round(travel * fraction);
    }

    private void scrollToThumbTop(double top) {
        int travel = this.height - this.thumbHeight();
        if (travel <= 0) {
            return;
        }
        double clamped = Mth.clamp(top, this.getY(), (double) (this.getY() + travel));
        float fraction = 1.0f - (float) ((clamped - this.getY()) / travel);
        int target = Math.round(fraction * this.maxScroll());
        ChatComponent chat = ChatLookup.getChat(this.minecraft);
        ChatLookup.ensureLoadedLines(chat, target);
        chat.scrollChat(target - ((ChatHudAccessor) chat).chatlookup$getScrolledLines());
        this.updateBounds();
    }

    @Override
    //? if >=1.21.9 {
    public void onClick(MouseButtonEvent click, boolean doubled) {
        double mouseY = click.y();
    //?} else {
    /*public void onClick(double mouseX, double mouseY) {
    *///?}
        int top = this.thumbTop();
        int thumb = this.thumbHeight();
        if (mouseY >= top && mouseY < top + thumb) {
            this.grabOffset = mouseY - top;
        } else {
            this.grabOffset = thumb / 2.0;
            this.scrollToThumbTop(mouseY - this.grabOffset);
        }
        this.dragging = true;
    }

    @Override
    //? if >=1.21.9 {
    protected void onDrag(MouseButtonEvent click, double dragX, double dragY) {
        double mouseY = click.y();
    //?} else {
    /*protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
    *///?}
        if (this.dragging) {
            this.scrollToThumbTop(mouseY - this.grabOffset);
        }
    }

    @Override
    //? if >=1.21.9 {
    public void onRelease(MouseButtonEvent click) {
    //?} else {
    /*public void onRelease(double mouseX, double mouseY) {
    *///?}
        this.dragging = false;
        this.focusRestore.run();
    }

    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent navigation) {
        return null;
    }

    @Override
    //? if >=26.1 {
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    //?} else {
    /*protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
    *///?}
        int x1 = this.getX();
        int x2 = x1 + this.width;
        context.fill(x1, this.getY(), x2, this.getY() + this.height, TRACK_COLOR);

        int top = this.thumbTop();
        int bottom = top + this.thumbHeight();
        int color = this.dragging || this.isHovered() ? THUMB_ACTIVE : THUMB_IDLE;
        WidgetSkin.fillRounded(context, x1, top, x2, bottom, color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        this.defaultButtonNarrationText(builder);
    }
}
