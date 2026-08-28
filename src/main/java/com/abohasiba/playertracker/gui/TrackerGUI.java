package com.abohasiba.playertracker.gui;

import com.abohasiba.playertracker.PlayerTracker;
import com.abohasiba.playertracker.tracker.TrackerItem;
import com.abohasiba.playertracker.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrackerGUI {

    public static final int PAGE_SIZE = 45;
    public static final int GUI_SIZE = 54;

    public static class TrackerHolder implements InventoryHolder {
        private final EquipmentSlot hand;
        private final int page;
        private final int totalPages;
        private Inventory inventory;

        public TrackerHolder(EquipmentSlot hand, int page, int totalPages) {
            this.hand = hand;
            this.page = page;
            this.totalPages = totalPages;
        }

        public EquipmentSlot getHand() {
            return hand;
        }

        public int getPage() {
            return page;
        }

        public int getTotalPages() {
            return totalPages;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    /**
     * Opens the tracker GUI for the player.
     *
     * @param viewer The player opening the GUI
     * @param hand   The hand holding the tracker compass (HAND or OFF_HAND)
     * @param page   The page number (0-indexed)
     */
    public static void open(Player viewer, EquipmentSlot hand, int page) {
        List<Player> targetablePlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getUniqueId().equals(viewer.getUniqueId())) {
                targetablePlayers.add(p);
            }
        }

        int totalPlayers = targetablePlayers.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalPlayers / PAGE_SIZE));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        TrackerHolder holder = new TrackerHolder(hand, page, totalPages);
        Component title = MessageUtil.color("&8Player Tracker");
        Inventory inv = Bukkit.createInventory(holder, GUI_SIZE, title);
        holder.setInventory(inv);

        // Fill player heads
        int startIndex = page * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalPlayers);

        for (int i = startIndex; i < endIndex; i++) {
            Player target = targetablePlayers.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(target);
                skullMeta.displayName(MessageUtil.color("&b" + target.getName()));
                skullMeta.lore(Collections.singletonList(MessageUtil.color("&7Click to track this player.")));
                head.setItemMeta(skullMeta);
            }
            inv.setItem(i - startIndex, head);
        }

        // Bottom row navigation / decoration
        ItemStack filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int slot = 45; slot < 54; slot++) {
            inv.setItem(slot, filler);
        }

        // Previous button
        if (page > 0) {
            inv.setItem(45, createGuiItem(Material.ARROW, "&a\u2190 Previous Page", Collections.singletonList("&7Go to page " + page)));
        }

        // Page info
        inv.setItem(49, createGuiItem(Material.BOOK, "&ePage " + (page + 1) + " of " + totalPages,
                Collections.singletonList("&7Total players: &f" + totalPlayers)));

        // Next button
        if (page < totalPages - 1) {
            inv.setItem(53, createGuiItem(Material.ARROW, "&aNext Page \u2192", Collections.singletonList("&7Go to page " + (page + 2))));
        }

        viewer.openInventory(inv);
    }

    private static ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtil.color(name));
            if (lore != null) {
                List<Component> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(MessageUtil.color(line));
                }
                meta.lore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
