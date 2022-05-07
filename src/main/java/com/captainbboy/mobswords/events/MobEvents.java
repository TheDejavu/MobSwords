package com.captainbboy.mobswords.events;

import com.captainbboy.mobswords.MSUtil;
import com.captainbboy.mobswords.MobSwords;
import com.captainbboy.mobswords.SQLite.SQLite;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class MobEvents implements Listener {

    private final MobSwords plugin;

    public MobEvents(MobSwords plg) {
        this.plugin = plg;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent e) {
        if(e.getEntity().getKiller() != null) {
            Player killer = e.getEntity().getKiller();
            if(e.getEntity().getType().toString().equalsIgnoreCase(this.plugin.getConfig().getString("mob-type"))) {
                ItemStack heldItem = killer.getInventory().getItemInHand();
                if (heldItem != null && heldItem.getType() != Material.AIR) {
                    NBTItem nbtItem = new NBTItem(heldItem);
                    if (nbtItem.hasKey("isMobSword") && nbtItem.getBoolean("isMobSword")) {

                        int amountOfStarsToGive = 1;
                        if (Math.random() < nbtItem.getDouble("mobSwordDoubleChance")) {
                            amountOfStarsToGive += 1;
                        }

                        int keyFinderLevelValue = nbtItem.getInteger("mobSwordKeyFinder");
                        if (Math.random() < (double) keyFinderLevelValue * 0.0001) {
                            String commands = MSUtil.chooseRandomCommand(this.plugin.getConfig().getStringList("key-finder"));
                            String[] cmds = commands.split(" \\| ");
                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), cmds[0].replaceAll("\\{PLAYER}", killer.getName()));
                            if (cmds.length > 1 && cmds[1] != null && cmds[1] != "")
                                Bukkit.broadcastMessage(MSUtil.clr(
                                        cmds[1].replaceAll("\\{PLAYER}", killer.getName())
                                ));
                        }

                        int spawnerFinderLevelValue = nbtItem.getInteger("mobSwordSpawnerFinder");
                        if (Math.random() < (double) spawnerFinderLevelValue * 0.0001) {
                            String commands = MSUtil.chooseRandomCommand(this.plugin.getConfig().getStringList("spawner-finder"));
                            String[] cmds = commands.split(" \\| ");
                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), cmds[0].replaceAll("\\{PLAYER}", killer.getName()));
                            if (cmds.length > 1 && cmds[1] != null && cmds[1] != "")
                                Bukkit.broadcastMessage(MSUtil.clr(
                                        cmds[1].replaceAll("\\{PLAYER}", killer.getName())
                                ));
                        }

                        if (nbtItem.hasKey("mobSwordAutoSell") && nbtItem.getBoolean("mobSwordAutoSell") && nbtItem.hasKey("mobSwordAutoSellEnabled") && nbtItem.getBoolean("mobSwordAutoSellEnabled")) {

                            int priceOfStar = this.plugin.getConfig().getInt("price-of-star");
                            Double multiplier = 1.0;
                            if (nbtItem.hasKey("mobSwordSellMult")) {
                                multiplier = nbtItem.getDouble("mobSwordSellMult");
                            }
                            if (priceOfStar == 0) {
                                for (int i = 0; i < amountOfStarsToGive; i++) {
                                    MSUtil.givePlayerItem(killer, Material.NETHER_STAR);
                                }
                            } else {
                                this.plugin.eco.depositPlayer(killer, amountOfStarsToGive * priceOfStar * multiplier);
                                this.plugin.getPlayerHandler().addToEarned(killer.getUniqueId(), amountOfStarsToGive * priceOfStar * multiplier);
                            }
                        } else {
                            for (int i = 0; i < amountOfStarsToGive; i++) {
                                MSUtil.givePlayerItem(killer, Material.NETHER_STAR);
                            }
                        }

                        Double currencyMultiplier = 1.0;

                        SQLite db = this.plugin.getSQLite();
                        MSUtil.updateBalance(db, killer.getUniqueId(), amountOfStarsToGive * currencyMultiplier);
                        e.setDroppedExp(0);
                        killer.giveExp(this.plugin.getConfig().getInt("mob-exp-drop"));
                        e.getDrops().clear();
                    } else {
                        e.getDrops().clear();
                        e.setDroppedExp(this.plugin.getConfig().getInt("mob-exp-drop"));
                        Location loc = e.getEntity().getLocation();
                        loc.getWorld().dropItem(loc, new ItemStack(Material.NETHER_STAR));
                    }
                }  else {
                    e.getDrops().clear();
                    e.setDroppedExp(this.plugin.getConfig().getInt("mob-exp-drop"));
                    Location loc = e.getEntity().getLocation();
                    loc.getWorld().dropItem(loc, new ItemStack(Material.NETHER_STAR));
                }
            }
        }
    }

}
