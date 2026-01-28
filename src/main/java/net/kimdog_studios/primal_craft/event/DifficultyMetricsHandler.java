package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.util.DifficultySystem;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 🎮 Primal Craft - Difficulty Metric Tracking Handler
 *
 * Tracks player events (death) and records them in the difficulty system.
 * This allows the system to scale difficulty based on player performance metrics.
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class DifficultyMetricsHandler {
    private DifficultyMetricsHandler() {}

    // Track last health to detect deaths
    private static final Map<UUID, Float> LAST_HEALTH = new HashMap<>();

    public static void register() {
        long startTime = System.currentTimeMillis();

        PrimalCraft.LOGGER.info("⚙️  [DIFFICULTY_METRICS] Initializing metric tracking system");

        try {
            // Register tick handler to detect deaths
            ServerTickEvents.END_SERVER_TICK.register(server -> {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    try {
                        float currentHealth = player.getHealth();
                        UUID playerUuid = player.getUuid();

                        float lastHealth = LAST_HEALTH.getOrDefault(playerUuid, currentHealth);
                        LAST_HEALTH.put(playerUuid, currentHealth);

                        // Detect death (health went to 0)
                        if (lastHealth > 0 && currentHealth <= 0) {
                            DifficultySystem.recordPlayerDeath(player);
                        }
                    } catch (Exception e) {
                        PrimalCraft.LOGGER.error("[DIFFICULTY_METRICS] Error processing player metrics", e);
                    }
                }
            });
            PrimalCraft.LOGGER.debug("   ├─ Registered death tracking");

            // Register block break tracking (proxy for resource gathering)
            PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    try {
                        // Track resource gathering
                        DifficultySystem.recordResourcesGathered(serverPlayer, 1);
                    } catch (Exception e) {
                        PrimalCraft.LOGGER.error("[DIFFICULTY_METRICS] Error tracking block break", e);
                    }
                }
            });
            PrimalCraft.LOGGER.debug("   ├─ Registered block break tracking");

            long elapsed = System.currentTimeMillis() - startTime;
            PrimalCraft.LOGGER.info("✅ [DIFFICULTY_METRICS] Metric tracking system initialized in {}ms", elapsed);

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("❌ [DIFFICULTY_METRICS] Failed to initialize metric tracking", e);
            throw new RuntimeException("Failed to initialize difficulty metrics", e);
        }
    }
}
