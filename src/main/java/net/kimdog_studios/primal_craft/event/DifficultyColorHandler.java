package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.kimdog_studios.primal_craft.util.PresetManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

/**
 * 🎮 Primal Craft - Difficulty Color Coding System
 *
 * Displays difficulty level with color-coded indicators:
 * - Green (Peaceful): 0.0x
 * - Lime (Easy): 0.5x
 * - Yellow (Normal): 1.0x
 * - Orange (Hard): 1.5x
 * - Red-Orange (Expert): 2.0x
 * - Red (Nightmare): 3.0x
 *
 * Colors are used in HUD overlays for visual feedback.
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class DifficultyColorHandler {
    private DifficultyColorHandler() {}

    // Color constants (ARGB format)
    public static final int COLOR_PEACEFUL = 0xFF00FF00;      // Bright Green
    public static final int COLOR_EASY = 0xFF00CC00;          // Lime Green
    public static final int COLOR_NORMAL = 0xFFFFFF00;        // Yellow
    public static final int COLOR_HARD = 0xFFFF6600;          // Orange
    public static final int COLOR_EXPERT = 0xFFFF3300;        // Red-Orange
    public static final int COLOR_NIGHTMARE = 0xFFFF0000;     // Red

    private static String currentPreset = "NORMAL";
    private static float currentMultiplier = 1.0f;

    public static void register() {
        PrimalCraft.LOGGER.info("🎨 [DIFFICULTY_COLOR] Registering Difficulty Color Handler");

        ClientTickEvents.START_CLIENT_TICK.register(DifficultyColorHandler::onClientTick);

        PrimalCraft.LOGGER.info("✅ [DIFFICULTY_COLOR] Difficulty Color Handler registered");
    }

    private static void onClientTick(MinecraftClient client) {
        try {
            if (!PrimalCraftConfig.getAdvanced().features.difficultyColors) {
                return;
            }
            // Update current difficulty from config
            var config = PrimalCraftConfig.getDifficulty();
            if (config != null && config.master != null) {
                String preset = config.master.currentPreset;
                if (!preset.equals(currentPreset)) {
                    currentPreset = preset;
                    currentMultiplier = PresetManager.getPresetMultiplier(preset);
                    PrimalCraft.LOGGER.debug("🎨 Difficulty color updated: {} ({:.2f}x)", preset, currentMultiplier);
                }
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DIFFICULTY_COLOR] Error updating color", e);
        }
    }

    /**
     * Get the color for the current difficulty preset
     */
    public static int getCurrentDifficultyColor() {
        return getPresetColor(currentPreset);
    }

    /**
     * Get color for a specific preset
     */
    public static int getPresetColor(String presetName) {
        return switch (presetName.toUpperCase()) {
            case "PEACEFUL" -> COLOR_PEACEFUL;
            case "EASY" -> COLOR_EASY;
            case "NORMAL" -> COLOR_NORMAL;
            case "HARD" -> COLOR_HARD;
            case "EXPERT" -> COLOR_EXPERT;
            case "NIGHTMARE" -> COLOR_NIGHTMARE;
            default -> COLOR_NORMAL;
        };
    }

    /**
     * Get color for a specific multiplier value
     */
    public static int getColorForMultiplier(float multiplier) {
        if (multiplier <= 0.0f) return COLOR_PEACEFUL;
        if (multiplier <= 0.5f) return COLOR_EASY;
        if (multiplier <= 1.0f) return COLOR_NORMAL;
        if (multiplier <= 1.5f) return COLOR_HARD;
        if (multiplier <= 2.0f) return COLOR_EXPERT;
        return COLOR_NIGHTMARE;
    }

    /**
     * Get color name for display
     */
    public static String getColorName(int color) {
        return switch (color) {
            case COLOR_PEACEFUL -> "§aGreen";
            case COLOR_EASY -> "§2Lime";
            case COLOR_NORMAL -> "§eYellow";
            case COLOR_HARD -> "§6Orange";
            case COLOR_EXPERT -> "§c Orange-Red";
            case COLOR_NIGHTMARE -> "§4Red";
            default -> "§7Gray";
        };
    }

    /**
     * Get the current preset name
     */
    public static String getCurrentPreset() {
        return currentPreset;
    }

    /**
     * Get the current difficulty multiplier
     */
    public static float getCurrentMultiplier() {
        return currentMultiplier;
    }

    /**
     * Interpolate between two colors based on a value (0-1)
     */
    public static int interpolateColor(int color1, int color2, float value) {
        value = MathHelper.clamp(value, 0.0f, 1.0f);

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) MathHelper.lerp(value, r1, r2);
        int g = (int) MathHelper.lerp(value, g1, g2);
        int b = (int) MathHelper.lerp(value, b1, b2);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Get display text for difficulty level
     */
    public static String getDifficultyDisplayText() {
        return switch (currentPreset.toUpperCase()) {
            case "PEACEFUL" -> "§a◆ PEACEFUL ◆";
            case "EASY" -> "§2◆ EASY ◆";
            case "NORMAL" -> "§e◆ NORMAL ◆";
            case "HARD" -> "§6◆ HARD ◆";
            case "EXPERT" -> "§c◆ EXPERT ◆";
            case "NIGHTMARE" -> "§4◆ NIGHTMARE ◆";
            default -> "§7◆ UNKNOWN ◆";
        };
    }

    /**
     * Get a gradient color based on progression (0-1)
     * Goes from green (easy) to red (hard)
     */
    public static int getGradientColor(float progress) {
        progress = MathHelper.clamp(progress, 0.0f, 1.0f);

        if (progress < 0.2f) {
            // Green to Yellow
            return interpolateColor(COLOR_EASY, COLOR_NORMAL, progress / 0.2f);
        } else if (progress < 0.4f) {
            // Yellow to Orange
            return interpolateColor(COLOR_NORMAL, COLOR_HARD, (progress - 0.2f) / 0.2f);
        } else if (progress < 0.6f) {
            // Orange to Red-Orange
            return interpolateColor(COLOR_HARD, COLOR_EXPERT, (progress - 0.4f) / 0.2f);
        } else {
            // Red-Orange to Red
            return interpolateColor(COLOR_EXPERT, COLOR_NIGHTMARE, (progress - 0.6f) / 0.4f);
        }
    }
}
