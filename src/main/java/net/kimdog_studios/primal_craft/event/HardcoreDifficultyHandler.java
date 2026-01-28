package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.kimdog_studios.primal_craft.util.DifficultySystem;

/**
 * 🎮 Primal Craft - Hardcore/World Difficulty Handler
 *
 * Ensures mob difficulty scales based on:
 * - World difficulty setting (Peaceful, Easy, Normal, Hard)
 * - Hardcore mode flag
 * - Dimension-specific multipliers
 *
 * Applies additional scaling for hardcore mode (2.5x multiplier).
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class HardcoreDifficultyHandler {
    private HardcoreDifficultyHandler() {}

    // Difficulty multipliers based on world difficulty
    private static final float PEACEFUL_MULTIPLIER = 0.0f;    // No mobs
    private static final float EASY_MULTIPLIER = 0.5f;       // 50% difficulty
    private static final float NORMAL_MULTIPLIER = 1.0f;     // 100% difficulty
    private static final float HARD_MULTIPLIER = 1.5f;       // 150% difficulty
    private static final float HARDCORE_ADDITIONAL_MULTIPLIER = 2.5f; // 250% total in hardcore

    private static boolean lastHardcoreState = false;
    private static Difficulty lastDifficulty = Difficulty.NORMAL;

    public static void register() {
        PrimalCraft.LOGGER.info("🏆 [HARDCORE_DIFFICULTY] Registering Hardcore Mode Handler");

        ServerTickEvents.END_SERVER_TICK.register(HardcoreDifficultyHandler::onServerTick);

        PrimalCraft.LOGGER.info("✅ [HARDCORE_DIFFICULTY] Handler registered successfully");
    }

    private static void onServerTick(MinecraftServer server) {
        try {
            if (!PrimalCraftConfig.getAdvanced().features.hardcoreDifficulty) {
                return;
            }
            if (!PrimalCraftConfig.getDifficulty().isDifficultySystemEnabled) {
                return;
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                updatePlayerDifficultyScaling(player);
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[HARDCORE_DIFFICULTY] Error during tick", e);
        }
    }

    /**
     * Update difficulty scaling for a player based on world difficulty and hardcore mode
     */
    private static void updatePlayerDifficultyScaling(ServerPlayerEntity player) {
        try {
            net.minecraft.world.World entityWorld = player.getEntityWorld();
            if (!(entityWorld instanceof ServerWorld)) return;

            ServerWorld world = (ServerWorld) entityWorld;
            Difficulty worldDifficulty = world.getDifficulty();
            boolean isHardcore = world.getLevelProperties().isHardcore();

            // Calculate base multiplier from world difficulty
            float baseMultiplier = getDifficultyMultiplier(worldDifficulty);

            // Apply hardcore modifier if in hardcore mode
            float finalMultiplier = isHardcore ? baseMultiplier * HARDCORE_ADDITIONAL_MULTIPLIER : baseMultiplier;

            // Apply multiplier to difficulty system
            applyDifficultyMultiplier(player, finalMultiplier, isHardcore);

            // Log if difficulty changed
            if (isHardcore != lastHardcoreState || worldDifficulty != lastDifficulty) {
                lastHardcoreState = isHardcore;
                lastDifficulty = worldDifficulty;

                String difficultyName = worldDifficulty.getName();
                String modeString = isHardcore ? "HARDCORE" : "SURVIVAL";
                PrimalCraft.LOGGER.info(
                    "🎮 [HARDCORE_DIFFICULTY] World difficulty: {} ({}), Multiplier: {:.2f}x",
                    difficultyName, modeString, finalMultiplier
                );
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[HARDCORE_DIFFICULTY] Failed to update difficulty for player: {}", player.getName().getString(), e);
        }
    }

    /**
     * Get base multiplier from world difficulty setting
     */
    private static float getDifficultyMultiplier(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> PEACEFUL_MULTIPLIER;
            case EASY -> EASY_MULTIPLIER;
            case NORMAL -> NORMAL_MULTIPLIER;
            case HARD -> HARD_MULTIPLIER;
        };
    }

    /**
     * Apply the calculated multiplier to all relevant difficulty systems
     */
    private static void applyDifficultyMultiplier(ServerPlayerEntity player, float multiplier, boolean isHardcore) {
        try {
            var config = PrimalCraftConfig.getDifficulty();

            // Apply to difficulty core multipliers
            if (config.staminaScalingEnabled) {
                config.core.stamina = multiplier;
            }

            if (config.thirstScalingEnabled) {
                config.core.thirst = multiplier;
            }

            if (config.temperatureScalingEnabled) {
                config.core.temperature = multiplier;
            }

            if (config.hazardScalingEnabled) {
                config.core.hazards = multiplier;
            }

            if (config.damageScalingEnabled) {
                config.damage.environmental = multiplier;
                config.damage.dehydration = multiplier;
            }

            if (config.mobScalingEnabled) {
                config.mobResources.mobDamage = multiplier;
                config.mobResources.mobHealth = multiplier;
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[HARDCORE_DIFFICULTY] Failed to apply difficulty multiplier", e);
        }
    }

    /**
     * Get the current world difficulty multiplier for display/debug purposes
     */
    public static float getCurrentDifficultyMultiplier(ServerPlayerEntity player) {
        try {
            net.minecraft.world.World world = player.getEntityWorld();
            if (!(world instanceof ServerWorld)) return 1.0f;

            ServerWorld serverWorld = (ServerWorld) world;
            Difficulty difficulty = serverWorld.getDifficulty();
            boolean isHardcore = serverWorld.getLevelProperties().isHardcore();

            float baseMultiplier = getDifficultyMultiplier(difficulty);
            return isHardcore ? baseMultiplier * HARDCORE_ADDITIONAL_MULTIPLIER : baseMultiplier;
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[HARDCORE_DIFFICULTY] Error calculating difficulty multiplier", e);
            return 1.0f;
        }
    }
}
