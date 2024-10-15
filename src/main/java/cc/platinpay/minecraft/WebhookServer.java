package cc.platinpay.minecraft;

import fi.iki.elonen.NanoHTTPD;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WebhookServer extends NanoHTTPD {

    private static final Logger log = LoggerFactory.getLogger(WebhookServer.class);
    private static BufferedWriter logWriter;

    private final JavaPlugin plugin;
    private final List<String> whitelistedIPs;
    private final Boolean whitelistOnly;
    private final Set<String> blockedCommandsSet;

    private WebhookServer(String hostname, int webhookPort, JavaPlugin plugin,
                          Set<String> blockedCommandsSet, Boolean whitelistOnly,
                          List<String> whitelistedIPs) throws IOException {
        super(hostname, webhookPort);
        this.plugin = plugin;
        this.blockedCommandsSet = blockedCommandsSet;
        this.whitelistedIPs = whitelistedIPs;
        this.whitelistOnly = whitelistOnly;

        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        plugin.getLogger().info("Webhook server started on port " + webhookPort);
    }

    public static WebhookServer createLocalServer(JavaPlugin plugin, int webhookPort, Set<String> blockedCommandsSet) throws IOException {
        return new WebhookServer("localhost", webhookPort, plugin, blockedCommandsSet, false, null);
    }

    public static WebhookServer createGlobalServer(JavaPlugin plugin, int webhookPort, Set<String> blockedCommandsSet,
                                                   Boolean whitelistOnly, List<String> whitelistedIPs) throws IOException {
        return new WebhookServer(null, webhookPort, plugin, blockedCommandsSet, whitelistOnly, whitelistedIPs);
    }

    public static void initLogger(File logFile) throws IOException {
        logWriter = new BufferedWriter(new FileWriter(logFile, true));
    }

    public static void closeLogger() throws IOException {
        if (logWriter != null) {
            logWriter.close();
        }
    }

    public static synchronized void logCommand(String command) {
        String logEntry = String.format("[%s] %s%n", LocalDateTime.now(), command);
        try {
            logWriter.write(logEntry);
            logWriter.flush();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to log command: " + e.getMessage());
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (whitelistOnly && whitelistedIPs == null) {
            plugin.getLogger().severe("Whitelist-only mode enabled but no IPs whitelisted.");
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Server Error");
        }
        String remoteIP = session.getRemoteIpAddress();
        if (whitelistOnly && !isAuthorizedIP(remoteIP)) {
            plugin.getLogger().severe("Unauthorized request from IP: " + remoteIP);
            return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden");
        }

        if (Method.POST.equals(session.getMethod())) {
            try {
                Map<String, String> files = new java.util.HashMap<>();
                session.parseBody(files);
                String rawBody = files.get("postData");

                if (rawBody != null) {
                    JSONObject json = new JSONObject(rawBody);
                    if (json.has("commands") && json.get("commands") instanceof JSONArray && json.has("playeruuid")) {
                        JSONArray commands = json.getJSONArray("commands");
                        if (commands.isEmpty()) {
                            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                                    "{\"error\":\"Commands array cannot be empty.\"}");
                        }
                        return processCommands(json);
                    } else {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                                "{\"error\":\"Invalid JSON structure. Expected 'commands' array and 'playeruuid'.\"}");
                    }
                } else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"No data received.\"}");
                }

            } catch (Exception e) {
                plugin.getLogger().severe("Error processing webhook: " + e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Server Error");
            }
        }
        return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Only POST requests are allowed");
    }

    private Response processCommands(JSONObject json) {
        JSONArray commands = json.getJSONArray("commands");
        UUID playerUUID = UUID.fromString(json.getString("playeruuid"));
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);

        if (!offlinePlayer.hasPlayedBefore() || offlinePlayer.getName() == null) {
            String errorMessage = String.format("{\"error\":\"Player not found or has never played: %s\"}", playerUUID);
            plugin.getLogger().severe(errorMessage);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", errorMessage);
        }

        for (int i = 0; i < commands.length(); i++) {
            String command = commands.getString(i).replace("{playeruuid}", offlinePlayer.getName());

            if (isBlockedCommand(command)) {
                plugin.getLogger().severe("Blocked command: " + command);
                logCommand("BLOCKED: " + command);
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Server Error");
            }

            String finalCommand = command;
            try {
                Bukkit.getScheduler().runTask(plugin, () ->
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), finalCommand)
                );
                logCommand(finalCommand);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to execute command: " + finalCommand + " - " + e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                        "{\"error\":\"Failed to execute command.\"}");
            }
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"received\"}");
    }

    private boolean isAuthorizedIP(String remoteIP) {
        return !whitelistOnly || (whitelistedIPs != null && whitelistedIPs.contains(remoteIP));
    }

    private boolean isBlockedCommand(String command) {
        String baseCommand = command.split("\\s+")[0];
        return blockedCommandsSet.contains(baseCommand.toLowerCase());
    }
}
