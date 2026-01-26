package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Proximity awareness - players sense nearby threats
 * More mobs = stronger warning effects
 */
public class ProximityThreatHandler {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                tickProximityThreat(player);
            });
        });
    }

    private static void tickProximityThreat(ServerPlayerEntity player) {
        // Count hostile entities within 40 blocks
        long nearbyHostiles = ((ServerWorld) player.getEntityWorld()).getOtherEntities(player, player.getBoundingBox().expand(40))
            .stream()
            .filter(e -> e instanceof HostileEntity)
            .count();

        if(nearbyHostiles > 0) {
            // Apply bad omen based on threat count
            int amplifier = (int)Math.min(2, nearbyHostiles - 1);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, 60, amplifier, false, false));

            // At night, amplify threat perception
            long timeOfDay = ((ServerWorld) player.getEntityWorld()).getTimeOfDay() % 24000;
            if(timeOfDay > 13000 || timeOfDay < 11000) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 30, 0, true, false));
            }
        }
    }
}
