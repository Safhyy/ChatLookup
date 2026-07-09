package com.safhy.chatlookup;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ChatLookupConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("ChatLookup");
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("chatlookup").resolve("settings.properties");

    public static void load() {
        if (!Files.isRegularFile(FILE)) {
            return;
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            properties.load(in);
        } catch (IOException e) {
            LOGGER.warn("[ChatLookup] Could not read settings", e);
            return;
        }
        ChatLookup.initFromConfig(
                Boolean.parseBoolean(properties.getProperty("regex_mode", "false")),
                Boolean.parseBoolean(properties.getProperty("highlight_matches", "true")));
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("regex_mode", Boolean.toString(ChatLookup.isRegexMode()));
        properties.setProperty("highlight_matches", Boolean.toString(ChatLookup.isHighlightEnabled()));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                properties.store(out, "ChatLookup settings");
            }
        } catch (IOException e) {
            LOGGER.warn("[ChatLookup] Could not save settings", e);
        }
    }

    private ChatLookupConfig() {
    }
}
