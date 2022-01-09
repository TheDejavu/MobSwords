package com.captainbboy.mobswords;

import java.util.HashMap;
import java.util.UUID;

public class PlayerHandler {

    private HashMap<UUID, Double> amountEarned = new HashMap<>();

    public void addToEarned(UUID playerUUID, Double amountToAdd) {
        if(this.amountEarned.containsKey(playerUUID)) {
            Double newValue = this.amountEarned.get(playerUUID) + amountToAdd;
            this.amountEarned.replace(playerUUID, newValue);
        } else {
            this.amountEarned.put(playerUUID, amountToAdd);
        }
    }

    public void clearAmountEarned() {
        this.amountEarned.clear();
    }

    public HashMap<UUID, Double> getAmountEarned() {
        return this.amountEarned;
    }

}
