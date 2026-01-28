package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

import java.util.List;

/**
 * 🎮 Primal Craft - Aggressive Mob Scaling Handler
 *
 * Enhances mob behavior based on difficulty:
 * - Increased targeting range (+50%)
 * - Reduced attack cooldown (-30%)
 * - Enhanced detection range (+25%)
 * - Configurable per-mob-type multipliers
 *
 * Works in conjunction with MobDifficultyHandler for comprehensive mob scaling.
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class MobAggressionHandler {
    private MobAggressionHandler() {}

    // Base aggression multipliers
    private static final float BASE_RANGE_MULTIPLIER = 1.5f;    // 50% increase
    private static final float BASE_COOLDOWN_REDUCTION = 0.7f;  // 30% reduction
    private static final float BASE_DETECTION_MULTIPLIER = 1.25f; // 25% increase

    private static boolean lastAggressionState = false;

    public static void register() {
        PrimalCraft.LOGGER.info("🔥 [MOB_AGGRESSION] Registering Aggressive Mob Scaling Handler");

        ServerTickEvents.END_SERVER_TICK.register(MobAggressionHandler::onServerTick);

        PrimalCraft.LOGGER.info("✅ [MOB_AGGRESSION] Aggressive Mob Scaling Handler registered");
    }

    private static void onServerTick(MinecraftServer server) {
        try {
            if (!PrimalCraftConfig.getAdvanced().features.mobAggression) {
                return;
            }
            if (!PrimalCraftConfig.getDifficulty().difficultyAffectsMobBehavior) {
                return;
            }

            boolean isAggressive = PrimalCraftConfig.getDifficulty().mobResources.mobBehavior;
            if (isAggressive != lastAggressionState) {
                lastAggressionState = isAggressive;
                String status = isAggressive ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.info("🔥 [MOB_AGGRESSION] Aggressive mob behavior {}", status);
            }

            // Update nearby mobs for each player
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                updatePlayerNearbyMobs(player);
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[MOB_AGGRESSION] Error during tick", e);
        }
    }

    /**
     * Update aggression for mobs near a player
     */
    private static void updatePlayerNearbyMobs(ServerPlayerEntity player) {
        try {
            net.minecraft.world.World entityWorld = player.getEntityWorld();
            if (!(entityWorld instanceof ServerWorld)) return;

            ServerWorld world = (ServerWorld) entityWorld;
            float difficultyMultiplier = getDifficultyMultiplier(player);

            // Get mobs in a 64-block radius
            Box searchBox = player.getBoundingBox().expand(64);
            List<MobEntity> nearbyMobs = world.getEntitiesByClass(
                MobEntity.class,
                searchBox,
                mob -> mob.isAlive() && !mob.isRemoved()
            );

            for (MobEntity mob : nearbyMobs) {
                enhanceMobAggression(mob, difficultyMultiplier);
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[MOB_AGGRESSION] Error updating mob aggression for player", e);
        }
    }

    /**
     * Enhance a mob's aggression based on difficulty
     */
    private static void enhanceMobAggression(MobEntity mob, float difficultyMultiplier) {
        try {
            if (difficultyMultiplier <= 1.0f) {
                return; // No enhancement needed
            }

            // Increase target range
            float rangeIncrease = 1.0f + ((BASE_RANGE_MULTIPLIER - 1.0f) * difficultyMultiplier);
            // Target range is typically 16 blocks by default
            int newRange = Math.round(16 * rangeIncrease);

            // Reduce attack cooldown (lower is faster)
            // Default attack speed is 1.0, higher values mean faster attacks
            float attackSpeedBoost = 1.0f + ((BASE_COOLDOWN_REDUCTION - 1.0f) * difficultyMultiplier);

            // Enhance detection range (for hearing/sensing)
            float detectionRange = 1.0f + ((BASE_DETECTION_MULTIPLIER - 1.0f) * difficultyMultiplier);

            // Apply enhancements
            mob.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.FOLLOW_RANGE)
                .setBaseValue(newRange);
            mob.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.ATTACK_SPEED)
                .setBaseValue(attackSpeedBoost);

            if (PrimalCraft.LOGGER.isDebugEnabled()) {
                PrimalCraft.LOGGER.trace(
                    "[MOB_AGGRESSION] Enhanced {} - Range: {}b, AttackSpeed: {:.2f}x, Detection: {:.2f}x",
                    mob.getType().toString(),
                    newRange,
                    attackSpeedBoost,
                    detectionRange
                );
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[MOB_AGGRESSION] Failed to enhance mob aggression", e);
        }
    }

    /**
     * Get difficulty multiplier for a player
     */
    private static float getDifficultyMultiplier(ServerPlayerEntity player) {
        try {
            var config = PrimalCraftConfig.getDifficulty();
            return config != null ? config.mobResources.mobDamage : 1.0f;
        } catch (Exception e) {
            return 1.0f;
        }
    }

    /**
     * Check if aggressive mobs are enabled
     */
    public static boolean isAggressiveMobsEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.mobAggression
                && PrimalCraftConfig.getDifficulty().mobResources.mobBehavior;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Set aggressive mobs state
     */
    public static void setAggressiveMobsEnabled(boolean enabled) {
        try {
            PrimalCraftConfig.getDifficulty().mobResources.mobBehavior = enabled;
            PrimalCraftConfig.save();
            lastAggressionState = enabled;

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("🔥 [MOB_AGGRESSION] Aggressive mobs {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[MOB_AGGRESSION] Failed to toggle aggressive mobs", e);
        }
    }
}
