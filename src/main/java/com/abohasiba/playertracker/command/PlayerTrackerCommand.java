package com.abohasiba.playertracker.command;

import com.abohasiba.playertracker.PlayerTracker;
import com.abohasiba.playertracker.tracker.TrackerItem;
import com.abohasiba.playertracker.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerTrackerCommand implements CommandExecutor, TabCompleter {

    private final PlayerTracker plugin;

    public PlayerTrackerCommand(PlayerTracker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("playertracker.admin")) {
            String noPerm = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this.");
            sender.sendMessage(MessageUtil.color(noPerm));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(MessageUtil.color("&b&lPlayerTracker &7v" + plugin.getPluginMeta().getVersion()));
            sender.sendMessage(MessageUtil.color("&7/playertracker reload &8- &fReload configuration"));
            sender.sendMessage(MessageUtil.color("&7/playertracker give <player> &8- &fGive a Player Tracker to a player"));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            plugin.reloadPluginConfig();
            String reloadMsg = plugin.getConfig().getString("messages.reload-success", "&aPlayerTracker configuration reloaded successfully!");
            sender.sendMessage(MessageUtil.color(reloadMsg));
            return true;
        }

        if (sub.equals("give")) {
            if (args.length < 2) {
                sender.sendMessage(MessageUtil.color("&cUsage: /playertracker give <player>"));
                return true;
            }

            String targetPlayerName = args[1];
            Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
            if (targetPlayer == null || !targetPlayer.isOnline()) {
                String notFound = plugin.getConfig().getString("messages.player-not-found", "&cPlayer '%player%' not found or not online.")
                        .replace("%player%", targetPlayerName);
                sender.sendMessage(MessageUtil.color(notFound));
                return true;
            }

            long lifetime = plugin.getConfig().getLong("tracker.lifetime-seconds", 1800L);
            ItemStack tracker = TrackerItem.createTracker(lifetime);

            HashMap<Integer, ItemStack> leftover = targetPlayer.getInventory().addItem(tracker);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    targetPlayer.getWorld().dropItemNaturally(targetPlayer.getLocation(), drop);
                }
            }

            String giveMsg = plugin.getConfig().getString("messages.give-success", "&aGiven a Player Tracker to &f%player%&a!")
                    .replace("%player%", targetPlayer.getName());
            sender.sendMessage(MessageUtil.color(giveMsg));
            return true;
        }

        sender.sendMessage(MessageUtil.color("&cUnknown subcommand. Use &f/playertracker &cfor help."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("playertracker.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subs = Arrays.asList("reload", "give");
            return subs.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
