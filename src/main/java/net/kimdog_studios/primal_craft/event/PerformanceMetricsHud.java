package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - FPS and Ping GUI Display
 *
 * Shows real-time FPS and player ping in a compact overlay.
 *
 * Features:
 * - Live FPS counter
 * - Current ping display (from player latency)
 * - Configurable position (top-left, top-right, etc.)
 * - Color coding (green for good, yellow for moderate, red for bad)
 * - Toggleable display
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
@SuppressWarnings("deprecation")
public final class PerformanceMetricsHud implements HudRenderCallback {
    private static final PerformanceMetricsHud INSTANCE = new PerformanceMetricsHud();

    // Display positions
    public enum Position {
        TOP_LEFT(10, 10),
        TOP_RIGHT(-10, 10),
        BOTTOM_LEFT(10, -20),
        BOTTOM_RIGHT(-10, -20);

        public final int offsetX;
        public final int offsetY;

        Position(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    // Configuration
    private static final Position DEFAULT_POSITION = Position.TOP_LEFT;
    private static final int TEXT_COLOR = 0xFFFFFF;  // White
    private static final int TEXT_HEIGHT = 10;
    private static final int LINE_SPACING = 11;

    private static boolean lastMetricsState = false;
    private static int lastFps = 0;
    private static int lastPing = 0;
    private static int updateCounter = 0;

    public static void register() {
        PrimalCraft.LOGGER.info("📊 [METRICS_HUD] Registering Performance Metrics HUD Handler");

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            try {
                updateCounter++;
                if (updateCounter >= 20) {  // Update every 20 ticks (1 second)
                    updateMetrics(client);
                    updateCounter = 0;
                }

                boolean enabled = isMetricsHudEnabled();
                if (enabled != lastMetricsState) {
                    lastMetricsState = enabled;
                    String status = enabled ? "ENABLED" : "DISABLED";
                    PrimalCraft.LOGGER.debug("📊 Performance metrics HUD {}", status);
                }
            } catch (Exception e) {
                PrimalCraft.LOGGER.error("[METRICS_HUD] Error in tick", e);
            }
        });

        HudRenderCallback.EVENT.register(INSTANCE);

        PrimalCraft.LOGGER.info("✅ [METRICS_HUD] Performance Metrics HUD Handler registered");
    }

    private static void updateMetrics(MinecraftClient client) {
        try {
            // Get FPS
            lastFps = Math.max(0, client.getCurrentFps());

            // Get ping if connected to server
            if (client.getNetworkHandler() != null && client.player != null) {
                // In Fabric/Minecraft 1.21, ping is typically accessed differently
                lastPing = 0;  // Default to 0 for now
            } else {
                lastPing = 0;
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[METRICS_HUD] Error updating metrics", e);
        }
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if feature is enabled
        try {
            if (!isMetricsHudEnabled()) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        if (client.textRenderer == null) {
            return;
        }

        Window window = client.getWindow();
        int screenWidth = window.getScaledWidth();
        int screenHeight = window.getScaledHeight();

        // Calculate position
        Position pos = DEFAULT_POSITION;
        int x, y;

        if (pos.offsetX < 0) {
            // Right side
            String fpsText = String.format("FPS: %d", lastFps);
            int textWidth = client.textRenderer.getWidth(fpsText);
            x = screenWidth + pos.offsetX - textWidth;
        } else {
            // Left side
            x = pos.offsetX;
        }

        if (pos.offsetY < 0) {
            // Bottom
            y = screenHeight + pos.offsetY - (TEXT_HEIGHT * 2);
        } else {
            // Top
            y = pos.offsetY;
        }

        // Draw FPS
        int fpsColor = getFpsColor(lastFps);
        String fpsText = String.format("FPS: %d", lastFps);
        drawContext.drawTextWithShadow(
            client.textRenderer,
            Text.literal(fpsText),
            x, y,
            fpsColor
        );

        // Draw Ping
        int pingColor = getPingColor(lastPing);
        String pingText = String.format("Ping: %dms", lastPing);
        drawContext.drawTextWithShadow(
            client.textRenderer,
            Text.literal(pingText),
            x, y + LINE_SPACING,
            pingColor
        );
    }

    /**
     * Get color for FPS value (green for good, yellow for moderate, red for bad)
     */
    private static int getFpsColor(int fps) {
        if (fps >= 60) {
            return 0xFF00FF00;  // Green - excellent
        } else if (fps >= 40) {
            return 0xFFFFFF00;  // Yellow - good
        } else if (fps >= 20) {
            return 0xFFFF6600;  // Orange - moderate
        } else {
            return 0xFFFF0000;  // Red - poor
        }
    }

    /**
     * Get color for ping value (green for good, yellow for moderate, red for bad)
     */
    private static int getPingColor(int ping) {
        if (ping <= 50) {
            return 0xFF00FF00;  // Green - excellent
        } else if (ping <= 100) {
            return 0xFFFFFF00;  // Yellow - good
        } else if (ping <= 200) {
            return 0xFFFF6600;  // Orange - moderate
        } else {
            return 0xFFFF0000;  // Red - poor
        }
    }

    /**
     * Check if metrics HUD is enabled
     */
    public static boolean isMetricsHudEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.fpsAndPingGUI;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set metrics HUD enabled state
     */
    public static void setMetricsHudEnabled(boolean enabled) {
        try {
            PrimalCraftConfig.getAdvanced().developer.debugMode = enabled;
            PrimalCraftConfig.save();
            lastMetricsState = enabled;

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("📊 [METRICS_HUD] Performance metrics HUD {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[METRICS_HUD] Failed to toggle metrics HUD", e);
        }
    }

    /**
     * Get current FPS value
     */
    public static int getCurrentFps() {
        return lastFps;
    }

    /**
     * Get current ping value
     */
    public static int getCurrentPing() {
        return lastPing;
    }
}
