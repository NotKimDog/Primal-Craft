package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Client-side weather notification HUD with animations
 */
public class WeatherNotificationHud implements HudRenderCallback {
    private static final List<WeatherNotification> notifications = new ArrayList<>();

    public static class WeatherNotification {
        public String message;
        public int color;
        public long spawnTime;
        public float alpha;
        public float offsetY;
        public float scaleX;
        public float rotateAngle;
        public float glowIntensity;

        public WeatherNotification(String message, int color) {
            this.message = message;
            this.color = color;
            this.spawnTime = System.currentTimeMillis();
            this.alpha = 0f;
            this.offsetY = -40f;
            this.scaleX = 0.7f;
            this.rotateAngle = 0f;
            this.glowIntensity = 0f;
        }

        public boolean update() {
            long age = System.currentTimeMillis() - spawnTime;

            // Elegant bounce-in with rotation (0-700ms)
            if (age < 700) {
                float progress = age / 700f;
                alpha = (float)Math.pow(progress, 0.5f); // Ease-in
                offsetY = -40f + (float)Math.pow(progress, 1.2f) * 40f; // Bounce effect
                scaleX = 0.7f + (float)Math.pow(progress, 0.8f) * 0.3f;
                rotateAngle = progress * 2f; // Slight rotation
                glowIntensity = progress * 0.6f;
            }
            // Hold with sophisticated animations (700-3800ms)
            else if (age < 3800) {
                alpha = 1f;
                offsetY = 0f;
                scaleX = 1f;
                rotateAngle = 0f;

                // Multiple layered animations
                float holdTime = (age - 700) / 3100f;

                // Subtle breathing effect
                glowIntensity = 0.5f + ((float)Math.sin(holdTime * Math.PI * 3) * 0.2f);

                // Gentle scale pulse
                scaleX = 1f + ((float)Math.sin(holdTime * Math.PI * 2) * 0.015f);
            }
            // Elegant fade-out with rotation (3800-4800ms)
            else if (age < 4800) {
                float fadeProgress = (age - 3800) / 1000f;
                alpha = 1f - (float)Math.pow(fadeProgress, 1.2f);
                offsetY = fadeProgress * 20f;
                scaleX = 1f + (fadeProgress * 0.1f);
                rotateAngle = fadeProgress * -3f; // Counter-rotate on exit
                glowIntensity = 0.5f * (1f - fadeProgress);
            }
            // Remove
            else {
                return false;
            }

            return true;
        }
    }

    public static void showNotification(String message, int color) {
        notifications.add(new WeatherNotification(message, color));

        // Play notification sound
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.playSound(net.kimdog_studios.primal_craft.sound.ModSounds.NOTIFICATION_PING, 1.0f, 1.0f);
        }
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        Iterator<WeatherNotification> iterator = notifications.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            WeatherNotification notif = iterator.next();
            if (!notif.update()) {
                iterator.remove();
                continue;
            }

            // Modern, larger card design
            int baseWidth = 280;
            int notifWidth = (int)(baseWidth * notif.scaleX);
            int notifHeight = 36;
            int x = (screenW - notifWidth) / 2;
            int y = 20 + (index * 44) + (int) notif.offsetY;

            int alpha = Math.max(0, Math.min(255, (int) (notif.alpha * 255)));

            // Clean, modern color scheme
            int bgColor = (int)(alpha * 0.85f) << 24 | 0x0a0a0a; // Nearly black, semi-transparent
            int borderColor = (alpha << 24) | (notif.color & 0xFFFFFF);
            int glowColor = (int)(alpha * 0.3f) << 24 | (notif.color & 0xFFFFFF);

            // Soft drop shadow for depth
            context.fill(x + 2, y + 2, x + notifWidth + 2, y + notifHeight + 2, (int)(alpha * 0.4f) << 24);

            // Single clean border
            context.fill(x - 1, y - 1, x + notifWidth + 1, y + notifHeight + 1, borderColor);

            // Main background
            context.fill(x, y, x + notifWidth, y + notifHeight, bgColor);

            // Subtle top glow
            long time = System.currentTimeMillis();
            float pulse = (float)(Math.sin(time * 0.003 + index) * 0.3f + 0.7f);
            int pulseGlow = (int)(alpha * pulse * 0.25f) << 24 | (notif.color & 0xFFFFFF);
            context.fillGradient(x, y - 3, x + notifWidth, y, pulseGlow, 0x00000000);

            // Left accent stripe (thinner, more elegant)
            context.fill(x, y, x + 2, y + notifHeight, (alpha << 24) | (notif.color & 0xFFFFFF));

            // Parse message
            String[] parts = notif.message.split("\\|");
            String icon = getWeatherIcon(parts.length > 0 ? parts[0] : notif.message);

            int textColor = (alpha << 24) | 0xFFFFFF;
            int iconX = x + 8;
            int iconY = y + (notifHeight / 2) - 4;

            // Draw icon (larger)
            var matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(iconX, iconY);
            matrices.scale(1.2f, 1.2f);
            context.drawText(client.textRenderer, icon, 0, 0, textColor, false);
            matrices.popMatrix();

            // Main text (single line, clean)
            int textX = x + 26;
            int textY = y + (notifHeight / 2) - 4;

            if (parts.length > 0) {
                String mainText = parts[0].trim();
                if (mainText.length() > 35) mainText = mainText.substring(0, 32) + "...";
                context.drawText(client.textRenderer, mainText, textX, textY, textColor, false);
            }

            index++;
        }
    }

    private String getWeatherIcon(String message) {
        String msg = message.toLowerCase();
        if (msg.contains("blizzard") || msg.contains("freeze")) return "❄";
        if (msg.contains("snow")) return "🌨";
        if (msg.contains("thunder") || msg.contains("storm")) return "⚡";
        if (msg.contains("rain")) return "🌧";
        if (msg.contains("heat") || msg.contains("scorch")) return "🔥";
        if (msg.contains("cold") || msg.contains("frigid")) return "🌙";
        if (msg.contains("dust")) return "💨";
        if (msg.contains("wind") || msg.contains("gale")) return "🌪";
        if (msg.contains("fog")) return "🌫";
        if (msg.contains("exposed")) return "☀";
        if (msg.contains("sheltered")) return "🏠";
        return "🌍";
    }
}
