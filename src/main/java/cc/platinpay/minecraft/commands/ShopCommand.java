package cc.platinpay.minecraft.commands;

import cc.platinpay.minecraft.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ShopCommand implements CommandExecutor {
    private String shopLink;
    public ShopCommand(String shopLink) {
        this.shopLink = shopLink;
    }

    public void setShopLink(String shopLink) {
        this.shopLink = shopLink;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (Objects.equals(shopLink, "none")) {
            sender.sendMessage(Main.PLATINPAY_PREFIX + ChatColor.RED + "Shop link not set, contact an administrator!");
            return true;
        }
        sender.sendMessage(Main.PLATINPAY_PREFIX + ChatColor.WHITE + "Shop link: " + shopLink);
        return true;
    }
}
