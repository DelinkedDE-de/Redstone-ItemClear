package de.delinkedde.redstoneItemClear.webpanel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * API Client for communicating with the web panel backend
 */
public class ApiClient {

    private final RedstoneItemClear plugin;
    private final Gson gson = new Gson();
    private String apiUrl;
    private String apiKey;
    private String serverId;

    public ApiClient(RedstoneItemClear plugin) {
        this.plugin = plugin;
        this.apiUrl = plugin.getConfig().getString("webpanel.api-url", "https://delinkedde.de/api/minecraft");

        // Check if serverId is null OR empty/blank (not just null!)
        String configuredId = plugin.getConfig().getString("webpanel.server-id");
        if (configuredId == null || configuredId.isEmpty() || configuredId.isBlank()) {
            // Generate new ID from hostname + world UUID
            this.serverId = generateServerId();
            // Save to config
            plugin.getConfig().set("webpanel.server-id", this.serverId);
            plugin.saveConfig();
            plugin.getLogger().info("Generated new server ID: " + this.serverId);
        } else {
            this.serverId = configuredId;
        }
    }

    /**
     * Generate a unique server ID based on hostname + world UUID
     */
    private String generateServerId() {
        try {
            // Get hostname from config (not from Bukkit which can be empty)
            String hostname = plugin.getConfig().getString("webpanel.hostname", "localhost");

            // Clean hostname (replace dots, colons, etc. with underscores)
            String cleanHostname = hostname.replaceAll("[^a-zA-Z0-9-]", "_");

            // Get world UID from first world (main world)
            String worldUid = "unknown";
            if (!Bukkit.getWorlds().isEmpty()) {
                // Take first 8 characters of world UUID for uniqueness
                worldUid = Bukkit.getWorlds().get(0).getUID().toString().substring(0, 8);
            }

            // Combine: hostname_worlduid
            String serverId = cleanHostname + "_" + worldUid;

            // Ensure it's not empty or starting with underscore
            if (serverId.startsWith("_") || serverId.equals("_" + worldUid)) {
                serverId = "minecraft_" + worldUid;
            }

            return serverId;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to generate server ID: " + e.getMessage());
            // Fallback to simple UUID-based ID
            return "minecraft_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }
    }

    /**
     * Register this server with the API
     * @return true if successful
     */
    public boolean registerServer() {
        try {
            String hostname = plugin.getConfig().getString("webpanel.hostname", "localhost");
            String displayNameCfg = plugin.getConfig().getString("webpanel.display-name", "");
            if (displayNameCfg == null) displayNameCfg = "";

            // Get the BungeeCord server name this server is known as (for multi-server setups)
            // This is the name in BungeeCord's config.yml servers: section
            String bungeeName = plugin.getConfig().getString("webpanel.bungee-name", "");

            // Auto-generate display name: use bungee-name if available, otherwise from config or server.properties
            String displayName;
            if (!displayNameCfg.isBlank()) {
                displayName = displayNameCfg;
            } else if (bungeeName != null && !bungeeName.isEmpty()) {
                // Capitalize first letter of bungee name (lobby -> Lobby)
                displayName = bungeeName.substring(0, 1).toUpperCase() + bungeeName.substring(1);
            } else {
                displayName = getServerNameFromProperties().orElse("Minecraft Server");
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("serverId", serverId);
            payload.addProperty("hostname", hostname);
            payload.addProperty("displayName", displayName);
            payload.addProperty("version", Bukkit.getVersion());
            // Spigot/Paper servers are NEVER BungeeCord proxies (only the actual BungeeCord is)
            payload.addProperty("isBungeecord", false);
            payload.addProperty("onlineMode", Bukkit.getOnlineMode());
            if (bungeeName != null && !bungeeName.isEmpty()) {
                payload.addProperty("bungeeName", bungeeName);
            }

            JsonObject response = sendPostRequest(apiUrl + "/servers/register", payload, null);

            if (response != null && response.has("apiKey")) {
                this.apiKey = response.get("apiKey").getAsString();

                // Save API key to config
                plugin.getConfig().set("webpanel.api-key", apiKey);
                plugin.getConfig().set("webpanel.server-id", serverId);
                plugin.saveConfig();

                plugin.getLogger().info("Server registered successfully");
                plugin.getLogger().info("Server ID: " + serverId);
                return true;
            }

            plugin.getLogger().warning("Failed to register server - no API key received");
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Try to read server-name from server.properties
     */
    private java.util.Optional<String> getServerNameFromProperties() {
        try {
            java.io.File f = new java.io.File("server.properties");
            if (!f.exists()) return java.util.Optional.empty();
            java.util.Properties props = new java.util.Properties();
            try (java.io.FileInputStream in = new java.io.FileInputStream(f)) {
                props.load(in);
            }
            String name = props.getProperty("server-name");
            if (name != null && !name.isBlank()) return java.util.Optional.of(name.trim());
        } catch (Exception ignored) {}
        return java.util.Optional.empty();
    }

    /**
     * Verify a code for authentication
     */
    public boolean verifyCode(String code, String uuid, String username) {
        try {
            // Load API key from config if not set
            if (apiKey == null) {
                apiKey = plugin.getConfig().getString("webpanel.api-key");
                if (apiKey == null) {
                    plugin.getLogger().warning("No API key found - run /ric reload");
                    return false;
                }
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("code", code.toUpperCase());
            payload.addProperty("uuid", uuid);
            payload.addProperty("username", username.toLowerCase());

            JsonObject response = sendPostRequest(apiUrl + "/auth/verify", payload, apiKey);

            return response != null && response.has("success") && response.get("success").getAsBoolean();
        } catch (Exception e) {
            plugin.getLogger().warning("Code verification failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send TPS data to API
     */
    public void sendTpsData(double tps, int playersOnline, int chunksLoaded) {
        try {
            if (apiKey == null) {
                apiKey = plugin.getConfig().getString("webpanel.api-key");
                if (apiKey == null) return;
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("timestamp", System.currentTimeMillis());
            payload.addProperty("tps", tps);
            payload.addProperty("playersOnline", playersOnline);
            payload.addProperty("chunksLoaded", chunksLoaded);

            sendPostRequest(apiUrl + "/tps", payload, apiKey);
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to send TPS data: " + e.getMessage());
        }
    }

    /**
     * Send problem zone data to API
     */
    public void sendProblemData(String world, int chunkX, int chunkZ, String problemType, String severity, int entityCount) {
        try {
            if (apiKey == null) {
                apiKey = plugin.getConfig().getString("webpanel.api-key");
                if (apiKey == null) return;
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("timestamp", System.currentTimeMillis());
            payload.addProperty("world", world);
            payload.addProperty("chunkX", chunkX);
            payload.addProperty("chunkZ", chunkZ);
            payload.addProperty("problemType", problemType);
            payload.addProperty("severity", severity);
            payload.addProperty("entityCount", entityCount);

            sendPostRequest(apiUrl + "/problems", payload, apiKey);
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to send problem data: " + e.getMessage());
        }
    }

    /**
     * Update user access (send to API)
     */
    public void updateUserAccess(String uuid, String username) {
        try {
            if (apiKey == null) {
                apiKey = plugin.getConfig().getString("webpanel.api-key");
                if (apiKey == null) return;
            }

            JsonObject userObj = new JsonObject();
            userObj.addProperty("uuid", uuid);
            userObj.addProperty("username", username);

            JsonObject payload = new JsonObject();
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            arr.add(userObj);
            payload.add("users", arr);

            // This will attribute access to THIS server via its API key
            sendPostRequest(apiUrl + "/users/bulk-access", payload, apiKey);
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to update user access: " + e.getMessage());
        }
    }


    /**
     * Send POST request to API
     */
    private JsonObject sendPostRequest(String endpoint, JsonObject payload, String apiKeyHeader) throws IOException {
        URL url = URI.create(endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", "RedstoneItemClear-Plugin/1.0");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        if (apiKeyHeader != null) {
            conn.setRequestProperty("X-API-Key", apiKeyHeader);
        }

        conn.setDoOutput(true);

        // Send payload
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = gson.toJson(payload).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                String response = scanner.useDelimiter("\\A").next();
                return gson.fromJson(response, JsonObject.class);
            }
        } else {
            // Log error details
            plugin.getLogger().warning("API request failed with code: " + responseCode);
            plugin.getLogger().warning("Request URL: " + endpoint);
            plugin.getLogger().warning("Request payload: " + gson.toJson(payload));

            // Try to read error response
            try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8)) {
                String errorResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                if (!errorResponse.isEmpty()) {
                    plugin.getLogger().warning("Error response: " + errorResponse);
                }
            } catch (Exception e) {
                // Ignore if can't read error stream
            }

            return null;
        }
    }
}
