package com.captainbboy.mobswords.commands;

import com.captainbboy.mobswords.MSUtil;
import com.captainbboy.mobswords.MobSwords;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MainCommand implements CommandExecutor {

    private final MobSwords plugin;

    public MainCommand(MobSwords plg) {
        this.plugin = plg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equalsIgnoreCase("mobsword")) {
            return true;
        }

        String currencyName = this.plugin.getConfig().getString("name-of-mobsword-currency");

        if (args.length == 0) {

            for(String msg : this.plugin.getConfig().getStringList("help-command")) {
                msg = msg.replaceAll("\\{version}", this.plugin.currVersion);
                sender.sendMessage(MSUtil.clr(msg));
            }

            return true;

        } else if (args[0].equalsIgnoreCase("reload")) {

            if(sender.hasPermission("mobswords.reload")) {
                this.plugin.reloadConfig();
                sender.sendMessage(MSUtil.clr(
                        "&7&l[&9&lMob&2Swords&7&l] &aReload successful."
                ));
            }

        } else if (args[0].equalsIgnoreCase("test")) {
            if (sender.hasPermission("mobswords.test")) {
                List<String> list = this.plugin.getConfig().getStringList("key-finder");
                ;
                Integer amount = 500;
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("spawner")) {
                        list = this.plugin.getConfig().getStringList("spawner-finder");
                    } else {
                        list = this.plugin.getConfig().getStringList("key-finder");
                    }
                }
                if (args.length > 2) {
                    if (MSUtil.isNumeric(args[2])) {
                        amount = Math.toIntExact(Math.round(MSUtil.getNumber(args[2])));
                    }
                }
                Map<String, Integer> times = new HashMap<>();
                for (int i = 0; i < amount; i++) {
                    String value = MSUtil.chooseRandomCommand(list);
                    if (times.containsKey(value)) {
                        times.replace(value, times.get(value) + 1);
                    } else {
                        times.put(value, 1);
                    }
                }
                times = MSUtil.sortByValueInt(times, true);
                sender.sendMessage(MSUtil.clr("&a&l(!) &aDid " + amount + " runs and got: "));
                for (Map.Entry<String, Integer> entry : times.entrySet()) {
                    sender.sendMessage(MSUtil.clr(
                            "&a&l(!) " + entry.getValue() + "/" + amount + " (" + Math.round(entry.getValue() / (amount / 100)) + "%) " + entry.getKey()
                    ));
                }
                return true;
            } else {
                sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("no-permission-message")));
            }
        } else if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 2 || args[1] == null || args[1].equals("")) {
                for(String msg : this.plugin.getConfig().getStringList("help-command")) {
                    msg = msg.replaceAll("\\{version}", this.plugin.currVersion);
                    if(!msg.contains("/mobswords") || (msg.contains("/mobswords") && msg.contains("give"))) {
                        sender.sendMessage(MSUtil.clr(msg));
                    }
                }
                return true;
            }

            //'&e&l(!) &7/mobswords give &e<player> [keyfinder] [spawnerfinder] [autosell] [sellmult]'
            if (sender.hasPermission("mobswords.give")) {
                Player target = this.plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("no-player-message")));
                    return true;
                }
                Material mat = Material.DIAMOND_SWORD;
                try {
                    mat = Material.valueOf(this.plugin.getConfig().getString("mob-sword-item-type"));
                } catch (Exception e) {}
                ItemStack hoe = new ItemStack(mat, 1);

                ItemMeta meta = hoe.getItemMeta();
                meta.setDisplayName(MSUtil.clr(this.plugin.getConfig().getString("mob-sword-item-name")));
                List<String> lores = new ArrayList();

                Integer keyFinderValue = 0;
                if(args.length > 2 && args[2] != null && MSUtil.isNumeric(args[2])) {
                    keyFinderValue = (int)MSUtil.getNumber(args[2]);
                }

                Integer spawnerFinderValue = 0;
                if(args.length > 3 && args[3] != null && MSUtil.isNumeric(args[3])) {
                    spawnerFinderValue = (int)MSUtil.getNumber(args[3]);
                }

                Double doubleChanceValue = 0.0;
                if(args.length > 4 && args[4] != null && MSUtil.isNumeric(args[4])) {
                    doubleChanceValue = MSUtil.getNumber(args[4]);
                }

                boolean autosellValue = false;
                if(args.length > 5 && args[5] != null && MSUtil.isBoolean(args[5])) {
                    autosellValue = MSUtil.getBoolean(args[5]);
                }

                Double sellMultiplierValue = 1.0;
                if(args.length > 6 && args[6] != null && MSUtil.isNumeric(args[6])) {
                    sellMultiplierValue = MSUtil.getNumber(args[6]);
                }

                Boolean autoSellEnabled = false;

                for(String s : this.plugin.getConfig().getStringList("mob-sword-item-lore")) {
                    s = s.replaceAll("\\{keyFinderValue}", String.valueOf(keyFinderValue));
                    s = s.replaceAll("\\{spawnerFinderValue}", String.valueOf(spawnerFinderValue));
                    s = s.replaceAll("\\{autosellValue}", String.valueOf(autosellValue));
                    s = s.replaceAll("\\{doubleChanceValue}", String.valueOf(doubleChanceValue));
                    if(s.contains("{autosellEnabled}")) {
                        if(autoSellEnabled)
                            s = s.replaceAll("\\{autosellEnabled}", "enabled");
                        else
                            s = s.replaceAll("\\{autosellEnabled}", "disabled");
                    }
                    s = s.replaceAll("\\{sellMultiplierValue}", String.valueOf(sellMultiplierValue));
                    lores.add(MSUtil.clr(s));
                }

                meta.setLore(lores);

                for (String s : this.plugin.getConfig().getStringList("mob-sword-item-enchants")) {
                    String[] a = s.split(":");
                    Enchantment enchant = Enchantment.getByName(a[0]);
                    if(enchant != null && MSUtil.isNumeric(a[1])) {
                        int lvl = (int) MSUtil.getNumber(a[1]);
                        meta.addEnchant(enchant, lvl, true);
                    }
                }

                meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
                meta.spigot().setUnbreakable(true);
                hoe.setItemMeta(meta);

                NBTItem nbti = new NBTItem(hoe);
                nbti.setBoolean("isMobSword", true);
                nbti.setInteger("mobSwordKeyFinder", keyFinderValue);
                nbti.setInteger("mobSwordSpawnerFinder", spawnerFinderValue);
                nbti.setBoolean("mobSwordAutoSell", autosellValue);
                nbti.setBoolean("mobSwordAutoSellEnabled", false);
                nbti.setDouble("mobSwordSellMult", sellMultiplierValue);
                nbti.setDouble("mobSwordDoubleChance", doubleChanceValue);

                target.getInventory().addItem(new ItemStack[]{nbti.getItem()});

                sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("item-sent-message")));
                target.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("item-received-message")));
            } else {
                sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("no-permission-message")));
            }
        } else if (args.length >= 1 && (args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("bal"))) {
            if(args.length == 1) {
                if (sender instanceof Player) {
                    String msg = this.plugin.getConfig().getString("balance-message");
                    msg = msg.replaceAll("\\{amount}", MSUtil.formatNumber(this.plugin.getSQLite().getBalance(((Player) sender).getUniqueId())));
                    msg = msg.replaceAll("\\{customCurrency}", currencyName);
                    sender.sendMessage(MSUtil.clr(msg));
                } else {
                    sender.sendMessage(MSUtil.clr(
                            "&c&l(!) &cOnly players can run this command!"
                    ));
                }
            } else {
                if (sender.hasPermission("mobswords.viewothersbalance")) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if(target == null) {
                        sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("no-player-message")));
                        return true;
                    }
                    UUID uuid = target.getUniqueId();
                    String msg = this.plugin.getConfig().getString("balance-message");
                    msg = msg.replaceAll("\\{amount}", MSUtil.formatNumber(this.plugin.getSQLite().getBalance(uuid)));
                    msg = msg.replaceAll("\\{customCurrency}", currencyName);
                    msg = msg.replaceAll("You", target.getName());
                    msg = msg.replaceAll("you", target.getName());
                    msg = msg.replaceAll("have", "has");
                    msg = msg.replaceAll("Have", "Has");
                    sender.sendMessage(MSUtil.clr(msg));
                } else {
                    sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("no-permission-message")));
                }
            }
        } else if (args.length >= 1 && (args[0].equalsIgnoreCase("setbalance") || args[0].equalsIgnoreCase("setbal"))) {
            if (sender.hasPermission("mobswords.adminset")) {
                if(args.length != 3) {
                    for(String msg : this.plugin.getConfig().getStringList("help-command")) {
                        msg = msg.replaceAll("\\{version}", this.plugin.currVersion);
                        if(!msg.contains("/mobswords") || (msg.contains("/mobswords") && msg.contains("setbal"))) {
                            sender.sendMessage(MSUtil.clr(msg));
                        }
                    }
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if(target == null) {
                    sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("no-player-message")));
                    return true;
                }
                UUID uuid = target.getUniqueId();
                if(!MSUtil.isNumeric(args[2])) {
                    sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("not-number-message")));
                    return true;
                }
                Double value = MSUtil.getNumber(args[2]);
                this.plugin.getSQLite().setBalance(uuid, value);

                String msg = this.plugin.getConfig().getString("change-balance-message");
                msg = msg.replaceAll("\\{amount}", MSUtil.formatNumber(value));
                msg = msg.replaceAll("\\{customCurrency}", currencyName);
                msg = msg.replaceAll("\\{player}", target.getName());
                sender.sendMessage(MSUtil.clr(msg));

                String msg2 = this.plugin.getConfig().getString("changed-balance-message");
                msg2 = msg2.replaceAll("\\{amount}", MSUtil.formatNumber(value));
                msg2 = msg2.replaceAll("\\{customCurrency}", currencyName);
                if(target.isOnline()) {
                    target.getPlayer().sendMessage(MSUtil.clr(msg2));
                }
            } else {
                sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("no-permission-message")));
            }
        } else if (args.length >= 1 && (args[0].equalsIgnoreCase("addbalance") || args[0].equalsIgnoreCase("addbal"))) {
            if (sender.hasPermission("mobswords.adminset")) {
                if(args.length != 3) {
                    for(String msg : this.plugin.getConfig().getStringList("help-command")) {
                        msg = msg.replaceAll("\\{version}", this.plugin.currVersion);
                        if(!msg.contains("/mobswords") || (msg.contains("/mobswords") && msg.contains("addbal"))) {
                            sender.sendMessage(MSUtil.clr(msg));
                        }
                    }
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if(target == null) {
                    sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("no-player-message")));
                    return true;
                }
                UUID uuid = target.getUniqueId();
                if(!MSUtil.isNumeric(args[2])) {
                    sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("not-number-message")));
                    return true;
                }
                Double value = MSUtil.getNumber(args[2]);
                Double newValue = MSUtil.updateBalance(this.plugin.getSQLite(), uuid, value);

                String msg = this.plugin.getConfig().getString("change-balance-message");
                msg = msg.replaceAll("\\{amount}", MSUtil.formatNumber(newValue));
                msg = msg.replaceAll("\\{customCurrency}", currencyName);
                msg = msg.replaceAll("\\{player}", target.getName());
                sender.sendMessage(MSUtil.clr(msg));

                String msg2 = this.plugin.getConfig().getString("changed-balance-message");
                msg2 = msg2.replaceAll("\\{amount}", MSUtil.formatNumber(newValue));
                msg2 = msg2.replaceAll("\\{customCurrency}", currencyName);
                if(target.isOnline()) {
                    target.getPlayer().sendMessage(MSUtil.clr(msg2));
                }
            } else {
                sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("no-permission-message")));
            }
        } else if (args.length >= 1 && (args[0].equalsIgnoreCase("removebalance") || args[0].equalsIgnoreCase("removebal") || args[0].equalsIgnoreCase("subtractbalance") || args[0].equalsIgnoreCase("subtractbal"))) {
            if (sender.hasPermission("mobswords.adminset")) {
                if(args.length != 3) {
                    for(String msg : this.plugin.getConfig().getStringList("help-command")) {
                        msg = msg.replaceAll("\\{version}", this.plugin.currVersion);
                        if(!msg.contains("/mobswords") || (msg.contains("/mobswords") && msg.contains("removebal"))) {
                            sender.sendMessage(MSUtil.clr(msg));
                        }
                    }
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if(target == null) {
                    sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("no-player-message")));
                    return true;
                }
                UUID uuid = target.getUniqueId();
                if(!MSUtil.isNumeric(args[2])) {
                    sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("not-number-message")));
                    return true;
                }
                Double value = MSUtil.getNumber(args[2]);
                Double newValue = MSUtil.updateBalance(this.plugin.getSQLite(), uuid, -value);

                String msg = this.plugin.getConfig().getString("change-balance-message");
                msg = msg.replaceAll("\\{amount}", MSUtil.formatNumber(newValue));
                msg = msg.replaceAll("\\{customCurrency}", currencyName);
                msg = msg.replaceAll("\\{player}", target.getName());
                sender.sendMessage(MSUtil.clr(msg));

                String msg2 = this.plugin.getConfig().getString("changed-balance-message");
                msg2 = msg2.replaceAll("\\{amount}", MSUtil.formatNumber(newValue));
                msg2 = msg2.replaceAll("\\{customCurrency}", currencyName);
                if(target.isOnline()) {
                    target.getPlayer().sendMessage(MSUtil.clr(msg2));
                }
            } else {
                sender.sendMessage(MSUtil.clr(this.plugin.getConfig().getString("no-permission-message")));
            }
        } else {
            for(String msg : this.plugin.getConfig().getStringList("help-command")) {
                msg = msg.replaceAll("\\{version}", this.plugin.currVersion);
                sender.sendMessage(MSUtil.clr(msg));
            }
            return true;
        }

        return true;
    }

}
