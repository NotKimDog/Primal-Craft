package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Environmental Hazards - Biomes are dangerous (cold, heat, altitude)
 */
public class EnvironmentHazardsHandler {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickHazards(player);
            }
        });
    }

    private static void tickHazards(ServerPlayerEntity player) {
        // Use TemperatureSystem to apply environmental hazards
        // Different biomes have unique dangers
        // Cold, heat, altitude, weather all affect player
        double temp = net.kaupenjoe.tutorialmod.util.TemperatureSystem.getEffectiveTemperature(player);

        // Temperature damage is handled by TemperatureSystem
        // This handler can be expanded with additional hazard mechanics
        // TODO: Implement additional environmental hazards (altitude, altitude sickness, etc.)
    }
}
