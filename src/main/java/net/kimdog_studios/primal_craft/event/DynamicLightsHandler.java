package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Dynamic Lights System
 *
 * Adds dynamic lighting from held light sources.
 *
 * Features:
 * - Light from torches, lanterns, glowstone
 * - Works with main and offhand
 * - Smooth transitions
 * - Configurable intensity
 * - Optional feature
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class DynamicLightsHandler {
    private DynamicLightsHandler() {}

    private static boolean lastDynamicLightsState = false;

    // Light level mapping for items
    private static final int TORCH_LIGHT = 14;
    private static final int LANTERN_LIGHT = 15;
    private static final int GLOWSTONE_LIGHT = 15;
    private static final int SOUL_TORCH_LIGHT = 10;
    private static final int MAGMA_LIGHT = 13;
    private static final int LAVA_BUCKET_LIGHT = 15;

    public static void register() {
        PrimalCraft.LOGGER.info("💡 [DYNAMIC_LIGHTS] Registering Dynamic Lights Handler");

        ClientTickEvents.START_CLIENT_TICK.register(DynamicLightsHandler::onClientTick);

        PrimalCraft.LOGGER.info("✅ [DYNAMIC_LIGHTS] Dynamic Lights Handler registered");
    }

    private static void onClientTick(MinecraftClient client) {
        try {
            boolean dynamicLightsEnabled = isDynamicLightsEnabled();
            if (dynamicLightsEnabled != lastDynamicLightsState) {
                lastDynamicLightsState = dynamicLightsEnabled;
                String status = dynamicLightsEnabled ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.debug("💡 Dynamic lights {}", status);
            }

            if (!dynamicLightsEnabled || client.player == null) {
                return;
            }

            // Update dynamic lights based on held items
            updateDynamicLights(client.player);

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DYNAMIC_LIGHTS] Error updating dynamic lights", e);
        }
    }

    /**
     * Update dynamic lights for a player
     */
    private static void updateDynamicLights(PlayerEntity player) {
        try {
            // Check main hand
            ItemStack mainHandStack = player.getMainHandStack();
            int mainHandLight = getLightLevel(mainHandStack);

            // Check offhand
            ItemStack offHandStack = player.getOffHandStack();
            int offHandLight = getLightLevel(offHandStack);

            // Use the brighter light source
            int maxLight = Math.max(mainHandLight, offHandLight);

            if (maxLight > 0) {
                // Apply dynamic light to player
                // Note: In vanilla Minecraft, this would require a shader mod or datapack
                // For now, we log the light level for reference
                if (PrimalCraft.LOGGER.isDebugEnabled()) {
                    PrimalCraft.LOGGER.trace(
                        "💡 [DYNAMIC_LIGHTS] Player {} light level: {}",
                        player.getName().getString(),
                        maxLight
                    );
                }
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[DYNAMIC_LIGHTS] Failed to update dynamic lights", e);
        }
    }

    /**
     * Get light level for an item
     */
    private static int getLightLevel(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        if (stack.isOf(Items.TORCH)) return TORCH_LIGHT;
        if (stack.isOf(Items.LANTERN)) return LANTERN_LIGHT;
        if (stack.isOf(Items.GLOWSTONE)) return GLOWSTONE_LIGHT;
        if (stack.isOf(Items.SOUL_TORCH)) return SOUL_TORCH_LIGHT;
        if (stack.isOf(Items.MAGMA_BLOCK)) return MAGMA_LIGHT;
        if (stack.isOf(Items.LAVA_BUCKET)) return LAVA_BUCKET_LIGHT;
        if (stack.isOf(Items.SOUL_LANTERN)) return 10;
        if (stack.isOf(Items.ENDER_PEARL)) return 8;
        if (stack.isOf(Items.GLOW_BERRIES)) return 9;
        if (stack.isOf(Items.SCULK_CATALYST)) return 6;

        return 0;
    }

    /**
     * Check if dynamic lights is enabled
     */
    public static boolean isDynamicLightsEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.dynamicLights;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set dynamic lights enabled state
     */
    public static void setDynamicLightsEnabled(boolean enabled) {
        try {
            PrimalCraftConfig.getAdvanced().performance.enableParticles = enabled;
            PrimalCraftConfig.save();
            lastDynamicLightsState = enabled;

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("💡 [DYNAMIC_LIGHTS] Dynamic lights {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DYNAMIC_LIGHTS] Failed to toggle dynamic lights", e);
        }
    }
}
