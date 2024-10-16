package cc.platinpay.minecraft;

import cc.platinpay.minecraft.commands.PlatinPayCommandHandler;
import cc.platinpay.minecraft.commands.ReloadCommand;
import cc.platinpay.minecraft.commands.ShopCommand;
import cc.platinpay.minecraft.commands.TokenCommand;
import cc.platinpay.minecraft.utils.TokenManager;
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
    private Boolean useSigning;
    private Boolean firstRun = true;

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

        shopCommand = new ShopCommand(storeLink);

        ReloadCommand reloadCommand = new ReloadCommand(this);
        TokenCommand tokenCommand = new TokenCommand(this);
        PlatinPayCommandHandler commandHandler = new PlatinPayCommandHandler(shopCommand, reloadCommand, tokenCommand);

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
                    } else {
                        getLogger().info("Log file (commands.log) created.");
                    }
                } catch (IOException e) {
                    getLogger().severe("Failed to initialize command logger: " + e.getMessage());
                    Bukkit.getPluginManager().disablePlugin(this);
                }
            }
            getLogger().info("Initializing logger...");
            WebhookServer.initLogger(commandLogFile);
            getLogger().info("Initialized logger...");

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
            Boolean oldUseSigning = useSigning;

            webhookPort = config.getLong("config.port", 8081L).intValue();
            storeLink = config.getString("config.storeLink", "none");
            blockedCommands = config.getList("config.blockedCommands", List.of());
            localOnly = config.getBoolean("config.localOnly", true);
            whitelistOnly = config.getBoolean("config.whitelistOnly", false);
            List<String> whitelistedIPs = config.getList("config.whitelistedIPs", List.of());
            useSigning = config.getBoolean("config.useSigning", true);

            Set<String> blockedCommandsSet = new HashSet<>(blockedCommands);

            if (useSigning) {
                TokenManager tokenManager = new TokenManager(getDataFolder());
                if (!tokenManager.publicKeyExists()) {
                    getLogger().warning(
                            "Public key not found. Webhook server will not start. " +
                                    "Please set the public key and reload the config with /platinpay reload."
                    );
                    return;
                }
            }

            getLogger().info("Config loaded successfully");


            if ((webhookServer != null && hasConfigChanged(oldWebhookPort, oldBlockedCommands, oldLocalOnly, oldWhitelistOnly, oldUseSigning)) || firstRun) {
                try {
                    if (webhookServer != null) {
                        webhookServer.stop();
                    }
                    if (localOnly) {
                        webhookServer = WebhookServer.createLocalServer(this, webhookPort, blockedCommandsSet, useSigning);
                    } else {
                        webhookServer = WebhookServer.createGlobalServer(this, webhookPort, blockedCommandsSet, whitelistOnly, whitelistedIPs, useSigning);
                    }
                    getLogger().info("Webhook server (re)started with (new) configuration.");
                } catch (IOException e) {
                    getLogger().severe("Failed to (re)start webhook server: " + e.getMessage());
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
        firstRun = false;
    }
    private boolean hasConfigChanged(Integer oldPort, List<String> oldBlockedCommands,
                                     Boolean oldLocalOnly, Boolean oldWhitelistOnly, Boolean oldUseSigning) {
        return !webhookPort.equals(oldPort) ||
                !Objects.equals(oldBlockedCommands, blockedCommands) ||
                !Objects.equals(oldLocalOnly, localOnly) ||
                !Objects.equals(oldWhitelistOnly, whitelistOnly) ||
                !Objects.equals(oldUseSigning, useSigning);
    }
}