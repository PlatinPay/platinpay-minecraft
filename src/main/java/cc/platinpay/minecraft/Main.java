package cc.platinpay.minecraft;

import cc.platinpay.minecraft.WebhookServer;

import com.moandjiezana.toml.Toml;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.File;

public class Main extends JavaPlugin {

    private WebhookServer webhookServer;
    private Integer webhookPort;

    @Override
    public void onEnable() {
        loadConfig();
        getLogger().info("PlatinPay enabled");
        try {
            webhookServer = new WebhookServer(this, webhookPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        if (webhookServer != null) {
            webhookServer.stop();
        }
        getLogger().info("PlatinPay disabled");
    }

    private void loadConfig() {
        try {
            File configFile = new File(getDataFolder(), "config.toml");
            Toml config = new Toml().read(configFile);

            webhookPort = config.getLong("webhook.port", 8081L).intValue();

        } catch (Exception e) {
            getLogger().severe("Failed to load config: " + e.getMessage());
        }
    }
}