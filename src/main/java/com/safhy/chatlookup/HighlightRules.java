package com.safhy.chatlookup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class HighlightRules {
    public static final int MAX_RULES = 256;

    private static final Logger LOGGER = LoggerFactory.getLogger("ChatLookup");
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("chatlookup").resolve("highlights.json");

    private static final List<HighlightRule> RULES = new ArrayList<>();

    public static List<HighlightRule> rules() {
        return RULES;
    }

    public static boolean isEmpty() {
        return RULES.isEmpty();
    }

    public static HighlightRule add() {
        HighlightRule rule = new HighlightRule();
        RULES.add(rule);
        return rule;
    }

    public static void remove(HighlightRule rule) {
        RULES.remove(rule);
    }

    public static void load() {
        RULES.clear();
        if (!Files.isRegularFile(FILE)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonArray()) {
                return;
            }
            for (JsonElement element : root.getAsJsonArray()) {
                if (!element.isJsonObject() || RULES.size() >= MAX_RULES) {
                    continue;
                }
                JsonObject object = element.getAsJsonObject();
                HighlightRule rule = new HighlightRule();
                rule.setText(string(object, "text"));
                rule.setColor(color(object, "color"));
                rule.setBold(bool(object, "bold"));
                rule.setItalic(bool(object, "italic"));
                rule.setUnderline(bool(object, "underline"));
                rule.setStrikethrough(bool(object, "strikethrough"));
                rule.setRegex(bool(object, "regex"));
                rule.setSound(bool(object, "sound"));
                RULES.add(rule);
            }
        } catch (Exception e) {
            LOGGER.warn("[ChatLookup] Could not read highlight rules", e);
        }
    }

    public static void save() {
        JsonArray array = new JsonArray();
        for (HighlightRule rule : RULES) {
            if (rule.text().isBlank()) {
                continue;
            }
            JsonObject object = new JsonObject();
            object.addProperty("text", rule.text());
            object.addProperty("color", String.format("#%06X", rule.color()));
            object.addProperty("bold", rule.bold());
            object.addProperty("italic", rule.italic());
            object.addProperty("underline", rule.underline());
            object.addProperty("strikethrough", rule.strikethrough());
            object.addProperty("regex", rule.regex());
            object.addProperty("sound", rule.sound());
            array.add(object);
        }
        try {
            Files.createDirectories(FILE.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                writer.write(array.toString());
            }
        } catch (IOException e) {
            LOGGER.warn("[ChatLookup] Could not save highlight rules", e);
        }
    }

    private static String string(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
    }

    private static boolean bool(JsonObject object, String key) {
        JsonElement element = object.get(key);
        try {
            return element != null && element.isJsonPrimitive() && element.getAsBoolean();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static int color(JsonObject object, String key) {
        String raw = string(object, key).replace("#", "").trim();
        if (raw.isEmpty()) {
            return HighlightRule.DEFAULT_COLOR;
        }
        try {
            return (int) (Long.parseLong(raw, 16) & 0xFFFFFF);
        } catch (NumberFormatException e) {
            return HighlightRule.DEFAULT_COLOR;
        }
    }

    private HighlightRules() {
    }
}
