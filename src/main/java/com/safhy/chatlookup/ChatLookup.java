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

    public static boolean isQueryInvalid() {
        return queryInvalid;
    }

    public static ChatComponent getChat(Minecraft minecraft) {
        //? if >=26.2 {
        return minecraft.gui.hud.getChat();
        //?} else {
        /*return minecraft.gui.getChat();
        *///?}
    }

    static void initFromConfig(boolean regex, boolean highlight) {
        regexMode = regex;
        highlightEnabled = highlight;
        recompile();
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
