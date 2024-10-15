package cc.platinpay.minecraft.commands;

import cc.platinpay.minecraft.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PlatinPayCommandHandler implements CommandExecutor {
    private final ShopCommand shopCommand;
    private final ReloadCommand reloadCommand;
    private final TokenCommand tokenCommand;

    public PlatinPayCommandHandler(ShopCommand shopCommand, ReloadCommand reloadCommand, TokenCommand tokenCommand) {
        this.shopCommand = shopCommand;
        this.reloadCommand = reloadCommand;
        this.tokenCommand = tokenCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            return shopCommand.onCommand(sender, command, label, args);
        } else if (args.length == 1 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl"))) {
            return reloadCommand.onCommand(sender, command, label, args);
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("settoken") || args[0].equalsIgnoreCase("token"))) {
            return tokenCommand.onCommand(sender, command, label, args);
        } else {
            sender.sendMessage(Main.PLATINPAY_PREFIX + ChatColor.RED + "Invalid command usage.");
            return false;
        }
    }
}