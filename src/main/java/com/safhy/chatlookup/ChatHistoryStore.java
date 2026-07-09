package com.safhy.chatlookup;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.safhy.chatlookup.mixin.ChatHudAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class ChatHistoryStore {
    private static final Logger LOGGER = LoggerFactory.getLogger("ChatLookup");
    /** Far in the past, so restored lines never flash on the unfocused chat HUD. */
    public static final int RESTORED_CREATION_TICK = -1_000_000;

    private static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("chatlookup");
    private static final Path HISTORY_FILE = DIRECTORY.resolve("history.ndjson");
    private static final Path TEMP_FILE = DIRECTORY.resolve("history.ndjson.tmp");

    private static BufferedWriter appendWriter;
    private static boolean disabled;

    public static void restoreInto(ChatHud chatHud) {
        try {
            Files.createDirectories(DIRECTORY);
            List<Text> history = loadHistory();
            compact(history);
            openAppendWriter();
            Runtime.getRuntime().addShutdownHook(new Thread(ChatHistoryStore::close, "ChatLookup-history-close"));
            if (history.isEmpty()) {
                return;
            }

            List<ChatHudLine> messages = ((ChatHudAccessor) chatHud).chatlookup$getMessages();
            for (int i = history.size() - 1; i >= 0; i--) {
                messages.add(new ChatHudLine(RESTORED_CREATION_TICK, history.get(i), null, null));
            }
            ChatLookup.budgetedRefresh(chatHud);
            LOGGER.info("[ChatLookup] Restored {} chat messages from the previous session", history.size());
        } catch (Throwable t) {
            disabled = true;
            LOGGER.warn("[ChatLookup] Could not restore chat history; persistence disabled for this session", t);
        }
    }

    public static synchronized void append(Text content) {
        if (disabled || appendWriter == null) {
            return;
        }
        try {
            appendWriter.write(serialize(content));
            appendWriter.newLine();
            appendWriter.flush();
        } catch (IOException e) {
            disabled = true;
            LOGGER.warn("[ChatLookup] Could not append to chat history; persistence disabled for this session", e);
            close();
        }
    }

    public static synchronized void clearFile() {
        if (disabled) {
            return;
        }
        try {
            close();
            appendWriter = Files.newBufferedWriter(HISTORY_FILE, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            disabled = true;
            LOGGER.warn("[ChatLookup] Could not clear the chat history file; persistence disabled for this session", e);
        }
    }

    private static List<Text> loadHistory() {
        if (!Files.isRegularFile(HISTORY_FILE)) {
            return List.of();
        }
        List<Text> history = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(HISTORY_FILE, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    JsonElement json = JsonParser.parseString(line);
                    TextCodecs.CODEC.parse(JsonOps.INSTANCE, json).result().ifPresent(history::add);
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            LOGGER.warn("[ChatLookup] Could not read chat history file", e);
            return List.of();
        }
        if (history.size() > ChatLookup.HISTORY_LIMIT) {
            history = new ArrayList<>(history.subList(history.size() - ChatLookup.HISTORY_LIMIT, history.size()));
        }
        return history;
    }

    private static void compact(List<Text> history) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(TEMP_FILE, StandardCharsets.UTF_8)) {
            for (Text text : history) {
                writer.write(serialize(text));
                writer.newLine();
            }
        }
        try {
            Files.move(TEMP_FILE, HISTORY_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(TEMP_FILE, HISTORY_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static synchronized void openAppendWriter() throws IOException {
        appendWriter = Files.newBufferedWriter(HISTORY_FILE, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static String serialize(Text text) {
        JsonElement json = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).result().orElse(null);
        if (json == null) {
            String plain = Formatting.strip(text.getString());
            json = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, Text.literal(plain == null ? "" : plain)).result().orElseThrow();
        }
        return json.toString();
    }

    private static synchronized void close() {
        if (appendWriter != null) {
            try {
                appendWriter.close();
            } catch (IOException ignored) {
            }
            appendWriter = null;
        }
    }

    private ChatHistoryStore() {
    }
}
