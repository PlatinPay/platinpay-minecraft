package cc.platinpay.minecraft.commands;

import cc.platinpay.minecraft.Main;
import cc.platinpay.minecraft.utils.TokenManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;

public class TokenCommand implements CommandExecutor {

    private final TokenManager tokenManager;

    public TokenCommand(Main plugin) {
        this.tokenManager = new TokenManager(plugin.getDataFolder());
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Main.PLATINPAY_PREFIX + ChatColor.RED +
                    "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(Main.PLATINPAY_PREFIX + ChatColor.RED +
                    "Usage: /platinpay settoken <token>");
            return true;
        }

        try {
            PublicKey publicKey = decodePublicKey(args[1]);
            tokenManager.setPublicKey(publicKey);
            sender.sendMessage(Main.PLATINPAY_PREFIX + ChatColor.GREEN +
                    "Token set successfully.");
        } catch (IOException e) {
            sender.sendMessage(Main.PLATINPAY_PREFIX + ChatColor.RED +
                    "Error writing token: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }
    private PublicKey decodePublicKey(String key) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return java.security.KeyFactory.getInstance("Ed25519")
                .generatePublic(new java.security.spec.X509EncodedKeySpec(decodedKey));
    }
}
