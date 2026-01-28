package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * Day/Night Cycle Overhaul - Day gives benefits, night is threatening
 */
public class DayNightOverhaulHandler {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickDayNight(player);
            }
        });
    }

    private static void tickDayNight(ServerPlayerEntity player) {
        // Get time of day from player's world
        long timeOfDay = player.getEntityWorld().getTimeOfDay() % 24000;

        // Day (5000-19000) - benefits
        if (timeOfDay > 5000 && timeOfDay < 19000) {
            // Day gives slight bonuses (faster regen, etc.)
            if (timeOfDay % 100 == 0) { // Every ~5 seconds
                // Give small regen bonus during day
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.REGENERATION, 20, 0, false, false
                ));
            }
        } else {
            // Night (19000-5000) - threats
            // Mobs spawn more aggressively, visibility reduced, threats increase
            // TODO: Implement night threat mechanics
        }
    }
}
