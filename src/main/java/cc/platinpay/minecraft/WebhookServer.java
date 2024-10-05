package cc.platinpay.minecraft;

import fi.iki.elonen.NanoHTTPD;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class WebhookServer extends NanoHTTPD {

    private final JavaPlugin plugin;

    public WebhookServer(JavaPlugin plugin, Integer webhookPort) throws IOException {
        super(webhookPort);
        this.plugin = plugin;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        plugin.getLogger().info("Webhook server running on port " + webhookPort + "...");
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (Method.POST.equals(session.getMethod())) {
            try {
                Map<String, String> files = new java.util.HashMap<>();
                session.parseBody(files);

                String rawBody = files.get("postData");

                if (rawBody != null) {
                    JSONObject json = new JSONObject(rawBody);

                    if (json.has("commands") && json.get("commands") instanceof JSONArray) {
                        JSONArray commands = json.getJSONArray("commands");
                        if (json.has("playeruuid")) {
                            for (int i = 0; i < commands.length(); i++) {
                                String command = commands.getString(i);
                                OfflinePlayer offlinePlayer;
                                try {
                                    offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(json.getString("playeruuid")));
                                } catch (Exception e) {
                                    String errorMessage = "{\"error\":\"Error parsing player UUID " + json.getString("playeruuid") + "\"}";
                                    plugin.getLogger().severe("Error parsing player UUID" + json.getString("playeruuid") + " " + e.getMessage());
                                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", errorMessage);
                                }
                                if (!offlinePlayer.hasPlayedBefore()) {
                                    plugin.getLogger().severe("Player not played before: " + json.getString("playeruuid"));
                                    String errorMessage = "{\"error\":\"Player not played before: " + json.getString("playeruuid") + "\"}";
                                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", errorMessage);
                                }
                                if (offlinePlayer.getName() == null) {
                                    plugin.getLogger().severe("Player name is null: " + json.getString("playeruuid"));
                                    String errorMessage = "{\"error\":\"Player name is null: " + json.getString("playeruuid") + "\"}";
                                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", errorMessage);
                                }
                                command = command.replace("{playeruuid}", offlinePlayer.getName());
                                String finalCommand = command;
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), finalCommand);
                                });
                            }
                        } else {
                            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"Invalid JSON structure. Expected 'playeruuid'.\"}");
                        }
                        return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"received\"}");
                    } else {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"Invalid JSON structure. Expected 'commands' array.\"}");
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
}