package com.safhy.chatlookup;

import com.safhy.chatlookup.mixin.ChatHudAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ChatComponent;
//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?} else {
/*import net.minecraft.client.GuiMessage;
*///?}
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntFunction;

public final class MessageDecorator {
    private static final DateTimeFormatter TIME_FORMAT_24H = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_FORMAT_12H = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final String HEAD_SPACER = "   ";
    private static final String HEAD_MARKER = "chatlookup:head";
    private static final String TIME_MARKER = "chatlookup:time";
    private static final String STACK_MARKER = "chatlookup:stack";

    private static GuiMessage lastMessage;
    private static Component baseContent;
    private static String baseKey = "";
    private static String basePlain = "";
    private static int count;

    private record StackEntry(GuiMessage message, int count) {
    }

    private static final Map<String, StackEntry> RECENT_BY_KEY = new HashMap<>();

    private static boolean refreshing;
    private static boolean applying;
    private static boolean applyingStack;
    private static boolean skipQueueAdd;
    private static GuiMessage queueReplacement;

    public static void setRefreshing(boolean value) {
        refreshing = value;
    }

    public static boolean isRefreshing() {
        return refreshing;
    }

    public static boolean isApplyingStack() {
        return applyingStack;
    }

    public static boolean consumeSkipQueueAdd() {
        if (skipQueueAdd) {
            skipQueueAdd = false;
            return true;
        }
        return false;
    }

    public static GuiMessage consumeQueueReplacement() {
        GuiMessage replacement = queueReplacement;
        queueReplacement = null;
        return replacement;
    }

    public static boolean onDisplayAdd(ChatComponent chatHud, GuiMessage incoming) {
        if (refreshing || applying) {
            return false;
        }
        Component recolored = MentionDetector.process(incoming.content());
        boolean mentioned = recolored != incoming.content();
        if (mentioned) {
            incoming = withContent(incoming, recolored);
        }

        List<GuiMessage> messages = ((ChatHudAccessor) chatHud).chatlookup$getMessages();
        String key = incoming.content().getString();
        boolean stacks = ChatLookup.isStackingEnabled()
                && !messages.isEmpty() && messages.get(0) == lastMessage && incoming != lastMessage
                && key.equals(baseKey) && !basePlain.isBlank();
        String time = ChatLookup.isTimestampsEnabled()
                ? LocalTime.now().format(ChatLookup.isTwelveHourClock() ? TIME_FORMAT_12H : TIME_FORMAT_24H)
                : null;

        boolean spacer = ChatLookup.isHeadsEnabled();

        if (!stacks) {
            if (ChatLookup.isStackingEnabled() && !ChatLookup.isStackConsecutiveOnly()
                    && restack(chatHud, messages, incoming, key, time, spacer)) {
                return true;
            }
            track(incoming, key);
            PlayerInfo owner = ChatHeads.detect(incoming.content());
            if (time == null && !spacer && !mentioned) {
                rememberRecent(key, incoming, 1);
                return false;
            }
            GuiMessage decorated = withContent(incoming, compose(time, spacer, incoming.content(), 1));
            lastMessage = decorated;
            ChatHeads.remember(decorated, owner);
            rememberRecent(key, decorated, 1);
            queueReplacement = decorated;
            applyToDisplay(chatHud, decorated);
            return true;
        }

        count++;
        PlayerInfo owner = ChatHeads.owner(lastMessage);
        Component stackedContent = compose(time, spacer, baseContent, count);
        GuiMessage stacked = withContent(incoming, stackedContent);

        int removed = removeDisplayedLines(chatHud, lastMessage);
        if (removed > 0 && ((ChatHudAccessor) chatHud).chatlookup$getScrolledLines() > 0) {
            chatHud.scrollChat(-removed);
        }

        messages.set(0, stacked);
        lastMessage = stacked;
        skipQueueAdd = true;
        ChatHeads.remember(stacked, owner);
        rememberRecent(key, stacked, count);
        ChatLookup.overridePlainText(stacked, plainOf(compose(time, spacer, baseContent, 1)));
        ChatHistoryStore.replaceLast(stackedContent);
        ChatLookup.markCountsDirty();
        applyingStack = true;
        try {
            applyToDisplay(chatHud, stacked);
        } finally {
            applyingStack = false;
        }
        return true;
    }

