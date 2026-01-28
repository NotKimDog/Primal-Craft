package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.kimdog_studios.primal_craft.util.LoggingHelper;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Environmental Hazards - Biomes are dangerous (cold, heat, altitude)
 * Tracks environmental conditions and their effects on players
 */
public class EnvironmentHazardsHandler {
    private static final Map<UUID, String> lastBiome = new HashMap<>();
    private static final Map<UUID, Integer> lastAltitude = new HashMap<>();
    private static final Map<UUID, String> lastHazardLevel = new HashMap<>();
    private static int hazardTicks = 0;
    private static int biomeChangeEvents = 0;
    private static int hazardWarnings = 0;
    private static int altitudeWarnings = 0;

    public static void register() {
        LoggingHelper.logSystemInit("[ENVIRONMENT_HAZARDS]");
        LoggingHelper.logSubsection("Temperature hazard system with damage threshold");
        LoggingHelper.logSubsection("Altitude hazard detection (high altitude effects)");
        LoggingHelper.logSubsection("Weather hazard tracking and alerts");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Check if environmental hazards are enabled
            if (!PrimalCraftConfig.getGameplay().environmentalHazardsEnabled) {
                return; // Skip processing if disabled
            }

            hazardTicks++;
            int playerCount = server.getPlayerManager().getPlayerList().size();

            if (hazardTicks % 200 == 0) {
                PrimalCraft.LOGGER.info("📊 [HAZARD_STATS] Tick #{} - Players: {} | Biome Changes: {} | Warnings: {} | Altitude: {}",
                    hazardTicks, playerCount, biomeChangeEvents, hazardWarnings, altitudeWarnings);
            }

            if (hazardTicks % 100 == 0) {
                PrimalCraft.LOGGER.trace("⏱️  [HAZARD_TICK] Tick #{} - Processing {} players for hazards",
                    hazardTicks, playerCount);
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickHazards(player);
            }
        });

        PrimalCraft.LOGGER.info("✅ [ENVIRONMENT_HAZARDS] EnvironmentHazardsHandler registered successfully");
    }

    private static void tickHazards(ServerPlayerEntity player) {
        UUID id = player.getUuid();

        // Use TemperatureSystem to apply environmental hazards
        double temp = net.kimdog_studios.primal_craft.util.TemperatureSystem.getEffectiveTemperature(player);
        String biome = player.getEntityWorld().getBiome(player.getBlockPos()).getKey().map(k -> k.getValue().getPath()).orElse("unknown");
        String previousBiome = lastBiome.getOrDefault(id, biome);
        int altitude = (int) player.getY();
        int lastAlt = lastAltitude.getOrDefault(id, altitude);

        // Log biome changes
        if (!biome.equals(previousBiome)) {
            biomeChangeEvents++;
            PrimalCraft.LOGGER.debug("🌍 [BIOME_CHANGE] Event #{}: {} entered: {} from {}",
                biomeChangeEvents, player.getName().getString(), biome, previousBiome);
            PrimalCraft.LOGGER.trace("   ├─ Temperature: {}°C", String.format("%.1f", temp));
            PrimalCraft.LOGGER.trace("   ├─ Altitude: {} blocks (change: {:+d})",  altitude, altitude - lastAlt);
            PrimalCraft.LOGGER.trace("   └─ Assessing environmental hazards...");

            LoggingHelper.trackStateChange(
                "biome_" + id,
                previousBiome,
                biome
            );

            lastBiome.put(id, biome);
        }

        // Temperature hazards
        String hazardLevel = getHazardLevel(temp);
        String lastHazard = lastHazardLevel.getOrDefault(id, hazardLevel);

        if (!hazardLevel.equals(lastHazard)) {
            hazardWarnings++;
            String emoji = getHazardEmoji(hazardLevel);

            PrimalCraft.LOGGER.warn("{} [HAZARD_CHANGE] Event #{}: {} - {} ({}°C) [Changed from: {}]",
                emoji, hazardWarnings, player.getName().getString(),
                hazardLevel, String.format("%.1f", temp), lastHazard);

            LoggingHelper.trackStateChange(
                "hazard_" + id,
                lastHazard + " at " + String.format("%.1f", temp) + "°C",
                hazardLevel + " at " + String.format("%.1f", temp) + "°C"
            );

            lastHazardLevel.put(id, hazardLevel);
        }

        // Periodic hazard status
        if (hazardTicks % 300 == 0) {
            PrimalCraft.LOGGER.trace("⚠️  [HAZARD_STATUS] {} - Level: {} | Temp: {}°C | Altitude: {} | Biome: {}",
                player.getName().getString(), hazardLevel, String.format("%.1f", temp), altitude, biome);
        }

        // Altitude hazards
        if (Math.abs(altitude - lastAlt) > 20) {
            altitudeWarnings++;
            String altitudeWarning = "";
            if (altitude > 250) {
                altitudeWarning = "EXTREME HEIGHT (>250 blocks)";
            } else if (altitude > 200) {
                altitudeWarning = "HIGH ALTITUDE (>200 blocks)";
            } else if (altitude > 150) {
                altitudeWarning = "ELEVATED (>150 blocks)";
            } else if (altitude < -50) {
                altitudeWarning = "EXTREME DEPTH (<-50 blocks)";
            } else if (altitude < 0) {
                altitudeWarning = "DEEP UNDERGROUND";
            }

            if (!altitudeWarning.isEmpty()) {
                PrimalCraft.LOGGER.debug("📈 [ALTITUDE_CHANGE] Event #{}: {} {} | Altitude: {} ({:+d})",
                    altitudeWarnings, player.getName().getString(), altitudeWarning,
                    altitude, altitude - lastAlt);
            }

            lastAltitude.put(id, altitude);
        }
    }

    private static String getHazardLevel(double temp) {
        if (temp < -35) return "DEADLY_COLD";
        else if (temp < -20) return "EXTREME_COLD";
        else if (temp < -10) return "SEVERE_COLD";
        else if (temp < 5) return "COLD";
        else if (temp >= 15 && temp <= 25) return "COMFORTABLE";
        else if (temp > 25 && temp <= 35) return "WARM";
        else if (temp > 35 && temp <= 45) return "HOT";
        else if (temp > 45 && temp <= 55) return "SEVERE_HEAT";
        else return "DEADLY_HEAT";
    }

    private static String getHazardEmoji(String hazardLevel) {
        return switch(hazardLevel) {
            case "DEADLY_COLD" -> "❄️";
            case "EXTREME_COLD" -> "🥶";
            case "SEVERE_COLD" -> "❄️";
            case "COLD" -> "🧊";
            case "COMFORTABLE" -> "✅";
            case "WARM" -> "☀️";
            case "HOT" -> "🔥";
            case "SEVERE_HEAT" -> "🔥";
            case "DEADLY_HEAT" -> "💥";
            default -> "⚠️";
        };
    }
}
