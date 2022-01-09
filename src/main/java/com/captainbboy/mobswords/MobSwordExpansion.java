package com.captainbboy.mobswords;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class MobSwordExpansion extends PlaceholderExpansion {

    private final MobSwords plugin;

    public MobSwordExpansion(MobSwords plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return "captain_bboy";
    }

    @Override
    public String getIdentifier() {
        return "mobswords";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if(params.equalsIgnoreCase("currency_balance")){
            return plugin.getSQLite().getBalance(player.getUniqueId());
        }

        if(params.equalsIgnoreCase("currency_name")) {
            return plugin.getConfig().getString("name-of-mobsword-currency", "Shards");
        }

        return null; // Placeholder is unknown by the Expansion
    }
}