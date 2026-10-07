package com.safhy.chatlookup;

import com.mojang.blaze3d.platform.InputConstants;
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
    private static final int MAX_ROWS = 8;
    private static final int TITLE_COLOR = 0xFFE8E8F0;
    private static final int LABEL_COLOR = 0xFFC8C8D0;
    private static final int REVEAL_MS = 180;
    private static final int UNDERLINE_MS = 160;
    private static final int REVEAL_SLIDE = 8;

    private enum Tab {
        GENERAL("General"),
        SEARCH("Search"),
        MESSAGES("Messages"),
        COPYING("Copying"),
        MENTIONS("Mentions"),
        WORDS("Words"),
        MACROS("Macros");

        final String label;

        Tab(String label) {
            this.label = label;
        }

        Component label() {
            return Component.literal(this.label);
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
    private int tabRowCount = 1;
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
        super(Component.literal("ChatLookup Settings"));
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
            tabsWidth += tabWidth(tab);
        }
        int widest = Math.max(Math.max(220, tabsWidth + 16), longestRowLabel() + chatHeightWidth() + 28);
        this.panelW = Math.min(widest, Math.max(220, this.width - 8));
        this.tabRowCount = countTabRows();
        this.panelH = 20 + this.tabRowCount * TAB_H + 4 + MAX_ROWS * ROW_H + 4 + 14 + 8;
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        int tabX = this.panelX + 8;
        int tabY = this.panelY + 18;
        int tabLimit = this.panelX + this.panelW - 8;
        for (Tab tab : Tab.values()) {
            int width = tabWidth(tab);
            if (tabX > this.panelX + 8 && tabX + width > tabLimit) {
                tabX = this.panelX + 8;
                tabY += TAB_H;
            }
            TabButton button = new TabButton(tabX, tabY, width, TAB_H, tab);
            this.tabButtons.add(button);
            this.allControls.add(button);
            this.addRenderableWidget(button);
            tabX += width + 2;
        }

        int rowsTop = tabY + TAB_H + 4;
        int y = rowsTop;
        y = addSwitchRow(Tab.GENERAL, y, Component.literal("Smooth chat animations"),
                ChatLookup::isAnimationEnabled, () -> ChatLookup.setAnimationEnabled(!ChatLookup.isAnimationEnabled()));
        y = addSwitchRow(Tab.GENERAL, y, Component.literal("Player heads in chat"),
                ChatLookup::isHeadsEnabled, () -> ChatLookup.setHeadsEnabled(!ChatLookup.isHeadsEnabled()));
        y = addSwitchRow(Tab.GENERAL, y, Component.literal("Message security indicator"),
                () -> !ChatLookup.isIndicatorHidden(), () -> ChatLookup.setIndicatorHidden(!ChatLookup.isIndicatorHidden()));
        y = addSwitchRow(Tab.GENERAL, y, Component.literal("Save chat history to disk"),
                ChatLookup::isHistorySaveEnabled, () -> ChatLookup.setHistorySaveEnabled(!ChatLookup.isHistorySaveEnabled()));
        y = addSwitchRow(Tab.GENERAL, y, Component.literal("Chat scrollbar"),
                ChatLookup::isScrollbarEnabled, () -> ChatLookup.setScrollbarEnabled(!ChatLookup.isScrollbarEnabled()));
        y = addSwitchRow(Tab.GENERAL, y, Component.literal("Math preview in the input"),
                ChatLookup::isMathPreviewEnabled, () -> ChatLookup.setMathPreviewEnabled(!ChatLookup.isMathPreviewEnabled()));
        y = addChatHeightRow(Tab.GENERAL, y, Component.literal("Max chat height"));
        addSwitchRow(Tab.GENERAL, y, Component.literal("Check for updates"),
                ChatLookup::isUpdateCheckEnabled, () -> ChatLookup.setUpdateCheckEnabled(!ChatLookup.isUpdateCheckEnabled()));

        y = rowsTop;
        y = addSwitchRow(Tab.SEARCH, y, Component.literal("Show inverted search button"),
                ChatLookup::isInvertButtonVisible,
                () -> ChatLookup.setInvertButtonVisible(!ChatLookup.isInvertButtonVisible()));
        y = addSwitchRow(Tab.SEARCH, y, Component.literal("Jump to surrounding button"),
                ChatLookup::isJumpButtonEnabled, () -> ChatLookup.setJumpButtonEnabled(!ChatLookup.isJumpButtonEnabled()));
        y = addSwitchRow(Tab.SEARCH, y, Component.literal("Flash the message after jumping"),
                ChatLookup::isJumpFlashEnabled, () -> ChatLookup.setJumpFlashEnabled(!ChatLookup.isJumpFlashEnabled()));
        addColorRow(Tab.SEARCH, y, Component.literal("Highlight color"),
                ChatLookup::getHighlightColor, ChatLookup.DEFAULT_HIGHLIGHT_COLOR, ChatLookup::setHighlightColor);

        y = rowsTop;
        y = addSwitchRow(Tab.MESSAGES, y, Component.literal("Stack repeated messages"),
                ChatLookup::isStackingEnabled, () -> ChatLookup.setStackingEnabled(!ChatLookup.isStackingEnabled()));
        y = addSwitchRow(Tab.MESSAGES, y, Component.literal("Stack only consecutive messages"),
                ChatLookup::isStackConsecutiveOnly, () -> ChatLookup.setStackConsecutiveOnly(!ChatLookup.isStackConsecutiveOnly()));
        y = addColorRow(Tab.MESSAGES, y, Component.literal("Stack counter color"),
                ChatLookup::getStackColor, ChatLookup.DEFAULT_STACK_COLOR, ChatLookup::setStackColor);
        y = addSwitchRow(Tab.MESSAGES, y, Component.literal("Show message timestamps"),
                ChatLookup::isTimestampsEnabled, () -> ChatLookup.setTimestampsEnabled(!ChatLookup.isTimestampsEnabled()));
        y = addSwitchRow(Tab.MESSAGES, y, Component.literal("12-hour clock (AM/PM)"),
                ChatLookup::isTwelveHourClock, () -> ChatLookup.setTwelveHourClock(!ChatLookup.isTwelveHourClock()));
        addColorRow(Tab.MESSAGES, y, Component.literal("Timestamp color"),
                ChatLookup::getTimestampColor, ChatLookup.DEFAULT_TIMESTAMP_COLOR, ChatLookup::setTimestampColor);

        y = rowsTop;
        y = addSwitchRow(Tab.COPYING, y, Component.literal("Copy messages (Ctrl+click)"),
                ChatLookup::isCopyEnabled, () -> ChatLookup.setCopyEnabled(!ChatLookup.isCopyEnabled()));
        y = addSwitchRow(Tab.COPYING, y, Component.literal("Multi-message copy (Ctrl+Shift+click)"),
                ChatLookup::isMultiCopyEnabled, () -> ChatLookup.setMultiCopyEnabled(!ChatLookup.isMultiCopyEnabled()));
        y = addSwitchRow(Tab.COPYING, y, Component.literal("Show copy hint on Ctrl"),
                ChatLookup::isCopyHintEnabled, () -> ChatLookup.setCopyHintEnabled(!ChatLookup.isCopyHintEnabled()));
        y = addSwitchRow(Tab.COPYING, y, Component.literal("Copy without timestamp"),
                ChatLookup::isCopyStripTimestamp, () -> ChatLookup.setCopyStripTimestamp(!ChatLookup.isCopyStripTimestamp()));
        y = addSwitchRow(Tab.COPYING, y, Component.literal("Copy without stack counter"),
                ChatLookup::isCopyStripCounter, () -> ChatLookup.setCopyStripCounter(!ChatLookup.isCopyStripCounter()));
        addColorRow(Tab.COPYING, y, Component.literal("Popup border color"),
                ChatLookup::getCopyBorderColor, ChatLookup.DEFAULT_COPY_BORDER_COLOR, ChatLookup::setCopyBorderColor);

        y = rowsTop;
        y = addSwitchRow(Tab.MENTIONS, y, Component.literal("Mention detector"),
                ChatLookup::isMentionEnabled, () -> ChatLookup.setMentionEnabled(!ChatLookup.isMentionEnabled()));
        y = addSwitchRow(Tab.MENTIONS, y, Component.literal("Mention sound"),
                ChatLookup::isMentionSoundEnabled, () -> ChatLookup.setMentionSoundEnabled(!ChatLookup.isMentionSoundEnabled()));
        y = addSwitchRow(Tab.MENTIONS, y, Component.literal("Recolor mentions"),
                ChatLookup::isMentionHighlightEnabled, () -> ChatLookup.setMentionHighlightEnabled(!ChatLookup.isMentionHighlightEnabled()));
        addColorRow(Tab.MENTIONS, y, Component.literal("Mention color"),
                ChatLookup::getMentionColor, ChatLookup.DEFAULT_MENTION_COLOR, ChatLookup::setMentionColor);

        y = rowsTop;
        y = addSwitchRow(Tab.WORDS, y, Component.literal("Highlight words in chat"),
                ChatLookup::isWordHighlightEnabled,
                () -> ChatLookup.setWordHighlightEnabled(!ChatLookup.isWordHighlightEnabled()));
        addButtonRow(Tab.WORDS, y, Component.literal("Word list"),
                Component.literal("Configure"), () -> {
                    if (this.minecraft != null) {
                        ChatLookup.setScreen(this.minecraft, new HighlightRulesScreen(this));
                    }
                });

        y = rowsTop;
        y = addSwitchRow(Tab.MACROS, y, Component.literal("Run command macros"),
                ChatLookup::isMacrosEnabled, () -> ChatLookup.setMacrosEnabled(!ChatLookup.isMacrosEnabled()));
        addButtonRow(Tab.MACROS, y, Component.literal("Macro list"),
                Component.literal("Configure"), () -> {
                    if (this.minecraft != null) {
                        ChatLookup.setScreen(this.minecraft, new CommandMacrosScreen(this));
                    }
                });

        FlatButton done = new FlatButton(this.panelX + (this.panelW - 60) / 2, rowsTop + MAX_ROWS * ROW_H + 4, 60, 14,
                Component.literal("Done"), null, true, this::onClose);
        this.allControls.add(done);
        this.addRenderableWidget(done);

        SearchFieldWidget hexField = new SearchFieldWidget(this.font, 0, 0,
                ColorPickerOverlay.HEX_W, 12, Component.literal("Hex color"));
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

    private int tabWidth(Tab tab) {
        return this.font.width(tab.label()) + 10;
    }

    private int countTabRows() {
        int rows = 1;
        int x = 8;
        int limit = this.panelW - 8;
        for (Tab tab : Tab.values()) {
            int width = tabWidth(tab);
            if (x > 8 && x + width > limit) {
                rows++;
                x = 8;
            }
            x += width + 2;
        }
        return rows;
    }

    private int longestRowLabel() {
        int widest = 0;
        for (String label : new String[]{
                "Smooth chat animations", "Player heads in chat", "Message security indicator",
                "Save chat history to disk", "Chat scrollbar", "Math preview in the input",
                "Show inverted search button", "Jump to surrounding button",
                "Flash the message after jumping", "Highlight color",
                "Stack repeated messages", "Stack only consecutive messages",
                "Stack counter color", "Show message timestamps",
                "12-hour clock (AM/PM)", "Timestamp color",
                "Copy messages (Ctrl+click)", "Multi-message copy (Ctrl+Shift+click)",
                "Show copy hint on Ctrl", "Copy without timestamp",
                "Copy without stack counter", "Popup border color",
                "Mention detector", "Mention sound",
                "Recolor mentions", "Mention color",
                "Highlight words in chat", "Word list",
                "Run command macros", "Macro list",
                "Max chat height", "Check for updates"}) {
            widest = Math.max(widest, this.font.width(Component.literal(label)));
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

    private int addButtonRow(Tab tab, int y, Component label, Component action, Runnable onPress) {
        this.tabLabels.get(tab).add(new RowLabel(label, y + 3));
        int width = Math.max(SwitchWidget.WIDTH, this.font.width(action) + 10);
        FlatButton button = new FlatButton(this.panelX + this.panelW - 8 - width, y, width, SwitchWidget.HEIGHT,
                action, null, false, onPress);
        this.tabRows.get(tab).add(button);
        this.allControls.add(button);
        this.rowBaseY.put(button, y);
        this.addRenderableWidget(button);
        return y + ROW_H;
    }

    private int addChatHeightRow(Tab tab, int y, Component label) {
        this.tabLabels.get(tab).add(new RowLabel(label, y + 3));
        int valueWidth = chatHeightValueWidth();
        SliderWidget slider = new SliderWidget(
                this.panelX + this.panelW - 8 - SliderWidget.width(valueWidth), y, valueWidth, label,
                Component.literal("Raises the cap of the vanilla Chat Settings sliders (Focused Height / Unfocused Height)"),
                ChatLookup.VANILLA_CHAT_HEIGHT, ChatLookup.MAX_CHAT_HEIGHT, ChatLookup.CHAT_HEIGHT_STEP,
                ChatLookup::getMaxChatHeight, ChatLookup::setMaxChatHeight, ChatLookupConfig::save,
                ChatLookupSettingsScreen::chatHeightLabel);
        this.tabRows.get(tab).add(slider);
        this.allControls.add(slider);
        this.rowBaseY.put(slider, y);
        this.addRenderableWidget(slider);
        return y + ROW_H;
    }

    private int chatHeightValueWidth() {
        return Math.max(this.font.width(chatHeightLabel(ChatLookup.VANILLA_CHAT_HEIGHT)),
                this.font.width(chatHeightLabel(ChatLookup.MAX_CHAT_HEIGHT)));
    }

    private int chatHeightWidth() {
        return SliderWidget.width(chatHeightValueWidth());
    }

    private static Component chatHeightLabel(int pixels) {
        return Component.literal(pixels + " px");
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
        int tabBarBottom = this.panelY + 18 + this.tabRowCount * TAB_H;
        context.fill(this.panelX + 8, tabBarBottom, this.panelX + this.panelW - 8, tabBarBottom + 1,
                WidgetSkin.BORDER_IDLE);

        long now = Util.getMillis();

        float glide = progress(now, this.underlineStart, UNDERLINE_MS);
        TabButton from = buttonFor(this.underlineFrom);
        TabButton to = buttonFor(this.currentTab);
        if (to != null) {
            TabButton origin = from != null ? from : to;
            int lineX = origin.getX() + Math.round((to.getX() - origin.getX()) * glide);
            int lineW = origin.getWidth() + Math.round((to.getWidth() - origin.getWidth()) * glide);
            int lineY = origin.getY() + Math.round((to.getY() - origin.getY()) * glide) + TAB_H;
            context.fill(lineX + 1, lineY - 1, lineX + lineW - 1, lineY + 1, WidgetSkin.ACCENT);
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
        if (key == InputConstants.KEY_ESCAPE && this.picker != null && this.picker.isOpen()) {
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
