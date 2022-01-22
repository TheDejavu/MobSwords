package com.captainbboy.mobswords;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MobSwordExpansion extends PlaceholderExpansion {

    private HashMap<UUID, Double> values = new HashMap<>();

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
            if(values.containsKey(player.getUniqueId()))
                return String.valueOf(values.get(player.getUniqueId()));
            return "0.0";
        }

        if(params.equalsIgnoreCase("currency_name")) {
            return plugin.getConfig().getString("name-of-mobsword-currency", "Shards");
        }

        return null; // Placeholder is unknown by the Expansion
    }

    public void removeFromMap(UUID uuid) {
        values.remove(uuid);
    }

    public void setValue(UUID uuid, Double d) {
        if(values.containsKey(uuid))
            values.replace(uuid, d);
        else
            addToMap(uuid, d);
    }

    public void updateMap(UUID uuid, Double d) {
        if(values.containsKey(uuid))
            values.replace(uuid, d + values.get(uuid));
        else
            addToMap(uuid, d);
    }

    public void addToMap(UUID uuid, Double d) {
        values.put(uuid, d);
    }
}