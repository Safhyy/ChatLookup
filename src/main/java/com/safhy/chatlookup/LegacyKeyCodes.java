package com.safhy.chatlookup;

import java.util.HashMap;
import java.util.Map;

public final class LegacyKeyCodes {
    private static final String PREFIX = "key.keyboard.";

    private static final Map<Integer, String> NAMES = Map.ofEntries(
            Map.entry(32, "space"),
            Map.entry(39, "apostrophe"),
            Map.entry(44, "comma"),
            Map.entry(45, "minus"),
            Map.entry(46, "period"),
            Map.entry(47, "slash"),
            Map.entry(48, "0"),
            Map.entry(49, "1"),
            Map.entry(50, "2"),
            Map.entry(51, "3"),
            Map.entry(52, "4"),
            Map.entry(53, "5"),
            Map.entry(54, "6"),
            Map.entry(55, "7"),
            Map.entry(56, "8"),
            Map.entry(57, "9"),
            Map.entry(59, "semicolon"),
            Map.entry(61, "equal"),
            Map.entry(65, "a"),
            Map.entry(66, "b"),
            Map.entry(67, "c"),
            Map.entry(68, "d"),
            Map.entry(69, "e"),
            Map.entry(70, "f"),
            Map.entry(71, "g"),
            Map.entry(72, "h"),
            Map.entry(73, "i"),
            Map.entry(74, "j"),
            Map.entry(75, "k"),
            Map.entry(76, "l"),
            Map.entry(77, "m"),
            Map.entry(78, "n"),
            Map.entry(79, "o"),
            Map.entry(80, "p"),
            Map.entry(81, "q"),
            Map.entry(82, "r"),
            Map.entry(83, "s"),
            Map.entry(84, "t"),
            Map.entry(85, "u"),
            Map.entry(86, "v"),
            Map.entry(87, "w"),
            Map.entry(88, "x"),
            Map.entry(89, "y"),
            Map.entry(90, "z"),
            Map.entry(91, "left.bracket"),
            Map.entry(92, "backslash"),
            Map.entry(93, "right.bracket"),
            Map.entry(96, "grave.accent"),
            Map.entry(161, "world.1"),
            Map.entry(162, "world.2"),
            Map.entry(256, "escape"),
            Map.entry(257, "enter"),
            Map.entry(258, "tab"),
            Map.entry(259, "backspace"),
            Map.entry(260, "insert"),
            Map.entry(261, "delete"),
            Map.entry(262, "right"),
            Map.entry(263, "left"),
            Map.entry(264, "down"),
            Map.entry(265, "up"),
            Map.entry(266, "page.up"),
            Map.entry(267, "page.down"),
            Map.entry(268, "home"),
            Map.entry(269, "end"),
            Map.entry(280, "caps.lock"),
            Map.entry(281, "scroll.lock"),
            Map.entry(282, "num.lock"),
            Map.entry(283, "print.screen"),
            Map.entry(284, "pause"),
            Map.entry(290, "f1"),
            Map.entry(291, "f2"),
            Map.entry(292, "f3"),
            Map.entry(293, "f4"),
            Map.entry(294, "f5"),
            Map.entry(295, "f6"),
            Map.entry(296, "f7"),
            Map.entry(297, "f8"),
            Map.entry(298, "f9"),
            Map.entry(299, "f10"),
            Map.entry(300, "f11"),
            Map.entry(301, "f12"),
            Map.entry(302, "f13"),
            Map.entry(303, "f14"),
            Map.entry(304, "f15"),
            Map.entry(305, "f16"),
            Map.entry(306, "f17"),
            Map.entry(307, "f18"),
            Map.entry(308, "f19"),
            Map.entry(309, "f20"),
            Map.entry(310, "f21"),
            Map.entry(311, "f22"),
            Map.entry(312, "f23"),
            Map.entry(313, "f24"),
            Map.entry(314, "f25"),
            Map.entry(320, "keypad.0"),
            Map.entry(321, "keypad.1"),
            Map.entry(322, "keypad.2"),
            Map.entry(323, "keypad.3"),
            Map.entry(324, "keypad.4"),
            Map.entry(325, "keypad.5"),
            Map.entry(326, "keypad.6"),
            Map.entry(327, "keypad.7"),
            Map.entry(328, "keypad.8"),
            Map.entry(329, "keypad.9"),
            Map.entry(330, "keypad.decimal"),
            Map.entry(331, "keypad.divide"),
            Map.entry(332, "keypad.multiply"),
            Map.entry(333, "keypad.subtract"),
            Map.entry(334, "keypad.add"),
            Map.entry(335, "keypad.enter"),
            Map.entry(336, "keypad.equal"),
            Map.entry(340, "left.shift"),
            Map.entry(341, "left.control"),
            Map.entry(342, "left.alt"),
            Map.entry(343, "left.win"),
            Map.entry(344, "right.shift"),
            Map.entry(345, "right.control"),
            Map.entry(346, "right.alt"),
            Map.entry(347, "right.win"),
            Map.entry(348, "menu")
    );

    private static final Map<String, Integer> CODES = new HashMap<>();

    static {
        NAMES.forEach((code, name) -> CODES.put(PREFIX + name, code));
    }

    public static String name(int code) {
        String name = NAMES.get(code);
        return name == null ? null : PREFIX + name;
    }

    public static int code(String name) {
        return CODES.getOrDefault(name, CommandMacro.UNBOUND);
    }

    private LegacyKeyCodes() {
    }
}
