package com.captainbboy.mobswords.events;

import com.captainbboy.mobswords.MSUtil;
import com.captainbboy.mobswords.MobSwords;
import com.captainbboy.mobswords.upgrades.UpgradeMenu;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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
                        UpgradeMenu.promptUpgradeMenu(this.plugin, p, nbtItem);
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
                            String message = MSUtil.clr(this.plugin.getConfig().getString("toggle-autosell-message"));
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
                                lores.add(MSUtil.clr(s));
                            }
                            itemMeta.setLore(lores);
                            item2.setItemMeta(itemMeta);

                            p.setItemInHand(item2);
                            p.sendMessage(message);
                        }
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

}
