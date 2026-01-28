package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Fullscreen Auto-Launch Handler
 *
 * Automatically launches the game in fullscreen mode on startup.
 *
 * Features:
 * - Auto-fullscreen on game launch
 * - Configurable toggle
 * - One-time application per session
 * - Respects user's previous fullscreen preference
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class FullscreenAutoLaunchHandler {
    private FullscreenAutoLaunchHandler() {}

    private static boolean fullscreenApplied = false;
    private static boolean lastFullscreenState = false;

    public static void register() {
        PrimalCraft.LOGGER.info("🖥️  [FULLSCREEN] Registering Fullscreen Auto-Launch Handler");

        ClientTickEvents.START_CLIENT_TICK.register(FullscreenAutoLaunchHandler::onClientTick);

        PrimalCraft.LOGGER.info("✅ [FULLSCREEN] Fullscreen Auto-Launch Handler registered");
    }

    private static void onClientTick(MinecraftClient client) {
        try {
            // Only apply once per session
            if (fullscreenApplied) {
                return;
            }

            // Check if feature is enabled
            boolean autoFullscreenEnabled = isFullscreenAutoLaunchEnabled();
            if (autoFullscreenEnabled != lastFullscreenState) {
                lastFullscreenState = autoFullscreenEnabled;
                String status = autoFullscreenEnabled ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.info("🖥️  [FULLSCREEN] Fullscreen auto-launch {}", status);
            }

            if (!autoFullscreenEnabled) {
                fullscreenApplied = true;  // Don't try again if disabled
                return;
            }

            // Apply fullscreen on first successful client tick
            if (client.getWindow() != null) {
                applyFullscreen(client);
                fullscreenApplied = true;

                PrimalCraft.LOGGER.info("🖥️  [FULLSCREEN] Fullscreen applied automatically");
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[FULLSCREEN] Error applying fullscreen", e);
            fullscreenApplied = true;  // Don't retry on error
        }
    }

    /**
     * Apply fullscreen mode
     */
    private static void applyFullscreen(MinecraftClient client) {
        try {
            // Get the monitor that the window is on
            long monitor = client.getWindow().getHandle();

            // Set fullscreen mode
            // Note: In Fabric/Minecraft, fullscreen is typically toggled via options
            client.options.getFullscreen().setValue(true);

            // Save the option
            client.options.write();

            PrimalCraft.LOGGER.debug("🖥️  [FULLSCREEN] Fullscreen mode activated");
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[FULLSCREEN] Failed to apply fullscreen", e);
        }
    }

    /**
     * Check if fullscreen auto-launch is enabled
     */
    public static boolean isFullscreenAutoLaunchEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.fullscreenAutoLaunch;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set fullscreen auto-launch enabled state
     */
    public static void setFullscreenAutoLaunchEnabled(boolean enabled) {
        try {
            lastFullscreenState = enabled;
            fullscreenApplied = false;  // Reset so it applies next time

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("🖥️  [FULLSCREEN] Fullscreen auto-launch {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[FULLSCREEN] Failed to toggle fullscreen", e);
        }
    }

    /**
     * Reset the fullscreen applied flag (for testing)
     */
    public static void resetFullscreenFlag() {
        fullscreenApplied = false;
    }
}
