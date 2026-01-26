package net.kaupenjoe.tutorialmod.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced stamina system with:
 * - Fatigue/rest mechanics
 * - Dynamic max stamina based on inventory weight
 * - Stamina regeneration rate tracking
 * - Critical stamina states
 */
public final class EnhancedStaminaManager {
    private static final double MAX_STAMINA = 100.0;
    private static final double REGEN_PER_TICK = 0.2;
    private static final double FATIGUE_PENALTY_PER_MINUTE = 0.5; // Reduces max stamina over time without rest
    private static final double FATIGUE_MAX_PENALTY = 30.0; // Can reduce max stamina by up to 30%
    private static final double WEIGHT_MAX_REDUCTION = 20.0; // Heavy items can reduce max stamina by 20%

    private static final Map<UUID, Double> STAMINA = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> FATIGUE = new ConcurrentHashMap<>(); // Fatigue level (0-100)
    private static final Map<UUID, Integer> LAST_REST_TICK = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> STAMINA_DRAIN_TRACKER = new ConcurrentHashMap<>(); // Track recent drains for notifications

    private EnhancedStaminaManager() {}

    /**
     * Get current stamina for a player
     */
    public static double getStamina(ServerPlayerEntity player) {
        return STAMINA.getOrDefault(player.getUuid(), MAX_STAMINA);
    }

    /**
     * Get maximum stamina (affected by weight and fatigue)
     */
    public static double getMaxStamina(ServerPlayerEntity player) {
        double weight = ItemWeightSystem.calculateInventoryWeightPenalty(player);
        double weightReduction = Math.min(WEIGHT_MAX_REDUCTION, weight * 2.0);

        double fatigue = FATIGUE.getOrDefault(player.getUuid(), 0.0);
        double fatigueReduction = (fatigue / 100.0) * FATIGUE_MAX_PENALTY;

        return MAX_STAMINA - weightReduction - fatigueReduction;
    }

    /**
     * Try to consume stamina
     */
    public static boolean tryConsume(ServerPlayerEntity player, double amount) {
        UUID id = player.getUuid();
        double current = getStamina(player);
        double max = getMaxStamina(player);

        if (current < amount) {
            return false;
        }

        STAMINA.put(id, Math.max(0, current - amount));
        STAMINA_DRAIN_TRACKER.put(id, (int) amount); // Track for HUD notification

        // Update last activity for fatigue system
        LAST_REST_TICK.put(id, 0);

        return true;
    }

    /**
     * Restore stamina (for items/effects)
     */
    public static void restoreStamina(ServerPlayerEntity player, double amount) {
        UUID id = player.getUuid();
        double current = getStamina(player);
        double max = getMaxStamina(player);
        STAMINA.put(id, Math.min(max, current + amount));

        // Reduce fatigue when resting
        double fatigue = FATIGUE.getOrDefault(id, 0.0);
        FATIGUE.put(id, Math.max(0, fatigue - (amount * 0.5))); // Rest reduces fatigue
        LAST_REST_TICK.put(id, 0);
    }

    /**
     * Tick update - regenerate stamina, apply fatigue
     */
    public static void tick(ServerPlayerEntity player) {
        UUID id = player.getUuid();

        // Regenerate stamina
        double current = getStamina(player);
        double max = getMaxStamina(player);
        STAMINA.put(id, Math.min(max, current + REGEN_PER_TICK));

        // Update fatigue (increases when player doesn't rest)
        int restTicks = LAST_REST_TICK.getOrDefault(id, 0) + 1;
        LAST_REST_TICK.put(id, restTicks);

        double fatigue = FATIGUE.getOrDefault(id, 0.0);
        // Fatigue increases after 30 seconds (600 ticks) without activity
        if (restTicks > 600) {
            double fatigueIncrease = FATIGUE_PENALTY_PER_MINUTE / 1200.0; // Per tick increase
            fatigue = Math.min(100.0, fatigue + fatigueIncrease);
            FATIGUE.put(id, fatigue);
        }
    }

    /**
     * Get fatigue level (0-100)
     */
    public static double getFatigue(ServerPlayerEntity player) {
        return FATIGUE.getOrDefault(player.getUuid(), 0.0);
    }

    /**
     * Check if stamina is critical (below 20%)
     */
    public static boolean isCritical(ServerPlayerEntity player) {
        double stamina = getStamina(player);
        double max = getMaxStamina(player);
        return (stamina / max) < 0.2;
    }

    /**
     * Check if player is fatigued (above 50%)
     */
    public static boolean isFatigued(ServerPlayerEntity player) {
        return getFatigue(player) > 50.0;
    }

    /**
     * Get recent drain amount for notification
     */
    public static int getRecentDrain(ServerPlayerEntity player) {
        return STAMINA_DRAIN_TRACKER.getOrDefault(player.getUuid(), 0);
    }

    /**
     * Clear recent drain tracker
     */
    public static void clearRecentDrain(ServerPlayerEntity player) {
        STAMINA_DRAIN_TRACKER.remove(player.getUuid());
    }
}
