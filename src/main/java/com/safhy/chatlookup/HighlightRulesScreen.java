package com.safhy.chatlookup;

//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
//?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HighlightRulesScreen extends Screen {
    private static final int ROW_H = 16;
    private static final int MAX_VISIBLE = 6;
    private static final int WORD_W = 96;
    private static final int SMALL = 12;
    private static final int GAP = 4;
    private static final int TITLE_COLOR = 0xFFE8E8F0;
    private static final int HINT_COLOR = 0xFF8A8A94;
    private static final int BROKEN_COLOR = 0xFFFF5555;
    private static final int LIMIT_COLOR = 0xFFFFAA55;

    private final Screen parent;
    private final List<AbstractWidget> controls = new ArrayList<>();

    private int scroll;
    private ColorPickerOverlay picker;
    private FlatButton addButton;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int addY;

    public HighlightRulesScreen(Screen parent) {
        super(Component.translatable("chatlookup.words"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.controls.clear();

        this.panelW = WORD_W + GAP + ColorSwatchButton.WIDTH + GAP + SMALL * 4 + 6 + GAP
                + SMALL + 2 + SMALL + GAP + SMALL + 16;
        this.panelH = 20 + MAX_VISIBLE * ROW_H + 6 + SMALL + 6 + 14 + 8;
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        List<HighlightRule> rules = HighlightRules.rules();
        this.scroll = Math.max(0, Math.min(this.scroll, Math.max(0, rules.size() - MAX_VISIBLE)));

        int rowsTop = this.panelY + 20;
        int shown = Math.min(MAX_VISIBLE, rules.size() - this.scroll);
        for (int i = 0; i < shown; i++) {
            addRow(rules.get(this.scroll + i), rowsTop + i * ROW_H);
        }

        this.addY = rowsTop + MAX_VISIBLE * ROW_H + 6;
        boolean full = isFull();
        FlatButton addButton = new FlatButton(this.panelX + 8, this.addY, SMALL, SMALL, Icons.PLUS,
                Component.translatable("chatlookup.words.add"),
                full ? limitMessage() : Component.translatable("chatlookup.words.add"), true, this::addRule);
        addButton.active = !full;
        this.addButton = addButton;
        this.addRenderableWidget(addButton);

        FlatButton done = new FlatButton(this.panelX + (this.panelW - 60) / 2, this.addY + SMALL + 6, 60, 14,
                Component.translatable("chatlookup.settings.done"), null, true, this::onClose);
        this.controls.add(done);
        this.addRenderableWidget(done);

        SearchFieldWidget hexField = new SearchFieldWidget(this.font, 0, 0,
                ColorPickerOverlay.HEX_W, 12, Component.translatable("chatlookup.settings.hex"));
        hexField.setHint(Component.literal("#RRGGBB").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        this.picker = new ColorPickerOverlay(this.width, this.height, hexField, () -> {
            setControlsActive(true);
            HighlightRules.save();
        });
        this.addRenderableWidget(this.picker);
        this.addRenderableWidget(hexField);
    }

    private void addRow(HighlightRule rule, int y) {
        int x = this.panelX + 8;

        EditBox word = new SearchFieldWidget(this.font, x, y, WORD_W, SMALL,
                Component.translatable("chatlookup.words.word"));
        word.setMaxLength(128);
        word.setHint(Component.translatable("chatlookup.words.word")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        word.setValue(rule.text());
        word.setResponder(rule::setText);
        this.controls.add(word);
        this.addRenderableWidget(word);
        x += WORD_W + GAP;

        ColorSwatchButton swatch = new ColorSwatchButton(x, y, Component.translatable("chatlookup.words.color"),
                rule::color, () -> openPicker(rule));
        this.controls.add(swatch);
        this.addRenderableWidget(swatch);
        x += ColorSwatchButton.WIDTH + GAP;

        x = addToggle(x, y, Component.literal("B"), "chatlookup.words.bold", rule::bold,
                () -> rule.setBold(!rule.bold()));
        x = addToggle(x, y, Component.literal("I"), "chatlookup.words.italic", rule::italic,
                () -> rule.setItalic(!rule.italic()));
        x = addToggle(x, y, Component.literal("U"), "chatlookup.words.underline", rule::underline,
                () -> rule.setUnderline(!rule.underline()));
        x = addToggle(x, y, Component.literal("S"), "chatlookup.words.strikethrough", rule::strikethrough,
                () -> rule.setStrikethrough(!rule.strikethrough()));
        x += 2;

        ToggleButton regex = new ToggleButton(x, y, Component.literal(".*"),
                Component.translatable("chatlookup.words.regex"), false,
                rule::regex, () -> rule.setRegex(!rule.regex()));
        this.controls.add(regex);
        this.addRenderableWidget(regex);
        x += SMALL + 2;

        ToggleButton sound = new ToggleButton(x, y, Icons.SOUND,
                Component.translatable("chatlookup.words.sound"),
                Component.translatable("chatlookup.words.sound"), false,
                rule::sound, () -> rule.setSound(!rule.sound()));
        this.controls.add(sound);
        this.addRenderableWidget(sound);
        x += SMALL + GAP;

        FlatButton delete = new FlatButton(x, y, SMALL, SMALL, Icons.CLOSE,
                Component.translatable("chatlookup.words.remove"),
                Component.translatable("chatlookup.words.remove"), false,
                () -> removeRule(rule));
        this.controls.add(delete);
        this.addRenderableWidget(delete);
    }

    private int addToggle(int x, int y, Component label, String tooltipKey,
                          java.util.function.BooleanSupplier state, Runnable onToggle) {
        ToggleButton button = new ToggleButton(x, y, label, Component.translatable(tooltipKey), false,
                state, onToggle);
        this.controls.add(button);
        this.addRenderableWidget(button);
        return x + SMALL + 2;
    }

    private static boolean isFull() {
        return HighlightRules.rules().size() >= HighlightRules.MAX_RULES;
    }

    private static Component limitMessage() {
        return Component.translatable("chatlookup.words.limit", HighlightRules.MAX_RULES);
    }

    private void addRule() {
        List<HighlightRule> rules = HighlightRules.rules();
        if (isFull()) {
            return;
        }
        HighlightRules.add();
        this.scroll = Math.max(0, rules.size() - MAX_VISIBLE);
        this.rebuildWidgets();
    }

    private void removeRule(HighlightRule rule) {
        HighlightRules.remove(rule);
        this.rebuildWidgets();
    }

    private void openPicker(HighlightRule rule) {
        setControlsActive(false);
        this.picker.open(Component.translatable("chatlookup.words.color"), rule.color(),
                HighlightRule.DEFAULT_COLOR, rule::setColor);
    }

    private void setControlsActive(boolean value) {
        for (AbstractWidget widget : this.controls) {
            widget.active = value;
        }
        if (this.addButton != null) {
            this.addButton.active = value && !isFull();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        if (this.picker != null && this.picker.isOpen()) {
            return super.mouseScrolled(mouseX, mouseY, amountX, amountY);
        }
        int max = Math.max(0, HighlightRules.rules().size() - MAX_VISIBLE);
        int updated = Math.max(0, Math.min(this.scroll - (int) Math.signum(amountY), max));
        if (updated != this.scroll) {
            this.scroll = updated;
            this.rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amountX, amountY);
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
    //? if >=26.1 {
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    //?} else {
    /*public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    *///?}
        context.fill(0, 0, this.width, this.height, 0x88000000);
        WidgetSkin.drawPanel(context, this.panelX, this.panelY,
                this.panelX + this.panelW, this.panelY + this.panelH, 0xF4111116, WidgetSkin.BORDER_HOVER);
        WidgetSkin.text(context, this.font, this.title, this.panelX + 8, this.panelY + 6, TITLE_COLOR, false);
        if (isFull()) {
            WidgetSkin.text(context, this.font, limitMessage(),
                    this.panelX + 8 + SMALL + GAP, this.addY + 2, LIMIT_COLOR, false);
        }

        List<HighlightRule> rules = HighlightRules.rules();
        int rowsTop = this.panelY + 20;
        if (rules.isEmpty()) {
            WidgetSkin.text(context, this.font, Component.translatable("chatlookup.words.empty"),
                    this.panelX + 8, rowsTop + 4, HINT_COLOR, false);
            return;
        }

        int shown = Math.min(MAX_VISIBLE, rules.size() - this.scroll);
        for (int i = 0; i < shown; i++) {
            HighlightRule rule = rules.get(this.scroll + i);
            if (rule.isBroken()) {
                int y = rowsTop + i * ROW_H;
                context.fill(this.panelX + 4, y, this.panelX + 6, y + SMALL, BROKEN_COLOR);
            }
        }

        int max = Math.max(0, rules.size() - MAX_VISIBLE);
        if (max > 0) {
            int trackTop = rowsTop;
            int trackHeight = MAX_VISIBLE * ROW_H;
            int thumb = Math.max(8, trackHeight * MAX_VISIBLE / rules.size());
            int thumbTop = trackTop + Math.round((trackHeight - thumb) * (this.scroll / (float) max));
            int barX = this.panelX + this.panelW - 5;
            context.fill(barX, trackTop, barX + 2, trackTop + trackHeight, 0x40FFFFFF);
            WidgetSkin.fillRounded(context, barX, thumbTop, barX + 2, thumbTop + thumb, WidgetSkin.ACCENT);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            ChatLookup.setScreen(this.minecraft, this.parent);
        }
    }

    @Override
    public void removed() {
        HighlightRules.save();
    }
}
