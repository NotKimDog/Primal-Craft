package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.minecraft.particle.ParticleTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 🎮 Primal Craft - Item Drop Particle Effects
 *
 * Adds visual particle effects when items are dropped:
 * - Sparkle particles around dropped items
 * - Glow effect based on item rarity
 * - Configurable particle types and intensity
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class ItemDropParticleHandler {
    private ItemDropParticleHandler() {}

    // Track recently dropped items to avoid duplicate particles
    private static final Set<UUID> recentlyDroppedItems = new HashSet<>();
    private static final long PARTICLE_COOLDOWN_TICKS = 5; // 5 ticks (250ms)

    private static boolean lastParticleState = false;

    public static void register() {
        PrimalCraft.LOGGER.info("✨ [ITEM_PARTICLES] Registering Item Drop Particle Handler");

        ServerTickEvents.END_SERVER_TICK.register(ItemDropParticleHandler::onServerTick);

        PrimalCraft.LOGGER.info("✅ [ITEM_PARTICLES] Item Drop Particle Handler registered");
    }

    private static void onServerTick(MinecraftServer server) {
        try {
            if (!PrimalCraftConfig.getAdvanced().features.itemDropParticles) {
                recentlyDroppedItems.clear();
                return;
            }
            boolean particlesEnabled = PrimalCraftConfig.getAdvanced().performance.enableParticles;
            if (particlesEnabled != lastParticleState) {
                lastParticleState = particlesEnabled;
                String status = particlesEnabled ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.info("✨ [ITEM_PARTICLES] Item drop particles {}", status);
            }

            if (!particlesEnabled) {
                recentlyDroppedItems.clear();
                return;
            }

            // Process item drops for each world
            for (ServerWorld world : server.getWorlds()) {
                spawnItemDropParticles(world);
            }

            // Clear old entries periodically
            if (server.getTicks() % 100 == 0) {
                recentlyDroppedItems.clear();
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[ITEM_PARTICLES] Error during tick", e);
        }
    }

    /**
     * Spawn particles for items in a world
     */
    private static void spawnItemDropParticles(ServerWorld world) {
        try {
            // Get all items in the world
            var items = world.getEntitiesByClass(
                ItemEntity.class,
                null,
                itemEntity -> itemEntity.isAlive() && !itemEntity.isRemoved()
            );

            for (ItemEntity itemEntity : items) {
                UUID itemUuid = itemEntity.getUuid();

                // Skip if we recently processed this item
                if (recentlyDroppedItems.contains(itemUuid)) {
                    continue;
                }

                // Check if this is a recently dropped item (spawned in last few ticks)
                if (itemEntity.age < PARTICLE_COOLDOWN_TICKS) {
                    recentlyDroppedItems.add(itemUuid);
                    spawnParticlesForItem(world, itemEntity);
                }
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[ITEM_PARTICLES] Error spawning item particles", e);
        }
    }

    /**
     * Spawn particles around a dropped item
     */
    private static void spawnParticlesForItem(ServerWorld world, ItemEntity itemEntity) {
        try {
            double x = itemEntity.getX();
            double y = itemEntity.getY();
            double z = itemEntity.getZ();
            ItemStack stack = itemEntity.getStack();

            // Determine particle count based on item rarity
            int particleCount = getParticleCount(stack);

            // Spawn sparkle particles
            for (int i = 0; i < particleCount; i++) {
                double offsetX = (Math.random() - 0.5) * 0.5;
                double offsetY = Math.random() * 0.5;
                double offsetZ = (Math.random() - 0.5) * 0.5;

                world.spawnParticles(
                    ParticleTypes.ENCHANT,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0, 0, 0,
                    0.1
                );
            }

            // Add glow particles for valuable items
            if (isValuableItem(stack)) {
                world.spawnParticles(
                    ParticleTypes.GLOW,
                    x,
                    y + 0.25,
                    z,
                    1,
                    0, 0, 0,
                    0
                );
            }

            if (PrimalCraft.LOGGER.isDebugEnabled()) {
                PrimalCraft.LOGGER.trace(
                    "[ITEM_PARTICLES] Spawned {} particles for: {}",
                    particleCount,
                    stack.getName().getString()
                );
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[ITEM_PARTICLES] Failed to spawn particles for item", e);
        }
    }

    /**
     * Determine particle count based on item properties
     */
    private static int getParticleCount(ItemStack stack) {
        // Enchanted items get more particles
        if (stack.hasEnchantments()) {
            return 8;
        }

        // Rare items (gems, ores)
        String itemName = stack.getItem().getName(stack).getString().toLowerCase();
        if (itemName.contains("diamond") || itemName.contains("emerald") ||
            itemName.contains("amethyst") || itemName.contains("gold")) {
            return 6;
        }

        // Iron/valuable items
        if (itemName.contains("iron") || itemName.contains("copper")) {
            return 4;
        }

        // Default (common items)
        return 2;
    }

    /**
     * Check if an item is valuable
     */
    private static boolean isValuableItem(ItemStack stack) {
        String itemName = stack.getItem().getName(stack).getString().toLowerCase();
        return itemName.contains("diamond") ||
               itemName.contains("emerald") ||
               itemName.contains("amethyst") ||
               itemName.contains("ancient_debris") ||
               itemName.contains("netherite") ||
               stack.hasEnchantments();
    }

    /**
     * Check if item particles are enabled
     */
    public static boolean isItemParticlesEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.itemDropParticles
                && PrimalCraftConfig.getAdvanced().performance.enableParticles;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Set item particles enabled state
     */
    public static void setItemParticlesEnabled(boolean enabled) {
        try {
            PrimalCraftConfig.getAdvanced().performance.enableParticles = enabled;
            PrimalCraftConfig.save();
            lastParticleState = enabled;

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("✨ [ITEM_PARTICLES] Item drop particles {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[ITEM_PARTICLES] Failed to toggle item particles", e);
        }
    }
}
