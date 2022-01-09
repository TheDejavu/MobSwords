package com.captainbboy.mobswords.SQLite;

import com.captainbboy.mobswords.MobSwords;

import java.util.logging.Level;

public class Error {
    public static void execute(MobSwords plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(MobSwords plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}