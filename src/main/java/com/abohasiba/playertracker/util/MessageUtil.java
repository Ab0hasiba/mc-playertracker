package com.abohasiba.playertracker.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public final class MessageUtil {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private MessageUtil() {
    }

    /**
     * Converts a message with '&' color codes to an Adventure Component.
     *
     * @param message Message with color codes
     * @return Adventure Component
     */
    public static Component color(String message) {
        if (message == null) {
            return Component.empty();
        }
        return SERIALIZER.deserialize(message);
    }

    /**
     * Translates '&' color codes in a legacy string.
     *
     * @param message String with '&' codes
     * @return Color-translated string
     */
    @SuppressWarnings("deprecation")
    public static String colorLegacy(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Formats seconds into a human-readable duration string (e.g., "30 minutes" or "14m 20s").
     *
     * @param totalSeconds Total seconds
     * @return Formatted string
     */
    public static String formatDuration(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0s";
        }
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes > 0 && seconds == 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }
}
