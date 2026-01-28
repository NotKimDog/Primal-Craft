package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class ZoomHudOverlay implements HudRenderCallback {
    private static float displayAlpha = 0.0f;
    private static float smoothZoomDisplay = 1.0f;
    private static float zoomPulseEffect = 0.0f;
    private static float vignetteAlpha = 0.0f; // NEW: Cinematic vignette

    // NEW: Track original HUD hidden state so we can restore it
    private static Boolean originalHudHidden = null;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.getDebugHud().shouldShowDebugHud()) {
            return;
        }

        boolean zooming = ZoomHandler.isZooming();

        // Toggle HUD hidden while zooming to remove XP bar and other elements
        // Save original state once when entering zoom, restore when exiting
        if (zooming) {
            if (originalHudHidden == null) {
                originalHudHidden = client.options.hudHidden;
            }
            client.options.hudHidden = true;
        } else {
            if (originalHudHidden != null) {
                client.options.hudHidden = originalHudHidden;
                originalHudHidden = null;
            }
        }

        // INSTANT fade in/out for our overlay
        if (zooming) {
            displayAlpha = Math.min(1.0f, displayAlpha + 0.25f);
            vignetteAlpha = Math.min(0.3f, vignetteAlpha + 0.1f); // Quick vignette

            // Pulse effect when zooming in/out
            if (ZoomHandler.getZoomChangeRate() > 0.1) {
                zoomPulseEffect = 1.0f;
            }
        } else {
            displayAlpha = Math.max(0.0f, displayAlpha - 0.12f);
            vignetteAlpha = Math.max(0.0f, vignetteAlpha - 0.1f);
        }

        // Quick pulse decay
        zoomPulseEffect = Math.max(0.0f, zoomPulseEffect - 0.1f);

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Draw cinematic vignette
        if (vignetteAlpha > 0.01f) {
            drawCinematicVignette(drawContext, screenWidth, screenHeight, vignetteAlpha);
        }

        if (displayAlpha > 0.01f) {
            // INSTANT smooth zoom display
            double targetZoom = ZoomHandler.getZoomMultiplier();
            smoothZoomDisplay += (float)(targetZoom - smoothZoomDisplay) * 0.8f;

            boolean usingSpyglass = ZoomHandler.isUsingSpyglass();

            // Enhanced zoom text with better formatting
            String zoomText = String.format("%.1fx", smoothZoomDisplay);
            String prefix = "";
            String suffix = "";

            // Add zoom direction indicator with animation
            if (ZoomHandler.isZoomingIn() && zoomPulseEffect > 0.3f) {
                prefix = "▲ ";
            } else if (ZoomHandler.isZoomingOut() && zoomPulseEffect > 0.3f) {
                prefix = "▼ ";
            }

            if (usingSpyglass) {
                suffix = " 🔭";
            }

            String fullText = prefix + zoomText + suffix;
            int textWidth = client.textRenderer.getWidth(fullText);

            // Position near the bottom center when zooming, else default slightly below center
            int x = (screenWidth - textWidth) / 2;
            int baseY = zooming ? (int)(screenHeight * 0.85) : (screenHeight / 2 + 55);
            int y = baseY;

            int alpha = (int) (displayAlpha * 255);

            // Enhanced dynamic color with smooth transitions
            int baseColor = getZoomColor(smoothZoomDisplay, usingSpyglass);

            // Enhanced pulse glow effect with better sizing
            if (zoomPulseEffect > 0.3f) {
                int glowAlpha = (int) (zoomPulseEffect * 80);
                int glowColor = (glowAlpha << 24) | baseColor;
                // Draw larger, softer glow
                drawContext.fill(x - 4, y - 3, x + textWidth + 4, y + 12, glowColor);
            }

            // Draw background for better readability
            int bgAlpha = (int) (alpha * 0.6f);
            int bgColor = (bgAlpha << 24) | 0x000000;
            drawContext.fill(x - 3, y - 2, x + textWidth + 3, y + 11, bgColor);

            int color = (alpha << 24) | baseColor;

            // Draw zoom text with enhanced shadow and outline effect
            drawContext.drawText(client.textRenderer, fullText, x, y, color, true);

            // Draw subtle zoom category label
            if (smoothZoomDisplay > 1.5f) {
                drawZoomCategoryLabel(drawContext, screenWidth, screenHeight, smoothZoomDisplay, alpha, usingSpyglass);
            }

            // Draw enhanced zoom level bar near the bottom as well
            if (smoothZoomDisplay > 1.5f) {
                drawEnhancedZoomBar(drawContext, screenWidth, screenHeight, smoothZoomDisplay, alpha);
            }
        }

        // If we're hiding the HUD, we need to draw our own minimal crosshair to keep it visible
        if (zooming) {
            drawMinimalCrosshair(drawContext, screenWidth, screenHeight, (int)(displayAlpha * 255));
        }
    }

    // Helper method to get zoom color based on level
    private int getZoomColor(float zoom, boolean usingSpyglass) {
        if (usingSpyglass) {
            return 0x00FFFF; // Cyan for spyglass
        } else if (zoom > 48) {
            return 0xFF0000; // Bright red for extreme zoom
        } else if (zoom > 32) {
            return 0xFF6B6B; // Red for very high zoom
        } else if (zoom > 16) {
            return 0xFFD700; // Gold for high zoom
        } else if (zoom > 8) {
            return 0x90EE90; // Light green for medium zoom
        } else {
            return 0xFFFFFF; // White for normal zoom
        }
    }

    // Draw zoom category label (e.g., "EXTREME", "HIGH", etc.)
    private void drawZoomCategoryLabel(DrawContext context, int screenWidth, int screenHeight,
                                       float zoom, int alpha, boolean usingSpyglass) {
        String category = "";

        if (usingSpyglass && zoom > 64) {
            category = "ULTRA SPYGLASS";
        } else if (zoom > 48) {
            category = "EXTREME";
        } else if (zoom > 32) {
            category = "VERY HIGH";
        } else if (zoom > 16) {
            category = "HIGH";
        } else if (zoom > 8) {
            category = "MEDIUM";
        } else if (zoom > 4) {
            category = "STANDARD";
        } else {
            category = "LOW";
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int labelWidth = client.textRenderer.getWidth(category);
        int labelX = (screenWidth - labelWidth) / 2;
        // Place the label slightly above the bottom text when zooming
        int labelY = (int)(screenHeight * 0.85) - 15;

        int labelAlpha = (int) (alpha * 0.7f);
        int labelColor = (labelAlpha << 24) | 0xAAAAAA;

        // Draw small category label
        context.drawText(client.textRenderer, category, labelX, labelY, labelColor, true);
    }

    private void drawEnhancedZoomBar(DrawContext context, int screenWidth, int screenHeight, float zoom, int alpha) {
        int barWidth = 140; // Even wider bar for better visibility
        int barHeight = 4; // Thicker bar
        int barX = (screenWidth - barWidth) / 2;
        // Place the bar near the bottom
        int barY = (int)(screenHeight * 0.85) + 20;

        // Border for bar
        int borderColor = (alpha << 24) | 0x555555;
        context.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, borderColor);

        // Background bar (dark)
        int bgColor = (alpha / 2 << 24) | 0x222222;
        context.fill(barX, barY, barX + barWidth, barY + barHeight, bgColor);

        // Foreground bar (shows zoom level) with gradient
        float maxZoom = ZoomHandler.isUsingSpyglass() ? 128.0f : 64.0f;
        int fillWidth = (int) ((zoom / maxZoom) * barWidth);

        // Get bar color based on zoom
        int barColor = (alpha << 24) | 0x00FF00; // Green bar
        if (zoom > 48) barColor = (alpha << 24) | 0xFF0000; // Red for extreme
        else if (zoom > 32) barColor = (alpha << 24) | 0xFF6B6B; // Red for high
        else if (zoom > 16) barColor = (alpha << 24) | 0xFFD700; // Gold

        // Draw filled portion
        context.fill(barX, barY, barX + fillWidth, barY + barHeight, barColor);

        // Draw gradient overlay for depth
        for (int i = 0; i < barHeight / 2; i++) {
            int gradAlpha = (int) (alpha * 0.3f * (1.0f - (float) i / (barHeight / 2)));
            int gradColor = (gradAlpha << 24) | 0xFFFFFF;
            context.fill(barX, barY + i, barX + fillWidth, barY + i + 1, gradColor);
        }

        // Add preset markers on the bar
        drawPresetMarkers(context, barX, barY, barWidth, barHeight, maxZoom, alpha);

        // Draw current zoom marker
        if (fillWidth > 2) {
            int markerColor = (alpha << 24) | 0xFFFFFF;
            context.fill(barX + fillWidth - 1, barY - 2, barX + fillWidth + 1, barY + barHeight + 2, markerColor);
        }
    }

    private void drawPresetMarkers(DrawContext context, int barX, int barY, int barWidth, int barHeight, float maxZoom, int alpha) {
        double[] presets = {2.0, 4.0, 8.0, 16.0, 32.0, 64.0};

        for (double preset : presets) {
            if (preset <= maxZoom) {
                int markerX = barX + (int) ((preset / maxZoom) * barWidth);

                // Draw tick mark above bar
                int tickColor = (alpha / 2 << 24) | 0xCCCCCC;
                context.fill(markerX, barY - 3, markerX + 1, barY, tickColor);

                // Draw subtle line on bar
                int lineColor = (alpha / 4 << 24) | 0xFFFFFF;
                context.fill(markerX, barY, markerX + 1, barY + barHeight, lineColor);
            }
        }
    }

    // NEW: Minimal custom crosshair rendering while HUD is hidden
    private void drawMinimalCrosshair(DrawContext context, int screenWidth, int screenHeight, int alpha) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int color = (alpha << 24) | 0xFFFFFF; // white

        // Simple plus-shaped crosshair
        int size = 6;
        int thickness = 1;
        // Horizontal line
        context.fill(centerX - size, centerY - thickness, centerX + size, centerY + thickness, color);
        // Vertical line
        context.fill(centerX - thickness, centerY - size, centerX + thickness, centerY + size, color);
    }

    // NEW: Cinematic vignette effect (darkens edges for film look)
    private void drawCinematicVignette(DrawContext context, int screenWidth, int screenHeight, float alpha) {
        int vignetteDepth = Math.min(screenWidth, screenHeight) / 3;
        int baseAlpha = (int) (alpha * 255);

        // Top vignette
        for (int i = 0; i < vignetteDepth; i++) {
            float factor = 1.0f - ((float) i / vignetteDepth);
            int lineAlpha = (int) (baseAlpha * factor * factor);
            int color = (lineAlpha << 24);
            context.fill(0, i, screenWidth, i + 1, color);
        }

        // Bottom vignette
        for (int i = 0; i < vignetteDepth; i++) {
            float factor = 1.0f - ((float) i / vignetteDepth);
            int lineAlpha = (int) (baseAlpha * factor * factor);
            int color = (lineAlpha << 24);
            context.fill(0, screenHeight - i - 1, screenWidth, screenHeight - i, color);
        }

        // Left vignette
        for (int i = 0; i < vignetteDepth; i++) {
            float factor = 1.0f - ((float) i / vignetteDepth);
            int lineAlpha = (int) (baseAlpha * factor * factor);
            int color = (lineAlpha << 24);
            context.fill(i, 0, i + 1, screenHeight, color);
        }

        // Right vignette
        for (int i = 0; i < vignetteDepth; i++) {
            float factor = 1.0f - ((float) i / vignetteDepth);
            int lineAlpha = (int) (baseAlpha * factor * factor);
            int color = (lineAlpha << 24);
            context.fill(screenWidth - i - 1, 0, screenWidth - i, screenHeight, color);
        }
    }

    // Helper method to check if HUD should be hidden
    public static boolean shouldHideHud() {
        return ZoomHandler.isZooming() && displayAlpha > 0.5f;
    }
}
