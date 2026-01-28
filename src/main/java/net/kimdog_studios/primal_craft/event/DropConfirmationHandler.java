package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.util.math.MatrixStack;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Drop Confirmation Handler
 *
 * Adds a confirmation dialog when dropping items.
 *
 * Features:
 * - Confirmation prompt before drop
 * - Configurable timeout
 * - Quick re-drop option
 * - Safety for valuable items
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class DropConfirmationHandler {
    private DropConfirmationHandler() {}

    private static final int CONFIRMATION_TIMEOUT_TICKS = 100;  // 5 seconds
    private static int confirmationCounter = 0;
    private static boolean showConfirmation = false;
    private static String lastDropItemName = "";
    private static boolean lastConfirmationState = false;

    public static void register() {
        PrimalCraft.LOGGER.info("⚠️  [DROP_CONFIRM] Registering Drop Confirmation Handler");

        ClientTickEvents.END_CLIENT_TICK.register(DropConfirmationHandler::onClientTick);

        PrimalCraft.LOGGER.info("✅ [DROP_CONFIRM] Drop Confirmation Handler registered");
    }

    private static void onClientTick(MinecraftClient client) {
        try {
            boolean confirmationEnabled = isDropConfirmationEnabled();
            if (confirmationEnabled != lastConfirmationState) {
                lastConfirmationState = confirmationEnabled;
                String status = confirmationEnabled ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.debug("⚠️  Drop confirmation {}", status);
            }

            if (!confirmationEnabled) {
                showConfirmation = false;
                return;
            }

            // Update confirmation timeout
            if (showConfirmation) {
                confirmationCounter++;
                if (confirmationCounter >= CONFIRMATION_TIMEOUT_TICKS) {
                    showConfirmation = false;
                    confirmationCounter = 0;
                    PrimalCraft.LOGGER.debug("⚠️  Drop confirmation timed out");
                }
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DROP_CONFIRM] Error in tick", e);
        }
    }

    /**
     * Show drop confirmation dialog
     */
    public static void showDropConfirmation(String itemName) {
        if (!isDropConfirmationEnabled()) {
            return;
        }

        showConfirmation = true;
        confirmationCounter = 0;
        lastDropItemName = itemName;

        PrimalCraft.LOGGER.debug("⚠️  [DROP_CONFIRM] Drop confirmation shown for: {}", itemName);
    }

    /**
     * Check if drop confirmation is enabled
     */
    public static boolean isDropConfirmationEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.dropConfirmation;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set drop confirmation enabled state
     */
    public static void setDropConfirmationEnabled(boolean enabled) {
        try {
            PrimalCraftConfig.getAdvanced().performance.enableParticles = enabled;
            PrimalCraftConfig.save();
            lastConfirmationState = enabled;

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("⚠️  [DROP_CONFIRM] Drop confirmation {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DROP_CONFIRM] Failed to toggle drop confirmation", e);
        }
    }

    /**
     * Check if confirmation is currently shown
     */
    public static boolean isConfirmationShown() {
        return showConfirmation;
    }

    /**
     * Get the item name being confirmed
     */
    public static String getConfirmingItemName() {
        return lastDropItemName;
    }

    /**
     * Get confirmation progress (0-1)
     */
    public static float getConfirmationProgress() {
        return (float) confirmationCounter / CONFIRMATION_TIMEOUT_TICKS;
    }

    /**
     * Confirm the drop
     */
    public static void confirmDrop() {
        showConfirmation = false;
        confirmationCounter = 0;
        PrimalCraft.LOGGER.debug("⚠️  [DROP_CONFIRM] Drop confirmed");
    }

    /**
     * Cancel the drop
     */
    public static void cancelDrop() {
        showConfirmation = false;
        confirmationCounter = 0;
        PrimalCraft.LOGGER.debug("⚠️  [DROP_CONFIRM] Drop cancelled");
    }
}
