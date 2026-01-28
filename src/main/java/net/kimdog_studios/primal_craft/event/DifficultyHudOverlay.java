package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

/**
 * 🎮 Primal Craft - Difficulty HUD Overlay
 *
 * Client-side HUD overlay showing current difficulty level and scaling metrics.
 * Displays difficulty indicator, multipliers, and progression info.
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public class DifficultyHudOverlay implements HudRenderCallback {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static boolean showDifficultyHud = false;
    private static String difficultyLevel = "Normal";
    private static int scalingLevel = 0;
    private static float[] multipliers = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};

    public static void register() {
        HudRenderCallback.EVENT.register(new DifficultyHudOverlay());
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        try {
            if (!shouldRender()) {
                return;
            }

            int screenWidth = drawContext.getScaledWindowWidth();
            int screenHeight = drawContext.getScaledWindowHeight();

            // Position: Top-right corner with padding
            int x = screenWidth - 180;
            int y = 20;

            // Background panel
            drawContext.fill(x - 5, y - 5, x + 175, y + 75, 0x9A000000); // Semi-transparent black

            // Border
            drawContext.fill(x - 6, y - 6, x - 5, y + 76, 0xFFFFFF00); // Yellow border (top-left)
            drawContext.fill(x + 175, y - 6, x + 176, y + 76, 0xFFFFFF00); // Yellow border (right)
            drawContext.fill(x - 6, y + 75, x + 176, y + 76, 0xFFFFFF00); // Yellow border (bottom)

            // Title
            drawContext.drawTextWithBackground(
                CLIENT.textRenderer,
                Text.literal("§6▪ Difficulty Status"),
                x + 2,
                y + 2,
                0xFFFFFF,
                0x00000000
            );

            // Difficulty level and scaling indicator
            String difficultyText = String.format("§e%s §7(Level %d)", difficultyLevel, scalingLevel);
            drawContext.drawTextWithBackground(
                CLIENT.textRenderer,
                Text.literal(difficultyText),
                x + 2,
                y + 14,
                0xFFFFFF,
                0x00000000
            );

            // Multipliers display (compact format)
            String multiplierText = String.format(
                "§7Multipliers: §eStamina%.1f §7| §eThirst%.1f",
                multipliers[0], multipliers[1]
            );
            drawContext.drawTextWithBackground(
                CLIENT.textRenderer,
                Text.literal(multiplierText),
                x + 2,
                y + 26,
                0xFFFFFF,
                0x00000000
            );

            String multiplierText2 = String.format(
                "§eTemp%.1f §7| §eHazard%.1f",
                multipliers[2], multipliers[3]
            );
            drawContext.drawTextWithBackground(
                CLIENT.textRenderer,
                Text.literal(multiplierText2),
                x + 2,
                y + 36,
                0xFFFFFF,
                0x00000000
            );

            String multiplierText3 = String.format(
                "§eDmg%.1f §7| §eMobs%.1f",
                multipliers[4], multipliers[5]
            );
            drawContext.drawTextWithBackground(
                CLIENT.textRenderer,
                Text.literal(multiplierText3),
                x + 2,
                y + 46,
                0xFFFFFF,
                0x00000000
            );

            // Press Shift+D to toggle (info text)
            drawContext.drawTextWithBackground(
                CLIENT.textRenderer,
                Text.literal("§7Press §eShift+D §7to toggle"),
                x + 2,
                y + 58,
                0xFFFFFF,
                0x00000000
            );

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DIFFICULTY_HUD] Error rendering HUD", e);
        }
    }

    private static boolean shouldRender() {
        var player = CLIENT.player;
        if (player == null) {
            return false;
        }

        // Don't render if config says not to
        if (!PrimalCraftConfig.getHUD().visibility.showDebugInfo && !showDifficultyHud) {
            return false;
        }

        // Don't render if a screen is open
        return CLIENT.currentScreen == null;
    }

    /**
     * Update difficulty HUD data from server
     */
    public static void updateDifficultyHud(String level, int scaling, float[] newMultipliers) {
        difficultyLevel = level;
        scalingLevel = scaling;
        if (newMultipliers.length >= 6) {
            System.arraycopy(newMultipliers, 0, multipliers, 0, 6);
        }
    }

    /**
     * Toggle difficulty HUD visibility
     */
    public static void toggleVisibility() {
        showDifficultyHud = !showDifficultyHud;
        String state = showDifficultyHud ? "§aShown" : "§cHidden";
        MinecraftClient.getInstance().player.sendMessage(
            Text.of("§6[Difficulty] HUD " + state),
            true
        );
    }

    /**
     * Check if HUD is currently visible
     */
    public static boolean isVisible() {
        return showDifficultyHud;
    }
}
