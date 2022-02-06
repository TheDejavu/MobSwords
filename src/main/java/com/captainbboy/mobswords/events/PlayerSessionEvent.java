package com.captainbboy.mobswords.events;

import com.captainbboy.mobswords.MSUtil;
import com.captainbboy.mobswords.MobSwords;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerSessionEvent implements Listener {

    private final MobSwords plugin;

    public PlayerSessionEvent(MobSwords plg) {
        this.plugin = plg;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        String value = plugin.getSQLite().getBalance(e.getPlayer().getUniqueId());
        Double d;
        if(MSUtil.isNumeric(value))
            d = MSUtil.getNumber(value);
        else
            d = 0.0d;
        
        plugin.getExpansion().addToMap(e.getPlayer().getUniqueId(), d);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        plugin.getExpansion().removeFromMap(e.getPlayer().getUniqueId());
    }
}
