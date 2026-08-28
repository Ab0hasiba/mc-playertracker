package com.abohasiba.playertracker.tracker;

import com.abohasiba.playertracker.PlayerTracker;
import com.abohasiba.playertracker.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TrackerItem {

    public static final NamespacedKey KEY_IS_TRACKER = new NamespacedKey(PlayerTracker.getInstance(), "tracker");
    public static final NamespacedKey KEY_TRACKER_ID = new NamespacedKey(PlayerTracker.getInstance(), "tracker_id");
    public static final NamespacedKey KEY_TARGET_UUID = new NamespacedKey(PlayerTracker.getInstance(), "target_uuid");
    public static final NamespacedKey KEY_TARGET_NAME = new NamespacedKey(PlayerTracker.getInstance(), "target_name");
    public static final NamespacedKey KEY_EXPIRES_AT = new NamespacedKey(PlayerTracker.getInstance(), "expires_at");

    private TrackerItem() {
    }

    /**
     * Creates a new, fresh, unused Player Tracker compass item.
     *
     * @param lifetimeSeconds Configured lifetime in seconds (e.g. 1800)
     * @return Fresh Player Tracker ItemStack
     */
    public static ItemStack createTracker(long lifetimeSeconds) {
        ItemStack item = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) item.getItemMeta();

        if (meta != null) {
            meta.displayName(MessageUtil.color("&b&lPlayer Tracker"));

            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Right-click to track a player."));
            lore.add(MessageUtil.color("&7Duration: &f" + MessageUtil.formatDuration(lifetimeSeconds)));
            meta.lore(lore);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(KEY_IS_TRACKER, PersistentDataType.BYTE, (byte) 1);
            pdc.set(KEY_TRACKER_ID, PersistentDataType.STRING, UUID.randomUUID().toString());

            meta.setLodestoneTracked(false);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Checks whether an ItemStack is a valid Player Tracker compass.
     *
     * @param item The ItemStack to inspect
     * @return true if valid Player Tracker
     */
    public static boolean isTracker(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(KEY_IS_TRACKER, PersistentDataType.BYTE);
    }

    /**
     * Retrieves the unique Tracker UUID from the item's PDC.
     */
    public static UUID getTrackerId(ItemStack item) {
        if (!isTracker(item)) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String idStr = pdc.get(KEY_TRACKER_ID, PersistentDataType.STRING);
        if (idStr != null) {
            try {
                return UUID.fromString(idStr);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    /**
     * Retrieves the target player UUID from the item's PDC.
     */
    public static UUID getTargetUUID(ItemStack item) {
        if (!isTracker(item)) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String uuidStr = pdc.get(KEY_TARGET_UUID, PersistentDataType.STRING);
        if (uuidStr != null && !uuidStr.isEmpty()) {
            try {
                return UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    /**
     * Retrieves the target player's name from the item's PDC.
     */
    public static String getTargetName(ItemStack item) {
        if (!isTracker(item)) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(KEY_TARGET_NAME, PersistentDataType.STRING);
    }

    /**
     * Retrieves the expiration timestamp (in epoch milliseconds) from the item's PDC.
     */
    public static long getExpiresAt(ItemStack item) {
        if (!isTracker(item)) return 0L;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        Long expiresAt = pdc.get(KEY_EXPIRES_AT, PersistentDataType.LONG);
        return expiresAt != null ? expiresAt : 0L;
    }

    /**
     * Checks if the tracker has an active target assigned.
     */
    public static boolean hasTarget(ItemStack item) {
        return getTargetUUID(item) != null;
    }

    /**
     * Checks if the tracker has expired.
     */
    public static boolean isExpired(ItemStack item) {
        long expiresAt = getExpiresAt(item);
        return expiresAt > 0L && System.currentTimeMillis() >= expiresAt;
    }

    /**
     * Sets the target player on this tracker and starts the timer.
     *
     * @param item            The tracker item stack
     * @param target          The target player
     * @param lifetimeSeconds Configured duration in seconds
     */
    public static void setTarget(ItemStack item, Player target, long lifetimeSeconds) {
        if (!isTracker(item)) return;

        CompassMeta meta = (CompassMeta) item.getItemMeta();
        if (meta == null) return;

        long expiresAt = System.currentTimeMillis() + (lifetimeSeconds * 1000L);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_TARGET_UUID, PersistentDataType.STRING, target.getUniqueId().toString());
        pdc.set(KEY_TARGET_NAME, PersistentDataType.STRING, target.getName());
        pdc.set(KEY_EXPIRES_AT, PersistentDataType.LONG, expiresAt);

        // Update display lore
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.color("&7Tracking: &b" + target.getName()));
        lore.add(MessageUtil.color("&7Duration: &f" + MessageUtil.formatDuration(lifetimeSeconds)));
        meta.lore(lore);

        // Point compass to target
        meta.setLodestoneTracked(false);
        meta.setLodestone(target.getLocation());

        item.setItemMeta(meta);
    }

    /**
     * Updates compass destination pointing to target.
     *
     * @param item The tracker item
     * @param target The target player
     */
    public static void updateCompassLocation(ItemStack item, Player target) {
        if (!isTracker(item) || target == null || !target.isOnline()) return;

        CompassMeta meta = (CompassMeta) item.getItemMeta();
        if (meta == null) return;

        meta.setLodestoneTracked(false);
        meta.setLodestone(target.getLocation());
        item.setItemMeta(meta);
    }
}