    private static boolean restack(ChatComponent chatHud, List<GuiMessage> messages, GuiMessage incoming,
                                   String key, String time, boolean spacer) {
        StackEntry prev = RECENT_BY_KEY.get(key);
        if (prev == null || prev.message() == incoming) {
            return false;
        }
        String plain = ChatFormatting.stripFormatting(key);
        if (plain == null || plain.isBlank()) {
            return false;
        }
        int index = indexOfIdentity(messages, prev.message());
        if (index < 0) {
            RECENT_BY_KEY.remove(key);
            return false;
        }

        int removed = removeDisplayedLinesAnywhere(chatHud, prev.message());
        if (removed > 0 && ((ChatHudAccessor) chatHud).chatlookup$getScrolledLines() > 0) {
            chatHud.scrollChat(-removed);
        }
        messages.remove(index);

        int newCount = prev.count() + 1;
        track(incoming, key);
        count = newCount;
        PlayerInfo owner = ChatHeads.detect(incoming.content());
        GuiMessage stacked = withContent(incoming, compose(time, spacer, incoming.content(), newCount));
        lastMessage = stacked;
        ChatHeads.remember(stacked, owner);
        ChatLookup.overridePlainText(stacked, plainOf(compose(time, spacer, incoming.content(), 1)));
        rememberRecent(key, stacked, newCount);
        ChatLookup.markCountsDirty();
        queueReplacement = stacked;
        applyToDisplay(chatHud, stacked);
        return true;
    }

    private static void rememberRecent(String key, GuiMessage message, int stackCount) {
        if (RECENT_BY_KEY.size() > ChatLookup.HISTORY_LIMIT) {
            RECENT_BY_KEY.clear();
        }
        RECENT_BY_KEY.put(key, new StackEntry(message, stackCount));
    }

