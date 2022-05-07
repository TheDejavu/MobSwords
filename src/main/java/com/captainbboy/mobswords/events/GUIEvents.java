package com.captainbboy.mobswords.events;

import com.captainbboy.mobswords.MSUtil;
import com.captainbboy.mobswords.MobSwords;
import com.captainbboy.mobswords.SQLite.SQLite;
import com.captainbboy.mobswords.upgrades.UpgradeMenu;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GUIEvents implements Listener {

    private final MobSwords plugin;

    public GUIEvents(MobSwords plugin) {
        this.plugin = plugin;
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().getTitle().equals(MSUtil.clr("&5&lMobSword Upgrades"))) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.STAINED_GLASS_PANE) return;

        final Player p = (Player) e.getWhoClicked();

        ItemStack sword = p.getItemInHand();
        FileConfiguration config = this.plugin.getConfig();
        NBTItem nbtItem = new NBTItem(sword);

        if (!nbtItem.hasKey("isMobSword") || !nbtItem.getBoolean("isMobSword")) {
            p.sendMessage(MSUtil.clr(config.getString("no-sword-message")));
            return;
        }

        int keyFinderLevel = nbtItem.getInteger("mobSwordKeyFinder");
        int spawnerFinderLevel = nbtItem.getInteger("mobSwordSpawnerFinder");
        double doubleChance = nbtItem.getDouble("mobSwordDoubleChance");
        double sellMultiplier = nbtItem.getDouble("mobSwordSellMult");
        boolean autoSell = nbtItem.getBoolean("mobSwordAutoSell");
        boolean autoSellEnabled = nbtItem.getBoolean("mobSwordAutoSellEnabled");


        ItemStack item = e.getCurrentItem();

        switch (item.getType()) {
            case TRIPWIRE_HOOK:
                // Clicked Key Finder Upgrade
                if(keyFinderLevel >= config.getInt("max-key-finder")) {
                    p.sendMessage(MSUtil.clr(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "Key Finder")));
                    break;
                }
                Double keyFinderPrice = calculatePrice(config.getDouble("key-finder-price-start"), config.getDouble("key-finder-price-rate"), Double.valueOf(keyFinderLevel));
                if(handlePurchase(p, "Key Finder", keyFinderPrice)) {
                    ItemStack newSword = handleItem(sword, keyFinderLevel + 1, spawnerFinderLevel, autoSell, sellMultiplier, doubleChance, autoSellEnabled);
                    NBTItem nbti2 = new NBTItem(newSword);
                    nbti2.setInteger("mobSwordKeyFinder", keyFinderLevel + 1);
                    p.setItemInHand(nbti2.getItem());
                    endPurchase(p);
                };
                break;
            case MOB_SPAWNER:
                // Clicked Spawner Finder Upgrade
                if(spawnerFinderLevel >= config.getInt("max-spawner-finder")) {
                    p.sendMessage(MSUtil.clr(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "Spawner Finder")));
                    break;
                }
                Double spawnerFinderPrice = calculatePrice(config.getDouble("spawner-finder-price-start"), config.getDouble("spawner-finder-price-rate"), Double.valueOf(spawnerFinderLevel));
                if(handlePurchase(p, "Spawner Finder", spawnerFinderPrice)) {
                    ItemStack newSword = handleItem(sword, keyFinderLevel, spawnerFinderLevel + 1, autoSell, sellMultiplier, doubleChance, autoSellEnabled);
                    NBTItem nbti2 = new NBTItem(newSword);
                    nbti2.setInteger("mobSwordSpawnerFinder", spawnerFinderLevel + 1);
                    p.setItemInHand(nbti2.getItem());
                    endPurchase(p);
                }
                break;
            case GOLD_SWORD:
                // Clicked AutoSell Upgrade
                if(autoSell == true) {
                    p.sendMessage(MSUtil.clr(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "AutoSell")));
                    break;
                }
                Double autoSellPrice = config.getDouble("auto-sell-price");
                if(handlePurchase(p, "AutoSell", autoSellPrice)) {
                    ItemStack newHoe = handleItem(sword, keyFinderLevel, spawnerFinderLevel, true, sellMultiplier, doubleChance, autoSellEnabled);
                    NBTItem nbti2 = new NBTItem(newHoe);
                    nbti2.setBoolean("mobSwordAutoSell", true);
                    p.setItemInHand(nbti2.getItem());
                    endPurchase(p);
                }
                break;
            case GOLD_NUGGET:
                // Clicked Sell Multiplier Upgrade
                if(sellMultiplier >= config.getDouble("max-sell-multiplier")) {
                    p.sendMessage(MSUtil.clr(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "Sell Multiplier")));
                    break;
                }
                Double sellMultIncr = config.getDouble("sell-multiplier-increment");
                Double sellMultPrice = calculatePrice(config.getDouble("sell-multiplier-price-start"), config.getDouble("sell-multiplier-price-rate"), MSUtil.roundToHundredths((sellMultiplier - 1.0)/sellMultIncr));
                if(handlePurchase(p, "Sell Multiplier", sellMultPrice)) {
                    ItemStack newHoe = handleItem(sword, keyFinderLevel, spawnerFinderLevel, autoSell, MSUtil.roundToHundredths(sellMultiplier + config.getDouble("sell-multiplier-increment")), doubleChance, autoSellEnabled);
                    NBTItem nbti2 = new NBTItem(newHoe);
                    nbti2.setDouble("mobSwordSellMult", MSUtil.roundToHundredths(sellMultiplier + config.getDouble("sell-multiplier-increment")));
                    p.setItemInHand(nbti2.getItem());
                    endPurchase(p);
                }
                break;
            case DIAMOND:
                // Clicked Double Chance Upgrade
                if(doubleChance >= config.getDouble("max-double-chance")) {
                    p.sendMessage(MSUtil.clr(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "Double Chance")));
                    break;
                }
                Double doubleChanceIncr = config.getDouble("double-chance-increment");
                Double price = calculatePrice(config.getDouble("double-chance-price-start"), config.getDouble("double-chance-price-rate"), MSUtil.roundToHundredths(doubleChance/doubleChanceIncr));
                if(handlePurchase(p, "Double Chance", price)) {
                    ItemStack newHoe = handleItem(sword, keyFinderLevel, spawnerFinderLevel, autoSell, sellMultiplier, MSUtil.roundToHundredths(sellMultiplier + config.getDouble("double-chance-increment")), autoSellEnabled);
                    NBTItem nbti2 = new NBTItem(newHoe);
                    nbti2.setDouble("mobSwordDoubleChance", MSUtil.roundToHundredths(doubleChance + config.getDouble("double-chance-increment")));
                    p.setItemInHand(nbti2.getItem());
                    endPurchase(p);
                }
                break;
        }
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (e.getInventory().getTitle().equals(MSUtil.clr("&5&lMobSword Upgrades"))) {
            e.setCancelled(true);
        }
    }

    private boolean handlePurchase(Player p, String type, Double price) {
        FileConfiguration config = this.plugin.getConfig();
        SQLite db = this.plugin.getSQLite();

        Double bal = MSUtil.getNumber(db.getBalance(p.getUniqueId()));
        if(bal < price) {
            p.sendMessage(MSUtil.clr(config.getString("too-poor-message")));
            return false;
        } else {
            db.setBalance(p.getUniqueId(), bal - price);
            String message = MSUtil.clr(config.getString("successful-upgrade-purchase-message"));
            message = message.replaceAll("\\{type}", type);
            message = message.replaceAll("\\{price}", MSUtil.formatNumber(price));
            p.sendMessage(message);
            return true;
        }
    }

    private void endPurchase(Player p) {
        UpgradeMenu.promptUpgradeMenu(this.plugin, p, new NBTItem(p.getItemInHand()));
    }

    private ItemStack handleItem(ItemStack sword, Integer keyFinderLevel, Integer spawnerFinderLevel, Boolean autoSell, Double sellMultiplier, Double doubleChance, Boolean autoSellEnabled) {
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName(MSUtil.clr(this.plugin.getConfig().getString("mob-sword-item-name")));
        List<String> lores = new ArrayList();
        Iterator loreIterator = this.plugin.getConfig().getStringList("mob-sword-item-lore").iterator();

        while(loreIterator.hasNext()) {
            String s = (String) loreIterator.next();
            s = s.replaceAll("\\{keyFinderValue}", String.valueOf(keyFinderLevel));
            s = s.replaceAll("\\{spawnerFinderValue}", String.valueOf(spawnerFinderLevel));
            s = s.replaceAll("\\{autosellValue}", String.valueOf(autoSell));
            if(s.contains("{autosellEnabled}")) {
                if(autoSellEnabled)
                    s = s.replaceAll("\\{autosellEnabled}", "enabled");
                else
                    s = s.replaceAll("\\{autosellEnabled}", "disabled");
            }
            s = s.replaceAll("\\{sellMultiplierValue}", String.valueOf(MSUtil.roundToHundredths(sellMultiplier)));
            s = s.replaceAll("\\{doubleChanceValue}", String.valueOf(MSUtil.roundToHundredths(doubleChance)));
            lores.add(MSUtil.clr(s));
        }
        meta.setLore(lores);
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
        meta.spigot().setUnbreakable(true);
        sword.setItemMeta(meta);

        return sword;
    }

    private Double calculatePrice(Double startPrice, Double rate, Double currentLevel) {
        return MSUtil.roundToHundred(startPrice * rate * (currentLevel+1));
    }

}