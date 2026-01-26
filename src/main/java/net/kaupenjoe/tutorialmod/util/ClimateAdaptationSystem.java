package net.kaupenjoe.tutorialmod.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClimateAdaptationSystem {
    private static final Map<UUID, Double> coldAdapt = new HashMap<>(); // 0..1
    private static final Map<UUID, Double> heatAdapt = new HashMap<>(); // 0..1

    private ClimateAdaptationSystem() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) tick(p);
        });
    }

    private static void tick(ServerPlayerEntity player) {
        double eff = TemperatureSystem.getEffectiveTemperature(player);
        UUID id = player.getUuid();
        double c = coldAdapt.getOrDefault(id, 0.0);
        double h = heatAdapt.getOrDefault(id, 0.0);

        // Adaptation rates: slow and bounded
        // Cold exposure (<5°C) increases cold adaptation; hot exposure (>30°C) increases heat adaptation
        double rate = 0.0005; // per tick
        if (eff <= 5) c = Math.min(1.0, c + rate);
        else c = Math.max(0.0, c - rate * 0.5);

        if (eff >= 30) h = Math.min(1.0, h + rate);
        else h = Math.max(0.0, h - rate * 0.5);

        // Activity amplifies adaptation
        if (player.isSprinting()) {
            h = Math.min(1.0, h + rate * 0.5);
        }
        if (player.getVelocity().horizontalLengthSquared() > 0.02) {
            c = Math.min(1.0, c + (eff < 8 ? rate * 0.3 : 0.0));
        }

        coldAdapt.put(id, c);
        heatAdapt.put(id, h);
    }

    public static double getColdAdaptation(ServerPlayerEntity player) {
        return coldAdapt.getOrDefault(player.getUuid(), 0.0);
    }
    public static double getHeatAdaptation(ServerPlayerEntity player) {
        return heatAdapt.getOrDefault(player.getUuid(), 0.0);
    }

    /** Adjust stamina drain multiplier using adaptation: shift comfort band. */
    public static double adjustStaminaDrain(double effTemp, ServerPlayerEntity player) {
        double c = getColdAdaptation(player);
        double h = getHeatAdaptation(player);
        double shift = (h * 3.0) - (c * 3.0); // shift comfort by ±3°C
        return TemperatureSystem.getTemperatureStaminaMultiplier(effTemp - shift);
    }

    /** Adjust stamina regen multiplier using adaptation. */
    public static double adjustStaminaRegen(double effTemp, ServerPlayerEntity player) {
        double c = getColdAdaptation(player);
        double h = getHeatAdaptation(player);
        double shift = (h * 3.0) - (c * 3.0);
        return TemperatureSystem.getTemperatureRegenMultiplier(effTemp - shift);
    }
}
