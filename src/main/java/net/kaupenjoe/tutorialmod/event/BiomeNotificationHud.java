package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.kaupenjoe.tutorialmod.sound.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple, centered biome notification with a soft fade/slide animation.
 */
public class BiomeNotificationHud implements HudRenderCallback {
    private static final List<Entry> entries = new ArrayList<>();

    private static final long FADE_IN_MS = 250;
    private static final long HOLD_MS = 2500;
    private static final long FADE_OUT_MS = 400;
    private static final float SCALE = 1.4f; // Larger text

    private record Entry(String message, int color, long spawnTime) {}

    public static void showNotification(String message, int color) {
        // Only keep one notification at a time; replace the current if another arrives
        entries.clear();
        entries.add(new Entry(message, color, System.currentTimeMillis()));
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.playSound(ModSounds.BIOME_SOUND, 1.0f, 1.0f);
        }
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        Iterator<Entry> it = entries.iterator();
        int idx = 0;
        while (it.hasNext()) {
            Entry e = it.next();
            long age = System.currentTimeMillis() - e.spawnTime;
            if (age > FADE_IN_MS + HOLD_MS + FADE_OUT_MS) {
                it.remove();
                continue;
            }

            float alpha;
            float offsetY;
            if (age < FADE_IN_MS) {
                float t = age / (float) FADE_IN_MS;
                alpha = t;
                offsetY = MathHelper.lerp(t, 12f, 0f);
            } else if (age < FADE_IN_MS + HOLD_MS) {
                alpha = 1f;
                offsetY = 0f;
            } else {
                float t = (age - FADE_IN_MS - HOLD_MS) / (float) FADE_OUT_MS;
                alpha = 1f - t;
                offsetY = MathHelper.lerp(t, 0f, -10f);
            }

            // Calculate proper centered position accounting for scale
            int textW = client.textRenderer.getWidth(e.message);
            int textH = client.textRenderer.fontHeight;

            // Center position before scaling
            float centerX = screenW / 2f;
            float centerY = (screenH / 2f - 20 + idx * 18) + offsetY;

            // Calculate scaled text dimensions for proper centering
            float scaledWidth = textW * SCALE;
            float scaledHeight = textH * SCALE;

            // Position to draw at (top-left corner of scaled text)
            int drawX = (int)(centerX - scaledWidth / 2f);
            int drawY = (int)(centerY - scaledHeight / 2f);

            int argb = ((int)(alpha * 255) << 24) | (e.color & 0xFFFFFF);

            // Apply transformations in correct order
            var matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(drawX, drawY);
            matrices.scale(SCALE, SCALE);

            // Draw at origin since we've already positioned via translate
            context.drawText(client.textRenderer, e.message, 0, 0, argb, false);
            matrices.popMatrix();

            idx++;
        }
    }
}
