package com.abohasiba.playertracker;

import com.abohasiba.playertracker.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageUtilTest {

    @Test
    @DisplayName("Verify duration formatting into readable strings")
    void testFormatDuration() {
        assertEquals("30 minutes", MessageUtil.formatDuration(1800L));
        assertEquals("1 minute", MessageUtil.formatDuration(60L));
        assertEquals("2 minutes", MessageUtil.formatDuration(120L));
        assertEquals("1m 30s", MessageUtil.formatDuration(90L));
        assertEquals("45s", MessageUtil.formatDuration(45L));
        assertEquals("0s", MessageUtil.formatDuration(0L));
        assertEquals("0s", MessageUtil.formatDuration(-15L));
    }

    @Test
    @DisplayName("Verify color deserialization with Adventure Components")
    void testColor() {
        Component empty = MessageUtil.color(null);
        assertNotNull(empty);
        assertEquals(Component.empty(), empty);

        Component colored = MessageUtil.color("&aPlayer &bTracker");
        assertNotNull(colored);
    }

    @Test
    @DisplayName("Verify legacy color code translation")
    void testColorLegacy() {
        assertEquals("", MessageUtil.colorLegacy(null));
        assertEquals("§aPlayer §bTracker", MessageUtil.colorLegacy("&aPlayer &bTracker"));
    }
}
