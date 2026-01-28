package net.kimdog_studios.primal_craft.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class ThirstHudOverlay {
    private static double current = 20.0;
    private static double max = 20.0;

    private ThirstHudOverlay() {}

    public static void update(double value, double maxValue) {
        current = value;
        max = maxValue;
    }

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;
        if (mc.options.hudHidden) return;

        int screenWidth = ctx.getScaledWindowWidth();
        int screenHeight = ctx.getScaledWindowHeight();

        int barWidth = 100;
        int barHeight = 8;
        int x = screenWidth / 2 - barWidth / 2;
        int y = screenHeight - 49; // above vanilla hotbar/health

        double ratio = Math.max(0.0, Math.min(1.0, current / Math.max(1.0, max)));
        int filled = (int) Math.round(barWidth * ratio);

        int bgColor = 0xAA000000; // semi-transparent black
        int fgColor = ratio > 0.5 ? 0xFF00A8FF : (ratio > 0.2 ? 0xFFFFA000 : 0xFFFF3030);

        // Background
        ctx.fill(x, y, x + barWidth, y + barHeight, bgColor);
        // Fill
        ctx.fill(x, y, x + filled, y + barHeight, fgColor);

        // Segment markers every 10%
        for (int i = 1; i < 10; i++) {
            int sx = x + i * (barWidth / 10);
            ctx.fill(sx, y, sx + 1, y + barHeight, 0x22000000);
        }

        // Text label
        String label = String.format("Thirst %.1f/%.0f", current, max);
        ctx.drawText(mc.textRenderer, label, x + barWidth + 6, y + 1, 0xFFFFFFFF, false);
    }
}
