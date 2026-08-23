package com.safhy.chatlookup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WordHighlighter {
    private static final int MAX_SPANS = 128;

    private record Span(int start, int end, HighlightRule rule) {
    }

    public static Component process(Component content) {
        List<HighlightRule> rules = HighlightRules.rules();
        if (rules.isEmpty() || !ChatLookup.isWordHighlightEnabled()) {
            return content;
        }
        String plain = content.getString();
        if (plain.isEmpty()) {
            return content;
        }
        String lower = plain.toLowerCase(Locale.ROOT);

        List<Span> spans = new ArrayList<>();
        boolean playSound = false;
        for (HighlightRule rule : rules) {
            if (!rule.isUsable()) {
                continue;
            }
            int before = spans.size();
            collect(rule, plain, lower, spans);
            if (spans.size() > before && rule.sound()) {
                playSound = true;
            }
        }
        if (spans.isEmpty()) {
            return content;
        }

        spans.sort((a, b) -> a.start() != b.start() ? Integer.compare(a.start(), b.start())
                : Integer.compare(b.end(), a.end()));
        List<Span> merged = new ArrayList<>(spans.size());
        int cursor = 0;
        for (Span span : spans) {
            if (span.start() >= cursor) {
                merged.add(span);
                cursor = span.end();
            }
        }

        if (playSound) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) {
                minecraft.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f));
            }
        }
        return apply(content, merged);
    }

    private static void collect(HighlightRule rule, String plain, String lower, List<Span> spans) {
        if (rule.regex()) {
            Pattern pattern = rule.pattern();
            if (pattern == null) {
                return;
            }
            Matcher matcher = pattern.matcher(plain);
            int from = 0;
            while (from <= plain.length() && spans.size() < MAX_SPANS && matcher.find(from)) {
                if (matcher.end() > matcher.start()) {
                    spans.add(new Span(matcher.start(), matcher.end(), rule));
                    from = matcher.end();
                } else {
                    from = matcher.start() + 1;
                }
            }
            return;
        }
        String needle = rule.lowerText();
        int index = 0;
        while (spans.size() < MAX_SPANS && (index = lower.indexOf(needle, index)) >= 0) {
            spans.add(new Span(index, index + needle.length(), rule));
            index += needle.length();
        }
    }

    private static Component apply(Component content, List<Span> spans) {
        MutableComponent out = Component.empty();
        int[] offset = {0};
        content.visit((style, text) -> {
            int base = offset[0];
            offset[0] += text.length();
            int pos = 0;
            for (Span span : spans) {
                int from = Math.max(span.start() - base, pos);
                int to = Math.min(span.end() - base, text.length());
                if (to <= from || from >= text.length()) {
                    continue;
                }
                if (from > pos) {
                    out.append(Component.literal(text.substring(pos, from)).setStyle(style));
                }
                out.append(Component.literal(text.substring(from, to)).setStyle(span.rule().decorate(style)));
                pos = to;
            }
            if (pos < text.length()) {
                out.append(Component.literal(text.substring(pos)).setStyle(style));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }

    private WordHighlighter() {
    }
}
