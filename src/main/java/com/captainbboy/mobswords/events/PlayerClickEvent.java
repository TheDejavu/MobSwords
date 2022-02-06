package com.captainbboy.mobswords.events;

import com.captainbboy.mobswords.MSUtil;
import com.captainbboy.mobswords.MobSwords;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerClickEvent implements Listener {

    private final MobSwords plugin;

    public PlayerClickEvent(MobSwords plg) {
        this.plugin = plg;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            ItemStack item = e.getItem();
            if(item != null && item.getType() != Material.AIR) {
                NBTItem nbtItem = new NBTItem(item);
                if(nbtItem.hasKey("isMobSword") && nbtItem.getBoolean("isMobSword")) {
                    Player p = e.getPlayer();
                    if(p.isSneaking()) {
                        // prompt upgrade menu
                        promptUpgradeMenu(this.plugin, p, nbtItem);
                    } else {
                        // Toggle autosell
                        if(nbtItem.hasKey("mobSwordAutoSell") && nbtItem.getBoolean("mobSwordAutoSell")) {

                            Boolean autosellValue = nbtItem.getBoolean("mobSwordAutoSell");
                            Double sellMultiplierValue = nbtItem.getDouble("mobSwordSellMult");
                            Double doubleChanceValue = nbtItem.getDouble("mobSwordDoubleChance");
                            int keyFinderValue = nbtItem.getInteger("mobSwordKeyFinder");
                            int spawnerFinderValue = nbtItem.getInteger("mobSwordSpawnerFinder");

                            Boolean oldValue = nbtItem.getBoolean("mobSwordAutoSellEnabled");
                            nbtItem.setBoolean("mobSwordAutoSellEnabled", !oldValue);
                            ItemStack item2 = nbtItem.getItem();
                            String message = MSUtil.cvtStr(this.plugin.getConfig().getString("toggle-autosell-message"));
                            if(!oldValue) {
                                message = message.replaceAll("\\{value}", "enabled");
                            } else {
                                message = message.replaceAll("\\{value}", "disabled");
                            }
                            ItemMeta itemMeta = item2.getItemMeta();
                            List<String> lores = new ArrayList<>();
                            for(String s : this.plugin.getConfig().getStringList("mob-sword-item-lore")) {
                                s = s.replaceAll("\\{keyFinderValue}", String.valueOf(keyFinderValue));
                                s = s.replaceAll("\\{spawnerFinderValue}", String.valueOf(spawnerFinderValue));
                                s = s.replaceAll("\\{autosellValue}", String.valueOf(autosellValue));
                                s = s.replaceAll("\\{doubleChanceValue}", String.valueOf(doubleChanceValue));
                                if(s.contains("{autosellEnabled}")) {
                                    if(!oldValue)
                                        s = s.replaceAll("\\{autosellEnabled}", "enabled");
                                    else
                                        s = s.replaceAll("\\{autosellEnabled}", "disabled");
                                }
                                s = s.replaceAll("\\{sellMultiplierValue}", String.valueOf(sellMultiplierValue));
                                lores.add(MSUtil.cvtStr(s));
                            }
                            itemMeta.setLore(lores);
                            item2.setItemMeta(itemMeta);

//                            NBTItem nbtItem2 = new NBTItem(item2);
//                            this.plugin.getLogger().info("after: "+nbtItem2.getBoolean("mobSwordAutoSellEnabled"));

                            p.setItemInHand(item2);
                            p.sendMessage(message);
                        }
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    public static Double calculatePrice(Double startPrice, Double rate, Double currentLevel) {
        return MSUtil.roundToHundred(startPrice * rate * (currentLevel+1));
    }

    public static void promptUpgradeMenu(MobSwords plugin, Player p, NBTItem nbtItem) {
        promptUpgradeMenu(plugin, p, nbtItem, null);
    }

    public static void promptUpgradeMenu(MobSwords plugin, Player p, NBTItem nbtItem, Inventory inventory) {

        Inventory inv;

        if(inventory == null)
            inv = Bukkit.createInventory(p, InventoryType.CHEST, MSUtil.cvtStr("&5&lMobSword Upgrades"));
        else
            inv = inventory;

        ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
            items[i] = glass;
        }

        // Get current upgrade values
        int keyFinderLevel = nbtItem.getInteger("mobSwordKeyFinder");
        int spawnerFinderLevel = nbtItem.getInteger("mobSwordSpawnerFinder");
        double doubleChance = nbtItem.getDouble("mobSwordDoubleChance");
        double sellMultiplier = nbtItem.getDouble("mobSwordSellMult");
        boolean autoSell = nbtItem.getBoolean("mobSwordAutoSell");

        // Get config values
        FileConfiguration config = plugin.getConfig();
        String baseName = config.getString("upgrade-item-name");
        List<String> baseLore = config.getStringList("upgrade-item-lore");

        // Make key finder upgrader
        ItemStack keyFinderUpgrade = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        ItemMeta keyFinderMeta = keyFinderUpgrade.getItemMeta();
        keyFinderMeta.addEnchant(Enchantment.DURABILITY, 3, true);
        keyFinderMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        keyFinderMeta.setDisplayName(MSUtil.cvtStr(baseName.replaceAll("\\{type}", "Key Finder")));
        List<String> keyFinderLore =  new ArrayList<>();
        Double keyFinderUpgradePrice = calculatePrice(config.getDouble("key-finder-price-start"), config.getDouble("key-finder-price-rate"), Double.valueOf(keyFinderLevel));
        for (String s : baseLore) {
            s = s.replaceAll("\\{currentValue}", String.valueOf(keyFinderLevel));
            if (s.contains("{nextValue}")) {
                if (keyFinderLevel >= config.getInt("max-key-finder")) {
                    s = s.replaceAll("\\{nextValue}", "Max Value Reached");
                } else {
                    s = s.replaceAll("\\{nextValue}", String.valueOf(keyFinderLevel + 1));
                }
            }
            s = s.replaceAll("\\{price}", MSUtil.formatNumber(keyFinderUpgradePrice));
            keyFinderLore.add(MSUtil.cvtStr(s));
        }
        keyFinderMeta.setLore(keyFinderLore);
        keyFinderUpgrade.setItemMeta(keyFinderMeta);
        items[11] = keyFinderUpgrade;

        // Make spawner finder upgrader
        ItemStack spawnerFinderUpgrade = new ItemStack(Material.MOB_SPAWNER, 1);
        ItemMeta spawnerFinderMeta = spawnerFinderUpgrade.getItemMeta();
        spawnerFinderMeta.addEnchant(Enchantment.DURABILITY, 3, true);
        spawnerFinderMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        spawnerFinderMeta.setDisplayName(MSUtil.cvtStr(baseName.replaceAll("\\{type}", "Spawner Finder")));
        List<String> spawnerFinderLore =  new ArrayList<>();
        Double spawnerFinderUpgradePrice = calculatePrice(config.getDouble("spawner-finder-price-start"), config.getDouble("spawner-finder-price-rate"), Double.valueOf(spawnerFinderLevel));
        for (String s : baseLore) {
            s = s.replaceAll("\\{currentValue}", String.valueOf(spawnerFinderLevel));
            if(s.contains("{nextValue}")) {
                if(spawnerFinderLevel >= config.getInt("max-spawner-finder")) {
                    s = s.replaceAll("\\{nextValue}", "Max Value Reached");
                } else {
                    s = s.replaceAll("\\{nextValue}", String.valueOf(spawnerFinderLevel + 1));
                }
            }
            s = s.replaceAll("\\{price}", MSUtil.formatNumber(spawnerFinderUpgradePrice));
            spawnerFinderLore.add(MSUtil.cvtStr(s));
        }
        spawnerFinderMeta.setLore(spawnerFinderLore);
        spawnerFinderUpgrade.setItemMeta(spawnerFinderMeta);
        items[12] = spawnerFinderUpgrade;

        // Make autosell upgrader
        ItemStack autoSellUpgrade = new ItemStack(Material.GOLD_SWORD, 1);
        ItemMeta autoSellMeta = autoSellUpgrade.getItemMeta();
        autoSellMeta.addEnchant(Enchantment.DURABILITY, 3, true);
        autoSellMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        autoSellMeta.setDisplayName(MSUtil.cvtStr(baseName.replaceAll("\\{type}", "AutoSell")));
        List<String> autoSellLore =  new ArrayList<>();
        for (String s : baseLore) {
            s = s.replaceAll("\\{currentValue}", String.valueOf(autoSell));
            if(s.contains("{nextValue}")) {
                if(autoSell == true) {
                    s = s.replaceAll("\\{nextValue}", "Max Value Reached");
                } else {
                    s = s.replaceAll("\\{nextValue}", "true");
                }
            }
            s = s.replaceAll("\\{price}", MSUtil.formatNumber(config.getDouble("auto-sell-price")));
            autoSellLore.add(MSUtil.cvtStr(s));
        }
        autoSellMeta.setLore(autoSellLore);
        autoSellUpgrade.setItemMeta(autoSellMeta);
        items[13] = autoSellUpgrade;

        // Make sell multiplier upgrader
        ItemStack sellMultUpgrade = new ItemStack(Material.GOLD_NUGGET, 1);
        ItemMeta sellMultMeta = sellMultUpgrade.getItemMeta();
        sellMultMeta.addEnchant(Enchantment.DURABILITY, 3, true);
        sellMultMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        sellMultMeta.setDisplayName(MSUtil.cvtStr(baseName.replaceAll("\\{type}", "Sell Multiplier")));
        Double sellMultIncr = config.getDouble("sell-multiplper-increment");
        Double sellMultUpgradePrice = calculatePrice(config.getDouble("sell-multiplier-price-start"), config.getDouble("sell-multiplier-price-rate"), (sellMultiplier - 1.0)/sellMultIncr);
        List<String> sellMultLore =  new ArrayList<>();
        for (String s : baseLore) {
            s = s.replaceAll("\\{currentValue}", String.valueOf(MSUtil.roundToHundredths(sellMultiplier)));
            if(s.contains("{nextValue}")) {
                if(sellMultiplier >= config.getDouble("max-sell-multiplier")) {
                    s = s.replaceAll("\\{nextValue}", "Max Value Reached");
                } else {
                    s = s.replaceAll("\\{nextValue}", String.valueOf(MSUtil.roundToHundredths(sellMultiplier + sellMultIncr)));
                }
            }
            s = s.replaceAll("\\{price}", MSUtil.formatNumber(sellMultUpgradePrice));
            sellMultLore.add(MSUtil.cvtStr(s));
        }
        sellMultMeta.setLore(sellMultLore);
        sellMultUpgrade.setItemMeta(sellMultMeta);
        items[14] = sellMultUpgrade;

        // Make double chance upgrader
        ItemStack doubleChanceUpgrade = new ItemStack(Material.DIAMOND, 1);
        ItemMeta doubleChanceMeta = doubleChanceUpgrade.getItemMeta();
        doubleChanceMeta.addEnchant(Enchantment.DURABILITY, 3, true);
        doubleChanceMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        doubleChanceMeta.setDisplayName(MSUtil.cvtStr(baseName.replaceAll("\\{type}", "Double Chance")));
        Double doubleChanceIncr = config.getDouble("double-chance-increment");
        Double doubleChanceUpgradePrice = calculatePrice(config.getDouble("double-chance-price-start"), config.getDouble("double-chance-price-rate"), (doubleChance)/doubleChanceIncr);
        List<String> doubleChanceLore =  new ArrayList<>();
        for (String s : baseLore) {
            s = s.replaceAll("\\{currentValue}", String.valueOf(MSUtil.roundToHundredths(doubleChance)));
            if(s.contains("{nextValue}")) {
                if(doubleChance >= config.getDouble("max-double-chance")) {
                    s = s.replaceAll("\\{nextValue}", "Max Value Reached");
                } else {
                    s = s.replaceAll("\\{nextValue}", String.valueOf(MSUtil.roundToHundredths(doubleChance + doubleChanceIncr)));
                }
            }
            s = s.replaceAll("\\{price}", MSUtil.formatNumber(doubleChanceUpgradePrice));
            doubleChanceLore.add(MSUtil.cvtStr(s));
        }
        doubleChanceMeta.setLore(doubleChanceLore);
        doubleChanceUpgrade.setItemMeta(doubleChanceMeta);
        items[15] = doubleChanceUpgrade;

        // Make currency show-er
        ItemStack currencyShow = new ItemStack(Material.NETHER_STAR, 1);
        ItemMeta currencyShowMeta = currencyShow.getItemMeta();
        currencyShowMeta.addEnchant(Enchantment.DURABILITY, 3, true);
        currencyShowMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        Double currencyValue = MSUtil.getNumber(plugin.getSQLite().getBalance(p.getUniqueId()));

        String baseCurrencyName = config.getString("currency-item-name");
        String currencyName = config.getString("name-of-mobsword-currency");

        List<String> baseCurrencyLore = config.getStringList("currency-item-lore");
        List<String> currencyShowLore =  new ArrayList<>();
        for (String s : baseCurrencyLore) {
            s = s.replaceAll("\\{currentValue}", MSUtil.formatNumber(MSUtil.roundToHundredths(currencyValue)));
            s = s.replaceAll("\\{currency}", currencyName);
            currencyShowLore.add(MSUtil.cvtStr(s));
        }
        currencyShowMeta.setDisplayName(MSUtil.cvtStr(baseCurrencyName.replaceAll("\\{currency}", currencyName)));
        currencyShowMeta.setLore(currencyShowLore);
        currencyShow.setItemMeta(currencyShowMeta);
        items[18] = currencyShow;

        inv.setContents(items);
        if(inventory == null)
            p.openInventory(inv);

    }

}
