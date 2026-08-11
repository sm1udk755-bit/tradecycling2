package de.felix.tradecycling;

import org.bukkit.ChatColor;

public final class Messages {
    private Messages() {}

    public static String color(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
