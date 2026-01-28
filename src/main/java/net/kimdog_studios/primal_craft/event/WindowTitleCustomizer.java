package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Window Title Customizer
 *
 * Customizes the game window title to display "Primal Craft - 1.21.X".
 *
 * Features:
 * - Custom window title
 * - Configurable display name
 * - One-time application per session
 * - Toggleable feature
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class WindowTitleCustomizer {
    private WindowTitleCustomizer() {}

    private static boolean titleApplied = false;
    private static boolean lastTitleState = false;
    private static String lastAppliedTitle = "";

    public static void register() {
        PrimalCraft.LOGGER.info("🪟 [WINDOW_TITLE] Registering Window Title Customizer");

        ClientTickEvents.START_CLIENT_TICK.register(WindowTitleCustomizer::onClientTick);

        PrimalCraft.LOGGER.info("✅ [WINDOW_TITLE] Window Title Customizer registered");
    }

    private static void onClientTick(MinecraftClient client) {
        try {
            // Check if feature is enabled
            boolean customTitleEnabled = isWindowTitleCustomizationEnabled();
            if (customTitleEnabled != lastTitleState) {
                lastTitleState = customTitleEnabled;
                String status = customTitleEnabled ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.debug("🪟 [WINDOW_TITLE] Custom window title {}", status);
                titleApplied = false;
            }

            if (!customTitleEnabled) {
                return;
            }

            if (client.getWindow() == null) {
                return;
            }

            String configuredTitle = PrimalCraftConfig.getAdvanced().window.customTitle;
            String titleToApply = (configuredTitle == null || configuredTitle.trim().isEmpty())
                ? "Primal Craft"
                : configuredTitle.trim();

            if (!titleApplied || !titleToApply.equals(lastAppliedTitle)) {
                applyCustomTitle(client.getWindow(), titleToApply);
                titleApplied = true;
                lastAppliedTitle = titleToApply;

                PrimalCraft.LOGGER.info("🪟 [WINDOW_TITLE] Window title set to: {}", titleToApply);
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[WINDOW_TITLE] Error applying custom title", e);
            titleApplied = false;
        }
    }

    /**
     * Apply custom window title
     */
    private static void applyCustomTitle(Window window, String title) {
        try {
            window.setTitle(title);
            PrimalCraft.LOGGER.debug("🪟 [WINDOW_TITLE] Window title updated successfully");
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[WINDOW_TITLE] Failed to set window title", e);
        }
    }

    /**
     * Check if window title customization is enabled
     */
    public static boolean isWindowTitleCustomizationEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.customWindowTitle;
        } catch (Exception e) {
            return true;  // Default enabled
        }
    }

    /**
     * Get the custom title
     */
    public static String getCustomTitle() {
        try {
            String configuredTitle = PrimalCraftConfig.getAdvanced().window.customTitle;
            return (configuredTitle == null || configuredTitle.trim().isEmpty())
                ? "Primal Craft"
                : configuredTitle.trim();
        } catch (Exception e) {
            return "Primal Craft";
        }
    }

    /**
     * Reset the title applied flag (for testing)
     */
    public static void resetTitleFlag() {
        titleApplied = false;
    }
}
