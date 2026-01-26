package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kaupenjoe.tutorialmod.util.TemperatureSystem;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies direct effects to players based on temperature
 */
public final class TemperatureEffectsHandler {
    private static final Map<UUID, Integer> playerTicks = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int serverTick = server.getTicks();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                double temperature = TemperatureSystem.getPlayerTemperature(player);
                applyTemperatureEffects(player, temperature, serverTick);

                // Check weather notifications every 5 seconds
                if (serverTick % 100 == 0) {
                    net.kaupenjoe.tutorialmod.util.WeatherNotificationSystem.checkAndNotify(player);
                }
            }
        });
    }

    /**
     * Apply status effects and damage based on extreme temperatures
     */
    private static void applyTemperatureEffects(ServerPlayerEntity player, double temperature, int serverTick) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // EXTREME COLD effects (<-10°C)
        if (temperature < -10) {
            // Freezing - slowness and mining fatigue
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 0, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 100, 0, false, false, false));

            // Very cold - damage over time
            if (temperature < -20 && serverTick % 40 == 0) { // Every 2 seconds
                player.damage(world, player.getDamageSources().freeze(), 1.0f);
            }
        }
        // VERY COLD effects (-10°C to 5°C)
        else if (temperature < 5) {
            // Slight slowness
            if (serverTick % 60 == 0) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0, false, false, false));
            }
        }
        // COMFORTABLE range (15°C to 25°C)
        else if (temperature >= 15 && temperature <= 25) {
            // Give slight regeneration bonus when comfortable
            if (serverTick % 100 == 0) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 0, false, false, false));
            }
        }
        // HOT effects (35°C to 45°C)
        else if (temperature > 35 && temperature <= 45) {
            // Heat exhaustion - hunger and weakness
            if (serverTick % 80 == 0) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 80, 0, false, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 0, false, false, false));
            }
        }
        // EXTREME HEAT effects (>45°C)
        else if (temperature > 45) {
            // Severe heat - nausea, weakness, hunger
            //player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1, false, false, false));
            //player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 100, 1, false, false, false));

            // Very hot - damage over time
            if (temperature > 55 && serverTick % 40 == 0) { // Every 2 seconds
                player.damage(world, player.getDamageSources().onFire(), 1.0f);
            }
        }

        // LAVA HEAT (>100°C) - severe damage
        if (temperature > 100 && !player.isInLava()) {
            // Near lava but not in it
            if (serverTick % 20 == 0) { // Every second
                player.damage(world, player.getDamageSources().hotFloor(), 2.0f);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, 0, false, false, false));
            }
        }

        // IN LAVA (>800°C) - extreme damage (handled separately by game but amplified)
        if (temperature > 800) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 20, 2, false, false, false));
        }
    }
}
