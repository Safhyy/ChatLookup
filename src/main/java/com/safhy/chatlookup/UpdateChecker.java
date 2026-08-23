package com.safhy.chatlookup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Pattern;

public final class UpdateChecker {
    public static final String PAGE_URL = "https://modrinth.com/mod/chatlookup";
    public static final String DOWNLOAD_URL = PAGE_URL + "#download";

    private static final Logger LOGGER = LoggerFactory.getLogger("ChatLookup");
    private static final String API_URL = "https://api.modrinth.com/v2/project/chatlookup/version";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int ANNOUNCE_DELAY_TICKS = 60;
    private static final long[] RETRY_DELAYS_MS = {5_000L, 20_000L, 60_000L};

    private static final int PREFIX_COLOR = 0xFFE14D;
    private static final int LINK_COLOR = 0x1BD96A;

    private enum Attempt {
        DONE,
        RETRY
    }

    private static volatile String latestVersion;
    private static String currentVersion = "";
    private static boolean started;
    private static boolean announced;
    private static int ticks;

    public static void start() {
        if (started || !ChatLookup.isUpdateCheckEnabled()) {
            return;
        }
        started = true;
        currentVersion = modVersion("chatlookup");
        Thread worker = new Thread(UpdateChecker::run, "ChatLookup Update Check");
        worker.setDaemon(true);
        worker.start();
    }

    public static void tick(Minecraft minecraft) {
        if (announced || !ChatLookup.isUpdateCheckEnabled()) {
            return;
        }
        String latest = latestVersion;
        if (latest == null || minecraft == null || minecraft.player == null) {
            return;
        }
        if (++ticks < ANNOUNCE_DELAY_TICKS) {
            return;
        }
        announced = true;
        announce(minecraft, latest);
    }

