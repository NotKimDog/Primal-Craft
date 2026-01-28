package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Threat System - Mobs and darkness create real danger
 */
public class ThreatSystemHandler {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickThreats(player);
            }
        });
    }

    private static void tickThreats(ServerPlayerEntity player) {
        // Nearby hostile mobs increase threat level
        // Darkness increases threat (low light level = danger)
        // TODO: Implement threat tracking mechanics
        // This should affect mob behavior and player effects
    }
}
