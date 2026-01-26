package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Day/Night intensity system - affects recovery and player abilities
 * Night: reduced regen, increased stamina drain
 * Day: better recovery, stamina restoration
 */
public class DayNightCycleHandler {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                tickDayNightEffects(player);
            });
        });
    }

    private static void tickDayNightEffects(ServerPlayerEntity player) {
        long timeOfDay = ((ServerWorld) player.getEntityWorld()).getTimeOfDay() % 24000;
        boolean isDay = timeOfDay > 0 && timeOfDay < 12000;
        boolean isTwilight = (timeOfDay > 11000 && timeOfDay < 13000) || (timeOfDay > 22000 && timeOfDay < 24000);

        if(isTwilight) {
            // Twilight is disorienting
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 0, true, false));
        }

        if(!isDay) {
            // Night: reduced health regen, fatigue increases faster
            if(player.age % 60 == 0) {
                EnhancedFatigueHandler.playerFatigue.put(player,
                    Math.min(100, EnhancedFatigueHandler.playerFatigue.getOrDefault(player, 0) + 2));
            }

            // Ambient danger effect at night
            if(player.age % 80 == 0 && ((ServerWorld) player.getEntityWorld()).getLightLevel(player.getBlockPos()) < 7) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 10, 0, true, false));
            }
        } else {
            // Day: better recovery
            if(player.age % 100 == 0) {
                // Reduce fatigue during day
                EnhancedFatigueHandler.playerFatigue.put(player,
                    Math.max(0, EnhancedFatigueHandler.playerFatigue.getOrDefault(player, 0) - 3));
            }
        }
    }
}
