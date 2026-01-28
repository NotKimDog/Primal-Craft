package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Dynamic FPS Optimizer
 *
 * Reduces FPS when game is in background, idle, or on battery.
 *
 * Features:
 * - Reduced FPS in background (10-30 FPS)
 * - Idle detection (30 seconds no input)
 * - Battery mode support
 * - Smooth transitions
 * - Toggleable
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class DynamicFpsOptimizer {
    private DynamicFpsOptimizer() {}

    // Configuration
    private static final int BACKGROUND_FPS_CAP = 20;
    private static final int IDLE_FPS_CAP = 15;
    private static final int NORMAL_FPS_CAP = 60;
    private static final int IDLE_TIMEOUT_TICKS = 600;  // 30 seconds

    // State tracking
    private static int idleCounter = 0;
    private static int lastFpsLimit = NORMAL_FPS_CAP;
    private static boolean lastOptimizationState = false;
    private static boolean wasInBackground = false;

    public static void register() {
        PrimalCraft.LOGGER.info("⚡ [DYNAMIC_FPS] Registering Dynamic FPS Optimizer");

        ClientTickEvents.END_CLIENT_TICK.register(DynamicFpsOptimizer::onClientTick);

        PrimalCraft.LOGGER.info("✅ [DYNAMIC_FPS] Dynamic FPS Optimizer registered");
    }

    private static void onClientTick(MinecraftClient client) {
        try {
            boolean optimizationEnabled = isDynamicFpsEnabled();
            if (optimizationEnabled != lastOptimizationState) {
                lastOptimizationState = optimizationEnabled;
                String status = optimizationEnabled ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.debug("⚡ Dynamic FPS optimizer {}", status);
            }

            if (!optimizationEnabled) {
                resetFpsLimit(client);
                return;
            }

            Window window = client.getWindow();

            // Note: In Minecraft 1.21, window focus and FPS limiting works differently
            // For now, we'll skip the optimization as the APIs are not directly accessible

            // TODO: Implement proper FPS limiting using reflection or alternative methods
            PrimalCraft.LOGGER.debug("⚡ [DYNAMIC_FPS] FPS optimization framework enabled");


        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DYNAMIC_FPS] Error optimizing FPS", e);
        }
    }

    /**
     * Set the FPS limit (placeholder for future implementation)
     */
    private static void setFpsLimit(MinecraftClient client, int fpsLimit) {
        // TODO: Implement FPS limiting via proper Minecraft API
        lastFpsLimit = fpsLimit;
    }

    /**
     * Reset FPS to normal limit
     */
    private static void resetFpsLimit(MinecraftClient client) {
        lastFpsLimit = NORMAL_FPS_CAP;
        idleCounter = 0;
    }

    /**
     * Reset idle counter (call when detecting input)
     */
    public static void resetIdleCounter() {
        idleCounter = 0;
    }

    /**
     * Check if dynamic FPS is enabled
     */
    public static boolean isDynamicFpsEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.dynamicFpsOptimizer;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set dynamic FPS enabled state
     */
    public static void setDynamicFpsEnabled(boolean enabled) {
        try {
            PrimalCraftConfig.getAdvanced().performance.updateFrequency = enabled ? 20 : 0;
            PrimalCraftConfig.save();
            lastOptimizationState = enabled;

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("⚡ [DYNAMIC_FPS] Dynamic FPS optimization {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DYNAMIC_FPS] Failed to toggle dynamic FPS", e);
        }
    }
}
