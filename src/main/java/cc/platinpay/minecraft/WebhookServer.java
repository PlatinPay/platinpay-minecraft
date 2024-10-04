package cc.platinpay.minecraft;

import fi.iki.elonen.NanoHTTPD;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
                session.parseBody(new java.util.HashMap<>());

                Map<String, List<String>> parameters = session.getParameters();
                String postData = parameters.get("postData") != null ? parameters.get("postData").getFirst() : null;

                plugin.getLogger().info("Received Webhook: " + postData);

                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"received\"}");

            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Server Error");
            }
        }
        return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Only POST requests are allowed");
    }
}