package de.delinkedde.redstoneItemClear.discord;

import de.delinkedde.redstoneItemClear.RedstoneItemClear;
import de.delinkedde.redstoneItemClear.action.ActionExecutor;
import de.delinkedde.redstoneItemClear.config.ConfigManager;
import de.delinkedde.redstoneItemClear.model.ActionLevel;
import de.delinkedde.redstoneItemClear.model.ProblemZone;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Sendet Benachrichtigungen an Discord über Webhooks
 */
public class DiscordWebhook {
    private final RedstoneItemClear plugin;
    private final ConfigManager config;

    public DiscordWebhook(RedstoneItemClear plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Sendet eine TPS-Warnung
     */
    public void sendTPSWarning(ActionLevel level, double currentTPS, List<ProblemZone> problemZones) {
        if (!config.isDiscordEnabled() || !config.isDiscordWarningEnabled()) {
            return;
        }

        String webhookUrl = config.getDiscordWebhookUrl();
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        // Async senden um Server nicht zu blockieren
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String json = buildWarningEmbed(level, currentTPS, problemZones);
                    sendWebhook(webhookUrl, json);
                } catch (Exception e) {
                    plugin.getLogger().warning("Fehler beim Senden der Discord-Benachrichtigung: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Sendet eine Benachrichtigung über durchgeführte Aktionen
     */
    public void sendActionTaken(ActionLevel level, double currentTPS, ActionExecutor.ActionResult result,
                                 List<ProblemZone> problemZones) {
        if (!config.isDiscordEnabled() || !config.isDiscordActionTakenEnabled()) {
            return;
        }

        String webhookUrl = config.getDiscordWebhookUrl();
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String json = buildActionEmbed(level, currentTPS, result, problemZones);
                    sendWebhook(webhookUrl, json);
                } catch (Exception e) {
                    plugin.getLogger().warning("Fehler beim Senden der Discord-Benachrichtigung: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Erstellt ein Warning-Embed
     */
    private String buildWarningEmbed(ActionLevel level, double currentTPS, List<ProblemZone> zones) {
        StringBuilder description = new StringBuilder();
        description.append("**Aktuelle TPS:** ").append(String.format("%.2f", currentTPS)).append("\n");
        description.append("**Warnstufe:** ").append(level.getDisplayName()).append("\n\n");

        if (config.includeStatsInDiscord() && !zones.isEmpty()) {
            description.append("**Problematische Bereiche:**\n");
            int count = Math.min(5, zones.size());
            for (int i = 0; i < count; i++) {
                ProblemZone zone = zones.get(i);
                description.append(formatZone(zone)).append("\n");
            }
            if (zones.size() > 5) {
                description.append("... und ").append(zones.size() - 5).append(" weitere\n");
            }
        }

        int color = getColorForLevel(level);

        return String.format(
            "{\"embeds\":[{" +
            "\"title\":\"⚠️ TPS-Warnung\"," +
            "\"description\":\"%s\"," +
            "\"color\":%d," +
            "\"timestamp\":\"%s\"," +
            "\"footer\":{\"text\":\"Redstone ItemClear\"}" +
            "}]}",
            escapeJson(description.toString()),
            color,
            Instant.now().toString()
        );
    }

    /**
     * Erstellt ein Action-Embed
     */
    private String buildActionEmbed(ActionLevel level, double currentTPS, ActionExecutor.ActionResult result,
                                     List<ProblemZone> zones) {
        StringBuilder description = new StringBuilder();
        description.append("**Aktuelle TPS:** ").append(String.format("%.2f", currentTPS)).append("\n");
        description.append("**Warnstufe:** ").append(level.getDisplayName()).append("\n\n");

        description.append("**Durchgeführte Maßnahmen:**\n");
        if (result.itemsRemoved > 0) {
            description.append("• ").append(result.itemsRemoved).append(" Items entfernt\n");
        }
        if (result.mobsRemoved > 0) {
            description.append("• ").append(result.mobsRemoved).append(" Mobs entfernt\n");
        }
        if (result.redstoneDisabled > 0) {
            description.append("• Redstone in ").append(result.redstoneDisabled).append(" Chunks deaktiviert\n");
        }
        if (result.plantGrowthDisabled > 0) {
            description.append("• Pflanzenwachstum in ").append(result.plantGrowthDisabled).append(" Chunks gestoppt\n");
        }
        if (result.mobSpawningDisabled > 0) {
            description.append("• Mob-Spawning in ").append(result.mobSpawningDisabled).append(" Chunks deaktiviert\n");
        }
        description.append("• ").append(result.affectedChunks.size()).append(" Chunks betroffen\n\n");

        if (config.includeStatsInDiscord() && !zones.isEmpty()) {
            description.append("**Betroffene Bereiche:**\n");
            int count = Math.min(3, zones.size());
            for (int i = 0; i < count; i++) {
                ProblemZone zone = zones.get(i);
                description.append(formatZone(zone)).append("\n");
            }
        }

        int color = getColorForLevel(level);

        return String.format(
            "{\"embeds\":[{" +
            "\"title\":\"✅ Maßnahmen durchgeführt\"," +
            "\"description\":\"%s\"," +
            "\"color\":%d," +
            "\"timestamp\":\"%s\"," +
            "\"footer\":{\"text\":\"Redstone ItemClear\"}" +
            "}]}",
            escapeJson(description.toString()),
            color,
            Instant.now().toString()
        );
    }

    /**
     * Formatiert eine ProblemZone für die Anzeige
     */
    private String formatZone(ProblemZone zone) {
        StringBuilder sb = new StringBuilder();

        if (config.includeWorldInDiscord()) {
            sb.append("• **").append(zone.getWorld().getName()).append("** - ");
        }

        if (config.includeCoordinatesInDiscord()) {
            sb.append(zone.getCoordinates()).append(" - ");
        }

        sb.append("Probleme: ");
        if (zone.getProblems().isEmpty()) {
            sb.append("Keine");
        } else {
            sb.append(zone.getProblems().size());
            sb.append(" (");
            if (zone.getItemCount() > 0) sb.append(zone.getItemCount()).append(" Items, ");
            if (zone.getMobCount() > 0) sb.append(zone.getMobCount()).append(" Mobs, ");
            if (zone.getRedstoneActivity() > 0) sb.append(zone.getRedstoneActivity()).append(" Redstone");
            // Entferne letztes Komma wenn vorhanden
            String result = sb.toString();
            if (result.endsWith(", ")) {
                result = result.substring(0, result.length() - 2);
            }
            sb = new StringBuilder(result);
            sb.append(")");
        }

        return sb.toString();
    }

    /**
     * Gibt die Farbe für ein Level zurück
     */
    private int getColorForLevel(ActionLevel level) {
        switch (level) {
            case WARNING:
                return config.getDiscordColor("warning");
            case MODERATE:
            case SEVERE:
                return config.getDiscordColor("critical");
            case EMERGENCY:
                return config.getDiscordColor("emergency");
            default:
                return config.getDiscordColor("success");
        }
    }

    /**
     * Sendet einen Webhook
     */
    private void sendWebhook(String webhookUrl, String jsonPayload) throws IOException {
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "RedstoneItemClear/1.0");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            plugin.getLogger().warning("Discord Webhook fehlgeschlagen: HTTP " + responseCode);
        } else if (config.isDebugEnabled()) {
            plugin.getLogger().info("Discord Webhook erfolgreich gesendet");
        }

        connection.disconnect();
    }

    /**
     * Escaped JSON-Strings
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
