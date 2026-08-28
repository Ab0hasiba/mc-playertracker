package com.abohasiba.playertracker.listener;

import com.abohasiba.playertracker.PlayerTracker;
import com.abohasiba.playertracker.gui.TrackerGUI;
import com.abohasiba.playertracker.tracker.TrackerItem;
import com.abohasiba.playertracker.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;

public class TrackerListener implements Listener {

    private final PlayerTracker plugin;

    public TrackerListener(PlayerTracker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (!TrackerItem.isTracker(item)) {
            return;
        }

        Player player = event.getPlayer();

        // Check permission
        if (!player.hasPermission("playertracker.use")) {
            event.setCancelled(true);
            String noPerm = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this.");
            player.sendMessage(MessageUtil.color(noPerm));
            return;
        }

        // Check expired
        if (TrackerItem.isExpired(item)) {
            event.setCancelled(true);
            if (event.getHand() == EquipmentSlot.HAND) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().setItemInOffHand(null);
            }
            String expiredMsg = plugin.getConfig().getString("messages.tracker-expired", "&cYour player tracker has expired!");
            player.sendMessage(MessageUtil.color(expiredMsg));
            return;
        }

        // Cancel vanilla behavior for tracker compasses
        event.setCancelled(true);

        // If tracker is unused, open target selection GUI
        if (!TrackerItem.hasTarget(item)) {
            EquipmentSlot hand = event.getHand() != null ? event.getHand() : EquipmentSlot.HAND;
            TrackerGUI.open(player, hand, 0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TrackerGUI.TrackerHolder holder)) {
            return;
        }

        // Always prevent taking or moving items in the tracker GUI
        event.setCancelled(true);

        HumanEntity whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player player)) {
            return;
        }

        // Only handle clicks in top inventory
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Navigation: Previous Page
        if (slot == 45 && clickedItem.getType() == Material.ARROW) {
            TrackerGUI.open(player, holder.getHand(), holder.getPage() - 1);
            return;
        }

        // Navigation: Next Page
        if (slot == 53 && clickedItem.getType() == Material.ARROW) {
            TrackerGUI.open(player, holder.getHand(), holder.getPage() + 1);
            return;
        }

        // Player selection (slots 0 to 44)
        if (slot < TrackerGUI.PAGE_SIZE && clickedItem.getType() == Material.PLAYER_HEAD) {
            if (!(clickedItem.getItemMeta() instanceof SkullMeta skullMeta)) {
                return;
            }

            OfflinePlayer owningPlayer = skullMeta.getOwningPlayer();
            Player target = owningPlayer != null ? owningPlayer.getPlayer() : null;

            if (target == null || !target.isOnline()) {
                String offlineMsg = plugin.getConfig().getString("messages.target-offline", "&cThe tracked player is offline.");
                player.sendMessage(MessageUtil.color(offlineMsg));
                player.closeInventory();
                return;
            }

            if (target.getUniqueId().equals(player.getUniqueId())) {
                String cantTrackSelf = plugin.getConfig().getString("messages.cannot-track-self", "&cYou cannot track yourself.");
                player.sendMessage(MessageUtil.color(cantTrackSelf));
                player.closeInventory();
                return;
            }

            // Retrieve held item
            ItemStack trackerItem;
            if (holder.getHand() == EquipmentSlot.OFF_HAND) {
                trackerItem = player.getInventory().getItemInOffHand();
            } else {
                trackerItem = player.getInventory().getItemInMainHand();
            }

            if (!TrackerItem.isTracker(trackerItem)) {
                player.closeInventory();
                return;
            }

            long lifetimeSeconds = plugin.getConfig().getLong("tracker.lifetime-seconds", 1800L);
            TrackerItem.setTarget(trackerItem, target, lifetimeSeconds);

            if (holder.getHand() == EquipmentSlot.OFF_HAND) {
                player.getInventory().setItemInOffHand(trackerItem);
            } else {
                player.getInventory().setItemInMainHand(trackerItem);
            }

            player.closeInventory();

            String trackingStarted = plugin.getConfig().getString("messages.tracking-started", "&aNow tracking &f%player%&a!")
                    .replace("%player%", target.getName());
            player.sendMessage(MessageUtil.color(trackingStarted));
        }
    }

    private boolean isTrackerRecipe(Recipe recipe) {
        if (recipe instanceof Keyed keyed) {
            return keyed.getKey().equals(plugin.getRecipeKey()) || keyed.getKey().equals(plugin.getRecipeAltKey());
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        if (plugin.getConfig().getBoolean("recipe.enabled", true)) {
            event.getPlayer().discoverRecipe(plugin.getRecipeKey());
            event.getPlayer().discoverRecipe(plugin.getRecipeAltKey());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (isTrackerRecipe(recipe)) {
            HumanEntity viewer = event.getViewers().isEmpty() ? null : event.getViewers().get(0);
            if (viewer instanceof Player player) {
                if (!player.hasPermission("playertracker.craft")) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
            }
            long lifetime = plugin.getConfig().getLong("tracker.lifetime-seconds", 1800L);
            event.getInventory().setResult(TrackerItem.createTracker(lifetime));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCraftItem(CraftItemEvent event) {
        Recipe recipe = event.getRecipe();
        if (isTrackerRecipe(recipe)) {
            HumanEntity whoClicked = event.getWhoClicked();
            if (whoClicked instanceof Player player) {
                if (!player.hasPermission("playertracker.craft")) {
                    event.setCancelled(true);
                    String noPerm = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this.");
                    player.sendMessage(MessageUtil.color(noPerm));
                    return;
                }

                String createdMsg = plugin.getConfig().getString("messages.tracker-created", "&aPlayer tracker created!");
                player.sendMessage(MessageUtil.color(createdMsg));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Item itemEntity = event.getItem();
        ItemStack itemStack = itemEntity.getItemStack();

        if (TrackerItem.isTracker(itemStack) && TrackerItem.isExpired(itemStack)) {
            event.setCancelled(true);
            itemEntity.remove();
            String expiredMsg = plugin.getConfig().getString("messages.tracker-expired", "&cYour player tracker has expired!");
            player.sendMessage(MessageUtil.color(expiredMsg));
        }
    }
}
