package cc.platinpay.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getServer().broadcastMessage("Test message from PlatinPayMinecraft plugin!");
    }

    @Override
    public void onDisable() {
        Bukkit.getServer().broadcastMessage("PlatinPayMinecraft plugin disabled.");
    }
}