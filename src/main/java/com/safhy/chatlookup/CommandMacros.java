package com.safhy.chatlookup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CommandMacros {
    public static final int MAX_MACROS = 256;
    public static final int MAX_COMMAND_LENGTH = 256;

    private static final Logger LOGGER = LoggerFactory.getLogger("ChatLookup");
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("chatlookup").resolve("macros.json");

    private static final List<CommandMacro> MACROS = new ArrayList<>();
    private static final Set<Integer> HELD = new HashSet<>();

    public static List<CommandMacro> macros() {
        return MACROS;
    }

    public static CommandMacro add() {
        CommandMacro macro = new CommandMacro();
        MACROS.add(macro);
        return macro;
    }

    public static void remove(CommandMacro macro) {
        MACROS.remove(macro);
    }

    public static void tick(Minecraft minecraft) {
        if (MACROS.isEmpty() || minecraft == null) {
            HELD.clear();
            return;
        }
        boolean ready = ChatLookup.isMacrosEnabled() && minecraft.player != null
                && ChatLookup.getScreen(minecraft) == null;
        for (CommandMacro macro : MACROS) {
            int key = macro.key();
            if (key == CommandMacro.UNBOUND) {
                continue;
            }
            if (!isKeyDown(minecraft, key)) {
                HELD.remove(key);
                continue;
            }
            if (HELD.add(key) && ready && !macro.command().isBlank()) {
                run(minecraft, macro);
            }
        }
    }

    private static boolean isKeyDown(Minecraft minecraft, int key) {
        //? if >=26.3 {
        return InputConstants.isKeyDown(key);
        //?} else if >=1.21.9 {
        /*return InputConstants.isKeyDown(minecraft.getWindow(), key);
        *///?} else {
        /*return InputConstants.isKeyDown(minecraft.getWindow().getWindow(), key);
        *///?}
    }

    private static void run(Minecraft minecraft, CommandMacro macro) {
        String text = macro.command().trim();
        if (text.isEmpty() || minecraft.player == null) {
            return;
        }
        if (text.length() > MAX_COMMAND_LENGTH) {
            text = text.substring(0, MAX_COMMAND_LENGTH);
        }
        ChatLookup.getChat(minecraft).addRecentChat(text);
        if (text.startsWith("/")) {
            String command = text.substring(1);
            if (!command.isBlank()) {
                minecraft.player.connection.sendCommand(command);
            }
        } else {
            minecraft.player.connection.sendChat(text);
        }
    }

    public static void load() {
        MACROS.clear();
        HELD.clear();
        if (!Files.isRegularFile(FILE)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonArray()) {
                return;
            }
            for (JsonElement element : root.getAsJsonArray()) {
                if (!element.isJsonObject() || MACROS.size() >= MAX_MACROS) {
                    continue;
                }
                JsonObject object = element.getAsJsonObject();
                CommandMacro macro = new CommandMacro();
                macro.setCommand(string(object, "command"));
                //? if >=26.3 {
                int scancode = scancode(string(object, "name"));
                macro.setKey(scancode != CommandMacro.UNBOUND ? scancode : scancode(LegacyKeyCodes.name(key(object))));
                //?} else {
                /*macro.setKey(key(object));
                *///?}
                MACROS.add(macro);
            }
        } catch (Exception e) {
            LOGGER.warn("[ChatLookup] Could not read command macros", e);
        }
    }

    public static void save() {
        JsonArray array = new JsonArray();
        for (CommandMacro macro : MACROS) {
            if (macro.command().isBlank()) {
                continue;
            }
            JsonObject object = new JsonObject();
            object.addProperty("command", macro.command());
            //? if >=26.3 {
            if (macro.isBound()) {
                String name = InputConstants.Type.KEYBOARD.getOrCreate(macro.key()).getName();
                object.addProperty("key", LegacyKeyCodes.code(name));
                object.addProperty("name", name);
            } else {
                object.addProperty("key", CommandMacro.UNBOUND);
            }
            //?} else {
            /*object.addProperty("key", macro.key());
            *///?}
            array.add(object);
        }
        try {
            Files.createDirectories(FILE.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                writer.write(array.toString());
            }
        } catch (IOException e) {
            LOGGER.warn("[ChatLookup] Could not save command macros", e);
        }
    }

    private static String string(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
    }

    private static int key(JsonObject object) {
        JsonElement element = object.get("key");
        if (element == null || !element.isJsonPrimitive()) {
            return CommandMacro.UNBOUND;
        }
        try {
            return element.getAsInt();
        } catch (RuntimeException e) {
            return CommandMacro.UNBOUND;
        }
    }

    //? if >=26.3 {
    private static int scancode(String name) {
        if (name == null || name.isEmpty()) {
            return CommandMacro.UNBOUND;
        }
        try {
            InputConstants.Key key = InputConstants.getKey(name);
            if (key.getType() != InputConstants.Type.KEYBOARD || key.equals(InputConstants.UNKNOWN)) {
                return CommandMacro.UNBOUND;
            }
            return key.getValue();
        } catch (IllegalArgumentException e) {
            return CommandMacro.UNBOUND;
        }
    }
    //?}

    private CommandMacros() {
    }
}
