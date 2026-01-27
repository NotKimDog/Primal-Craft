package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Environmental Hazards - Biomes are dangerous (cold, heat, altitude)
 */
public class EnvironmentHazardsHandler {
    private static final Map<UUID, String> lastBiome = new HashMap<>();
    private static int hazardTicks = 0;

    public static void register() {
        TutorialMod.LOGGER.info("⚠️  [ENVIRONMENT_HAZARDS] Registering EnvironmentHazardsHandler");
        TutorialMod.LOGGER.debug("   ├─ Temperature hazard system");
        TutorialMod.LOGGER.debug("   ├─ Altitude hazard detection");
        TutorialMod.LOGGER.debug("   └─ Weather hazard tracking");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            hazardTicks++;
            if (hazardTicks % 100 == 0) {
                TutorialMod.LOGGER.trace("⏱️  [HAZARD_TICK] Tick #{} - Processing {} players",
                    hazardTicks, server.getPlayerManager().getPlayerList().size());
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickHazards(player);
            }
        });

        TutorialMod.LOGGER.info("✅ [ENVIRONMENT_HAZARDS] Handler registered");
    }

    private static void tickHazards(ServerPlayerEntity player) {
        UUID id = player.getUuid();

        // Use TemperatureSystem to apply environmental hazards
        double temp = net.kaupenjoe.tutorialmod.util.TemperatureSystem.getEffectiveTemperature(player);
        String biome = player.getEntityWorld().getBiome(player.getBlockPos()).getKey().toString();
        String previousBiome = lastBiome.getOrDefault(id, biome);

        // Log biome changes
        if (!biome.equals(previousBiome)) {
            TutorialMod.LOGGER.debug("🌍 [BIOME] {} entered: {}",
                player.getName().getString(), biome);
            TutorialMod.LOGGER.trace("   ├─ Temperature: {}°C", String.format("%.1f", temp));
            TutorialMod.LOGGER.trace("   ├─ Altitude: {} blocks", Math.round(player.getY()));
            TutorialMod.LOGGER.trace("   └─ Assessing hazards...");
            lastBiome.put(id, biome);
        }

        // Temperature hazards
        if (hazardTicks % 100 == 0) {
            String hazardLevel = "SAFE";
            if (temp < -10) hazardLevel = "EXTREME COLD";
            else if (temp < 5) hazardLevel = "COLD";
            else if (temp >= 15 && temp <= 25) hazardLevel = "COMFORTABLE";
            else if (temp > 35 && temp <= 45) hazardLevel = "HOT";
            else if (temp > 45) hazardLevel = "EXTREME HEAT";

            TutorialMod.LOGGER.trace("   ⚠️  [HAZARD] {} {} - Temp: {}°C",
                player.getName().getString(), hazardLevel, String.format("%.1f", temp));
        }

        // Altitude hazards
        int altitude = (int) player.getY();
        if (altitude > 200) {
            if (hazardTicks % 100 == 0) {
                TutorialMod.LOGGER.trace("   ⚠️  [ALTITUDE] {} at high altitude: {} blocks",
                    player.getName().getString(), altitude);
            }
        }
    }
}
