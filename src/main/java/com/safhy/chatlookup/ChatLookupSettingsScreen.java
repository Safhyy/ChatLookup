package com.safhy.chatlookup;

//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Util;
//?} else {
/*import net.minecraft.Util;
*///?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ChatLookupSettingsScreen extends Screen {
    private static final int ROW_H = 16;
    private static final int TAB_H = 14;
    private static final int MAX_ROWS = 6;
    private static final int TITLE_COLOR = 0xFFE8E8F0;
    private static final int LABEL_COLOR = 0xFFC8C8D0;
    private static final int REVEAL_MS = 180;
    private static final int UNDERLINE_MS = 160;
    private static final int REVEAL_SLIDE = 8;

    private enum Tab {
        GENERAL("chatlookup.settings.tab.general"),
        MESSAGES("chatlookup.settings.tab.messages"),
        COPYING("chatlookup.settings.tab.copying"),
        MENTIONS("chatlookup.settings.tab.mentions");

        final String key;

        Tab(String key) {
            this.key = key;
        }

        Component label() {
            return Component.translatable(this.key);
        }
    }

    private record RowLabel(Component text, int y) {
    }

    private final Screen parent;

    private final Map<Tab, List<AbstractWidget>> tabRows = new EnumMap<>(Tab.class);
    private final Map<Tab, List<RowLabel>> tabLabels = new EnumMap<>(Tab.class);
    private final List<AbstractWidget> allControls = new ArrayList<>();
    private final Map<AbstractWidget, Integer> rowBaseY = new IdentityHashMap<>();
    private final List<TabButton> tabButtons = new ArrayList<>();
    private Tab currentTab = Tab.GENERAL;
    private Tab underlineFrom = Tab.GENERAL;
    private long revealStart;
    private long underlineStart;
    private ColorPickerOverlay picker;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    public ChatLookupSettingsScreen(Screen parent) {
        super(Component.translatable("chatlookup.settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.tabRows.clear();
        this.tabLabels.clear();
        this.allControls.clear();
        this.rowBaseY.clear();
        this.tabButtons.clear();
        for (Tab tab : Tab.values()) {
            this.tabRows.put(tab, new ArrayList<>());
            this.tabLabels.put(tab, new ArrayList<>());
        }

        int tabsWidth = 2 * (Tab.values().length - 1);
        for (Tab tab : Tab.values()) {
            tabsWidth += this.font.width(tab.label()) + 10;
        }
        this.panelW = Math.max(Math.max(220, tabsWidth + 16), longestRowLabel() + SwitchWidget.WIDTH + 28);
        this.panelH = 20 + TAB_H + 4 + MAX_ROWS * ROW_H + 4 + 14 + 8;
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        int tabX = this.panelX + 8;
        int tabY = this.panelY + 18;
        for (Tab tab : Tab.values()) {
            TabButton button = new TabButton(tabX, tabY, this.font.width(tab.label()) + 10, TAB_H, tab);
            this.tabButtons.add(button);
            this.allControls.add(button);
            this.addRenderableWidget(button);
            tabX += button.getWidth() + 2;
        }

        int rowsTop = tabY + TAB_H + 4;
        int y = rowsTop;
        y = addSwitchRow(Tab.GENERAL, y, Component.translatable("chatlookup.settings.animation"),
                ChatLookup::isAnimationEnabled, () -> ChatLookup.setAnimationEnabled(!ChatLookup.isAnimationEnabled()));
        y = addSwitchRow(Tab.GENERAL, y, Component.translatable("chatlookup.settings.heads"),
                ChatLookup::isHeadsEnabled, () -> ChatLookup.setHeadsEnabled(!ChatLookup.isHeadsEnabled()));
        y = addSwitchRow(Tab.GENERAL, y, Component.translatable("chatlookup.settings.indicator"),
                () -> !ChatLookup.isIndicatorHidden(), () -> ChatLookup.setIndicatorHidden(!ChatLookup.isIndicatorHidden()));
        y = addSwitchRow(Tab.GENERAL, y, Component.translatable("chatlookup.settings.save_history"),
                ChatLookup::isHistorySaveEnabled, () -> ChatLookup.setHistorySaveEnabled(!ChatLookup.isHistorySaveEnabled()));
        addColorRow(Tab.GENERAL, y, Component.translatable("chatlookup.settings.highlight_color"),
                ChatLookup::getHighlightColor, ChatLookup.DEFAULT_HIGHLIGHT_COLOR, ChatLookup::setHighlightColor);

        y = rowsTop;
        y = addSwitchRow(Tab.MESSAGES, y, Component.translatable("chatlookup.settings.stack"),
                ChatLookup::isStackingEnabled, () -> ChatLookup.setStackingEnabled(!ChatLookup.isStackingEnabled()));
        y = addSwitchRow(Tab.MESSAGES, y, Component.translatable("chatlookup.settings.stack_consecutive"),
                ChatLookup::isStackConsecutiveOnly, () -> ChatLookup.setStackConsecutiveOnly(!ChatLookup.isStackConsecutiveOnly()));
        y = addColorRow(Tab.MESSAGES, y, Component.translatable("chatlookup.settings.stack_color"),
                ChatLookup::getStackColor, ChatLookup.DEFAULT_STACK_COLOR, ChatLookup::setStackColor);
        y = addSwitchRow(Tab.MESSAGES, y, Component.translatable("chatlookup.settings.timestamps"),
                ChatLookup::isTimestampsEnabled, () -> ChatLookup.setTimestampsEnabled(!ChatLookup.isTimestampsEnabled()));
        y = addSwitchRow(Tab.MESSAGES, y, Component.translatable("chatlookup.settings.clock12"),
                ChatLookup::isTwelveHourClock, () -> ChatLookup.setTwelveHourClock(!ChatLookup.isTwelveHourClock()));
        addColorRow(Tab.MESSAGES, y, Component.translatable("chatlookup.settings.timestamp_color"),
                ChatLookup::getTimestampColor, ChatLookup.DEFAULT_TIMESTAMP_COLOR, ChatLookup::setTimestampColor);

        y = rowsTop;
        y = addSwitchRow(Tab.COPYING, y, Component.translatable("chatlookup.settings.copy"),
                ChatLookup::isCopyEnabled, () -> ChatLookup.setCopyEnabled(!ChatLookup.isCopyEnabled()));
        y = addSwitchRow(Tab.COPYING, y, Component.translatable("chatlookup.settings.copy_no_timestamp"),
                ChatLookup::isCopyStripTimestamp, () -> ChatLookup.setCopyStripTimestamp(!ChatLookup.isCopyStripTimestamp()));
        y = addSwitchRow(Tab.COPYING, y, Component.translatable("chatlookup.settings.copy_no_counter"),
                ChatLookup::isCopyStripCounter, () -> ChatLookup.setCopyStripCounter(!ChatLookup.isCopyStripCounter()));
        addColorRow(Tab.COPYING, y, Component.translatable("chatlookup.settings.copy_border_color"),
                ChatLookup::getCopyBorderColor, ChatLookup.DEFAULT_COPY_BORDER_COLOR, ChatLookup::setCopyBorderColor);

        y = rowsTop;
        y = addSwitchRow(Tab.MENTIONS, y, Component.translatable("chatlookup.settings.mentions"),
                ChatLookup::isMentionEnabled, () -> ChatLookup.setMentionEnabled(!ChatLookup.isMentionEnabled()));
        y = addSwitchRow(Tab.MENTIONS, y, Component.translatable("chatlookup.settings.mention_sound"),
                ChatLookup::isMentionSoundEnabled, () -> ChatLookup.setMentionSoundEnabled(!ChatLookup.isMentionSoundEnabled()));
        addColorRow(Tab.MENTIONS, y, Component.translatable("chatlookup.settings.mention_color"),
                ChatLookup::getMentionColor, ChatLookup.DEFAULT_MENTION_COLOR, ChatLookup::setMentionColor);

        FlatButton done = new FlatButton(this.panelX + (this.panelW - 60) / 2, rowsTop + MAX_ROWS * ROW_H + 4, 60, 14,
                Component.translatable("chatlookup.settings.done"), null, true, this::onClose);
        this.allControls.add(done);
        this.addRenderableWidget(done);

        SearchFieldWidget hexField = new SearchFieldWidget(this.font, 0, 0,
                ColorPickerOverlay.HEX_W, 12, Component.translatable("chatlookup.settings.hex"));
        hexField.setHint(Component.literal("#RRGGBB")
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY, net.minecraft.ChatFormatting.ITALIC));
        this.picker = new ColorPickerOverlay(this.width, this.height, hexField,
                () -> setControlsActive(true));
        this.addRenderableWidget(this.picker);
        this.addRenderableWidget(hexField);

        applyTabVisibility();
        this.underlineFrom = this.currentTab;
        this.underlineStart = 0;
        this.revealStart = Util.getMillis();
    }

    private int longestRowLabel() {
        int widest = 0;
        for (String key : new String[]{
                "chatlookup.settings.animation", "chatlookup.settings.heads", "chatlookup.settings.indicator",
                "chatlookup.settings.save_history", "chatlookup.settings.highlight_color",
                "chatlookup.settings.stack", "chatlookup.settings.stack_consecutive",
                "chatlookup.settings.stack_color", "chatlookup.settings.timestamps",
                "chatlookup.settings.clock12", "chatlookup.settings.timestamp_color",
                "chatlookup.settings.copy", "chatlookup.settings.copy_no_timestamp",
                "chatlookup.settings.copy_no_counter", "chatlookup.settings.copy_border_color",
                "chatlookup.settings.mentions", "chatlookup.settings.mention_sound",
                "chatlookup.settings.mention_color"}) {
            widest = Math.max(widest, this.font.width(Component.translatable(key)));
        }
        return widest;
    }

    private int addSwitchRow(Tab tab, int y, Component label, BooleanSupplier state, Runnable onToggle) {
        this.tabLabels.get(tab).add(new RowLabel(label, y + 3));
        SwitchWidget widget = new SwitchWidget(this.panelX + this.panelW - 8 - SwitchWidget.WIDTH, y,
                label, state, onToggle);
        this.tabRows.get(tab).add(widget);
        this.allControls.add(widget);
        this.rowBaseY.put(widget, y);
        this.addRenderableWidget(widget);
        return y + ROW_H;
    }

    private int addColorRow(Tab tab, int y, Component label,
                            IntSupplier color, int defaultColor, IntConsumer apply) {
        this.tabLabels.get(tab).add(new RowLabel(label, y + 3));
        ColorSwatchButton swatch = new ColorSwatchButton(this.panelX + this.panelW - 8 - ColorSwatchButton.WIDTH, y,
                label, color, () -> openPicker(label, color, defaultColor, apply));
        this.tabRows.get(tab).add(swatch);
        this.allControls.add(swatch);
        this.rowBaseY.put(swatch, y);
        this.addRenderableWidget(swatch);
        return y + ROW_H;
    }

    private void openPicker(Component label, IntSupplier color, int defaultColor, IntConsumer apply) {
        setControlsActive(false);
        this.picker.open(label, color.getAsInt(), defaultColor, apply);
    }

    private void setControlsActive(boolean value) {
        for (AbstractWidget widget : this.allControls) {
            widget.active = value;
        }
    }

    private void applyTabVisibility() {
        for (Tab tab : Tab.values()) {
            for (AbstractWidget widget : this.tabRows.get(tab)) {
                widget.visible = tab == this.currentTab;
            }
        }
    }

    @Override
    //? if >=26.1 {
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    //?} else {
    /*public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    *///?}
        context.fill(0, 0, this.width, this.height, 0x88000000);
        WidgetSkin.drawPanel(context, this.panelX, this.panelY,
                this.panelX + this.panelW, this.panelY + this.panelH, 0xF4111116, WidgetSkin.BORDER_HOVER);
        WidgetSkin.text(context, this.font, this.title, this.panelX + 8, this.panelY + 6, TITLE_COLOR, false);
        int tabBarBottom = this.panelY + 18 + TAB_H;
        context.fill(this.panelX + 8, tabBarBottom, this.panelX + this.panelW - 8, tabBarBottom + 1,
                WidgetSkin.BORDER_IDLE);

        long now = Util.getMillis();

        float glide = progress(now, this.underlineStart, UNDERLINE_MS);
        TabButton from = buttonFor(this.underlineFrom);
        TabButton to = buttonFor(this.currentTab);
        if (to != null) {
            int fromX = (from != null ? from : to).getX();
            int fromW = (from != null ? from : to).getWidth();
            int lineX = fromX + Math.round((to.getX() - fromX) * glide);
            int lineW = fromW + Math.round((to.getWidth() - fromW) * glide);
            context.fill(lineX + 1, tabBarBottom - 1, lineX + lineW - 1, tabBarBottom + 1, WidgetSkin.ACCENT);
        }

        float reveal = progress(now, this.revealStart, REVEAL_MS);
        int slide = Math.round((1.0f - reveal) * REVEAL_SLIDE);
        for (AbstractWidget widget : this.tabRows.get(this.currentTab)) {
            Integer base = this.rowBaseY.get(widget);
            if (base != null) {
                widget.setY(base + slide);
            }
        }
        int labelAlpha = 24 + (int) (231 * reveal);
        int labelColor = (labelAlpha << 24) | (LABEL_COLOR & 0xFFFFFF);
        for (RowLabel label : this.tabLabels.get(this.currentTab)) {
            WidgetSkin.text(context, this.font, label.text(), this.panelX + 8, label.y() + slide, labelColor, false);
        }
    }

    private static float progress(long now, long start, int durationMs) {
        if (start == 0) {
            return 1.0f;
        }
        float t = (now - start) / (float) durationMs;
        t = t < 0.0f ? 0.0f : Math.min(t, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    private TabButton buttonFor(Tab tab) {
        for (TabButton button : this.tabButtons) {
            if (button.tab == tab) {
                return button;
            }
        }
        return null;
    }

    @Override
    //? if >=1.21.9 {
    public boolean keyPressed(KeyEvent keyEvent) {
        int key = keyEvent.key();
    //?} else {
    /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int key = keyCode;
    *///?}
        if (key == GLFW.GLFW_KEY_ESCAPE && this.picker != null && this.picker.isOpen()) {
            this.picker.close();
            return true;
        }
        //? if >=1.21.9 {
        return super.keyPressed(keyEvent);
        //?} else {
        /*return super.keyPressed(keyCode, scanCode, modifiers);
        *///?}
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            ChatLookup.setScreen(this.minecraft, this.parent);
        }
    }

    @Override
    public void removed() {
        ChatLookupConfig.save();
    }

    private class TabButton extends AbstractWidget {
        private final Tab tab;

        TabButton(int x, int y, int width, int height, Tab tab) {
            super(x, y, width, height, tab.label());
            this.tab = tab;
        }

        @Override
        //? if >=1.21.9 {
        public void onClick(MouseButtonEvent click, boolean doubled) {
        //?} else {
        /*public void onClick(double mouseX, double mouseY) {
        *///?}
            ChatLookupSettingsScreen screen = ChatLookupSettingsScreen.this;
            if (screen.currentTab != this.tab) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                screen.underlineFrom = screen.currentTab;
                screen.currentTab = this.tab;
                long now = Util.getMillis();
                screen.underlineStart = now;
                screen.revealStart = now;
                screen.applyTabVisibility();
            }
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
            boolean selected = ChatLookupSettingsScreen.this.currentTab == this.tab;
            int x1 = this.getX();
            int y1 = this.getY();
            int x2 = x1 + this.width;
            int y2 = y1 + this.height;
            if (this.isHovered() && !selected) {
                context.fill(x1, y1, x2, y2 - 1, 0x18FFFFFF);
            }
            int textColor = selected ? 0xFFFFDE5C : this.isHovered() ? 0xFFFFFFFF : WidgetSkin.LABEL_IDLE;
            Minecraft client = Minecraft.getInstance();
            int textWidth = client.font.width(this.getMessage());
            WidgetSkin.text(context, client.font, this.getMessage(),
                    x1 + (this.width - textWidth) / 2, y1 + (this.height - 8) / 2 + 1, textColor, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
            this.defaultButtonNarrationText(builder);
        }
    }
}
