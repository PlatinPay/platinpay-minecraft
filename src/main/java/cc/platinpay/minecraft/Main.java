package cc.platinpay.minecraft;

import cc.platinpay.minecraft.commands.PlatinPayCommandHandler;
import cc.platinpay.minecraft.commands.ReloadCommand;
import cc.platinpay.minecraft.commands.ShopCommand;
import com.moandjiezana.toml.Toml;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Main extends JavaPlugin {

    private WebhookServer webhookServer;
    private Integer webhookPort;
    private String storeLink;
    private List<String> blockedCommands;
    private ShopCommand shopCommand;
    private Boolean localOnly;
    private Boolean whitelistOnly;

    private Set<String> blockedCommandsSet;

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
        if (localOnly) {
            webhookServer = WebhookServer.createLocalServer(this, webhookPort, blockedCommandsSet);
        } else {
            webhookServer = WebhookServer.createGlobalServer(this, webhookPort, blockedCommandsSet, whitelistOnly, null);
        }} catch (IOException e) {
            getLogger().severe("Failed to start webhook server: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
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
            getLogger().info("Webhook server stopped");
        }
        try {
            WebhookServer.closeLogger();
        } catch (IOException e) {
            getLogger().severe("Failed to close command logger: " + e.getMessage());
        }
        getLogger().info("PlatinPay disabled");
    }

    public void loadConfig() {
        try {
            if (!getDataFolder().exists()) {
                boolean created = getDataFolder().mkdirs();
                if (!created) {
                    getLogger().severe("Failed to create data folder.");
                    Bukkit.getPluginManager().disablePlugin(this);
                }
            }

            File configFile = new File(getDataFolder(), "config.toml");
            if (!configFile.exists()) {
                getLogger().info("Creating default config.toml...");
                saveResource("config.toml", false);
            }

            File commandLogFile = new File(getDataFolder(), "commands.log");
            if (!commandLogFile.exists()) {
                getLogger().info("Creating log file (commands.log)...");
                try {
                    boolean created = commandLogFile.createNewFile();
                    if (!created) {
                        getLogger().warning("Log file (commands.log) already exists or could not be created.");
                        WebhookServer.initLogger(commandLogFile);
                    }
                } catch (IOException e) {
                    getLogger().severe("Failed to initialize command logger: " + e.getMessage());
                    Bukkit.getPluginManager().disablePlugin(this);
                }
            }

            Toml config = new Toml();
            try {
                config = new Toml().read(configFile);
            } catch (Exception e) {
                getLogger().severe("Failed to read config: " + e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
            }

            Integer oldWebhookPort = webhookPort;
            String oldStoreLink = storeLink;
            List<String> oldBlockedCommands = blockedCommands;
            Boolean oldLocalOnly = localOnly;
            Boolean oldWhitelistOnly = whitelistOnly;

            webhookPort = config.getLong("config.port", 8081L).intValue();
            storeLink = config.getString("config.storeLink", "none");
            blockedCommands = config.getList("config.blockedCommands", List.of());
            localOnly = config.getBoolean("config.localOnly", true);
            whitelistOnly = config.getBoolean("config.whitelistOnly", false);
            List<String> whitelistedIPs = config.getList("config.whitelistedIPs", List.of());

            blockedCommandsSet = new HashSet<>(blockedCommands);

            getLogger().info("Config loaded successfully");

            if (webhookServer != null && hasConfigChanged(oldWebhookPort, oldBlockedCommands, oldLocalOnly, oldWhitelistOnly)) {
                try {
                    webhookServer.stop();
                    if (localOnly) {
                        webhookServer = WebhookServer.createLocalServer(this, webhookPort, blockedCommandsSet);
                    } else {
                        webhookServer = WebhookServer.createGlobalServer(this, webhookPort, blockedCommandsSet, whitelistOnly, whitelistedIPs);
                    }
                    getLogger().info("Webhook server restarted with new configuration.");
                } catch (IOException e) {
                    getLogger().severe("Failed to restart webhook server: " + e.getMessage());
                    Bukkit.getPluginManager().disablePlugin(this);
                }
            }

            if (shopCommand != null && !storeLink.equals(oldStoreLink)) {
                shopCommand.setShopLink(storeLink);
            }

        } catch (Exception e) {
            getLogger().severe("Failed to load config: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    private boolean hasConfigChanged(Integer oldPort, List<String> oldBlockedCommands,
                                     Boolean oldLocalOnly, Boolean oldWhitelistOnly) {
        return !webhookPort.equals(oldPort) ||
                !Objects.equals(oldBlockedCommands, blockedCommands) ||
                !Objects.equals(oldLocalOnly, localOnly) ||
                !Objects.equals(oldWhitelistOnly, whitelistOnly);
    }
}