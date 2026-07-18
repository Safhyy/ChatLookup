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
        ChatLookup.initFromConfig(properties);
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("regex_mode", Boolean.toString(ChatLookup.isRegexMode()));
        properties.setProperty("highlight_matches", Boolean.toString(ChatLookup.isHighlightEnabled()));
        properties.setProperty("stack_messages", Boolean.toString(ChatLookup.isStackingEnabled()));
        properties.setProperty("stack_consecutive_only", Boolean.toString(ChatLookup.isStackConsecutiveOnly()));
        properties.setProperty("show_timestamps", Boolean.toString(ChatLookup.isTimestampsEnabled()));
        properties.setProperty("smooth_animation", Boolean.toString(ChatLookup.isAnimationEnabled()));
        properties.setProperty("hide_indicator_line", Boolean.toString(ChatLookup.isIndicatorHidden()));
        properties.setProperty("chat_heads", Boolean.toString(ChatLookup.isHeadsEnabled()));
        properties.setProperty("timestamp_12h", Boolean.toString(ChatLookup.isTwelveHourClock()));
        properties.setProperty("save_history", Boolean.toString(ChatLookup.isHistorySaveEnabled()));
        properties.setProperty("copy_messages", Boolean.toString(ChatLookup.isCopyEnabled()));
        properties.setProperty("copy_strip_timestamp", Boolean.toString(ChatLookup.isCopyStripTimestamp()));
        properties.setProperty("copy_strip_counter", Boolean.toString(ChatLookup.isCopyStripCounter()));
        properties.setProperty("mention_detector", Boolean.toString(ChatLookup.isMentionEnabled()));
        properties.setProperty("mention_sound", Boolean.toString(ChatLookup.isMentionSoundEnabled()));
        properties.setProperty("timestamp_color", String.format("#%06X", ChatLookup.getTimestampColor()));
        properties.setProperty("stack_color", String.format("#%06X", ChatLookup.getStackColor()));
        properties.setProperty("highlight_color", String.format("#%06X", ChatLookup.getHighlightColor()));
        properties.setProperty("mention_color", String.format("#%06X", ChatLookup.getMentionColor()));
        properties.setProperty("copy_border_color", String.format("#%06X", ChatLookup.getCopyBorderColor()));
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
