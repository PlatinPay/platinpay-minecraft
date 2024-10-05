package cc.platinpay.minecraft.commands;

import cc.platinpay.minecraft.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final Main plugin;

    public ReloadCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Main.PLATINPAY_PREFIX + ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        try {
            plugin.loadConfig();
            sender.sendMessage(Main.PLATINPAY_PREFIX + ChatColor.GREEN + "Configuration reloaded successfully.");
        } catch (Exception e) {
            sender.sendMessage(Main.PLATINPAY_PREFIX + ChatColor.RED + "Error reloading configuration: " + e.getMessage());
        }
        return true;
    }
}