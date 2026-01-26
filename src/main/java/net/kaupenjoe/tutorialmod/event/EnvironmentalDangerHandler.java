package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Environmental hazards - random events and environmental effects
 * Cold biomes, high altitude, and storms cause effects
 */
public class EnvironmentalDangerHandler {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                tickEnvironmentalHazards(player);
            });
        });
    }

    private static void tickEnvironmentalHazards(ServerPlayerEntity player) {
        var biome = ((ServerWorld) player.getEntityWorld()).getBiome(player.getBlockPos()).value();
        float biomeTemp = biome.getTemperature();
        double playerY = player.getY();
        boolean isRaining = ((ServerWorld) player.getEntityWorld()).isRaining();

        // Cold biomes - slowness
        if(biomeTemp < -0.5f) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 0, false, false));
        }

        // High altitude - darkness effect
        if(playerY > 200) {
            if(player.age % 60 == 0) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0, true, false));
            }
        }

        // Rain + Low temperature = hypothermia risk
        if(isRaining && biomeTemp < 0.2f) {
            if(player.age % 100 == 0) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1, false, false));
                // Increase fatigue from cold
                EnhancedFatigueHandler.playerFatigue.put(player,
                    Math.min(100, EnhancedFatigueHandler.playerFatigue.getOrDefault(player, 0) + 3));
            }
        }

        // Random environmental events (20% chance every 2 minutes)
        if(player.age % 2400 == 0 && Math.random() < 0.2) {
            triggerRandomEvent(player);
        }
    }

    private static void triggerRandomEvent(ServerPlayerEntity player) {
        int eventType = (int)(Math.random() * 3);

        switch(eventType) {
            case 0:
                // Lightning hazard warning
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, 300, 1, false, false));
                break;
            case 1:
                // Tremor effect
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, false, false));
                break;
            case 2:
                // Darkness falls
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 200, 0, false, false));
                break;
        }
    }
}
