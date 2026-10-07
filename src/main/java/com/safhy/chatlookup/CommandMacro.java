package com.safhy.chatlookup;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;

public final class CommandMacro {
    public static final int UNBOUND = -1;

    private String command = "";
    private int key = UNBOUND;

    public String command() {
        return this.command;
    }

    public void setCommand(String value) {
        this.command = value == null ? "" : value;
    }

    public int key() {
        return this.key;
    }

    public void setKey(int value) {
        this.key = value;
    }

    public boolean isBound() {
        return this.key != UNBOUND;
    }

    public boolean isUsable() {
        return isBound() && !this.command.isBlank();
    }

    public Component keyLabel() {
        if (!isBound()) {
            return Component.literal("None");
        }
        //? if >=26.3 {
        return InputConstants.Type.KEYBOARD.getOrCreate(this.key).getDisplayName();
        //?} else {
        /*return InputConstants.Type.KEYSYM.getOrCreate(this.key).getDisplayName();
        *///?}
    }
}
