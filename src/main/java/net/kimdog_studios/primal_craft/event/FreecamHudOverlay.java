package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class FreecamHudOverlay implements HudRenderCallback {
    private static int ticksRemaining = 0;
    private static int totalTicks = 0;
    private static float smoothFill = 0f;

    public static void updateCountdown(int ticksLeft, int total) {
        ticksRemaining = ticksLeft;
        totalTicks = total;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || client.getDebugHud().shouldShowDebugHud()) return;
        if (ticksRemaining <= 0 || totalTicks <= 0) return;

        // Bottom center bar above hotbar
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int barWidth = 120;
        int barHeight = 6;
        int x = (width - barWidth) / 2;
        int y = height - 35;

        // Background
        context.fill(x - 2, y - 2, x + barWidth + 2, y + barHeight + 2, 0xAA111111);
        // Border
        context.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF888888);

        // Fill percent
        float target = (float) ticksRemaining / (float) totalTicks;
        smoothFill += (target - smoothFill) * 0.2f;
        int fillPixels = (int) (barWidth * smoothFill);
        // Fill
        context.fill(x, y, x + fillPixels, y + barHeight, 0xFF44AAFF);
        // Highlight
        context.fill(x, y, x + fillPixels, y + 1, 0xFF88CCFF);

        // Text
        int secondsLeft = ticksRemaining / 20;
        String label = "FREECAM: " + secondsLeft + "s";
        context.drawText(client.textRenderer, label, x + barWidth / 2 - client.textRenderer.getWidth(label) / 2, y - 10, 0xFFFFFFFF, true);
    }
}
