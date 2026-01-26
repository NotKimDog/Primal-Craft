package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

public class TypingIndicatorHud implements HudRenderCallback {
    private static class TypingEntry {
        public String playerName;
        public String text;
        public long lastUpdateMs;
        public boolean soundPlayed; // Track if we already played the sound for this entry

        public TypingEntry(String playerName, String text) {
            this.playerName = playerName;
            this.text = text;
            this.lastUpdateMs = System.currentTimeMillis();
            this.soundPlayed = false;
        }
    }

    private static final Map<String, TypingEntry> typingPlayers = new HashMap<>();
    private static final long DISPLAY_TIMEOUT_MS = 2000; // Show for 2 seconds after no update
    private static final int DOT_ANIMATION_SPEED = 500; // ms per dot cycle

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        long currentTime = System.currentTimeMillis();

        // Remove expired typing indicators
        typingPlayers.entrySet().removeIf(entry -> currentTime - entry.getValue().lastUpdateMs > DISPLAY_TIMEOUT_MS);

        if (typingPlayers.isEmpty()) return;

        int screenWidth = drawContext.getScaledWindowWidth();
        int screenHeight = drawContext.getScaledWindowHeight();

        // Position: bottom-left area, above the hotbar
        int x = 10;
        int y = screenHeight - 40;

        // Draw all typing players
        int offset = 0;
        for (TypingEntry entry : typingPlayers.values()) {
            drawTypingIndicator(drawContext, entry, x, y - offset, currentTime, client);
            offset += 18; // Space between each typing indicator
        }
    }

    private void drawTypingIndicator(DrawContext drawContext, TypingEntry entry, int x, int y, long currentTime, MinecraftClient client) {
        // Animated dots: . .. ... (cycle)
        long cycleTime = (currentTime / DOT_ANIMATION_SPEED) % 4;
        String dots = ".".repeat((int) cycleTime);

        // Build the display text - just name and typing indicator
        String fullText = entry.playerName + " is typing" + dots;

        // Calculate text width for background (now with space for head)
        int textWidth = client.textRenderer.getWidth(fullText);
        int padding = 8;
        int headSize = 18; // Size of player head
        int spaceBetween = 6;
        int bgWidth = headSize + spaceBetween + textWidth + padding * 2;
        int bgHeight = 18;

        // Pulsing animation for visual feedback
        long pulseTime = currentTime % 1000;
        float pulse = (float) Math.sin((pulseTime / 1000.0) * Math.PI * 2) * 0.1f + 0.9f;
        int glowAlpha = (int) (80 * pulse);
        int glowColor = (glowAlpha << 24) | 0x00FF00; // Green glow

        // Draw outer glow
        drawContext.fill(x - padding - 1, y - 3, x + bgWidth + 1, y + bgHeight + 1, glowColor);

        // Draw semi-transparent dark background with gradient effect
        drawContext.fillGradient(x - padding, y - 2, x + bgWidth, y + bgHeight, 0xDD1a1a1a, 0xCC2a2a2a);

        // Draw top border with gradient
        drawContext.fillGradient(x - padding, y - 2, x + bgWidth, y - 1, 0xFF66FF66, 0xFF404040);

        // Draw bottom border
        drawContext.fill(x - padding, y + bgHeight - 1, x + bgWidth, y + bgHeight, 0xFF404040);

        // Draw player head with gradient
        try {
            MinecraftClient client2 = MinecraftClient.getInstance();
            if (client2.getNetworkHandler() != null) {
                var playerListEntry = client2.getNetworkHandler().getPlayerListEntry(entry.playerName);
                if (playerListEntry != null) {
                    // Draw a colored square as head with gradient
                    drawContext.fillGradient(x + padding, y, x + padding + headSize, y + headSize, 0xFF4a9a4a, 0xFF2a6a2a);

                    // Draw border with glow
                    drawContext.fill(x + padding - 1, y - 1, x + padding + headSize + 1, y + 1, 0xFF66FF66);
                    drawContext.fill(x + padding - 1, y - 1, x + padding + 1, y + headSize + 1, 0xFF66FF66);
                    drawContext.fill(x + padding + headSize - 1, y - 1, x + padding + headSize + 1, y + headSize + 1, 0xFF66FF66);
                    drawContext.fill(x + padding - 1, y + headSize - 1, x + padding + headSize + 1, y + headSize + 1, 0xFF66FF66);
                }
            }
        } catch (Exception e) {
            // If we can't get the player, just skip the head
        }

        // Draw the text with better shadow
        int textX = x + padding + headSize + spaceBetween;
        int textY = y + 4;

        // Shadow effect
        drawContext.drawText(
            client.textRenderer,
            Text.literal(fullText),
            textX + 1,
            textY + 1,
            0x33000000,  // Dark shadow
            false
        );

        // Main text in bright white
        drawContext.drawText(
            client.textRenderer,
            Text.literal(fullText),
            textX,
            textY,
            0xFFFFFFFF,  // White text
            false
        );
    }

    public static void updateTypingState(String playerName, boolean isTyping, String partialText) {
        if (isTyping) {
            TypingEntry newEntry = new TypingEntry(playerName, partialText);
            TypingEntry oldEntry = typingPlayers.put(playerName, newEntry);

            // Play sound only if this is a new typing entry (not an update)
            if (oldEntry == null) {
                playTypingSound();
            }
        } else {
            typingPlayers.remove(playerName);
        }
    }

    private static void playTypingSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.playSound(net.kaupenjoe.tutorialmod.sound.ModSounds.TYPING_START, 0.5f, 1.0f);
        }
    }
}


