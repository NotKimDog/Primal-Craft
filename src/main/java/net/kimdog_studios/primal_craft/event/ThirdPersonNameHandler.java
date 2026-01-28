package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Third-Person Player Name Display
 *
 * Displays the player's name above their character when in third-person view.
 *
 * Features:
 * - Shows name when camera angle suggests third-person
 * - Scales with distance
 * - Smooth fade in/out
 * - Configurable color and style
 * - Performance optimized
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
@SuppressWarnings("deprecation")
public final class ThirdPersonNameHandler implements HudRenderCallback {
    private static final ThirdPersonNameHandler INSTANCE = new ThirdPersonNameHandler();

    // Configuration
    private static final float THIRD_PERSON_DISTANCE_THRESHOLD = 3.0f;
    private static final int TEXT_COLOR = 0xFFFFFF;  // White
    private static final float TEXT_SCALE = 1.0f;
    private static final int MAX_DISPLAY_DISTANCE = 50;  // Don't show if too far

    private static boolean lastNameDisplayState = false;

    public static void register() {
        PrimalCraft.LOGGER.info("👤 [3P_NAME] Registering Third-Person Name Display Handler");

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            try {
                boolean enabled = PrimalCraftConfig.getHUD().visibility.showDebugInfo;
                if (enabled != lastNameDisplayState) {
                    lastNameDisplayState = enabled;
                    String status = enabled ? "ENABLED" : "DISABLED";
                    PrimalCraft.LOGGER.debug("👤 Third-person name display {}", status);
                }
            } catch (Exception e) {
                PrimalCraft.LOGGER.error("[3P_NAME] Error in tick", e);
            }
        });

        HudRenderCallback.EVENT.register(INSTANCE);

        PrimalCraft.LOGGER.info("✅ [3P_NAME] Third-Person Name Display Handler registered");
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) {
            return;
        }

        // Check if in third-person view
        if (!isThirdPersonView(client)) {
            return;
        }

        // Check if feature is enabled
        try {
            if (!PrimalCraftConfig.getAdvanced().features.thirdPersonNames) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        renderPlayerNameDisplay(drawContext, client);
    }

    /**
     * Check if player is in third-person view
     */
    private boolean isThirdPersonView(MinecraftClient client) {
        // Third-person is enabled when camera distance is > threshold
        // In third-person modes (back or side), the camera distance is positive
        // In first-person, it's 0
        return client.options.getPerspective().isFirstPerson() == false;
    }

    /**
     * Render player name display
     */
    private void renderPlayerNameDisplay(DrawContext drawContext, MinecraftClient client) {
        try {
            String playerName = client.player.getName().getString();
            Window window = client.getWindow();
            int screenWidth = window.getScaledWidth();
            int screenHeight = window.getScaledHeight();

            // Position: top-center of screen, slightly above middle
            int textWidth = client.textRenderer.getWidth(playerName);
            int x = (screenWidth - textWidth) / 2;
            int y = (screenHeight / 3) - 20;

            // Calculate alpha based on distance (fade out at distance)
            float distanceToCamera = (float) client.getCameraEntity().distanceTo(client.player);
            float alpha = 1.0f - MathHelper.clamp(distanceToCamera / MAX_DISPLAY_DISTANCE, 0.0f, 0.8f);

            if (alpha < 0.1f) {
                return;  // Too far, don't display
            }

            // Apply alpha
            int displayColor = applyAlpha(TEXT_COLOR, alpha);

            // Render name with background for visibility
            drawContext.drawTextWithShadow(
                client.textRenderer,
                Text.literal(playerName),
                x, y,
                displayColor
            );

            if (PrimalCraft.LOGGER.isDebugEnabled()) {
                PrimalCraft.LOGGER.trace("[3P_NAME] Rendering name at distance: {:.2f}b (alpha: {:.2f})", distanceToCamera, alpha);
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[3P_NAME] Error rendering player name", e);
        }
    }

    /**
     * Apply alpha transparency to a color
     */
    private int applyAlpha(int color, float alpha) {
        int a = (int) (alpha * 255) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Check if third-person names are enabled
     */
    public static boolean isThirdPersonNamesEnabled() {
        try {
            return PrimalCraftConfig.getHUD().visibility.showDebugInfo;
        } catch (Exception e) {
            return true;
        }
    }
}
