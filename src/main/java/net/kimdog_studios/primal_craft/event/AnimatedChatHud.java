package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.Deque;

public class AnimatedChatHud implements HudRenderCallback {
    public static class ChatEntry {
        public final Text text;
        public final long startTimeMs;
        public final String role;
        public final int priority; // Higher = stays longer

        public ChatEntry(Text text) {
            this.text = text;
            this.startTimeMs = System.currentTimeMillis();
            this.role = "default";
            this.priority = 0;
        }

        public ChatEntry(Text text, String role, int priority) {
            this.text = text;
            this.startTimeMs = System.currentTimeMillis();
            this.role = role;
            this.priority = priority;
        }
    }

    private static final Deque<ChatEntry> entries = new ArrayDeque<>();
    private static final int MAX_ENTRIES = 8;
    private static final long BASE_LIFETIME_MS = 15_000; // messages live 15s
    private static final int FADE_IN_MS = 400;
    private static final int FADE_OUT_MS = 1000;
    private static final int SLIDE_PIXELS = 60;
    private static long lastMessageTime = 0;

    public static void push(Text styled) {
        push(styled, "default", 0);
    }

    public static void push(Text styled, String role, int priority) {
        if (entries.size() >= MAX_ENTRIES) {
            entries.removeFirst();
        }
        entries.addLast(new ChatEntry(styled, role, priority));

        // Play sound effect based on role
        long now = System.currentTimeMillis();
        if (now - lastMessageTime > 100) { // Throttle sounds
            playMessageSound(role);
            lastMessageTime = now;
        }
    }

    private static void playMessageSound(String role) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        net.minecraft.sound.SoundEvent sound = switch(role) {
            case "GAME", "SYS" -> net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_BELL.value();
            case "VEIN" -> net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value();
            default -> net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_HAT.value();
        };

