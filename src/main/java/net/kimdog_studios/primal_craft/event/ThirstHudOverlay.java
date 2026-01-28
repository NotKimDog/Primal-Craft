package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class ThirstHudOverlay implements HudRenderCallback {
    private static double clientThirst = 20.0;
    private static double clientMaxThirst = 20.0;
    private static float smoothThirst = 20.0f;

    public static void update(double thirst, double maxThirst) {
        clientThirst = thirst;
        clientMaxThirst = maxThirst;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || client.getDebugHud().shouldShowDebugHud()) {
            return;
        }

        // Check if thirst system is enabled and should be shown
        if (!PrimalCraftConfig.getGameplay().thirstSystemEnabled ||
            !PrimalCraftConfig.getHUD().showThirstBar) {
            return;
        }

        // Hide in creative/spectator
        if (client.player.isCreative() || client.player.isSpectator()) {
            return;
        }

        // Smooth animation
        float target = (float) clientThirst;
        smoothThirst += (target - smoothThirst) * 0.15f;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        // 10 water drop icons, same as hunger bar alignment
        int iconCount = 10;
        int xStart = width / 2 + 91; // align with hunger bar
        int y = height - 59; // above hunger bar (no background, just drops)

        for (int i = 0; i < iconCount; i++) {
            int x = xStart - i * 8;
            int thirstIndex = i * 2;

            // Draw water drop shape (3x4 pixels) using rectangles
            int dropWidth = 3;
            int dropHeight = 4;
            int dropX = x + 3;
            int dropY = y + 2;

            if (smoothThirst >= thirstIndex + 2) {
                // Full drop - bright blue
                context.fill(dropX, dropY, dropX + dropWidth, dropY + dropHeight, 0xFF5599FF);
            } else if (smoothThirst >= thirstIndex + 1) {
                // Half drop - lighter blue on bottom, darker on top
                context.fill(dropX, dropY + dropHeight / 2, dropX + dropWidth, dropY + dropHeight, 0xFF5599FF);
                context.fill(dropX, dropY, dropX + dropWidth, dropY + dropHeight / 2, 0xFF2255AA);
            } else {
                // Empty drop - dark blue outline
                context.fill(dropX, dropY, dropX + dropWidth, dropY + dropHeight, 0xFF001155);
            }
        }
    }
}
