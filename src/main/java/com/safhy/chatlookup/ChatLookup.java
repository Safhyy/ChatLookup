package com.safhy.chatlookup;

import com.safhy.chatlookup.mixin.ChatHudAccessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?} else {
/*import net.minecraft.client.GuiMessage;
*///?}

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class ChatLookup implements ClientModInitializer {
    public static final int HISTORY_LIMIT = 32_768;

    public static final int DEFAULT_TIMESTAMP_COLOR = 0xFFFFFF;
    public static final int DEFAULT_STACK_COLOR = 0xAAAAAA;
    public static final int DEFAULT_HIGHLIGHT_COLOR = 0xFFE14D;
    public static final int DEFAULT_MENTION_COLOR = 0xFFD24A;
    public static final int DEFAULT_COPY_BORDER_COLOR = 0xFFD24A;

    private static final int VISIBLE_REBUILD_BUDGET = 2048;

    private static final int MAX_MATCHES_PER_LINE = 128;
    private static final int[] NO_MATCHES = new int[0];

    private record PlainText(String raw, String lower) {
    }

    private static final Map<GuiMessage, PlainText> PLAIN_TEXT_CACHE = new IdentityHashMap<>();

    private static String query = "";
    private static String queryLowerCase = "";
    private static boolean regexMode;
    private static boolean highlightEnabled = true;
    private static boolean stackingEnabled = true;
    private static boolean stackConsecutiveOnly = true;
    private static boolean timestampsEnabled = true;
    private static boolean animationEnabled = true;
    private static boolean indicatorHidden = true;
    private static boolean headsEnabled = true;
    private static boolean twelveHourClock;
    private static boolean historySaveEnabled = true;
    private static boolean copyEnabled = true;
    private static boolean copyStripTimestamp = true;
    private static boolean copyStripCounter = true;
    private static boolean mentionEnabled = true;
    private static boolean mentionSoundEnabled = true;
    private static int timestampColor = DEFAULT_TIMESTAMP_COLOR;
    private static int stackColor = DEFAULT_STACK_COLOR;
    private static int highlightColor = DEFAULT_HIGHLIGHT_COLOR;
    private static int mentionColor = DEFAULT_MENTION_COLOR;
    private static int copyBorderColor = DEFAULT_COPY_BORDER_COLOR;
    private static Pattern pattern;
    private static boolean queryInvalid;

    private static boolean countsDirty = true;
    private static int matchedCount;
    private static int totalCount;
    private static int refreshSkip;

    @Override
    public void onInitializeClient() {
        ChatLookupConfig.load();
    }

    public static String getQuery() {
        return query;
    }

    public static boolean isFiltering() {
        return !query.isEmpty();
    }

    public static boolean isRegexMode() {
        return regexMode;
    }

    public static boolean isHighlightEnabled() {
        return highlightEnabled;
    }

    public static boolean isStackingEnabled() {
        return stackingEnabled;
    }

    public static boolean isStackConsecutiveOnly() {
        return stackConsecutiveOnly;
    }

    public static boolean isTimestampsEnabled() {
        return timestampsEnabled;
    }

    public static boolean isAnimationEnabled() {
        return animationEnabled;
    }

    public static boolean isIndicatorHidden() {
        return indicatorHidden;
    }

    public static boolean isHeadsEnabled() {
        return headsEnabled;
    }

    public static boolean isTwelveHourClock() {
        return twelveHourClock;
    }

    public static boolean isHistorySaveEnabled() {
        return historySaveEnabled;
    }

    public static boolean isCopyEnabled() {
        return copyEnabled;
    }

    public static boolean isCopyStripTimestamp() {
        return copyStripTimestamp;
    }

    public static boolean isCopyStripCounter() {
        return copyStripCounter;
    }

    public static boolean isMentionEnabled() {
        return mentionEnabled;
    }

    public static boolean isMentionSoundEnabled() {
        return mentionSoundEnabled;
    }

    public static boolean isQueryInvalid() {
        return queryInvalid;
    }

    public static int getTimestampColor() {
        return timestampColor;
    }

    public static int getStackColor() {
        return stackColor;
    }

    public static int getHighlightColor() {
        return highlightColor;
    }

    public static int getMentionColor() {
        return mentionColor;
    }

    public static int getCopyBorderColor() {
        return copyBorderColor;
    }

    public static ChatComponent getChat(Minecraft minecraft) {
        //? if >=26.2 {
        return minecraft.gui.hud.getChat();
        //?} else {
        /*return minecraft.gui.getChat();
        *///?}
    }

    public static void setScreen(Minecraft minecraft, net.minecraft.client.gui.screens.Screen screen) {
        //? if >=26.2 {
        minecraft.setScreenAndShow(screen);
        //?} else {
        /*minecraft.setScreen(screen);
        *///?}
    }

    static void initFromConfig(java.util.Properties p) {
        regexMode = boolOf(p, "regex_mode", false);
        highlightEnabled = boolOf(p, "highlight_matches", true);
        stackingEnabled = boolOf(p, "stack_messages", true);
        stackConsecutiveOnly = boolOf(p, "stack_consecutive_only", true);
        timestampsEnabled = boolOf(p, "show_timestamps", true);
        animationEnabled = boolOf(p, "smooth_animation", true);
        indicatorHidden = boolOf(p, "hide_indicator_line", true);
        headsEnabled = boolOf(p, "chat_heads", true);
        twelveHourClock = boolOf(p, "timestamp_12h", false);
        historySaveEnabled = boolOf(p, "save_history", true);
        copyEnabled = boolOf(p, "copy_messages", true);
        copyStripTimestamp = boolOf(p, "copy_strip_timestamp", true);
        copyStripCounter = boolOf(p, "copy_strip_counter", true);
        mentionEnabled = boolOf(p, "mention_detector", true);
        mentionSoundEnabled = boolOf(p, "mention_sound", true);
        timestampColor = colorOf(p, "timestamp_color", DEFAULT_TIMESTAMP_COLOR);
        stackColor = colorOf(p, "stack_color", DEFAULT_STACK_COLOR);
        highlightColor = colorOf(p, "highlight_color", DEFAULT_HIGHLIGHT_COLOR);
        mentionColor = colorOf(p, "mention_color", DEFAULT_MENTION_COLOR);
        copyBorderColor = colorOf(p, "copy_border_color", DEFAULT_COPY_BORDER_COLOR);
        recompile();
    }

    private static boolean boolOf(java.util.Properties p, String key, boolean fallback) {
        String value = p.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static int colorOf(java.util.Properties p, String key, int fallback) {
        String value = p.getProperty(key);
        if (value == null) {
            return fallback;
        }
        try {
            return (int) (Long.parseLong(value.trim().replace("#", ""), 16) & 0xFFFFFF);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static void setQuery(String newQuery) {
        String raw = newQuery == null ? "" : newQuery;
        if (raw.equals(query)) {
            return;
        }
        query = raw;
        queryLowerCase = raw.toLowerCase(Locale.ROOT);
        recompile();
        refreshChatHud();
    }

    public static void setRegexMode(boolean enabled) {
        if (regexMode == enabled) {
            return;
        }
        regexMode = enabled;
        ChatLookupConfig.save();
        recompile();
        refreshChatHud();
    }

    public static void setHighlightEnabled(boolean enabled) {
        if (highlightEnabled == enabled) {
            return;
        }
        highlightEnabled = enabled;
        ChatLookupConfig.save();
    }

    public static void setStackingEnabled(boolean enabled) {
        if (stackingEnabled == enabled) {
            return;
        }
        stackingEnabled = enabled;
        ChatLookupConfig.save();
    }

    public static void setStackConsecutiveOnly(boolean enabled) {
        if (stackConsecutiveOnly == enabled) {
            return;
        }
        stackConsecutiveOnly = enabled;
        ChatLookupConfig.save();
    }

    public static void setTimestampsEnabled(boolean enabled) {
        if (timestampsEnabled == enabled) {
            return;
        }
        timestampsEnabled = enabled;
        ChatLookupConfig.save();
    }

    public static void setAnimationEnabled(boolean enabled) {
        if (animationEnabled == enabled) {
            return;
        }
        animationEnabled = enabled;
        ChatLookupConfig.save();
    }

    public static void setHeadsEnabled(boolean enabled) {
        if (headsEnabled == enabled) {
            return;
        }
        headsEnabled = enabled;
        ChatLookupConfig.save();
    }

    public static void setIndicatorHidden(boolean hidden) {
        if (indicatorHidden == hidden) {
            return;
        }
        indicatorHidden = hidden;
        ChatLookupConfig.save();
    }

    public static void setTwelveHourClock(boolean enabled) {
        if (twelveHourClock == enabled) {
            return;
        }
        twelveHourClock = enabled;
        ChatLookupConfig.save();
    }

    public static void setHistorySaveEnabled(boolean enabled) {
        if (historySaveEnabled == enabled) {
            return;
        }
        historySaveEnabled = enabled;
        ChatLookupConfig.save();
    }

    public static void setCopyEnabled(boolean enabled) {
        if (copyEnabled == enabled) {
            return;
        }
        copyEnabled = enabled;
        ChatLookupConfig.save();
    }

    public static void setCopyStripTimestamp(boolean enabled) {
        if (copyStripTimestamp == enabled) {
            return;
        }
        copyStripTimestamp = enabled;
        ChatLookupConfig.save();
    }

    public static void setCopyStripCounter(boolean enabled) {
        if (copyStripCounter == enabled) {
            return;
        }
        copyStripCounter = enabled;
        ChatLookupConfig.save();
    }

    public static void setMentionEnabled(boolean enabled) {
        if (mentionEnabled == enabled) {
            return;
        }
        mentionEnabled = enabled;
        ChatLookupConfig.save();
    }

    public static void setMentionSoundEnabled(boolean enabled) {
        if (mentionSoundEnabled == enabled) {
            return;
        }
        mentionSoundEnabled = enabled;
        ChatLookupConfig.save();
    }

    public static void setTimestampColor(int rgb) {
        timestampColor = rgb & 0xFFFFFF;
    }

    public static void setStackColor(int rgb) {
        stackColor = rgb & 0xFFFFFF;
    }

    public static void setHighlightColor(int rgb) {
        highlightColor = rgb & 0xFFFFFF;
    }

    public static void setMentionColor(int rgb) {
        mentionColor = rgb & 0xFFFFFF;
    }

    public static void setCopyBorderColor(int rgb) {
        copyBorderColor = rgb & 0xFFFFFF;
    }

    private static void recompile() {
        pattern = null;
        queryInvalid = false;
        if (regexMode && !query.isEmpty()) {
            try {
                pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            } catch (PatternSyntaxException e) {
                queryInvalid = true;
            }
        }
    }

    public static boolean matches(GuiMessage line) {
        if (query.isEmpty()) {
            return true;
        }
        PlainText plain = plainText(line);
        if (regexMode) {
            return pattern == null || pattern.matcher(plain.raw()).find();
        }
        return plain.lower().contains(queryLowerCase);
    }

    public static int[] findMatches(String raw) {
        if (query.isEmpty() || queryInvalid) {
            return NO_MATCHES;
        }
        IntArrayList out = new IntArrayList();
        if (regexMode) {
            if (pattern == null) {
                return NO_MATCHES;
            }
            Matcher matcher = pattern.matcher(raw);
            int from = 0;
            while (from <= raw.length() && matcher.find(from) && out.size() < MAX_MATCHES_PER_LINE * 2) {
                if (matcher.end() > matcher.start()) {
                    out.add(matcher.start());
                    out.add(matcher.end());
                    from = matcher.end();
                } else {
                    from = matcher.start() + 1;
                }
            }
        } else {
            String lower = raw.toLowerCase(Locale.ROOT);
            int length = queryLowerCase.length();
            int i = 0;
            while ((i = lower.indexOf(queryLowerCase, i)) >= 0 && out.size() < MAX_MATCHES_PER_LINE * 2) {
                out.add(i);
                out.add(i + length);
                i += length;
            }
        }
        return out.isEmpty() ? NO_MATCHES : out.toIntArray();
    }

    public static void overridePlainText(GuiMessage line, String raw) {
        PLAIN_TEXT_CACHE.put(line, new PlainText(raw, raw.toLowerCase(Locale.ROOT)));
    }

    private static PlainText plainText(GuiMessage line) {
        PlainText cached = PLAIN_TEXT_CACHE.get(line);
        if (cached == null) {
            if (PLAIN_TEXT_CACHE.size() > HISTORY_LIMIT * 2) {
                PLAIN_TEXT_CACHE.clear();
            }
            String stripped = ChatFormatting.stripFormatting(line.content().getString());
            String raw = stripped == null ? "" : stripped;
            cached = new PlainText(raw, raw.toLowerCase(Locale.ROOT));
            PLAIN_TEXT_CACHE.put(line, cached);
        }
        return cached;
    }

    public static void budgetedRefresh(ChatComponent chatHud) {
        countsDirty = true;
        recountIfDirty(chatHud);
        refreshSkip = Math.max(0, matchedCount - VISIBLE_REBUILD_BUDGET);
        try {
            ((ChatHudAccessor) chatHud).chatlookup$refresh();
        } finally {
            refreshSkip = 0;
        }
    }

    public static boolean consumeRefreshSkip() {
        if (refreshSkip > 0) {
            refreshSkip--;
            return true;
        }
        return false;
    }

    private static void refreshChatHud() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.gui != null) {
            budgetedRefresh(getChat(minecraft));
        }
    }

    public static void markCountsDirty() {
        countsDirty = true;
    }

    public static int getMatchedCount(ChatComponent chatHud) {
        recountIfDirty(chatHud);
        return matchedCount;
    }

    public static int getTotalCount(ChatComponent chatHud) {
        recountIfDirty(chatHud);
        return totalCount;
    }

    private static void recountIfDirty(ChatComponent chatHud) {
        if (!countsDirty) {
            return;
        }
        countsDirty = false;
        List<GuiMessage> messages = ((ChatHudAccessor) chatHud).chatlookup$getMessages();
        totalCount = messages.size();
        if (!isFiltering()) {
            matchedCount = totalCount;
            return;
        }
        int matched = 0;
        for (GuiMessage line : messages) {
            if (matches(line)) {
                matched++;
            }
        }
        matchedCount = matched;
    }
}
