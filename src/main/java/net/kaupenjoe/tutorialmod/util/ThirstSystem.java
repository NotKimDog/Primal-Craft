package net.kaupenjoe.tutorialmod.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.network.ThirstSyncPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ThirstSystem {
    private static final double MAX_THIRST = 20.0;
    private static final Map<UUID, Double> thirstLevels = new HashMap<>();

    private static final double BASE_DRAIN_PER_TICK = 0.00033;
    private static final int SYNC_INTERVAL_TICKS = 20;

    private static int tickCounter = 0;
    private static int syncEvents = 0;
    private static int effectApplications = 0;

    private ThirstSystem() {}

    public static void register() {
        TutorialMod.LOGGER.info("⚙️  [THIRST_SYSTEM] Initializing ThirstSystem");
        TutorialMod.LOGGER.debug("   ├─ Max Thirst: {}", MAX_THIRST);
        TutorialMod.LOGGER.debug("   ├─ Base Drain: {}/tick", BASE_DRAIN_PER_TICK);
        TutorialMod.LOGGER.debug("   └─ Sync Interval: {} ticks", SYNC_INTERVAL_TICKS);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % 100 == 0) {
                TutorialMod.LOGGER.trace("📍 [THIRST_TICK] Tick #{} - Processing {} players",
                    tickCounter, server.getPlayerManager().getPlayerList().size());
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });

        TutorialMod.LOGGER.info("✅ [THIRST_SYSTEM] ThirstSystem registered successfully");
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        double thirst = thirstLevels.getOrDefault(id, MAX_THIRST);

        double drainPerTick = BASE_DRAIN_PER_TICK;
        double drainBreakdown = 0.0;

        TutorialMod.LOGGER.trace("💧 [THIRST] {} - Current: {}", player.getName().getString(), String.format("%.1f", thirst));
        TutorialMod.LOGGER.trace("   ├─ [DRAIN] Base: {}", String.format("%.6f", BASE_DRAIN_PER_TICK));

        // Activity modifiers
        if (player.isSprinting()) {
            drainPerTick += 0.0007;
            TutorialMod.LOGGER.trace("   ├─ [DRAIN] Sprint bonus: +0.0007");
        }
        if (player.getVelocity().horizontalLengthSquared() > 0.02) {
            drainPerTick += 0.0003;
            TutorialMod.LOGGER.trace("   ├─ [DRAIN] Movement bonus: +0.0003");
        }

        // Environment: heat increases drain
        double temp = TemperatureSystem.getEffectiveTemperature(player);
        TutorialMod.LOGGER.trace("   ├─ [TEMP] Player temperature: {}°C", String.format("%.1f", temp));

        if (temp >= 40) {
            drainPerTick += 0.0012;
            TutorialMod.LOGGER.trace("   │  ├─ Extreme heat (≥40°C): +0.0012");
        } else if (temp >= 35) {
            drainPerTick += 0.0008;
            TutorialMod.LOGGER.trace("   │  ├─ Very hot (≥35°C): +0.0008");
        } else if (temp >= 28) {
            drainPerTick += 0.0004;
            TutorialMod.LOGGER.trace("   │  ├─ Warm (≥28°C): +0.0004");
        } else if (temp <= 5) {
            drainPerTick -= 0.0002;
            TutorialMod.LOGGER.trace("   │  └─ Chilly (≤5°C): -0.0002");
        }

        // Hazards
        if (player.isInLava() || player.isOnFire()) {
            drainPerTick += 0.0015;
            TutorialMod.LOGGER.trace("   ├─ [HAZARD] Fire/Lava: +0.0015");
        }

        // Hunger synergy
        if (player.getHungerManager().isNotFull()) {
            drainPerTick += 0.0002;
            TutorialMod.LOGGER.trace("   └─ [HUNGER] Not full: +0.0002");
        }

        // Apply drain
        double finalDrain = drainPerTick * 20.0;
        double newThirst = thirst - finalDrain;

        double clamped = Math.max(0.0, Math.min(MAX_THIRST, newThirst));
        double actualDrain = thirst - clamped;

        TutorialMod.LOGGER.trace("   ├─ [CALCULATION] {} * 20 = {} total drain",
            String.format("%.6f", drainPerTick), String.format("%.6f", finalDrain));
        TutorialMod.LOGGER.trace("   └─ [RESULT] {} → {} (delta: {})",
            String.format("%.1f", thirst), String.format("%.1f", clamped), String.format("%.3f", actualDrain));

        applyDehydrationEffects(player, clamped);
        thirstLevels.put(id, clamped);

        // Sync to client periodically
        if (player.age % SYNC_INTERVAL_TICKS == 0) {
            syncEvents++;
            TutorialMod.LOGGER.trace("   🔀 [SYNC] Event #{}: Syncing thirst to {}: {}/{}",
                syncEvents, player.getName().getString(), String.format("%.1f", clamped), MAX_THIRST);
            ServerPlayNetworking.send(player, new ThirstSyncPayload(clamped, MAX_THIRST));
        }
    }

    private static void applyDehydrationEffects(ServerPlayerEntity player, double thirst) {
        TutorialMod.LOGGER.trace("   ✨ [EFFECTS] Evaluating dehydration effects (thirst: {})",
            String.format("%.1f", thirst));

        if (thirst <= 1.0) {
            effectApplications++;
            TutorialMod.LOGGER.debug("   ⚠️  [EFFECT] Event #{}: CRITICAL dehydration for {}",
                effectApplications, player.getName().getString());
            TutorialMod.LOGGER.trace("   │  ├─ Applying: Slowness II");
            TutorialMod.LOGGER.trace("   │  ├─ Applying: Weakness I");
            TutorialMod.LOGGER.trace("   │  └─ Applying: Mining Fatigue I");

            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1, true, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0, true, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 60, 0, true, false));
        } else if (thirst <= 3.0) {
            // Severe dehydration: slowness I, weakness I
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0, true, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0, true, false));
        } else if (thirst <= 6.0) {
            // Mild dehydration: just slowness I (very mild)
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0, true, false));
        }
        // No effects when thirst > 6.0 (30% of max)
    }

    // Public API
    public static void addThirst(ServerPlayerEntity player, double amount) {
        UUID id = player.getUuid();
        double thirst = thirstLevels.getOrDefault(id, MAX_THIRST);
        TutorialMod.LOGGER.debug("💧 [ADD_THIRST] {} adding thirst: +{}",
            player.getName().getString(), String.format("%.1f", amount));
        TutorialMod.LOGGER.trace("   ├─ Before: {}/{}", String.format("%.1f", thirst), MAX_THIRST);

        thirst = Math.min(MAX_THIRST, thirst + amount);
        TutorialMod.LOGGER.trace("   ├─ After: {}/{}", String.format("%.1f", thirst), MAX_THIRST);

        thirstLevels.put(id, thirst);
        TutorialMod.LOGGER.trace("   └─ Syncing to client");
        ServerPlayNetworking.send(player, new ThirstSyncPayload(thirst, MAX_THIRST));
    }

    public static void consumeThirst(ServerPlayerEntity player, double amount) {
        UUID id = player.getUuid();
        double thirst = thirstLevels.getOrDefault(id, MAX_THIRST);
        TutorialMod.LOGGER.debug("💧 [CONSUME_THIRST] {} consuming thirst: -{}",
            player.getName().getString(), String.format("%.1f", amount));
        TutorialMod.LOGGER.trace("   ├─ Before: {}/{}", String.format("%.1f", thirst), MAX_THIRST);

        thirst = Math.max(0.0, thirst - amount);
        TutorialMod.LOGGER.trace("   ├─ After: {}/{}", String.format("%.1f", thirst), MAX_THIRST);

        thirstLevels.put(id, thirst);
        TutorialMod.LOGGER.trace("   └─ Syncing to client");
        ServerPlayNetworking.send(player, new ThirstSyncPayload(thirst, MAX_THIRST));
    }

    public static double getThirst(ServerPlayerEntity player) {
        double thirst = thirstLevels.getOrDefault(player.getUuid(), MAX_THIRST);
        TutorialMod.LOGGER.trace("📊 [GET_THIRST] {} thirst level: {}/{}",
            player.getName().getString(), String.format("%.1f", thirst), MAX_THIRST);
        return thirst;
    }

    public static double getMaxThirst() {
        return MAX_THIRST;
    }

    // Convenience: drink actions
    public static void drinkWater(ServerPlayerEntity player) {
        TutorialMod.LOGGER.debug("🥛 [DRINK] {} drank water", player.getName().getString());
        addThirst(player, 2.0);
    }

    public static void drinkJuice(ServerPlayerEntity player) {
        TutorialMod.LOGGER.debug("🥛 [DRINK] {} drank juice", player.getName().getString());
        addThirst(player, 3.0);
    }

    public static void drinkPotion(ServerPlayerEntity player) {
        TutorialMod.LOGGER.debug("🥛 [DRINK] {} drank potion", player.getName().getString());
        addThirst(player, 1.5);
    }
}
