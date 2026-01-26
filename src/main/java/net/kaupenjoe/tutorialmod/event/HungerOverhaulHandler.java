package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Hunger Overhaul - Makes food meaningful, faster depletion, affects healing
 */
public class HungerOverhaulHandler {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickHunger(player);
            }
        });
    }

    private static void tickHunger(ServerPlayerEntity player) {
        // Hunger depletion is faster than vanilla
        // Food is more meaningful for healing and stamina restoration
        // TODO: Implement custom hunger mechanics
    }
}
