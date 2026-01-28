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
 * 🎮 Primal Craft - Day Transition Animation
 *
 * Displays animated "-- DAY XX --" text at sunrise with typewriter effect.
 * Shows for configurable duration with fade in/out animation.
 *
 * Features:
 * - Typewriter text animation
 * - Fade in/out effects
 * - Center screen display
 * - Day counter tracking (via player NBT)
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
@SuppressWarnings("deprecation")
public final class DayTransitionHandler implements HudRenderCallback {
    private static final DayTransitionHandler INSTANCE = new DayTransitionHandler();

    // Configuration
    private static final int ANIMATION_DURATION_TICKS = 200;   // 10 seconds at 20 ticks/sec
    private static final int CHARACTER_DELAY_TICKS = 3;        // Time per character for typewriter
    private static final int FADE_IN_TICKS = 30;               // Fade in duration
    private static final int FADE_OUT_TICKS = 30;              // Fade out duration
    private static final int COLOR = 0xFFFFFF;                 // White text

    // Animation state
    private int displayCounter = 0;
    private int dayCounter = 0;
    private long lastTimeOfDay = -1;
    private boolean animationActive = false;

    public static void register() {
        PrimalCraft.LOGGER.info("📅 [DAY_TRANSITION] Registering Day Transition Animation Handler");

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (PrimalCraftConfig.getAdvanced().features.dayTransitionAnimation) {
                INSTANCE.tick(client);
            }
        });

        HudRenderCallback.EVENT.register(INSTANCE);

        PrimalCraft.LOGGER.info("✅ [DAY_TRANSITION] Day Transition Handler registered");
    }

    private void tick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        net.minecraft.world.World world = client.world;
        if (world == null) {
            return;
        }

        long worldTimeOfDay = world.getTimeOfDay() % 24000;

        // Sunrise occurs around time 0 (night ends at 0, day starts)
        // We trigger between times 0 and 100 to catch the sunrise
        if (worldTimeOfDay >= 0 && worldTimeOfDay <= 100) {
            // Check if this is a new day
            if (lastTimeOfDay < 0 || lastTimeOfDay > worldTimeOfDay) {
                // New day detected
                dayCounter++;
                displayCounter = 0;
                animationActive = true;

                PrimalCraft.LOGGER.debug("📅 New day detected: Day {}", dayCounter);
            }
        }

        lastTimeOfDay = worldTimeOfDay;

        // Update animation counter
        if (animationActive) {
            displayCounter++;
            if (displayCounter >= ANIMATION_DURATION_TICKS) {
                animationActive = false;
                displayCounter = 0;
            }
        }
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if (!animationActive) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        Window window = client.getWindow();
        int screenWidth = window.getScaledWidth();
        int screenHeight = window.getScaledHeight();

        // Calculate animation progress
        float animationProgress = (float) displayCounter / ANIMATION_DURATION_TICKS;
        float alpha = calculateAlpha(animationProgress);

        // Only render if visible
        if (alpha < 0.01f) {
            return;
        }

        // Build the display text with typewriter effect
        String baseText = String.format("-- DAY %02d --", dayCounter);
        int charIndex = Math.min(baseText.length(), (displayCounter / CHARACTER_DELAY_TICKS) + 1);
        String displayText = baseText.substring(0, charIndex);

        // Calculate text position (center screen)
        int textWidth = client.textRenderer.getWidth(displayText);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 3;

        // Apply alpha to color
        int displayColor = applyAlpha(COLOR, alpha);

        // Draw the text with shadow for better visibility
        drawContext.drawTextWithShadow(
            client.textRenderer,
            Text.literal(displayText),
            x, y,
            displayColor
        );
    }

    /**
     * Calculate alpha based on animation progress
     */
    private float calculateAlpha(float progress) {
        // Fade in
        if (progress < (float) FADE_IN_TICKS / ANIMATION_DURATION_TICKS) {
            float fadeInProgress = progress / ((float) FADE_IN_TICKS / ANIMATION_DURATION_TICKS);
            return MathHelper.lerp(fadeInProgress, 0.0f, 1.0f);
        }

        // Fade out
        float fadeOutStart = 1.0f - ((float) FADE_OUT_TICKS / ANIMATION_DURATION_TICKS);
        if (progress > fadeOutStart) {
            float fadeOutProgress = (progress - fadeOutStart) / ((float) FADE_OUT_TICKS / ANIMATION_DURATION_TICKS);
            return MathHelper.lerp(fadeOutProgress, 1.0f, 0.0f);
        }

        // Full opacity in middle
        return 1.0f;
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
     * Get the current day count
     */
    public int getDayCount() {
        return dayCounter;
    }

    /**
     * Reset day counter
     */
    public void resetDayCounter() {
        dayCounter = 0;
        lastTimeOfDay = -1;
        displayCounter = 0;
        animationActive = false;
    }

    /**
     * Check if animation is active
     */
    public boolean isAnimationActive() {
        return animationActive;
    }
}
