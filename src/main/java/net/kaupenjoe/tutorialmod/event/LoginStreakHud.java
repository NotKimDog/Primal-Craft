package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;import net.kaupenjoe.tutorialmod.util.HudTheme;

/**
 * Renders the daily login streak indicator (bottom-left) when active.
 */
public class LoginStreakHud implements HudRenderCallback {
    private static int currentStreak = 0;
    private static long lastDay = -1;
    private static int visibleTicks = 0;
    private static boolean wasIncrease = false;
    private static boolean wasBroken = false;
    private static int brokenAt = 0;

    private static int animTicks = 0; // drives flame animation

    private static final int DISPLAY_DURATION = 160; // ~8s
    private static final int PULSE_TICKS = 24;
    private static final int EMBER_COUNT = 6;
    private static final float[][] EMBERS = new float[EMBER_COUNT][4]; // x,y,vy,alpha
    private static boolean embersInit = false;

    private static final int[] MILESTONES = new int[] {7, 30, 100};
    private static final int FLARE_DURATION = 40; // ~2s
    private static int flareTicks = 0;
    private static boolean isBigMilestone = false;

    // Confetti state for big milestones (100, 365)
    private static final int CONFETTI_COUNT = 20;
    private static final float[][] CONFETTI = new float[CONFETTI_COUNT][6]; // x,y,vx,vy,rotation,color
    private static int confettiTicks = 0;

    public static void update(int streak, long day, boolean increased, boolean broken, int previous) {
        currentStreak = streak;
        lastDay = day;
        wasIncrease = increased;
        wasBroken = broken;
        brokenAt = previous;
        visibleTicks = HudTheme.DEFAULT_DISPLAY_DURATION + (increased ? 20 : 0);
        animTicks = 0;

        // Milestone flare trigger
        if (increased && isMilestone(streak)) {
            flareTicks = FLARE_DURATION;
        }

        // Big milestone confetti
        isBigMilestone = (streak == 100 || streak == 365);
        if (increased && isBigMilestone) {
            confettiTicks = 120; // ~6s
            java.util.Random rand = new java.util.Random();
            for (int i = 0; i < CONFETTI_COUNT; i++) {
                CONFETTI[i][0] = 0f; // x
                CONFETTI[i][1] = 0f; // y
                CONFETTI[i][2] = (rand.nextFloat() - 0.5f) * 3f; // vx
                CONFETTI[i][3] = -2f - rand.nextFloat() * 2f; // vy (upward)
                CONFETTI[i][4] = rand.nextFloat() * 360f; // rotation
                CONFETTI[i][5] = rand.nextInt(6); // color index
            }
        }
    }

    private static boolean isMilestone(int streak) {
        for (int m : MILESTONES) if (streak == m) return true;
        if (streak > 100 && (streak - 100) % 50 == 0) return true;
        return false;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || client.getDebugHud().shouldShowDebugHud()) return;
        if (visibleTicks <= 0) return;

        animTicks++;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();
        int x = 6;
        int y = screenH - 36; // bottom-left

        // Use theme for alpha timing
        float uiAlpha = HudTheme.uiAlpha(visibleTicks, HudTheme.DEFAULT_DISPLAY_DURATION);

        int bg = HudTheme.applyAlpha(HudTheme.PANEL_BG, uiAlpha);
        int border = HudTheme.applyAlpha(HudTheme.PANEL_BORDER, uiAlpha);
        int baseTextColor = wasIncrease ? HudTheme.TEXT_HIGHLIGHT : HudTheme.TEXT_NORMAL;
        int textColor = HudTheme.applyAlpha(baseTextColor, uiAlpha);
        int accent = HudTheme.applyAlpha(wasBroken ? HudTheme.STATUS_NEGATIVE : HudTheme.PANEL_ACCENT, uiAlpha);

        // Animated flame color (flicker between hot orange and warm yellow)
        float flicker = 0.5f + 0.5f * (float) Math.sin(animTicks * 0.35f);
        int flameColorBase = HudTheme.lerpColor(0xFFFF6600, 0xFFFFAA33, flicker);
        int flameColor = HudTheme.applyAlpha(wasBroken ? 0xFFDD5555 : flameColorBase, uiAlpha);

        // Animated offsets: bob, slight shake on broken
        int bob = (int) Math.round(Math.sin(animTicks * HudTheme.BOB_FREQUENCY) * HudTheme.BOB_AMPLITUDE);
        int shakeX = wasBroken ? (int) Math.round(Math.sin(animTicks * 0.9f) * 1.0) : 0;

        // Compute text widths for layout
        String flame = "🔥";
        int flameW = client.textRenderer.getWidth(flame);
        String label = wasBroken ? ("Streak broken at " + brokenAt) : ("Streak: " + currentStreak);
        int labelW = client.textRenderer.getWidth(label);
        int pad = 4;
        int boxW = flameW + 4 + labelW + pad * 2;
        int boxH = 14;

        // Panel
        context.fill(x - 2, y - 2, x + boxW + 2, y + boxH + 2, border);
        context.fill(x, y, x + boxW, y + boxH, bg);

