package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class StaminaHudOverlay implements HudRenderCallback {
    private static double clientStamina = 100.0;
    private static double clientMaxStamina = 100.0;
    private static float smoothStamina = 100.0f;
    private static float pulse = 0f;
    private static double lastStamina = 100.0;
    private static int sprintCooldownTicks = 0;
    private static double clientFatigue = 0.0;
    private static float regenRate = 0.2f;
    private static double clientTemperature = 20.0; // Default comfortable temp
    private static double clientWorldTemperature = 20.0; // World/biome base temp
    private static double smoothedPlayerTemp = 20.0; // Smoothed player temp for display
    private static double smoothedWorldTemp = 20.0; // Smoothed world temp for display
    private static float smoothWorldFill = 0f; // Animated fill for world temp bar
    private static float smoothPlayerFill = 0f; // Animated fill for player temp bar
    private static int calibrationTicks = 0; // Counts frames for calibration animation
    private static final int CALIBRATION_DURATION = 80; // ~4s at 20 FPS
    private static double calibPlayerAccum = 0.0;
    private static double calibWorldAccum = 0.0;
    private static int calibSamples = 0;
    private static boolean calibAveragesApplied = false;
    private static final int SHINE_COLOR = 0x22FFFFFF;

    private static float lastInvFill = 0f;
    private static float invPulse = 0f; // decays over time; drives flash on inventory bar
    private static final float INV_PULSE_DECAY = 0.06f;
    private static final float INV_PULSE_INTENSITY = 0.6f;

    private static float invSlideX; // current animated X for inventory bar
    private static float invTargetX; // target X for inventory bar
    private static final float INV_SLIDE_SPEED = 0.25f; // interpolation speed

    private static boolean staminaDisplay = false; // latched visibility
    private static boolean thirstDisplay = false; // latched visibility for thirst

    private static int tempVisibilityTicks = 0; // countdown for showing temp bars
    private static final int TEMP_SHOW_DURATION = 120; // 6s at 20fps
    private static final double TEMP_COMFORT_MIN = 10.0;
    private static final double TEMP_COMFORT_MAX = 32.0;
    private static final double TEMP_TREND_THRESHOLD = 0.3; // trigger show when temp changes fast

    private static int invVisibilityTicks = 0; // countdown for showing inventory bar
    private static final int INV_SHOW_DURATION = 200; // 10s at 20fps after change
    private static final float INV_CHANGE_THRESHOLD = 0.02f; // minimal change to refresh visibility

    private static int idleVisibilityTicks = 60; // start visible briefly
    private static final int IDLE_SHOW_DURATION = 60; // 3s at 20fps after any activity
    private static final double IDLE_MOVE_THRESHOLD = 0.0004; // squared velocity threshold

    private static float cachedStaminaFill = 1.0f;
    private static float cachedInvFill = 0.0f;
    private static double cachedPlayerTemp = 20.0;
    private static double cachedWorldTemp = 20.0;
    private static int startupPulseTicks = 20; // brief flash on load

    // Thirst system fields
    private static double clientThirst = 20.0;
    private static double clientMaxThirst = 20.0;
    private static float smoothThirst = 20.0f;

    // Wind system fields
    private static double clientWindSpeed = 0.0;
    private static double clientMaxWindSpeed = 5.0;
    private static float smoothWindSpeed = 0.0f;
    private static final double BLOCKS_PER_SEC_TO_MPH = 2.23694;
    private static final double WIND_SHOW_THRESHOLD = 0.1; // Show wind bar when speed exceeds this (in blocks/sec)

    public static void update(double stamina, double maxStamina) {
        // Only pulse if stamina actually changed (decreased)
        if (stamina < lastStamina) {
            pulse = 0.5f; // Brief pulse only on drain
        }
        lastStamina = stamina;
        clientStamina = stamina;
        clientMaxStamina = maxStamina;
    }

    public static void updateCooldown(int ticks) {
        sprintCooldownTicks = ticks;
    }

    public static void updateFatigue(double fatigue) {
        clientFatigue = fatigue;
    }

    public static void updateRegenRate(float rate) {
        regenRate = rate;
    }

    // Getter methods for other systems to access stamina values
    public static double getClientStamina() {
        return clientStamina;
    }

    public static double getClientMaxStamina() {
        return clientMaxStamina;
    }

    public static double getClientFatigue() {
        return clientFatigue;
    }

    public static void updateTemperature(double temperature) {
        clientTemperature = temperature;
    }

    public static void updateWorldTemperature(double worldTemperature) {
        clientWorldTemperature = worldTemperature;
    }

    public static void updateThirst(double thirst, double maxThirst) {
        clientThirst = thirst;
        clientMaxThirst = maxThirst;
    }

    public static void updateWindSpeed(double windSpeed) {
        clientWindSpeed = windSpeed;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || client.getDebugHud().shouldShowDebugHud()) {
            return;
        }

        // Check config to see what should be displayed
        boolean showStamina = PrimalCraftConfig.getHUD().showStaminaBar &&
                              PrimalCraftConfig.getGameplay().staminaSystemEnabled;
        boolean showTemp = PrimalCraftConfig.getHUD().showTemperatureIndicator &&
                          PrimalCraftConfig.getGameplay().temperatureSystemEnabled;

        // Get HUD customization settings
        float hudScale = PrimalCraftConfig.getHUD().hudScale;
        float hudOpacity = PrimalCraftConfig.getHUD().hudOpacity;

        // Early return if nothing to show
        if (!showStamina && !showTemp) {
            return;
        }

        // Calibration progress
        float calibrationProgress = Math.min(1f, calibrationTicks / (float) CALIBRATION_DURATION);
        if (calibrationProgress < 1f) calibrationTicks++;

        // Smooth stamina and temps (fallback to cached values on first frames)
        smoothStamina += (((float) clientStamina) - smoothStamina) * 0.15f;
        smoothedPlayerTemp += ((clientTemperature != 0 ? clientTemperature : cachedPlayerTemp) - smoothedPlayerTemp) * 0.02;
        smoothedWorldTemp += ((clientWorldTemperature != 0 ? clientWorldTemperature : cachedWorldTemp) - smoothedWorldTemp) * 0.02;

        // Calibration averaging
        if (calibrationProgress < 1f) {
            calibPlayerAccum += smoothedPlayerTemp;
            calibWorldAccum += smoothedWorldTemp;
            calibSamples++;
        } else if (!calibAveragesApplied && calibSamples > 0) {
            smoothedPlayerTemp = calibPlayerAccum / calibSamples;
            smoothedWorldTemp = calibWorldAccum / calibSamples;
            smoothPlayerFill = Math.max(0f, Math.min(1f, (float) ((smoothedPlayerTemp + 40) / 100)));
            smoothWorldFill = Math.max(0f, Math.min(1f, (float) ((smoothedWorldTemp + 40) / 100)));
            calibAveragesApplied = true;
            calibPlayerAccum = calibWorldAccum = 0.0;
            calibSamples = 0;
        }

        // Smooth fill animations for temps
        smoothWorldFill += (((float) ((smoothedWorldTemp + 40) / 100)) - smoothWorldFill) * 0.1f;
        smoothPlayerFill += (((float) ((smoothedPlayerTemp + 40) / 100)) - smoothPlayerFill) * 0.1f;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();
        int panelColor = 0xCC1a1a1a;
        int border = 0xFF333333;
        int bgColor = 0xFF0a0a0a;
        int barWidth = 120;
        int barHeight = 6;
        int staminaX = screenW - barWidth - 8;
        int baseY = screenH - 14; // anchor for lowest bar row

        // Stamina latch & calibration
        float staminaFill = Math.max(0f, Math.min(1f, (float) (smoothStamina / clientMaxStamina)));
        if (!staminaDisplay && staminaFill < 0.30f) staminaDisplay = true;
        else if (staminaDisplay && staminaFill >= 0.999f) staminaDisplay = false;
        // Use config showStamina variable (already defined above)
        float displayStaminaFill = applyCalibrationFill(staminaFill, calibrationProgress);

        // Inventory visibility/pulse
        float invFillRaw = computeInventoryFill(client);
        float invDelta = Math.abs(invFillRaw - lastInvFill);
        boolean inventoryOpen = client.currentScreen != null && client.currentScreen.getClass().getName().contains("Inventory");

        if (invDelta > INV_CHANGE_THRESHOLD || inventoryOpen) {
            if (invDelta > INV_CHANGE_THRESHOLD) {
                invPulse = Math.min(1f, invPulse + INV_PULSE_INTENSITY);
            }
            invVisibilityTicks = INV_SHOW_DURATION;
        } else if (invVisibilityTicks > 0) {
            invVisibilityTicks--;
        }
        lastInvFill = invFillRaw;
        float displayInvFill = applyCalibrationFill(Math.max(0f, Math.min(1f, invFillRaw)), calibrationProgress);

        // Temp auto-visibility - ALWAYS SHOW PLAYER TEMP
        boolean tempComfortable = smoothedPlayerTemp >= TEMP_COMFORT_MIN && smoothedPlayerTemp <= TEMP_COMFORT_MAX;
        double tempTrend = Math.abs(smoothedPlayerTemp - clientTemperature);
        boolean tempChangingFast = tempTrend > TEMP_TREND_THRESHOLD;
        boolean worldComfortable = smoothedWorldTemp >= TEMP_COMFORT_MIN && smoothedWorldTemp <= TEMP_COMFORT_MAX;
        // Always show player temp; show world temp only when exposed to sky
        boolean skyExposed = client.player.getEntityWorld().isSkyVisible(client.player.getBlockPos());
        tempVisibilityTicks = TEMP_SHOW_DURATION; // Always set to show
        float tempAlpha = 1.0f; // Player temp always visible

        // Thirst visibility/smoothing
        smoothThirst += (((float) clientThirst) - smoothThirst) * 0.15f;
        float thirstFill = Math.max(0f, Math.min(1f, smoothThirst / (float) clientMaxThirst));

        // Thirst latch: show when below 50%, hide when at 100%
        if (!thirstDisplay && thirstFill < 0.50f) thirstDisplay = true;
        else if (thirstDisplay && thirstFill >= 0.999f) thirstDisplay = false;
        boolean showThirst = thirstDisplay;

        // Wind visibility/smoothing (calculate early for activity detection)
        smoothWindSpeed += (((float) clientWindSpeed) - smoothWindSpeed) * 0.15f;
        boolean showWind = smoothWindSpeed >= WIND_SHOW_THRESHOLD;

        // Activity + idle auto-hide (with startup pulse) - calculate AFTER all bar visibility
        boolean isMoving = client.player.getVelocity().lengthSquared() > IDLE_MOVE_THRESHOLD;
        boolean staminaLow = staminaFill < 0.999f;
        boolean cooldownActive = sprintCooldownTicks > 0;
        boolean invVisible = invVisibilityTicks > 0;
        boolean tempVisible = true; // Always true now - temp is always visible
        boolean thirstVisible = showThirst;
        boolean windVisible = showWind;
        boolean startupVisible = startupPulseTicks-- > 0; // force show briefly on load
        boolean isCalibrating = calibrationProgress < 1.0f; // Check if still calibrating
        boolean anyActivity = startupVisible || isMoving || staminaLow || cooldownActive || invVisible || tempVisible || thirstVisible || windVisible || isCalibrating;
        if (anyActivity) idleVisibilityTicks = IDLE_SHOW_DURATION; else if (idleVisibilityTicks > 0) idleVisibilityTicks--;
        float idleAlpha = idleVisibilityTicks > 0 ? 1.0f : 0.0f;
        if (idleAlpha <= 0f) return;

        float displayThirstFill = applyCalibrationFill(thirstFill, calibrationProgress);
        // During calibration, show all bars; after calibration, use normal visibility logic
        float thirstAlpha = (isCalibrating || showThirst) ? idleAlpha : 0.0f;

        // Draw stamina (uses idleAlpha, always visible during calibration)
        // Combine config setting with display latch
        boolean showStaminaNow = isCalibrating || (showStamina && staminaDisplay);

        // Inventory slide target (use showStaminaNow to account for calibration)
        int invDefaultX = staminaX - barWidth - 12;
        int invOnStaminaSlotX = staminaX;
        invTargetX = showStaminaNow ? invDefaultX : invOnStaminaSlotX;
        if (invSlideX == 0f) invSlideX = showStaminaNow ? invDefaultX : invOnStaminaSlotX;
        invSlideX += (invTargetX - invSlideX) * INV_SLIDE_SPEED;

        // Draw stamina (uses idleAlpha, always visible during calibration)
        if (showStaminaNow) {
            int base = applyAlpha(pickFillColor(displayStaminaFill), idleAlpha);
            int fillWidth = (int)(barWidth * displayStaminaFill);
            context.fill(staminaX - 4, baseY - 14, staminaX + barWidth + 4, baseY + barHeight + 2, applyAlpha(panelColor, idleAlpha));
            context.fill(staminaX - 3, baseY - 13, staminaX + barWidth + 3, baseY - 12, applyAlpha(border, idleAlpha));
            context.fill(staminaX - 3, baseY + barHeight + 1, staminaX + barWidth + 3, baseY + barHeight + 2, applyAlpha(border, idleAlpha));
            context.fill(staminaX - 3, baseY - 13, staminaX - 2, baseY + barHeight + 2, applyAlpha(border, idleAlpha));
            context.fill(staminaX + barWidth + 2, baseY - 13, staminaX + barWidth + 3, baseY + barHeight + 2, applyAlpha(border, idleAlpha));
            context.fill(staminaX, baseY, staminaX + barWidth, baseY + barHeight, applyAlpha(bgColor, idleAlpha));
            context.fill(staminaX, baseY, staminaX + fillWidth, baseY + barHeight, base);
            context.fill(staminaX, baseY, staminaX + fillWidth, baseY + 2, applyAlpha(SHINE_COLOR, idleAlpha));
            drawTextShadow(context, client, "⚡ Stamina", staminaX + 2, baseY - 11, applyAlpha(0xFFFFDD00, idleAlpha));
            String percentage = String.format("%.0f%%", displayStaminaFill * 100);
            int percentWidth = client.textRenderer.getWidth(percentage);
            drawTextShadow(context, client, percentage, staminaX + barWidth - percentWidth - 2, baseY - 11, applyAlpha(0xFFFFFFFF, idleAlpha));
        }

        // Inventory bar (visible during calibration)
        int invBarWidth = barWidth;
        int invBarHeight = barHeight;
        int invX = (int)invSlideX;
        int invY = baseY;
        float invAlpha = (isCalibrating || invVisibilityTicks > 0 ? 1.0f : 0.0f) * idleAlpha;

        if (invAlpha > 0f) {
            int invColor = applyAlpha(pickFillColor(displayInvFill), invAlpha);
            context.fill(invX - 4, invY - 14, invX + invBarWidth + 4, invY + invBarHeight + 2, applyAlpha(panelColor, invAlpha));
            context.fill(invX - 3, invY - 13, invX + invBarWidth + 3, invY - 12, applyAlpha(border, invAlpha));
            context.fill(invX - 3, invY + invBarHeight + 1, invX + invBarWidth + 3, invY + invBarHeight + 2, applyAlpha(border, invAlpha));
            context.fill(invX - 3, invY - 13, invX - 2, invY + invBarHeight + 2, applyAlpha(border, invAlpha));
            context.fill(invX + invBarWidth + 2, invY - 13, invX + invBarWidth + 3, invY + invBarHeight + 2, applyAlpha(border, invAlpha));
            context.fill(invX, invY, invX + invBarWidth, invY + invBarHeight, applyAlpha(bgColor, invAlpha));
            int invFillWidth = (int)(invBarWidth * displayInvFill);
            context.fill(invX, invY, invX + invFillWidth, invY + invBarHeight, invColor);
            context.fill(invX, invY, invX + invFillWidth, invY + 2, applyAlpha(SHINE_COLOR, invAlpha));
            if (invPulse > 0f) {
                int alpha = Math.max(0, Math.min(255, (int)(invPulse * 60 * invAlpha)));
                int flashColor = (alpha << 24) | 0x33FFCC;
                context.fill(invX - 2, invY - 2, invX + invBarWidth + 2, invY + invBarHeight + 2, flashColor);
                invPulse = Math.max(0f, invPulse - INV_PULSE_DECAY);
            }
            drawTextShadow(context, client, "📦 Inventory", invX + 2, invY - 11, applyAlpha(0xFFFFEEAA, invAlpha));
            String invPct = String.format("%.0f%%", displayInvFill * 100f);
            int invPctW = client.textRenderer.getWidth(invPct);
            drawTextShadow(context, client, invPct, invX + invBarWidth - invPctW - 2, invY - 11, applyAlpha(0xFFFFFFFF, invAlpha));
        }


        // Bar spacing constant
        int spacing = 22;

        // Thirst bar (positioned above stamina when both visible)
        // Use showStaminaNow to account for calibration mode
        if (thirstAlpha > 0f) {
            int thirstX = staminaX;
            int thirstY = baseY;
            // If stamina is showing, thirst goes above it
            if (showStaminaNow) {
                thirstY -= spacing;
            }
            int thirstColor = applyAlpha(pickThirstColor(displayThirstFill), thirstAlpha);
            context.fill(thirstX - 4, thirstY - 14, thirstX + barWidth + 4, thirstY + barHeight + 2, applyAlpha(panelColor, thirstAlpha));
            context.fill(thirstX - 3, thirstY - 13, thirstX + barWidth + 3, thirstY - 12, applyAlpha(border, thirstAlpha));
            context.fill(thirstX - 3, thirstY + barHeight + 1, thirstX + barWidth + 3, thirstY + barHeight + 2, applyAlpha(border, thirstAlpha));
            context.fill(thirstX - 3, thirstY - 13, thirstX - 2, thirstY + barHeight + 2, applyAlpha(border, thirstAlpha));
            context.fill(thirstX + barWidth + 2, thirstY - 13, thirstX + barWidth + 3, thirstY + barHeight + 2, applyAlpha(border, thirstAlpha));
            context.fill(thirstX, thirstY, thirstX + barWidth, thirstY + barHeight, applyAlpha(bgColor, thirstAlpha));
            int thirstFillWidth = (int)(barWidth * displayThirstFill);
            context.fill(thirstX, thirstY, thirstX + thirstFillWidth, thirstY + barHeight, thirstColor);
            context.fill(thirstX, thirstY, thirstX + thirstFillWidth, thirstY + 2, applyAlpha(SHINE_COLOR, thirstAlpha));
            drawTextShadow(context, client, "💧 Thirst", thirstX + 2, thirstY - 11, applyAlpha(0xFF5599FF, thirstAlpha));
            String thirstPct = String.format("%.0f%%", displayThirstFill * 100f);
            int thirstPctW = client.textRenderer.getWidth(thirstPct);
            drawTextShadow(context, client, thirstPct, thirstX + barWidth - thirstPctW - 2, thirstY - 11, applyAlpha(0xFFFFFFFF, thirstAlpha));
        }

        // Dynamic Y positioning for temp bars based on what's visible
        // During calibration, always show temp bars
        float tempAlphaApplied = (isCalibrating || tempAlpha > 0 ? 1.0f : 0.0f) * idleAlpha;
        int currentY = baseY; // start from bottom

        // Count visible bars below temps and move up accordingly
        // Use actual visibility flags (showStaminaNow accounts for calibration)
        int visibleBarsBelow = 0;
        if (showStaminaNow) visibleBarsBelow++; // Stamina
        if (invAlpha > 0f) visibleBarsBelow++; // Inventory
        if (thirstAlpha > 0f) visibleBarsBelow++; // Thirst

        currentY -= visibleBarsBelow * spacing;

        int playerTempBarY = currentY;
        int worldTempBarY = currentY - spacing;

        // Draw world temp bar ONLY if player is exposed to sky
        if (tempAlphaApplied > 0f && skyExposed) {
            context.fill(staminaX - 4, worldTempBarY - 14, staminaX + barWidth + 4, worldTempBarY + barHeight + 2, applyAlpha(panelColor, tempAlphaApplied));
            context.fill(staminaX - 3, worldTempBarY - 13, staminaX + barWidth + 3, worldTempBarY - 12, applyAlpha(border, tempAlphaApplied));
            context.fill(staminaX - 3, worldTempBarY + barHeight + 1, staminaX + barWidth + 3, worldTempBarY + barHeight + 2, applyAlpha(border, tempAlphaApplied));
            context.fill(staminaX - 3, worldTempBarY - 13, staminaX - 2, worldTempBarY + barHeight + 2, applyAlpha(border, tempAlphaApplied));
            context.fill(staminaX + barWidth + 2, worldTempBarY - 13, staminaX + barWidth + 3, worldTempBarY + barHeight + 2, applyAlpha(border, tempAlphaApplied));
            context.fill(staminaX, worldTempBarY, staminaX + barWidth, worldTempBarY + barHeight, applyAlpha(bgColor, tempAlphaApplied));
            float displayWorldFill = applyCalibrationFill(Math.max(0f, Math.min(1f, smoothWorldFill)), calibrationProgress);
            int worldTempFillColor = applyAlpha(getTemperatureColor(smoothedWorldTemp), tempAlphaApplied);
            int worldTempFillWidth = (int) (barWidth * displayWorldFill);
            context.fill(staminaX, worldTempBarY, staminaX + worldTempFillWidth, worldTempBarY + barHeight, worldTempFillColor);
            context.fill(staminaX, worldTempBarY, staminaX + worldTempFillWidth, worldTempBarY + 2, applyAlpha(SHINE_COLOR, tempAlphaApplied));
            double displayWorldTemp = applyCalibrationValue(smoothedWorldTemp, -40, 60, calibrationProgress);
            String worldTempStatus = getTemperatureStatus(displayWorldTemp);
            drawTextShadow(context, client, "🌍 " + worldTempStatus, staminaX + 2, worldTempBarY - 11, applyAlpha(worldTempFillColor, tempAlphaApplied));
            String worldTempValue = String.format("%.1f°C", displayWorldTemp);
            int worldTempValueWidth = client.textRenderer.getWidth(worldTempValue);
            drawTextShadow(context, client, worldTempValue, staminaX + barWidth - worldTempValueWidth - 2, worldTempBarY - 11, applyAlpha(0xFFFFFFFF, tempAlphaApplied));
        }

        if (tempAlphaApplied > 0f) {
            context.fill(staminaX - 4, playerTempBarY - 14, staminaX + barWidth + 4, playerTempBarY + barHeight + 2, applyAlpha(panelColor, tempAlphaApplied));
            context.fill(staminaX - 3, playerTempBarY - 13, staminaX + barWidth + 3, playerTempBarY - 12, applyAlpha(border, tempAlphaApplied));
            context.fill(staminaX - 3, playerTempBarY + barHeight + 1, staminaX + barWidth + 3, playerTempBarY + barHeight + 2, applyAlpha(border, tempAlphaApplied));
            context.fill(staminaX - 3, playerTempBarY - 13, staminaX - 2, playerTempBarY + barHeight + 2, applyAlpha(border, tempAlphaApplied));
            context.fill(staminaX + barWidth + 2, playerTempBarY - 13, staminaX + barWidth + 3, playerTempBarY + barHeight + 2, applyAlpha(border, tempAlphaApplied));
            context.fill(staminaX, playerTempBarY, staminaX + barWidth, playerTempBarY + barHeight, applyAlpha(bgColor, tempAlphaApplied));
            float displayPlayerFill = applyCalibrationFill(Math.max(0f, Math.min(1f, smoothPlayerFill)), calibrationProgress);
            int playerTempFillColor = applyAlpha(getTemperatureColor(smoothedPlayerTemp), tempAlphaApplied);
            int playerTempFillWidth = (int) (barWidth * displayPlayerFill);
            context.fill(staminaX, playerTempBarY, staminaX + playerTempFillWidth, playerTempBarY + barHeight, playerTempFillColor);
            context.fill(staminaX, playerTempBarY, staminaX + playerTempFillWidth, playerTempBarY + 2, applyAlpha(SHINE_COLOR, tempAlphaApplied));
            double displayPlayerTemp = applyCalibrationValue(smoothedPlayerTemp, -40, 60, calibrationProgress);
            String playerTempStatus = getTemperatureStatus(displayPlayerTemp);
            drawTextShadow(context, client, "🌡 " + playerTempStatus, staminaX + 2, playerTempBarY - 11, applyAlpha(playerTempFillColor, tempAlphaApplied));
            String playerTempValue = String.format("%.1f°C", displayPlayerTemp);
            int playerTempValueWidth = client.textRenderer.getWidth(playerTempValue);
            drawTextShadow(context, client, playerTempValue, staminaX + barWidth - playerTempValueWidth - 2, playerTempBarY - 11, applyAlpha(0xFFFFFFFF, tempAlphaApplied));
        }

        // Wind alpha calculation (showWind already calculated earlier)
        // During calibration, always show wind bar
        float windAlpha = (isCalibrating || showWind) ? idleAlpha : 0.0f;

        // Count visible temp bars for wind positioning (world temp only visible if skyExposed)
        int visibleTempBars = 1; // Player temp always visible
        if (skyExposed && tempAlphaApplied > 0f) visibleTempBars = 2; // Add world temp if exposed to sky

        // Wind bar (shows when windy, dynamically positioned)
        if (windAlpha > 0f) {
            double windMph = smoothWindSpeed * BLOCKS_PER_SEC_TO_MPH;
            float windFill = Math.max(0f, Math.min(1f, (float)(smoothWindSpeed / 15.0))); // scale: 0-15 blocks/sec (0-33 mph)
            int windBarY = currentY - (visibleTempBars * spacing); // Above temp bars if visible
            int windColor = applyAlpha(getWindColor(windMph), windAlpha);
            context.fill(staminaX - 4, windBarY - 14, staminaX + barWidth + 4, windBarY + barHeight + 2, applyAlpha(panelColor, windAlpha));
            context.fill(staminaX - 3, windBarY - 13, staminaX + barWidth + 3, windBarY - 12, applyAlpha(border, windAlpha));
            context.fill(staminaX - 3, windBarY + barHeight + 1, staminaX + barWidth + 3, windBarY + barHeight + 2, applyAlpha(border, windAlpha));
            context.fill(staminaX - 3, windBarY - 13, staminaX - 2, windBarY + barHeight + 2, applyAlpha(border, windAlpha));
            context.fill(staminaX + barWidth + 2, windBarY - 13, staminaX + barWidth + 3, windBarY + barHeight + 2, applyAlpha(border, windAlpha));
            context.fill(staminaX, windBarY, staminaX + barWidth, windBarY + barHeight, applyAlpha(bgColor, windAlpha));
            int windFillWidth = (int)(barWidth * windFill);
            context.fill(staminaX, windBarY, staminaX + windFillWidth, windBarY + barHeight, windColor);
            context.fill(staminaX, windBarY, staminaX + windFillWidth, windBarY + 2, applyAlpha(SHINE_COLOR, windAlpha));
            drawTextShadow(context, client, "💨 Wind", staminaX + 2, windBarY - 11, applyAlpha(0xFFAADDFF, windAlpha));
            String windMphStr = String.format("%.1f mph", windMph);
            int windMphW = client.textRenderer.getWidth(windMphStr);
            drawTextShadow(context, client, windMphStr, staminaX + barWidth - windMphW - 2, windBarY - 11, applyAlpha(0xFFFFFFFF, windAlpha));
        }

        // Cache last displayed values for next startup
        cachedStaminaFill = displayStaminaFill;
        cachedInvFill = displayInvFill;
        cachedPlayerTemp = smoothedPlayerTemp;
        cachedWorldTemp = smoothedWorldTemp;
    }

    private float computeInventoryFill(MinecraftClient client) {
        var inv = client.player.getInventory();
        int filled = 0;
        int total = 36; // main inventory 9x4
        for (int i = 0; i < total; i++) {
            if (!inv.getStack(i).isEmpty()) filled++;
        }
        return total == 0 ? 0f : (filled / (float) total);
    }

    private int getTemperatureColor(double temperature) {
        if (temperature < 0) return 0xFF88CCFF; // Icy blue
        if (temperature < 15) return 0xFF66AAFF; // Light blue
        if (temperature < 25) return 0xFF66FF66; // Green (comfortable)
        if (temperature < 35) return 0xFFFFDD00; // Yellow
        if (temperature < 45) return 0xFFFF8800; // Orange
        return 0xFFFF3333; // Red (dangerous heat)
    }

    private String getTemperatureStatus(double temperature) {
        if (temperature < -10) return "Freezing";
        if (temperature < 5) return "Cold";
        if (temperature < 15) return "Cool";
        if (temperature < 25) return "Comfy";
        if (temperature < 35) return "Warm";
        if (temperature < 45) return "Hot";
        return "Scorching";
    }

    private int pickFillColor(float fill) {
        if (fill < 0.18f) return 0xFFCC3333; // red
        if (fill < 0.35f) return 0xFFFFAA33; // orange
        if (fill < 0.65f) return 0xFF66DD66; // green
        return 0xFF44FF88; // bright green
    }

    private int pickThirstColor(float fill) {
        if (fill < 0.18f) return 0xFFCC3333; // red (dehydrated)
        if (fill < 0.35f) return 0xFFFFAA33; // orange (low)
        if (fill < 0.65f) return 0xFF5599FF; // light blue (medium)
        return 0xFF3377FF; // bright blue (hydrated)
    }

    private int getWindColor(double windSpeedMph) {
        if (windSpeedMph < 5) return 0xFF88CCFF; // Light blue (calm)
        if (windSpeedMph < 15) return 0xFF66FF66; // Green (breezy)
        if (windSpeedMph < 25) return 0xFFFFDD00; // Yellow (windy)
        if (windSpeedMph < 35) return 0xFFFF8800; // Orange (strong wind)
        return 0xFFFF3333; // Red (hurricane-level winds)
    }

    private float applyCalibrationFill(float target, float progress) {
        // progress 0..1. At 0: chaotic sweep; at 1: settle on target
        float wobble = 0.5f + 0.5f * (float) Math.sin(calibrationTicks * 0.35f);
        float flicker = 0.08f * (float) Math.sin(calibrationTicks * 0.9f);
        float chaotic = Math.max(0f, Math.min(1f, wobble + flicker));
        return target * progress + chaotic * (1f - progress);
    }

    private double applyCalibrationValue(double target, double min, double max, float progress) {
        double norm = (target - min) / (max - min);
        norm = Math.max(0, Math.min(1, norm));
        float wobble = 0.5f + 0.5f * (float) Math.sin(calibrationTicks * 0.25f + 0.5f);
        float flicker = 0.1f * (float) Math.sin(calibrationTicks * 1.1f);
        float chaotic = Math.max(0f, Math.min(1f, wobble + flicker));
        double blended = norm * progress + chaotic * (1f - progress);
        return min + blended * (max - min);
    }

    private void drawTextShadow(DrawContext ctx, MinecraftClient client, String text, int x, int y, int color) {
        int shadow = 0x99000000;
        ctx.drawText(client.textRenderer, text, x + 1, y + 1, shadow, false);
        ctx.drawText(client.textRenderer, text, x, y, color, false);
    }

    private int applyAlpha(int argb, float alpha) {
        int a = (int)(((argb >>> 24) & 0xFF) * alpha);
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
