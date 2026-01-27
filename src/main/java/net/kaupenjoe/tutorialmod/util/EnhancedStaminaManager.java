package net.kaupenjoe.tutorialmod.util;

import net.kaupenjoe.tutorialmod.TutorialMod;
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
    private static final double FATIGUE_PENALTY_PER_MINUTE = 0.5;
    private static final double FATIGUE_MAX_PENALTY = 30.0;
    private static final double WEIGHT_MAX_REDUCTION = 20.0;

    private static final Map<UUID, Double> STAMINA = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> FATIGUE = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> LAST_REST_TICK = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> STAMINA_DRAIN_TRACKER = new ConcurrentHashMap<>();

    private static int consumes = 0;
    private static int restorations = 0;
    private static int ticks = 0;

    private EnhancedStaminaManager() {}

    /**
     * Get current stamina for a player
     */
    public static double getStamina(ServerPlayerEntity player) {
        double stamina = STAMINA.getOrDefault(player.getUuid(), MAX_STAMINA);
        TutorialMod.LOGGER.trace("📊 [STAMINA_GET] {} current: {}/{}",
            player.getName().getString(), String.format("%.1f", stamina), MAX_STAMINA);
        return stamina;
    }

    /**
     * Get maximum stamina (affected by weight and fatigue)
     */
    public static double getMaxStamina(ServerPlayerEntity player) {
        double weight = ItemWeightSystem.calculateInventoryWeightPenalty(player);
        double weightReduction = Math.min(WEIGHT_MAX_REDUCTION, weight * 2.0);
        UUID id = player.getUuid();
        double fatigue = FATIGUE.getOrDefault(id, 0.0);
        double fatigueReduction = (fatigue / 100.0) * FATIGUE_MAX_PENALTY;
        double maxStamina = MAX_STAMINA - weightReduction - fatigueReduction;

        TutorialMod.LOGGER.trace("📊 [MAX_STAMINA] {} max: {}", player.getName().getString(), String.format("%.1f", maxStamina));
        TutorialMod.LOGGER.trace("   ├─ Base: {}", MAX_STAMINA);
        TutorialMod.LOGGER.trace("   ├─ Weight reduction: {}", String.format("%.1f", weightReduction));
        TutorialMod.LOGGER.trace("   ├─ Fatigue: {} (reduction: {})", String.format("%.1f", fatigue), String.format("%.1f", fatigueReduction));
        TutorialMod.LOGGER.trace("   └─ Final max: {}", String.format("%.1f", maxStamina));

        return maxStamina;
    }

    /**
     * Try to consume stamina
     */
    public static boolean tryConsume(ServerPlayerEntity player, double amount) {
        UUID id = player.getUuid();
        double current = getStamina(player);
        double max = getMaxStamina(player);
        consumes++;

        TutorialMod.LOGGER.debug("💸 [CONSUME] Event #{} - {} consuming {}",
            consumes, player.getName().getString(), String.format("%.2f", amount));
        TutorialMod.LOGGER.trace("   ├─ Current: {}/{}", String.format("%.1f", current), String.format("%.1f", max));
        TutorialMod.LOGGER.trace("   ├─ Required: {}", String.format("%.2f", amount));

        if (current < amount) {
            TutorialMod.LOGGER.trace("   └─ ✗ FAILED - Deficit: {}", String.format("%.2f", amount - current));
            return false;
        }

        double newStamina = Math.max(0, current - amount);
        STAMINA.put(id, newStamina);
        STAMINA_DRAIN_TRACKER.put(id, (int) amount);
        LAST_REST_TICK.put(id, 0);

        TutorialMod.LOGGER.trace("   └─ ✓ SUCCESS - {} → {}", String.format("%.1f", current), String.format("%.1f", newStamina));

        return true;
    }

    /**
     * Restore stamina (for items/effects)
     */
    public static void restoreStamina(ServerPlayerEntity player, double amount) {
        UUID id = player.getUuid();
        double current = getStamina(player);
        double max = getMaxStamina(player);
        restorations++;

        TutorialMod.LOGGER.debug("💚 [RESTORE] Event #{} - {} restoring {}",
            restorations, player.getName().getString(), String.format("%.2f", amount));
        TutorialMod.LOGGER.trace("   ├─ Before: {}/{}", String.format("%.1f", current), String.format("%.1f", max));

        double newStamina = Math.min(max, current + amount);
        STAMINA.put(id, newStamina);

        // Reduce fatigue when resting
        double fatigue = FATIGUE.getOrDefault(id, 0.0);
        double newFatigue = Math.max(0, fatigue - (amount * 0.5));
        FATIGUE.put(id, newFatigue);

        TutorialMod.LOGGER.trace("   ├─ After: {}/{}", String.format("%.1f", newStamina), String.format("%.1f", max));
        TutorialMod.LOGGER.trace("   ├─ Fatigue: {} → {} (reduced by {})",
            String.format("%.1f", fatigue), String.format("%.1f", newFatigue), String.format("%.1f", amount * 0.5));
        TutorialMod.LOGGER.trace("   └─ ✓ Restoration applied");

        LAST_REST_TICK.put(id, 0);
    }

    /**
     * Tick update - regenerate stamina, apply fatigue
     */
    public static void tick(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        ticks++;

        if (ticks % 100 == 0) {
            TutorialMod.LOGGER.trace("⏱️  [TICK] Tick #{} for {}", ticks, player.getName().getString());
        }

        // Regenerate stamina
        double current = getStamina(player);
        double max = getMaxStamina(player);
        double newStamina = Math.min(max, current + REGEN_PER_TICK);
        STAMINA.put(id, newStamina);

        if (ticks % 100 == 0) {
            TutorialMod.LOGGER.trace("   ├─ Stamina regen: {} + {} = {}",
                String.format("%.1f", current), REGEN_PER_TICK, String.format("%.1f", newStamina));
        }

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