    private static void run() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
        for (int attempt = 0; attempt <= RETRY_DELAYS_MS.length; attempt++) {
            if (attempt > 0 && !sleep(RETRY_DELAYS_MS[attempt - 1])) {
                return;
            }
            if (query(client) == Attempt.DONE) {
                return;
            }
        }
        LOGGER.info("[ChatLookup] Update check gave up after {} attempts, Modrinth is unreachable",
                RETRY_DELAYS_MS.length + 1);
    }

    private static boolean sleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static Attempt query(HttpClient client) {
        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(requestUrl()))
                    .header("User-Agent", "Safhyy/ChatLookup/" + currentVersion + " (" + PAGE_URL + ")")
                    .header("Accept", "application/json")
                    .timeout(TIMEOUT)
                    .GET()
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Attempt.DONE;
        } catch (IOException e) {
            LOGGER.debug("[ChatLookup] Update check could not reach Modrinth: {}", e.toString());
            return Attempt.RETRY;
        }

        int status = response.statusCode();
        if (status != 200) {
            LOGGER.debug("[ChatLookup] Update check returned HTTP {}", status);
            return status == 429 || status >= 500 ? Attempt.RETRY : Attempt.DONE;
        }
        try {
            String newest = newestRelease(response.body());
            if (newest != null && isNewer(newest, currentVersion)) {
                latestVersion = newest;
                LOGGER.info("[ChatLookup] Update available: {} (running {})", newest, currentVersion);
            }
        } catch (RuntimeException e) {
            LOGGER.debug("[ChatLookup] Update check could not read the Modrinth response: {}", e.toString());
        }
        return Attempt.DONE;
    }

    private static String requestUrl() {
        StringBuilder url = new StringBuilder(API_URL);
        url.append("?loaders=").append(encode("[\"fabric\"]"));
        String game = modVersion("minecraft");
        if (!game.isEmpty()) {
            url.append("&game_versions=").append(encode("[\"" + game + "\"]"));
        }
        return url.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String newestRelease(String body) {
        JsonElement root = JsonParser.parseString(body);
        if (!root.isJsonArray()) {
            return null;
        }
        JsonArray versions = root.getAsJsonArray();
        String best = null;
        for (JsonElement element : versions) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject version = element.getAsJsonObject();
            if (!version.has("version_number")) {
                continue;
            }
            if (version.has("version_type") && !"release".equals(version.get("version_type").getAsString())) {
                continue;
            }
            String number = version.get("version_number").getAsString();
            if (best == null || isNewer(number, best)) {
                best = number;
            }
        }
        return best;
    }

    private static boolean isNewer(String candidate, String reference) {
        int[] left = numbers(candidate);
        int[] right = numbers(reference);
        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int a = i < left.length ? left[i] : 0;
            int b = i < right.length ? right[i] : 0;
            if (a != b) {
                return a > b;
            }
        }
        return false;
    }

    private static int[] numbers(String version) {
        String core = core(version);
        if (core.isEmpty()) {
            return new int[0];
        }
        String[] parts = core.split(Pattern.quote("."));
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                out[i] = 0;
            }
        }
        return out;
    }

    private static String core(String version) {
        String value = version.trim();
        if (value.startsWith("v") || value.startsWith("V")) {
            value = value.substring(1);
        }
        int end = 0;
        while (end < value.length() && (Character.isDigit(value.charAt(end)) || value.charAt(end) == '.')) {
            end++;
        }
        return value.substring(0, end);
    }

    private static String modVersion(String id) {
        return FabricLoader.getInstance().getModContainer(id)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("");
    }

    private static void announce(Minecraft minecraft, String latest) {
        Component message = Component.empty()
                .append(Component.literal("[ChatLookup] ").withStyle(style -> style.withColor(PREFIX_COLOR)))
                .append(Component.literal("A new version is available: " + core(latest) + ".")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("\n"))
                .append(Component.literal("You are currently on " + core(currentVersion) + ".")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" "))
                .append(downloadLink());
        ChatLookup.getChatListener(minecraft).handleSystemMessage(message, false);
    }

    private static Component downloadLink() {
        ClickEvent click = Links.openUrl();
        if (click == null) {
            return Component.literal(DOWNLOAD_URL).withStyle(style -> style
                    .withColor(LINK_COLOR)
                    .withUnderlined(true));
        }
        return Component.literal("Download it from Modrinth").withStyle(style -> style
                .withColor(LINK_COLOR)
                .withUnderlined(true)
                .withClickEvent(click));
    }

    private static final class Links {
        //? if >=1.21.5 {
        private static final MethodHandle LEGACY_CONSTRUCTOR = legacyConstructor();

        static ClickEvent openUrl() {
            if (LEGACY_CONSTRUCTOR != null) {
                try {
                    return (ClickEvent) LEGACY_CONSTRUCTOR.invoke(ClickEvent.Action.OPEN_URL, DOWNLOAD_URL);
                } catch (Throwable t) {
                    LOGGER.warn("[ChatLookup] Could not build a legacy click event, "
                            + "falling back to a plain URL: {}", t.toString());
                    return null;
                }
            }
            try {
                return Modern.openUrl();
            } catch (LinkageError e) {
                LOGGER.warn("[ChatLookup] Clickable links are unavailable on this Minecraft build, "
                        + "falling back to a plain URL: {}", e.toString());
                return null;
            }
        }

        private static MethodHandle legacyConstructor() {
            try {
                return MethodHandles.lookup().findConstructor(ClickEvent.class,
                        MethodType.methodType(void.class, ClickEvent.Action.class, String.class));
            } catch (ReflectiveOperationException | LinkageError e) {
                return null;
            }
        }

        private static final class Modern {
            static ClickEvent openUrl() {
                return new ClickEvent.OpenUrl(URI.create(DOWNLOAD_URL));
            }

            private Modern() {
            }
        }
        //?} else {
        /*static ClickEvent openUrl() {
            return new ClickEvent(ClickEvent.Action.OPEN_URL, DOWNLOAD_URL);
        }
        *///?}

        private Links() {
        }
    }

    private UpdateChecker() {
    }
}
