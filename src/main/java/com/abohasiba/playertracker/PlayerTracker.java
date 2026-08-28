package com.abohasiba.playertracker;

import com.abohasiba.playertracker.command.PlayerTrackerCommand;
import com.abohasiba.playertracker.listener.TrackerListener;
import com.abohasiba.playertracker.tracker.TrackerItem;
import com.abohasiba.playertracker.tracker.TrackerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

public class PlayerTracker extends JavaPlugin {

    private static PlayerTracker instance;
    private TrackerManager trackerManager;
    private NamespacedKey recipeKey;
    private NamespacedKey recipeAltKey;

    public static PlayerTracker getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.recipeKey = new NamespacedKey(this, "player_tracker");
        this.recipeAltKey = new NamespacedKey(this, "player_tracker_alt");

        // Save default configuration
        saveDefaultConfig();

        // Initialize and start tracker manager
        this.trackerManager = new TrackerManager(this);
        this.trackerManager.start();

        // Register listeners
        getServer().getPluginManager().registerEvents(new TrackerListener(this), this);

        // Register commands and tab completion
        PlayerTrackerCommand cmd = new PlayerTrackerCommand(this);
        if (getCommand("playertracker") != null) {
            getCommand("playertracker").setExecutor(cmd);
            getCommand("playertracker").setTabCompleter(cmd);
        }

        // Register crafting recipe
        registerRecipe();

        getLogger().info("PlayerTracker v" + getDescription().getVersion() + " has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        // Cleanly stop tracker manager and cancel all tasks
        if (trackerManager != null) {
            trackerManager.stop();
            trackerManager = null;
        }

        Bukkit.getScheduler().cancelTasks(this);

        // Unregister custom recipes
        if (recipeKey != null) {
            Bukkit.removeRecipe(recipeKey);
        }
        if (recipeAltKey != null) {
            Bukkit.removeRecipe(recipeAltKey);
        }

        getLogger().info("PlayerTracker has been disabled.");
    }

    /**
     * Reloads configuration, recipe, and tracker interval.
     */
    public void reloadPluginConfig() {
        reloadConfig();
        registerRecipe();
        if (trackerManager != null) {
            trackerManager.start();
        }
    }

    /**
     * Registers the endgame Player Tracker shaped recipe according to configuration.
     */
    public void registerRecipe() {
        if (recipeKey != null) {
            Bukkit.removeRecipe(recipeKey);
        }
        if (recipeAltKey != null) {
            Bukkit.removeRecipe(recipeAltKey);
        }

        if (!getConfig().getBoolean("recipe.enabled", true)) {
            return;
        }

        long lifetime = getConfig().getLong("tracker.lifetime-seconds", 1800L);
        ItemStack result = TrackerItem.createTracker(lifetime);

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);

        List<String> shape = getConfig().getStringList("recipe.shape");
        if (shape.isEmpty()) {
            shape = List.of("DNW", "ECP", "BND");
        }

        recipe.shape(shape.toArray(new String[0]));

        ConfigurationSection ingredientsSec = getConfig().getConfigurationSection("recipe.ingredients");
        if (ingredientsSec != null) {
            for (String key : ingredientsSec.getKeys(false)) {
                if (key.isEmpty()) continue;
                char character = key.charAt(0);
                String matName = ingredientsSec.getString(key);
                if (matName != null) {
                    Material mat = Material.matchMaterial(matName);
                    if (mat != null) {
                        recipe.setIngredient(character, mat);
                    } else {
                        getLogger().log(Level.WARNING, "Invalid material in recipe configuration: " + matName);
                    }
                }
            }
        } else {
            // Default endgame recipe ingredients
            recipe.setIngredient('D', Material.DIAMOND_BLOCK);
            recipe.setIngredient('N', Material.NETHERITE_INGOT);
            recipe.setIngredient('W', Material.WIND_CHARGE);
            recipe.setIngredient('E', Material.ENDER_EYE);
            recipe.setIngredient('C', Material.COMPASS);
            recipe.setIngredient('P', Material.ENDER_PEARL);
            recipe.setIngredient('B', Material.BLAZE_ROD);
        }

        // Secondary recipe to support the diagram layout (BDN vs BND) seamlessly
        ShapedRecipe recipeAlt = new ShapedRecipe(recipeAltKey, result);
        recipeAlt.shape("DNW", "ECP", "BDN");
        recipeAlt.setIngredient('D', Material.DIAMOND_BLOCK);
        recipeAlt.setIngredient('N', Material.NETHERITE_INGOT);
        recipeAlt.setIngredient('W', Material.WIND_CHARGE);
        recipeAlt.setIngredient('E', Material.ENDER_EYE);
        recipeAlt.setIngredient('C', Material.COMPASS);
        recipeAlt.setIngredient('P', Material.ENDER_PEARL);
        recipeAlt.setIngredient('B', Material.BLAZE_ROD);

        try {
            Bukkit.addRecipe(recipe);
            Bukkit.addRecipe(recipeAlt);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.discoverRecipe(recipeKey);
                player.discoverRecipe(recipeAltKey);
            }
            getLogger().info("Registered Player Tracker crafting recipes.");
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to register recipe: " + ex.getMessage(), ex);
        }
    }

    public NamespacedKey getRecipeKey() {
        return recipeKey;
    }

    public NamespacedKey getRecipeAltKey() {
        return recipeAltKey;
    }

    public TrackerManager getTrackerManager() {
        return trackerManager;
    }
}
