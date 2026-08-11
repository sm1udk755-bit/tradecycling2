package de.felix.tradecycling;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class TradeCyclingPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new TradeCyclingListener(this), this);
        getLogger().info("TradeCycling enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("TradeCycling disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("tradecycling.admin")) {
                sender.sendMessage(Messages.color(getConfig().getString("messages.no-permission", "&cNo permission.")));
                return true;
            }
            reloadConfig();
            sender.sendMessage(Messages.color(getConfig().getString("messages.reloaded", "&aReloaded.")));
            return true;
        }
        sender.sendMessage(Messages.color("&eUsage: /tradecycling reload"));
        return true;
    }
}
