package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Enhanced Fatigue system - players get tired from constant activity
 * Fatigue reduces mining speed, attack speed, and causes slowness
 */
public class EnhancedFatigueHandler {
    public static final java.util.Map<ServerPlayerEntity, Integer> playerFatigue = new java.util.WeakHashMap<>();
    private static final int MAX_FATIGUE = 100;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                tickFatigue(player);
            });
        });
    }

    private static void tickFatigue(ServerPlayerEntity player) {
        int fatigue = playerFatigue.getOrDefault(player, 0);

        // Increase fatigue from activity
        if(player.isSprinting()) fatigue += 2;
        if(player.isClimbing()) fatigue += 2;
        if(player.isSwimming()) fatigue += 1;

        // Decrease fatigue at rest (moving slowly or standing)
        if(player.getVelocity().length() < 0.05) fatigue = Math.max(0, fatigue - 1);

        fatigue = Math.min(MAX_FATIGUE, Math.max(0, fatigue));
        playerFatigue.put(player, fatigue);

        // Apply effects based on fatigue level
        if(fatigue > 80) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 1, false, false));
        } else if(fatigue > 50) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 0, false, false));
        }
    }

    public static int getFatigue(ServerPlayerEntity player) {
        return playerFatigue.getOrDefault(player, 0);
    }
}
