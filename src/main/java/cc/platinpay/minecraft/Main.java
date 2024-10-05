package cc.platinpay.minecraft;

import cc.platinpay.minecraft.commands.PlatinPayCommandHandler;
import cc.platinpay.minecraft.commands.ReloadCommand;
import cc.platinpay.minecraft.commands.ShopCommand;
import com.moandjiezana.toml.Toml;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main extends JavaPlugin {

    private WebhookServer webhookServer;
    private Integer webhookPort;
    private String storeLink;
    private ShopCommand shopCommand;

    public static final String PLATINPAY_PREFIX =
            ChatColor.BOLD + "" +
                    ChatColor.of("#A9A9A9") + "[" +
                    ChatColor.of("#FFFFFF") + "Platin" +
                    ChatColor.of("#1F4E79") + "Pay" +
                    ChatColor.of("#A9A9A9") + "] ";

    @Override
    public void onEnable() {
        loadConfig();
        getLogger().info("PlatinPay enabled");

        try {
            webhookServer = new WebhookServer(this, webhookPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        shopCommand = new ShopCommand(storeLink);

        ReloadCommand reloadCommand = new ReloadCommand(this);
        PlatinPayCommandHandler commandHandler = new PlatinPayCommandHandler(shopCommand, reloadCommand);

        Objects.requireNonNull(this.getCommand("platinpay")).setExecutor(commandHandler);
    }

    @Override
    public void onDisable() {
        if (webhookServer != null) {
            webhookServer.stop();
        }
        getLogger().info("PlatinPay disabled");
    }

    public void loadConfig() {
        try {
            if (!getDataFolder().exists()) {
                boolean created = getDataFolder().mkdirs();
                if (!created) {
                    getLogger().severe("Failed to create data folder.");
                    return;
                }
            }

            File configFile = new File(getDataFolder(), "config.toml");
            if (!configFile.exists()) {
                getLogger().info("Creating default config.toml...");
                saveResource("config.toml", false);
            }

            Toml config = new Toml().read(configFile);

            Integer oldWebhookPort = webhookPort;
            String oldStoreLink = storeLink;

            webhookPort = config.getLong("config.port", 8081L).intValue();
            storeLink = config.getString("config.storeLink", "none");

            getLogger().info("Config loaded: port=" + webhookPort + ", storeLink=" + storeLink);

            if (webhookServer != null && !webhookPort.equals(oldWebhookPort)) {
                webhookServer.stop();
                webhookServer = new WebhookServer(this, webhookPort);
            }

            if (shopCommand != null && !storeLink.equals(oldStoreLink)) {
                shopCommand.setShopLink(storeLink);
            }

        } catch (Exception e) {
            getLogger().severe("Failed to load config: " + e.getMessage());
        }
    }
}