package net.kaupenjoe.tutorialmod.util;

/**
 * Centralized HUD theme registry for consistent styling across all custom HUDs.
 * Provides colors, alpha timing, and animation parameters.
 */
public final class HudTheme {
    private HudTheme() {}

    // ===== PANEL COLORS =====
    public static final int PANEL_BG = 0xCC111111;
    public static final int PANEL_BORDER = 0xFF333333;
    public static final int PANEL_ACCENT = 0xFFFF6600;

    // ===== TEXT COLORS =====
    public static final int TEXT_NORMAL = 0xFFFFEEAA;
    public static final int TEXT_HIGHLIGHT = 0xFFFFCC66;
    public static final int TEXT_SUCCESS = 0xFF66FF88;
    public static final int TEXT_WARNING = 0xFFFFAA66;
    public static final int TEXT_ERROR = 0xFFFF6666;

    // ===== STATUS COLORS =====
    public static final int STATUS_POSITIVE = 0xFF66DD77;
    public static final int STATUS_NEGATIVE = 0xFFCC4444;
    public static final int STATUS_NEUTRAL = 0xFF888888;

    // ===== ALPHA TIMING =====
    public static final int PULSE_IN_TICKS = 24;
    public static final int FADE_OUT_TICKS = 18;
    public static final int DEFAULT_DISPLAY_DURATION = 160; // ~8 seconds

    // ===== ANIMATION PARAMETERS =====
    public static final float BOB_AMPLITUDE = 2.0f;
    public static final float BOB_FREQUENCY = 0.25f;
    public static final float PULSE_AMPLITUDE = 0.06f;
    public static final float PULSE_FREQUENCY = 0.22f;
    public static final float BREATH_FREQUENCY = 0.12f;

    /**
     * Apply alpha to an ARGB color.
     */
    public static int applyAlpha(int argb, float alpha) {
        int a = (int)(((argb >>> 24) & 0xFF) * alpha);
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    /**
     * Linear interpolation between two ARGB colors.
     */
    public static int lerpColor(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a1 = (c1 >>> 24) & 0xFF, r1 = (c1 >>> 16) & 0xFF, g1 = (c1 >>> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF, r2 = (c2 >>> 16) & 0xFF, g2 = (c2 >>> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int)(a1 + (a2 - a1) * t);
        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Calculate pulse-in alpha (used at the start of HUD displays).
     */
    public static float pulseInAlpha(int visibleTicks, int displayDuration) {
        if (visibleTicks > displayDuration - PULSE_IN_TICKS) {
            float t = (displayDuration - visibleTicks) / (float) PULSE_IN_TICKS;
            return 0.6f + 0.4f * (float) Math.sin(t * Math.PI);
        }
        return 1.0f;
    }

    /**
     * Calculate fade-out multiplier (used at the end of HUD displays).
     */
    public static float fadeOutMultiplier(int visibleTicks) {
        return visibleTicks < FADE_OUT_TICKS ? (visibleTicks / (float) FADE_OUT_TICKS) : 1f;
    }

    /**
     * Combined UI alpha (pulse-in + fade-out).
     */
    public static float uiAlpha(int visibleTicks, int displayDuration) {
        return pulseInAlpha(visibleTicks, displayDuration) * fadeOutMultiplier(visibleTicks);
    }
}