        // Seed embers on first draw
        if (!embersInit) {
            java.util.Random rand = new java.util.Random(1337L);
            for (int i = 0; i < EMBER_COUNT; i++) {
                EMBERS[i][0] = 0f;
                EMBERS[i][1] = 6f + rand.nextFloat() * 6f;
                EMBERS[i][2] = 0.3f + rand.nextFloat() * 0.5f;
                EMBERS[i][3] = 0.4f + rand.nextFloat() * 0.4f;
            }
            embersInit = true;
        }

        // Draw animated flame with glow; boost on flare
        int flameX = x + pad + shakeX;
        int flameY = y + 3 + bob;
        float scalePulse = 1.0f + HudTheme.PULSE_AMPLITUDE * (float) Math.sin(animTicks * HudTheme.PULSE_FREQUENCY);
        int baseRadius = 1 + (scalePulse > 1.03f ? 1 : 0);
        int r = baseRadius + (flareTicks > 0 ? 1 : 0);
        float glowAlphaMul = flareTicks > 0 ? 0.75f : 0.35f;
        int glowColor = HudTheme.applyAlpha(HudTheme.lerpColor(0xFFAA5500, 0xFFFFAA33, 0.5f), Math.min(1f, glowAlphaMul * uiAlpha));

        // Glow ring using current radius
        context.drawText(client.textRenderer, "🔥", flameX - r, flameY, glowColor, false);
        context.drawText(client.textRenderer, "🔥", flameX + r, flameY, glowColor, false);
        context.drawText(client.textRenderer, "🔥", flameX, flameY - r, glowColor, false);
        context.drawText(client.textRenderer, "🔥", flameX, flameY + r, glowColor, false);
        // Main flame
        context.drawText(client.textRenderer, "🔥", flameX, flameY, flameColor, false);

        // Label text
        int labelX = flameX + flameW + 4;
        context.drawText(client.textRenderer, label, labelX, y + 3, textColor, false);

        // Accent bar with subtle breathing
        float breath = 0.5f + 0.5f * (float) Math.sin(animTicks * HudTheme.BREATH_FREQUENCY);
        int accentW = (int) (boxW * (0.85f + 0.15f * breath));
        context.fill(x, y + boxH - 2, x + accentW, y + boxH, accent);

        // HUD embers drifting upward near the flame (baseline)
        for (int i = 0; i < EMBER_COUNT; i++) {
            EMBERS[i][1] -= EMBERS[i][2]; // move up
            EMBERS[i][3] -= 0.01f; // fade
            if (EMBERS[i][1] < -2f || EMBERS[i][3] < 0.05f) {
                EMBERS[i][1] = 8f + (float) (Math.random() * 6f);
                EMBERS[i][2] = 0.3f + (float) (Math.random() * 0.5f);
                EMBERS[i][3] = 0.5f + (float) (Math.random() * 0.4f);
            }
            int ex = flameX + 2 + (int) (Math.sin(animTicks * 0.25f + i) * 2.0);
            int ey = (int) (y + 6 + EMBERS[i][1]);
            int emberCol = HudTheme.applyAlpha(HudTheme.lerpColor(0xFFFFAA66, 0xFFFF6600, flicker), uiAlpha * EMBERS[i][3]);
            context.fill(ex, ey, ex + 1, ey + 1, emberCol);
        }

        // Milestone flare: extra embers burst
        if (flareTicks > 0) {
            int extra = 10; // extra embers during flare
            for (int i = 0; i < extra; i++) {
                double ang = (i / (double) extra) * Math.PI * 2.0 + animTicks * 0.05;
                int ex = flameX + (int) Math.round(Math.cos(ang) * (2 + (flareTicks % 3)));
                int ey = flameY - 2 - (int) Math.round(Math.sin(ang) * 2);
                int emberCol = HudTheme.applyAlpha(HudTheme.lerpColor(0xFFFFDD88, 0xFFFF6600, (float) Math.abs(Math.sin(ang))), uiAlpha * 0.8f);
                context.fill(ex, ey, ex + 1, ey + 1, emberCol);
            }
            flareTicks--;
        }

        // Confetti for big milestones
        if (confettiTicks > 0) {
            int[] confettiColors = new int[] {0xFFFF6666, 0xFFFFAA66, 0xFFFFFF66, 0xFF66FF66, 0xFF6666FF, 0xFFFF66FF};
            for (int i = 0; i < CONFETTI_COUNT; i++) {
                // Update physics
                CONFETTI[i][0] += CONFETTI[i][2] * 0.016f; // x
                CONFETTI[i][1] += CONFETTI[i][3] * 0.016f; // y
                CONFETTI[i][3] += 0.15f; // gravity
                CONFETTI[i][4] += 5f; // rotation

                int cx = flameX + (int) CONFETTI[i][0];
                int cy = flameY + (int) CONFETTI[i][1];
                int colorIdx = (int) CONFETTI[i][5];
                int col = HudTheme.applyAlpha(confettiColors[colorIdx], uiAlpha * 0.9f);

                // Draw tiny rotating rectangle
                int w = 3, h = 2;
                context.fill(cx, cy, cx + w, cy + h, col);
            }
            confettiTicks--;
        }

        visibleTicks--;
    }
}
