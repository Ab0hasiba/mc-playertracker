package com.abohasiba.playertracker.tracker;

import com.abohasiba.playertracker.PlayerTracker;
import com.abohasiba.playertracker.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class TrackerManager {

    private final PlayerTracker plugin;
    private BukkitTask updateTask;

    public TrackerManager(PlayerTracker plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the centralized repeating tracker task.
     */
    public void start() {
        stop();
        long interval = plugin.getConfig().getLong("tracker.update-interval-ticks", 5L);
        if (interval < 1L) {
            interval = 5L;
        }

        this.updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    /**
     * Stops the centralized tracker task.
     */
    public void stop() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    /**
     * Centralized tick logic executed every configured ticks.
     */
    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            processPlayer(player);
        }
    }

    /**
     * Processes tracker compasses for a specific player.
     */
    private void processPlayer(Player player) {
        // Check main hand and off hand
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean processed = processHeldItem(player, mainHand, true);
        if (!processed) {
            processHeldItem(player, offHand, false);
        }

        // Clean up any expired trackers elsewhere in inventory
        cleanupExpiredInventory(player);
    }

    /**
     * Processes a tracker item held by a player.
     *
     * @param player     The player holding the item
     * @param item       The held item
     * @param isMainHand Whether it's in the main hand
     * @return true if an active tracker was processed
     */
    private boolean processHeldItem(Player player, ItemStack item, boolean isMainHand) {
        if (!TrackerItem.isTracker(item)) {
            return false;
        }

        // Expiration check
        if (TrackerItem.isExpired(item)) {
            if (isMainHand) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().setItemInOffHand(null);
            }
            sendExpiredNotification(player);
            return true;
        }

        // Target tracking
        UUID targetUUID = TrackerItem.getTargetUUID(item);
        if (targetUUID == null) {
            return false; // Tracker is unused, no active target
        }

        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null || !target.isOnline()) {
            String offlineMsg = plugin.getConfig().getString("messages.target-offline", "&cThe tracked player is offline.");
            player.sendActionBar(MessageUtil.color(offlineMsg));
            return true;
        }

        // Same dimension check
        if (player.getWorld().equals(target.getWorld())) {
            // Update compass lodestone direction
            TrackerItem.updateCompassLocation(item, target);
            player.setCompassTarget(target.getLocation());

            int distance = (int) Math.round(player.getLocation().distance(target.getLocation()));
            String format = plugin.getConfig().getString("messages.action-bar", "&bTracking: &f%player% &7| &bDistance: &f%distance%m");
            String formatted = format
                    .replace("%player%", target.getName())
                    .replace("%distance%", String.valueOf(distance));

            player.sendActionBar(MessageUtil.color(formatted));
        } else {
            String diffDimMsg = plugin.getConfig().getString("messages.different-dimension", "&c%player% is in another dimension.")
                    .replace("%player%", target.getName());
            player.sendActionBar(MessageUtil.color(diffDimMsg));
        }

        return true;
    }

    /**
     * Scans player inventory for any expired trackers and removes them.
     */
    private void cleanupExpiredInventory(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        boolean removedAny = false;

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (TrackerItem.isTracker(item) && TrackerItem.isExpired(item)) {
                contents[i] = null;
                removedAny = true;
            }
        }

        if (removedAny) {
            player.getInventory().setContents(contents);
            sendExpiredNotification(player);
        }
    }

    private void sendExpiredNotification(Player player) {
        String msg = plugin.getConfig().getString("messages.tracker-expired", "&cYour player tracker has expired!");
        player.sendMessage(MessageUtil.color(msg));
    }
}
