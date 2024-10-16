package cc.platinpay.minecraft.commands;

import cc.platinpay.minecraft.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class HelpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        sender.sendMessage(Main.PLATINPAY_PREFIX + ChatColor.WHITE + "PlatinPay Help");
        sender.sendMessage(ChatColor.WHITE + "/platinpay help | /platinpay - Show this help message.");
        sender.sendMessage(ChatColor.WHITE + "/platinpay shop - Show the shop link.");
        if (sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "OP: " + ChatColor.WHITE + "/platinpay reload - Reload the configuration.");
            sender.sendMessage(ChatColor.RED + "OP: " + ChatColor.WHITE + "/platinpay settoken <token> | token <token> - Set the token for the server.");
        }
        return true;
    }
}