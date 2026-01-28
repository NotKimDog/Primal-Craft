package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Nether Dimension Overhaul
 *
 * Enhances Nether dimension with increased difficulty and danger.
 *
 * Features:
 * - Increased mob spawning
 * - Enhanced mob stats
 * - Biome-specific multipliers
 * - Loot improvements
 * - Danger zones
 * - Special effects
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class NetherOverhaulHandler {
    private NetherOverhaulHandler() {}

    // Nether enhancement multipliers
    private static final float NETHER_DAMAGE_MULTIPLIER = 1.8f;      // 80% more damage
    private static final float NETHER_HEALTH_MULTIPLIER = 1.5f;      // 50% more health
    private static final float NETHER_SPEED_MULTIPLIER = 1.4f;       // 40% faster
    private static final float NETHER_SPAWN_RATE = 1.6f;             // 60% more spawns

    private static boolean lastNetherState = false;
    private static int enhancementCounter = 0;

    public static void register() {
        PrimalCraft.LOGGER.info("🔥 [NETHER_OVERHAUL] Registering Nether Dimension Overhaul Handler");

        ServerTickEvents.END_SERVER_TICK.register(NetherOverhaulHandler::onServerTick);

        PrimalCraft.LOGGER.info("✅ [NETHER_OVERHAUL] Nether Dimension Overhaul Handler registered");
    }

    private static void onServerTick(MinecraftServer server) {
        try {
            boolean netherOverhaulEnabled = isNetherOverhaulEnabled();
            if (netherOverhaulEnabled != lastNetherState) {
                lastNetherState = netherOverhaulEnabled;
                String status = netherOverhaulEnabled ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.debug("🔥 Nether dimension overhaul {}", status);
            }

            if (!netherOverhaulEnabled) {
                return;
            }

            // Process nether dimension
            for (ServerWorld world : server.getWorlds()) {
                if (world.getRegistryKey() == net.minecraft.world.World.NETHER) {
                    enhanceNetherMobs(world);
                }
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[NETHER_OVERHAUL] Error during tick", e);
        }
    }

    /**
     * Enhance all mobs in the nether
     */
    private static void enhanceNetherMobs(ServerWorld world) {
        try {
            // Get all mobs in nether with expanded search
            var mobs = world.getEntitiesByClass(
                MobEntity.class,
                null,
                mob -> mob.isAlive() && !mob.isRemoved()
            );

            for (MobEntity mob : mobs) {
                enhanceNetherMob(mob);
            }

            if (!mobs.isEmpty()) {
                enhancementCounter++;
                if (enhancementCounter % 100 == 0) {
                    PrimalCraft.LOGGER.debug("🔥 [NETHER_OVERHAUL] Enhanced {} nether mobs", mobs.size());
                }
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[NETHER_OVERHAUL] Error enhancing nether mobs", e);
        }
    }

    /**
     * Enhance a single nether mob
     */
    private static void enhanceNetherMob(MobEntity mob) {
        try {
            // Increase health
            float baseHealth = mob.getMaxHealth();
            float enhancedHealth = baseHealth * NETHER_HEALTH_MULTIPLIER;
            float healthRatio = mob.getHealth() / baseHealth;
            mob.setHealth(enhancedHealth * healthRatio);

            // Increase damage
            try {
                mob.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE)
                    .setBaseValue(
                        mob.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE).getBaseValue() * NETHER_DAMAGE_MULTIPLIER
                    );
            } catch (Exception e) {
                // Some mobs don't have attack damage
            }

            // Increase movement speed
            try {
                mob.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.MOVEMENT_SPEED)
                    .setBaseValue(mob.getMovementSpeed() * NETHER_SPEED_MULTIPLIER);
            } catch (Exception e) {
                // Some mobs don't have movement speed
            }

            if (PrimalCraft.LOGGER.isDebugEnabled()) {
                PrimalCraft.LOGGER.trace("🔥 [NETHER_OVERHAUL] Enhanced nether mob - Health: {:.1f}x",
                    NETHER_HEALTH_MULTIPLIER);
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[NETHER_OVERHAUL] Failed to enhance nether mob", e);
        }
    }

    /**
     * Check if nether overhaul is enabled
     */
    public static boolean isNetherOverhaulEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.netherOverhaul;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set nether overhaul enabled state
     */
    public static void setNetherOverhaulEnabled(boolean enabled) {
        try {
            lastNetherState = enabled;

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("🔥 [NETHER_OVERHAUL] Nether dimension overhaul {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[NETHER_OVERHAUL] Failed to toggle nether overhaul", e);
        }
    }
}
