package com.captainbboy.mobswords;

import com.captainbboy.mobswords.SQLite.SQLite;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MSUtil {

    public static String clr(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static void givePlayerItem(Player player, Material m) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(new ItemStack(m));
        } else if (getSlot(player, m) != -1) {
            player.getInventory().addItem(new ItemStack(m));
        } else {
            player.sendMessage(clr("&a&l(!) &7Your &ainventory &7is full!"));
            player.getWorld().dropItem(player.getLocation(), new ItemStack(m));
        }
    }

    public static void givePlayerItem(Player player, Material m, int amount) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(new ItemStack(m, amount));
        } else if (getSlot(player, m) != -1) {
            player.getInventory().addItem(new ItemStack(m, amount));
        } else {
            player.sendMessage(clr("&a&l(!) &7Your &ainventory &7is full!"));
            player.getWorld().dropItem(player.getLocation(), new ItemStack(m, amount));
        }
    }

    public static int getSlot(Player p, Material m) {
        for(int i = 0; i < p.getInventory().getSize(); ++i) {
            if (p.getInventory().getItem(i).getType() == m && p.getInventory().getItem(i).getAmount() < p.getInventory().getItem(i).getMaxStackSize()) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum.replaceAll(",", ""));
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static double getNumber(String strNum) {
        double d;
        try {
            d = Double.parseDouble(strNum.replaceAll(",", ""));
        } catch (NumberFormatException nfe) {
            return 0.0d;
        }
        return d;
    }

    public static boolean isBoolean(String str) {
        if(str.equalsIgnoreCase("false") || str.equalsIgnoreCase("true"))
            return true;
        return false;
    }

    public static boolean getBoolean(String str) {
        if(str.equalsIgnoreCase("true"))
            return true;
        return false;
    }

    public static Double roundToHundredths(Double x) {
        return getNumber(formatNumber(x));
    }

    public static Double roundToHundred(double input) {
        long i = (long) Math.ceil(input);
        return Double.valueOf(((i + 99) / 100) * 100);
    };

    public static String formatNumber(String str) {
        if(isNumeric(str)) {
            double amount = Double.parseDouble(str);
            DecimalFormat formatter = new DecimalFormat("#,##0.00");

            return formatter.format(amount);
        } else {
            return "is_not_numeric";
        }
    }

    public static String formatNumber(Double num) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(num);
    }

    public static Double updateBalance(SQLite db, UUID uuid, Double value) {
        String result = db.getBalance(uuid);
        if(result.equals("0.000") || result == null) {
            db.addRowToCurrency(uuid, value);
            return value;
        } else {
            if(isNumeric(result)) {
                Double oldValue = getNumber(result);
                db.setBalance(uuid, roundToHundredths(oldValue + value));
                return (oldValue + value);
            } else {
                db.setBalance(uuid, roundToHundredths(value));
                return value;
            }
        }
    }

    public static Map<String, Integer> sortByValueInt(Map<String, Integer> unsortMap, final boolean order) {
        List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));

    }

    public static Map<String, Double> sortByValue(Map<String, Double> unsortMap, final boolean order) {
        List<Map.Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));

    }

    public static String chooseRandomCommand(List<String> options) {
        double max = 0.0;
        HashMap<String, Double> map = new HashMap<>();
        for (String option: options) {
            String[] optionA = option.split(" ");
            String num = optionA[0];
            if(isNumeric(num)) {
                Double n = getNumber(num);
                max += n;
                map.put(String.join(" ", Arrays.copyOfRange(optionA, 1, optionA.length)), max);
            }
        }
        double chosenNumber = max * Math.random();

        String cmd = "";
        for (Map.Entry<String, Double> entry : sortByValue(map, false).entrySet()) {
            if(chosenNumber < entry.getValue()) {
                cmd = entry.getKey();
            }
        }

        return cmd;
    }

}
