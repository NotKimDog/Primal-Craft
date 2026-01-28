package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class UnifiedHudOverlay implements HudRenderCallback {
    public static void updateThirst(double value, double max) {
        StaminaHudOverlay.updateThirst(value, max);
    }

    // Freecam countdown state
    private static int freecamTicksRemaining = 0;
    private static int freecamTotalTicks = 0;
    private static float freecamSmoothFill = 0f;

    public static void updateFreecamCountdown(int remaining, int total) {
        freecamTicksRemaining = remaining;
        freecamTotalTicks = total;
        if (remaining <= 0) {
            freecamSmoothFill = 0f;
        }
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || client.getDebugHud().shouldShowDebugHud()) return;

        renderFreecamCountdown(context, client);
    }


    private void renderFreecamCountdown(DrawContext context, MinecraftClient client) {
        if (freecamTicksRemaining <= 0 || freecamTotalTicks <= 0) return;
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int barWidth = 120;
        int barHeight = 6;
        int x = (width - barWidth) / 2;
        int y = height - 35; // above hotbar

        // Background and border
        context.fill(x - 2, y - 2, x + barWidth + 2, y + barHeight + 2, 0xAA111111);
        context.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF888888);

        float target = (float) freecamTicksRemaining / (float) freecamTotalTicks;
        freecamSmoothFill += (target - freecamSmoothFill) * 0.2f;
        int fillPixels = (int) (barWidth * freecamSmoothFill);
        context.fill(x, y, x + fillPixels, y + barHeight, 0xFF44AAFF);
        context.fill(x, y, x + fillPixels, y + 1, 0xFF88CCFF);

        int secondsLeft = Math.max(0, freecamTicksRemaining / 20);
        String label = "FREECAM: " + secondsLeft + "s";
        context.drawText(client.textRenderer, label, x + barWidth / 2 - client.textRenderer.getWidth(label) / 2, y - 10, 0xFFFFFFFF, true);
    }
}
