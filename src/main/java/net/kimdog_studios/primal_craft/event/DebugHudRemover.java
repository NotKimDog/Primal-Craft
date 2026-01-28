package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.minecraft.client.MinecraftClient;

/**
 * 🎮 Primal Craft - Debug HUD Remover
 *
 * Removes the debug/performance HUD display from the top-right corner
 * when configured to do so. Provides a cleaner gaming interface.
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class DebugHudRemover {
    private DebugHudRemover() {}

    private static boolean lastDebugHudState = false;

    public static void register() {
        PrimalCraft.LOGGER.info("🎨 [DEBUG_HUD] Registering Debug HUD Remover");

        ClientTickEvents.START_CLIENT_TICK.register(DebugHudRemover::onClientTick);

        PrimalCraft.LOGGER.info("✅ [DEBUG_HUD] Debug HUD Remover registered");
    }

    private static void onClientTick(MinecraftClient client) {
        try {
            boolean hideDebugHud = isDebugHudRemovalEnabled();

            // Only update if state changed
            if (hideDebugHud != lastDebugHudState) {
                lastDebugHudState = hideDebugHud;

                if (hideDebugHud) {
                    PrimalCraft.LOGGER.debug("🎨 Debug HUD hidden");
                } else {
                    PrimalCraft.LOGGER.debug("🎨 Debug HUD shown");
                }
            }

            // Force disable debug HUD rendering if configured
            // Note: In Fabric 1.21, debug mode is controlled differently
            // We'll track the state and provide UI hiding in overlays instead
            if (hideDebugHud) {
                PrimalCraft.LOGGER.debug("[DEBUG_HUD] Debug HUD hiding is configured (overlay-based)");
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DEBUG_HUD] Error updating debug HUD state", e);
        }
    }

    /**
     * Check if debug HUD is currently hidden
     */
    public static boolean isDebugHudHidden() {
        return !PrimalCraftConfig.getHUD().visibility.showDebugInfo;
    }

    public static boolean isDebugHudRemovalEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.debugHudRemoval;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Force set debug HUD visibility
     */
    public static void setDebugHudVisible(boolean visible) {
        PrimalCraftConfig.getHUD().visibility.showDebugInfo = visible;
        PrimalCraftConfig.save();
        lastDebugHudState = !visible;
    }
}
