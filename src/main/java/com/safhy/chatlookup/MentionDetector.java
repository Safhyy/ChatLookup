package com.safhy.chatlookup;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;

import java.util.Locale;
import java.util.Optional;

public final class MentionDetector {

    public static Component process(Component content) {
        if (!ChatLookup.isMentionEnabled()) {
            return content;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getUser() == null) {
            return content;
        }
        String name = minecraft.getUser().getName();
        if (name == null || name.length() < 3) {
            return content;
        }

        String plain = content.getString();
        String lower = plain.toLowerCase(Locale.ROOT);
        String needle = name.toLowerCase(Locale.ROOT);

        if (needle.equals(firstToken(lower))) {
            return content;
        }

        IntArrayList ranges = new IntArrayList();
        int i = 0;
        while ((i = lower.indexOf(needle, i)) >= 0) {
            int end = i + needle.length();
            boolean startOk = i == 0 || !isNameChar(lower.charAt(i - 1));
            boolean endOk = end == lower.length() || !isNameChar(lower.charAt(end));
            if (startOk && endOk) {
                ranges.add(i);
                ranges.add(end);
            }
            i = end;
        }
        if (ranges.isEmpty()) {
            return content;
        }

        if (ChatLookup.isMentionSoundEnabled()) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f));
        }
        return recolor(content, ranges);
    }

    private static Component recolor(Component content, IntArrayList ranges) {
        TextColor color = TextColor.fromRgb(ChatLookup.getMentionColor());
        MutableComponent out = Component.empty();
        int[] offset = {0};
        content.visit((style, text) -> {
            int base = offset[0];
            offset[0] += text.length();
            int pos = 0;
            for (int r = 0; r < ranges.size(); r += 2) {
                int from = Math.max(ranges.getInt(r) - base, pos);
                int to = Math.min(ranges.getInt(r + 1) - base, text.length());
                if (to <= from || from >= text.length()) {
                    continue;
                }
                if (from > pos) {
                    out.append(Component.literal(text.substring(pos, from)).setStyle(style));
                }
                out.append(Component.literal(text.substring(from, to)).setStyle(style.withColor(color)));
                pos = to;
            }
            if (pos < text.length()) {
                out.append(Component.literal(text.substring(pos)).setStyle(style));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }

    private static String firstToken(String lower) {
        int start = 0;
        int length = lower.length();
        while (start < length && !isNameChar(lower.charAt(start))) {
            start++;
        }
        int end = start;
        while (end < length && isNameChar(lower.charAt(end))) {
            end++;
        }
        return end > start ? lower.substring(start, end) : "";
    }

    private static boolean isNameChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }

    private MentionDetector() {
    }
}