        client.player.playSound(sound, 0.3f, 1.2f + (float)(Math.random() * 0.2));
    }

    private static boolean isExpired(ChatEntry e) {
        long lifetime = BASE_LIFETIME_MS + (e.priority * 5000L); // +5s per priority level
        return System.currentTimeMillis() - e.startTimeMs > lifetime;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.getDebugHud().shouldShowDebugHud()) return;

        // Drop expired entries up front
        while (!entries.isEmpty() && isExpired(entries.peekFirst())) {
            entries.removeFirst();
        }

        int height = client.getWindow().getScaledHeight();
        int width = client.getWindow().getScaledWidth();
        int xBase = 8;
        int yBase = height - 80;
        int lineSpacing = 14;

        // Get mouse position for hover effects
        double mouseX = client.mouse.getX() * (double)width / client.getWindow().getWidth();
        double mouseY = client.mouse.getY() * (double)height / client.getWindow().getHeight();

        int i = 0;
        for (ChatEntry e : entries) {
            long elapsed = System.currentTimeMillis() - e.startTimeMs;
            long lifetime = BASE_LIFETIME_MS + (e.priority * 5000L);

            // Smooth cubic ease-in-out for slide
            float tIn = Math.min(1f, elapsed / (float) FADE_IN_MS);
            tIn = tIn < 0.5f ? 4f * tIn * tIn * tIn : 1f - (float)Math.pow(-2f * tIn + 2f, 3f) / 2f;

            float fadeOut = 1f;
            if (elapsed > lifetime - FADE_OUT_MS) {
                float fadeT = (lifetime - elapsed) / (float) FADE_OUT_MS;
                fadeOut = fadeT * fadeT; // ease-in fade
            }

            int alpha = (int) (255 * tIn * fadeOut);
            if (alpha <= 0) {
                i++;
                continue;
            }

            int x = xBase - (int) ((1f - tIn) * SLIDE_PIXELS);
            int y = yBase - i * lineSpacing;

            int textWidth = client.textRenderer.getWidth(e.text);
            int padding = 6;

            // Hover detection
            boolean isHovered = mouseX >= x - padding && mouseX <= x + textWidth + padding &&
                               mouseY >= y - 2 && mouseY <= y + 10;
            float hoverGlow = isHovered ? 0.3f : 0f;

            // Role badge pulsing effect
            float pulsePhase = (System.currentTimeMillis() % 2000) / 2000f;
            float pulse = (float)(Math.sin(pulsePhase * Math.PI * 2) * 0.15 + 0.85);

            // Multi-layer shadow for depth
            int shadowAlpha1 = (int) ((100 + hoverGlow * 50) * tIn * fadeOut);
            int shadowAlpha2 = (int) ((60 + hoverGlow * 30) * tIn * fadeOut);
            context.fill(x - padding - 1, y - 3, x + textWidth + padding + 1, y + 12, (shadowAlpha1 << 24) | 0x000000);
            context.fill(x - padding - 2, y - 4, x + textWidth + padding + 2, y + 13, (shadowAlpha2 << 24) | 0x000000);

            // Blur/frosted glass effect background
            int blurAlpha = (int) ((40 + hoverGlow * 20) * tIn * fadeOut);
            context.fill(x - padding - 3, y - 5, x + textWidth + padding + 3, y + 14, (blurAlpha << 24) | 0x0a0a0a);

            // Gradient background (darker to lighter top-to-bottom)
            int bgAlphaTop = (int) ((200 + hoverGlow * 30) * tIn * fadeOut);
            int bgAlphaBottom = (int) ((160 + hoverGlow * 20) * tIn * fadeOut);
            int bgColorTop = (bgAlphaTop << 24) | 0x0a0a0a;
            int bgColorBottom = (bgAlphaBottom << 24) | 0x1a1a1a;

            // Draw gradient manually (top half darker, bottom half lighter)
            context.fill(x - padding, y - 2, x + textWidth + padding, y + 3, bgColorTop);
            context.fill(x - padding, y + 3, x + textWidth + padding, y + 10, bgColorBottom);

            // Animated accent line on top (role-colored with pulse)
            int accentAlpha = (int) ((180 + hoverGlow * 50) * tIn * fadeOut * pulse);
            int roleAccentColor = getRoleAccentColor(e.role);
            int accentColor = (accentAlpha << 24) | roleAccentColor;
            context.fill(x - padding, y - 2, x + textWidth + padding, y - 1, accentColor);

            // Role-colored glow line on left edge with pulse
            int glowAlpha = (int) ((140 + hoverGlow * 60) * tIn * fadeOut * pulse);
            int glowColor = (glowAlpha << 24) | roleAccentColor;
            context.fill(x - padding, y - 2, x - padding + 1, y + 10, glowColor);

            // Hover highlight
            if (isHovered) {
                int hoverAlpha = (int) (40 * tIn * fadeOut);
                context.fill(x - padding, y - 2, x + textWidth + padding, y + 10, (hoverAlpha << 24) | 0xFFFFFF);
            }

            // Text with slight shadow for readability
            int textShadowAlpha = (int) (100 * tIn * fadeOut);
            int textShadowColor = (textShadowAlpha << 24) | 0x000000;
            context.drawText(client.textRenderer, e.text, x + 1, y + 1, textShadowColor, false);

            int color = (alpha << 24) | 0xFFFFFF;
            context.drawText(client.textRenderer, e.text, x, y, color, false);

            // Optional: Draw timestamp
            if (isHovered) {
                long secondsAgo = elapsed / 1000;
                String timeStr = secondsAgo < 60 ? secondsAgo + "s" : (secondsAgo / 60) + "m";
                int timeX = x + textWidth + padding + 4;
                int timeColor = ((alpha / 2) << 24) | 0x888888;
                context.drawText(client.textRenderer, timeStr, timeX, y, timeColor, false);
            }

            i++;
        }
    }

    private static int getRoleAccentColor(String role) {
        return switch(role) {
            case "MEMBER" -> 0x888888;
            case "SYS" -> 0x5599FF;
            case "GAME" -> 0x7744DD;
            case "VEIN" -> 0x44DD77;
            default -> 0x4a4a4a;
        };
    }
}
