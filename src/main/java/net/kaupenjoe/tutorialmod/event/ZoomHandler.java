package net.kaupenjoe.tutorialmod.event;

import net.kaupenjoe.tutorialmod.util.ModKeyBindings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class ZoomHandler {
    private static double zoomLevel = 1.0;
    private static double targetZoomLevel = 1.0;
    private static double zoomMultiplier = 4.0; // Default 4x zoom
    private static double zoomVelocity = 0.0; // For smooth acceleration/deceleration
    private static double savedZoomMultiplier = 4.0; // NEW: Remember last zoom level
    private static double previousZoomMultiplier = 4.0; // NEW: For transition tracking

    // NEW: Zoom presets for quick access
    private static final double[] ZOOM_PRESETS = {2.0, 4.0, 8.0, 16.0, 32.0, 64.0};
    private static int currentPresetIndex = 1; // Start at 4x

    // Zoom settings - INSTANT
    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 64.0;
    private static final double SPYGLASS_MAX_ZOOM = 128.0;
    private static final double ZOOM_STEP = 2.0; // Scroll step
    private static final double ZOOM_SMOOTHNESS = 0.92; // Nearly instant (very high)
    private static final double ZOOM_ACCELERATION = 0.85; // Nearly instant acceleration

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null) {
            // Check if zoom key is pressed OR right-click with spyglass
            if (isZoomActive()) {
                targetZoomLevel = 1.0 / zoomMultiplier;
                savedZoomMultiplier = zoomMultiplier; // NEW: Save current zoom
            } else {
                targetZoomLevel = 1.0;
            }

            // Direct interpolation (no velocity) for stable zoom
            double delta = targetZoomLevel - zoomLevel;
            zoomLevel += delta * 0.85; // Direct 85% interpolation - fast and stable

            // Snap to target when very close to prevent oscillation
            if (Math.abs(delta) < 0.001) {
                zoomLevel = targetZoomLevel;
                zoomVelocity = 0.0; // Reset velocity
            }

            previousZoomMultiplier = zoomMultiplier; // NEW: Track changes
        }
    }

    public static double getZoomLevel() {
        return zoomLevel;
    }

    public static boolean isZoomActive() {
        MinecraftClient client = MinecraftClient.getInstance();
        // Zoom is active if:
        // 1. Zoom key (C) is pressed, OR
        // 2. Right-click is held AND player is using a spyglass
        boolean zoomKeyPressed = ModKeyBindings.zoomKey.isPressed();
        boolean rightClickWithSpyglass = client.options.useKey.isPressed() &&
                                         client.player != null &&
                                         client.player.isUsingSpyglass();
        return zoomKeyPressed || rightClickWithSpyglass;
    }

    public static boolean isZooming() {
        return isZoomActive();
    }

    public static boolean isUsingSpyglass() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            return client.player.isUsingSpyglass();
        }
        return false;
    }

    public static double getEffectiveMaxZoom() {
        // Double the max zoom when using spyglass
        return isUsingSpyglass() ? SPYGLASS_MAX_ZOOM : MAX_ZOOM;
    }

    // Handle mouse scroll for zoom adjustment with constant sensitivity
    public static boolean onMouseScroll(double scrollDelta) {
        // Allow scrolling to zoom when either zoom key is pressed OR right-click is held
        if (isZoomActive()) {
            // Constant sensitivity - no scaling based on zoom level
            double change = scrollDelta * ZOOM_STEP;
            double maxZoom = getEffectiveMaxZoom();
            zoomMultiplier = MathHelper.clamp(zoomMultiplier + change, MIN_ZOOM, maxZoom);
            return true; // Cancel normal scroll behavior
        }
        return false;
    }

    public static double getZoomMultiplier() {
        return zoomMultiplier;
    }

    // NEW: Quick zoom preset cycling
    public static void cycleZoomPreset(boolean forward) {
        if (forward) {
            currentPresetIndex = (currentPresetIndex + 1) % ZOOM_PRESETS.length;
        } else {
            currentPresetIndex = (currentPresetIndex - 1 + ZOOM_PRESETS.length) % ZOOM_PRESETS.length;
        }
        zoomMultiplier = ZOOM_PRESETS[currentPresetIndex];
    }

    // NEW: Reset zoom to default
    public static void resetZoom() {
        zoomMultiplier = 4.0;
        currentPresetIndex = 1;
    }

    // NEW: Set specific zoom level
    public static void setZoomMultiplier(double zoom) {
        zoomMultiplier = MathHelper.clamp(zoom, MIN_ZOOM, getEffectiveMaxZoom());
    }

    // NEW: Get saved zoom level
    public static double getSavedZoomMultiplier() {
        return savedZoomMultiplier;
    }

    // NEW: Restore saved zoom
    public static void restoreSavedZoom() {
        zoomMultiplier = savedZoomMultiplier;
    }

    // NEW: Get zoom change rate
    public static double getZoomChangeRate() {
        return Math.abs(zoomMultiplier - previousZoomMultiplier);
    }

    // NEW: Check zoom direction
    public static boolean isZoomingIn() {
        return zoomMultiplier > previousZoomMultiplier;
    }

    public static boolean isZoomingOut() {
        return zoomMultiplier < previousZoomMultiplier;
    }
}
