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

public class CommandMacrosScreen extends Screen {
    private static final int ROW_H = 16;
    private static final int MAX_VISIBLE = 6;
    private static final int COMMAND_W = 150;
    private static final int BIND_W = 60;
    private static final int SMALL = 12;
    private static final int GAP = 4;
    private static final int TITLE_COLOR = 0xFFE8E8F0;
    private static final int HINT_COLOR = 0xFF8A8A94;
    private static final int LIMIT_COLOR = 0xFFFFAA55;

    private final Screen parent;
    private final List<AbstractWidget> controls = new ArrayList<>();

    private CommandMacro listening;
    private int scroll;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int addY;

    public CommandMacrosScreen(Screen parent) {
        super(Component.literal("Command macros"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.controls.clear();

        this.panelW = 8 + COMMAND_W + GAP + BIND_W + GAP + SMALL + 8;
        this.panelH = 20 + MAX_VISIBLE * ROW_H + 6 + SMALL + 6 + 14 + 8;
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        List<CommandMacro> macros = CommandMacros.macros();
        this.scroll = Math.max(0, Math.min(this.scroll, Math.max(0, macros.size() - MAX_VISIBLE)));

        int rowsTop = this.panelY + 20;
        int shown = Math.min(MAX_VISIBLE, macros.size() - this.scroll);
        for (int i = 0; i < shown; i++) {
            addRow(macros.get(this.scroll + i), rowsTop + i * ROW_H);
        }

        this.addY = rowsTop + MAX_VISIBLE * ROW_H + 6;
        boolean full = isFull();
        FlatButton addButton = new FlatButton(this.panelX + 8, this.addY, SMALL, SMALL, Icons.PLUS,
                Component.literal("Add a macro"),
                full ? limitMessage() : Component.literal("Add a macro"), true, this::addMacro);
        addButton.active = !full;
        this.controls.add(addButton);
        this.addRenderableWidget(addButton);

        FlatButton done = new FlatButton(this.panelX + (this.panelW - 60) / 2, this.addY + SMALL + 6, 60, 14,
                Component.literal("Done"), null, true, this::onClose);
        this.controls.add(done);
        this.addRenderableWidget(done);
    }

    private void addRow(CommandMacro macro, int y) {
        int x = this.panelX + 8;

        EditBox command = new SearchFieldWidget(this.font, x, y, COMMAND_W, SMALL,
                Component.literal("Command or message"));
        command.setMaxLength(CommandMacros.MAX_COMMAND_LENGTH);
        command.setHint(Component.literal("/command or a message")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        command.setValue(macro.command());
        command.setResponder(macro::setCommand);
        this.controls.add(command);
        this.addRenderableWidget(command);
        x += COMMAND_W + GAP;

        FlatButton bind = new FlatButton(x, y, BIND_W, SMALL, bindLabel(macro),
                Component.literal("Click, then press a key (Esc unbinds)"), macro.isBound(),
                () -> startListening(macro));
        this.controls.add(bind);
        this.addRenderableWidget(bind);
        x += BIND_W + GAP;

        FlatButton delete = new FlatButton(x, y, SMALL, SMALL, Icons.CLOSE,
                Component.literal("Remove"),
                Component.literal("Remove"), false,
                () -> removeMacro(macro));
        this.controls.add(delete);
        this.addRenderableWidget(delete);
    }

    private Component bindLabel(CommandMacro macro) {
        if (macro == this.listening) {
            return Component.literal("...");
        }
        return macro.keyLabel();
    }

    private void startListening(CommandMacro macro) {
        this.listening = macro;
        this.setFocused(null);
        this.rebuildWidgets();
    }

    private static boolean isFull() {
        return CommandMacros.macros().size() >= CommandMacros.MAX_MACROS;
    }

    private static Component limitMessage() {
        return Component.literal("Macro limit reached (" + CommandMacros.MAX_MACROS + ")");
    }

    private void addMacro() {
        List<CommandMacro> macros = CommandMacros.macros();
        if (isFull()) {
            return;
        }
        CommandMacros.add();
        this.scroll = Math.max(0, macros.size() - MAX_VISIBLE);
        this.rebuildWidgets();
    }

    private void removeMacro(CommandMacro macro) {
        CommandMacros.remove(macro);
        this.rebuildWidgets();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        int max = Math.max(0, CommandMacros.macros().size() - MAX_VISIBLE);
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
        if (this.listening != null) {
            this.listening.setKey(key == GLFW.GLFW_KEY_ESCAPE ? CommandMacro.UNBOUND : key);
            this.listening = null;
            CommandMacros.save();
            this.rebuildWidgets();
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

        List<CommandMacro> macros = CommandMacros.macros();
        int rowsTop = this.panelY + 20;
        if (macros.isEmpty()) {
            WidgetSkin.text(context, this.font, Component.literal("No macros yet - press + to add one."),
                    this.panelX + 8, rowsTop + 4, HINT_COLOR, false);
            return;
        }

        int max = Math.max(0, macros.size() - MAX_VISIBLE);
        if (max > 0) {
            int trackHeight = MAX_VISIBLE * ROW_H;
            int thumb = Math.max(8, trackHeight * MAX_VISIBLE / macros.size());
            int thumbTop = rowsTop + Math.round((trackHeight - thumb) * (this.scroll / (float) max));
            int barX = this.panelX + this.panelW - 5;
            context.fill(barX, rowsTop, barX + 2, rowsTop + trackHeight, 0x40FFFFFF);
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
        CommandMacros.save();
    }
}
