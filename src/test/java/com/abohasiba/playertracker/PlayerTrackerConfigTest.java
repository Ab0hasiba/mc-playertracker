package com.abohasiba.playertracker;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTrackerConfigTest {

    @Test
    @DisplayName("Verify default config.yml structure, timings, recipe, and messages")
    void testDefaultConfig() {
        InputStream stream = getClass().getResourceAsStream("/config.yml");
        assertNotNull(stream, "config.yml must exist on the classpath");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));

        // Timings
        assertEquals(1800L, config.getLong("tracker.lifetime-seconds"));
        assertEquals(5L, config.getLong("tracker.update-interval-ticks"));

        // Recipe configuration
        assertTrue(config.getBoolean("recipe.enabled"));
        List<String> shape = config.getStringList("recipe.shape");
        assertEquals(3, shape.size());
        assertEquals("DNW", shape.get(0));
        assertEquals("ECP", shape.get(1));
        assertEquals("BND", shape.get(2));

        // Verify ingredient materials exist in Bukkit Material registry
        assertEquals("DIAMOND_BLOCK", config.getString("recipe.ingredients.D"));
        assertNotNull(Material.matchMaterial(config.getString("recipe.ingredients.D")));

        assertEquals("NETHERITE_INGOT", config.getString("recipe.ingredients.N"));
        assertNotNull(Material.matchMaterial(config.getString("recipe.ingredients.N")));

        assertEquals("WIND_CHARGE", config.getString("recipe.ingredients.W"));
        assertNotNull(Material.matchMaterial(config.getString("recipe.ingredients.W")));

        assertEquals("ENDER_EYE", config.getString("recipe.ingredients.E"));
        assertNotNull(Material.matchMaterial(config.getString("recipe.ingredients.E")));

        assertEquals("COMPASS", config.getString("recipe.ingredients.C"));
        assertNotNull(Material.matchMaterial(config.getString("recipe.ingredients.C")));

        assertEquals("ENDER_PEARL", config.getString("recipe.ingredients.P"));
        assertNotNull(Material.matchMaterial(config.getString("recipe.ingredients.P")));

        assertEquals("BLAZE_ROD", config.getString("recipe.ingredients.B"));
        assertNotNull(Material.matchMaterial(config.getString("recipe.ingredients.B")));

        // Messages
        assertNotNull(config.getString("messages.tracker-created"));
        assertNotNull(config.getString("messages.tracking-started"));
        assertNotNull(config.getString("messages.tracker-expired"));
        assertNotNull(config.getString("messages.target-offline"));
        assertNotNull(config.getString("messages.different-dimension"));
        assertNotNull(config.getString("messages.no-permission"));
        assertNotNull(config.getString("messages.action-bar"));
        assertNotNull(config.getString("messages.reload-success"));
        assertNotNull(config.getString("messages.give-success"));
        assertNotNull(config.getString("messages.player-not-found"));
        assertNotNull(config.getString("messages.cannot-track-self"));
    }

    @Test
    @DisplayName("Verify plugin.yml descriptor, main class, commands, aliases, and permissions")
    void testPluginDescriptor() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/plugin.yml");
        assertNotNull(stream, "plugin.yml must exist on the classpath");

        PluginDescriptionFile pdf = new PluginDescriptionFile(stream);

        assertEquals("PlayerTracker", pdf.getName());
        assertEquals("com.abohasiba.playertracker.PlayerTracker", pdf.getMain());
        assertNotNull(pdf.getVersion());
        assertNotNull(pdf.getAPIVersion());

        // Commands
        assertNotNull(pdf.getCommands().get("playertracker"));
        assertEquals("playertracker.admin", pdf.getCommands().get("playertracker").get("permission"));
        @SuppressWarnings("unchecked")
        List<String> aliases = (List<String>) pdf.getCommands().get("playertracker").get("aliases");
        assertNotNull(aliases);
        assertTrue(aliases.contains("tracker"));
        assertTrue(aliases.contains("ptracker"));

        // Permissions
        assertTrue(pdf.getPermissions().stream().anyMatch(p -> p.getName().equals("playertracker.use")));
        assertTrue(pdf.getPermissions().stream().anyMatch(p -> p.getName().equals("playertracker.craft")));
        assertTrue(pdf.getPermissions().stream().anyMatch(p -> p.getName().equals("playertracker.admin")));
    }
}
