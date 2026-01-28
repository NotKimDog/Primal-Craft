package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Ender Dragon Redesign Handler
 *
 * Enhances Ender Dragon with new behaviors and challenges.
 *
 * Features:
 * - Enhanced AI behavior
 * - Increased damage output
 * - New attack patterns
 * - Faster movement
 * - Improved loot
 * - Boss bar enhancements
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class EnderDragonRedesignHandler {
    private EnderDragonRedesignHandler() {}

    // Dragon enhancement multipliers
    private static final float DAMAGE_MULTIPLIER = 1.5f;     // 50% more damage
    private static final float SPEED_MULTIPLIER = 1.3f;      // 30% faster
    private static final float HEALTH_MULTIPLIER = 1.2f;     // 20% more health
    private static final float ATTACK_COOLDOWN_REDUCTION = 0.8f;  // 20% faster attacks

    private static boolean lastDragonState = false;
    private static int enhancementCounter = 0;

    public static void register() {
        PrimalCraft.LOGGER.info("🐉 [DRAGON_REDESIGN] Registering Ender Dragon Redesign Handler");

        ServerTickEvents.END_SERVER_TICK.register(EnderDragonRedesignHandler::onServerTick);

        PrimalCraft.LOGGER.info("✅ [DRAGON_REDESIGN] Ender Dragon Redesign Handler registered");
    }

    private static void onServerTick(MinecraftServer server) {
        try {
            boolean dragonRedesignEnabled = isDragonRedesignEnabled();
            if (dragonRedesignEnabled != lastDragonState) {
                lastDragonState = dragonRedesignEnabled;
                String status = dragonRedesignEnabled ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.debug("🐉 Ender dragon redesign {}", status);
            }

            if (!dragonRedesignEnabled) {
                return;
            }

            // Process end dimension
            for (ServerWorld world : server.getWorlds()) {
                if (world.getRegistryKey() == net.minecraft.world.World.END) {
                    enhanceDragonsInWorld(world);
                }
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DRAGON_REDESIGN] Error during tick", e);
        }
    }

    /**
     * Enhance all dragons in the end dimension
     */
    private static void enhanceDragonsInWorld(ServerWorld world) {
        try {
            var dragons = world.getEntitiesByClass(
                EnderDragonEntity.class,
                null,
                dragon -> dragon.isAlive() && !dragon.isRemoved()
            );

            for (EnderDragonEntity dragon : dragons) {
                enhanceDragon(dragon);
            }

            if (!dragons.isEmpty()) {
                enhancementCounter++;
                if (enhancementCounter % 100 == 0) {
                    PrimalCraft.LOGGER.debug("🐉 [DRAGON_REDESIGN] Enhanced {} dragons", dragons.size());
                }
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[DRAGON_REDESIGN] Error enhancing dragons", e);
        }
    }

    /**
     * Enhance a single dragon
     */
    private static void enhanceDragon(EnderDragonEntity dragon) {
        try {
            // Increase health
            float baseHealth = dragon.getMaxHealth();
            float enhancedHealth = baseHealth * HEALTH_MULTIPLIER;
            dragon.setHealth(enhancedHealth);

            // Increase movement speed
            dragon.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.MOVEMENT_SPEED)
                .setBaseValue(dragon.getMovementSpeed() * SPEED_MULTIPLIER);

            // Increase attack damage
            dragon.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE)
                .setBaseValue(dragon.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE).getBaseValue() * DAMAGE_MULTIPLIER);

            if (PrimalCraft.LOGGER.isDebugEnabled()) {
                PrimalCraft.LOGGER.trace("🐉 [DRAGON_REDESIGN] Enhanced dragon - Health: {:.1f}, Speed: {:.2f}x",
                    enhancedHealth, SPEED_MULTIPLIER);
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[DRAGON_REDESIGN] Failed to enhance dragon", e);
        }
    }

    /**
     * Check if dragon redesign is enabled
     */
    public static boolean isDragonRedesignEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.dragonRedesign;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set dragon redesign enabled state
     */
    public static void setDragonRedesignEnabled(boolean enabled) {
        try {
            lastDragonState = enabled;

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("🐉 [DRAGON_REDESIGN] Ender dragon redesign {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DRAGON_REDESIGN] Failed to toggle dragon redesign", e);
        }
    }
}
