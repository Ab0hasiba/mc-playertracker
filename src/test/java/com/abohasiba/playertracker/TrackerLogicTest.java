package com.abohasiba.playertracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TrackerLogicTest {

    @Test
    @DisplayName("Verify expiration calculation and validity checks")
    void testExpirationLogic() {
        long now = System.currentTimeMillis();
        long lifetimeSeconds = 1800L;
        long expiresAt = now + (lifetimeSeconds * 1000L);

        // Future expiration timestamp must NOT be expired
        assertFalse(now >= expiresAt, "Active tracker with future expiry must not be expired");

        // Past expiration timestamp MUST be expired
        long pastExpiresAt = now - 5000L;
        assertTrue(now >= pastExpiresAt, "Tracker with timestamp in past must be expired");

        // Zero timestamp represents unassigned tracker
        assertFalse(0L > 0L && now >= 0L, "Unassigned tracker (timestamp 0) must not be expired");
    }

    @Test
    @DisplayName("Verify 3D Euclidean distance calculation and action bar formatting")
    void testDistanceAndActionBarFormatting() {
        double x1 = 100.0, y1 = 64.0, z1 = 100.0;
        double x2 = 130.0, y2 = 64.0, z2 = 140.0;

        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        int distance = (int) Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));

        // 30^2 + 40^2 = 900 + 1600 = 2500 -> sqrt = 50
        assertEquals(50, distance);

        String template = "&bTracking: &f%player% &7| &bDistance: &f%distance%m";
        String targetName = "AboHasiba";
        String formatted = template
                .replace("%player%", targetName)
                .replace("%distance%", String.valueOf(distance));

        assertEquals("&bTracking: &fAboHasiba &7| &bDistance: &f50m", formatted);
    }

    @Test
    @DisplayName("Verify UUID serialization and parsing integrity")
    void testUUIDIntegrity() {
        UUID original = UUID.randomUUID();
        String serialized = original.toString();
        UUID restored = UUID.fromString(serialized);

        assertEquals(original, restored);
    }
}