    private static int indexOfIdentity(List<GuiMessage> messages, GuiMessage target) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    private static Component compose(String time, boolean head, Component base, int stackCount) {
        if (time == null && !head && stackCount <= 1) {
            return base;
        }
        MutableComponent out = Component.empty();
        if (head) {
            out.append(Component.literal(HEAD_SPACER).withStyle(style -> style.withInsertion(HEAD_MARKER)));
        }
        if (time != null) {
            out.append(Component.literal("[" + time + "] ")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(ChatLookup.getTimestampColor()))
                            .withInsertion(TIME_MARKER)));
        }
        out.append(base);
        if (stackCount > 1) {
            out.append(Component.literal(" [x" + stackCount + "]")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(ChatLookup.getStackColor()))
                            .withInsertion(STACK_MARKER)));
        }
        return out;
    }

    public static Component stripHeadSpacer(Component content) {
        if (!hasHeadSpacer(content)) {
            return content;
        }
        List<Component> siblings = content.getSiblings();
        MutableComponent out = Component.empty();
        for (int i = 1; i < siblings.size(); i++) {
            out.append(siblings.get(i));
        }
        return out;
    }

    public static boolean hasHeadSpacer(Component content) {
        List<Component> siblings = content.getSiblings();
        return !siblings.isEmpty() && HEAD_MARKER.equals(siblings.get(0).getStyle().getInsertion());
    }

    public static Component stripForCopy(Component content) {
        List<Component> siblings = content.getSiblings();
        boolean marked = false;
        for (Component sibling : siblings) {
            String insertion = sibling.getStyle().getInsertion();
            if (HEAD_MARKER.equals(insertion) || TIME_MARKER.equals(insertion) || STACK_MARKER.equals(insertion)) {
                marked = true;
                break;
            }
        }
        if (!marked) {
            return content;
        }
        MutableComponent out = Component.empty();
        for (Component sibling : siblings) {
            String insertion = sibling.getStyle().getInsertion();
            if (HEAD_MARKER.equals(insertion)
                    || (ChatLookup.isCopyStripTimestamp() && TIME_MARKER.equals(insertion))
                    || (ChatLookup.isCopyStripCounter() && STACK_MARKER.equals(insertion))) {
                continue;
            }
            out.append(sibling);
        }
        return out;
    }

    public static List<FormattedCharSequence> indentContinuationLines(Component content, Font font,
                                                                     int width, IntFunction<List<FormattedCharSequence>> wrapper) {
        if (!hasHeadSpacer(content)) {
            return wrapper.apply(width);
        }
        int indent = font.width(HEAD_SPACER);
        List<FormattedCharSequence> lines = wrapper.apply(Math.max(width - indent, width / 2));
        if (lines.size() <= 1) {
            return lines;
        }
        FormattedCharSequence pad = FormattedCharSequence.forward(HEAD_SPACER, Style.EMPTY);
        List<FormattedCharSequence> out = new ArrayList<>(lines);
        for (int i = 1; i < out.size(); i++) {
            out.set(i, FormattedCharSequence.composite(pad, out.get(i)));
        }
        return out;
    }

    private static void applyToDisplay(ChatComponent chatHud, GuiMessage message) {
        applying = true;
        try {
            ((ChatHudAccessor) chatHud).chatlookup$addToDisplay(message);
        } finally {
            applying = false;
        }
    }

    private static void track(GuiMessage message, String key) {
        lastMessage = message;
        baseContent = message.content();
        baseKey = key;
        String stripped = ChatFormatting.stripFormatting(key);
        basePlain = stripped == null ? "" : stripped;
        count = 1;
    }

    private static GuiMessage withContent(GuiMessage message, Component content) {
        //? if >=26.1 {
        return new GuiMessage(message.addedTime(), content, message.signature(), message.source(), message.tag());
        //?} else {
        /*return new GuiMessage(message.addedTime(), content, message.signature(), message.tag());
        *///?}
    }

    private static int removeDisplayedLines(ChatComponent chatHud, GuiMessage message) {
        List<GuiMessage.Line> lines = ((ChatHudAccessor) chatHud).chatlookup$getVisibleMessages();
        //? if >=26.1 {
        int before = lines.size();
        lines.removeIf(line -> line.parent() == message);
        return before - lines.size();
        //?} else {
        /*if (!ChatLookup.matches(message) || lines.isEmpty() || lines.get(0).addedTime() != message.addedTime()) {
            return 0;
        }
        int removed = 1;
        lines.remove(0);
        while (!lines.isEmpty() && !lines.get(0).endOfEntry()) {
            lines.remove(0);
            removed++;
        }
        return removed;
        *///?}
    }

    private static int removeDisplayedLinesAnywhere(ChatComponent chatHud, GuiMessage message) {
        List<GuiMessage.Line> lines = ((ChatHudAccessor) chatHud).chatlookup$getVisibleMessages();
        //? if >=26.1 {
        int before = lines.size();
        lines.removeIf(line -> line.parent() == message);
        return before - lines.size();
        //?} else {
        /*if (!ChatLookup.matches(message)) {
            return 0;
        }
        int ordinal = -1;
        boolean found = false;
        for (GuiMessage candidate : ((ChatHudAccessor) chatHud).chatlookup$getMessages()) {
            if (ChatLookup.matches(candidate)) {
                ordinal++;
            }
            if (candidate == message) {
                found = true;
                break;
            }
        }
        if (!found) {
            return 0;
        }
        int start = -1;
        int seen = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).endOfEntry()) {
                seen++;
                if (seen == ordinal) {
                    start = i;
                    break;
                }
            }
        }
        if (start < 0) {
            return 0;
        }
        int end = start + 1;
        while (end < lines.size() && !lines.get(end).endOfEntry()) {
            end++;
        }
        lines.subList(start, end).clear();
        return end - start;
        *///?}
    }

    private static String plainOf(Component content) {
        String stripped = ChatFormatting.stripFormatting(content.getString());
        return stripped == null ? "" : stripped;
    }

    private MessageDecorator() {
    }
}
