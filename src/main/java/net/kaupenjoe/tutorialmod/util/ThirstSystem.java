package net.kaupenjoe.tutorialmod.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.network.ThirstSyncPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ThirstSystem {
    private static final double MAX_THIRST = 20.0; // 10 icons * 2 per icon
    private static final Map<UUID, Double> thirstLevels = new HashMap<>();

    // Drain tuning - MUCH more gentle now (about 1/3 of previous rate)
    private static final double BASE_DRAIN_PER_TICK = 0.00033; // ~3x slower base drain
    private static final int SYNC_INTERVAL_TICKS = 20; // Sync once per second instead of twice

    private ThirstSystem() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        double thirst = thirstLevels.getOrDefault(id, MAX_THIRST);

        // Base drain
        double drainPerTick = BASE_DRAIN_PER_TICK;

        // Activity modifiers - reduced significantly
        if (player.isSprinting()) drainPerTick += 0.0007; // was 0.0020
        if (player.getVelocity().horizontalLengthSquared() > 0.02) drainPerTick += 0.0003; // was 0.0010

        // Environment: heat increases drain, cold lowers slightly - more gentle
        double temp = TemperatureSystem.getEffectiveTemperature(player);
        if (temp >= 40) {
            drainPerTick += 0.0012; // extreme heat (reduced from 0.0030)
        } else if (temp >= 35) {
            drainPerTick += 0.0008; // very hot biomes (new tier)
        } else if (temp >= 28) {
            drainPerTick += 0.0004; // warm (reduced from 0.0015)
        } else if (temp <= 5) {
            drainPerTick -= 0.0002; // chilly slows thirst slightly (reduced from 0.0005)
        }

        // Hazards - reduced
        if (player.isInLava() || player.isOnFire()) drainPerTick += 0.0015; // was 0.0035

        // Regen sources: disabled per request (must drink water bottle)
        // Immersed in water = slow hydration
        // if (player.isSubmergedInWater()) drainPerTick -= 0.0012;
        // Touching water (rain or puddle) gives a tiny benefit
        // if (player.isTouchingWater() && !player.isSubmergedInWater()) drainPerTick -= 0.0006;
        // Raining on player (sky visible and raining) gives tiny hydration
        // boolean skyVisible = player.getEntityWorld().isSkyVisible(player.getBlockPos());
        // if (skyVisible && player.getEntityWorld().isRaining() && !player.isSubmergedInWater()) {
        //     drainPerTick -= 0.0004;
        // }

        // Hunger synergy: being hungry drains thirst a bit more - reduced
        if (player.getHungerManager().isNotFull()) drainPerTick += 0.0002; // was 0.0006

        // Apply drain
        thirst -= drainPerTick * 20.0;

        // Passive idle regen: disabled per request
        // boolean idle = player.getVelocity().horizontalLengthSquared() < 0.001;
        // boolean wellFed = !player.getHungerManager().isNotFull();
        // if (idle && wellFed && temp >= 10 && temp <= 30) {
        //     thirst += 0.012;
        // }

        // Dehydration effects thresholds - MORE FORGIVING
        // < 2.0: strong debuffs, < 6.0: mild debuffs (was 4.0)
        double clamped = Math.max(0.0, Math.min(MAX_THIRST, thirst));
        applyDehydrationEffects(player, clamped);

        thirstLevels.put(id, clamped);

        // Sync to client periodically
        if (player.age % SYNC_INTERVAL_TICKS == 0) {
            ServerPlayNetworking.send(player, new ThirstSyncPayload(clamped, MAX_THIRST));
        }
    }

    private static void applyDehydrationEffects(ServerPlayerEntity player, double thirst) {
        // More forgiving thresholds and gentler effects
        if (thirst <= 1.0) {
            // Critical dehydration: slowness II, weakness I, mining fatigue I
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
        thirst = Math.min(MAX_THIRST, thirst + amount);
        thirstLevels.put(id, thirst);
        ServerPlayNetworking.send(player, new ThirstSyncPayload(thirst, MAX_THIRST));
    }

    public static void consumeThirst(ServerPlayerEntity player, double amount) {
        UUID id = player.getUuid();
        double thirst = thirstLevels.getOrDefault(id, MAX_THIRST);
        thirst = Math.max(0.0, thirst - amount);
        thirstLevels.put(id, thirst);
        ServerPlayNetworking.send(player, new ThirstSyncPayload(thirst, MAX_THIRST));
    }

    public static double getThirst(ServerPlayerEntity player) {
        return thirstLevels.getOrDefault(player.getUuid(), MAX_THIRST);
    }

    public static double getMaxThirst() { return MAX_THIRST; }

    // Convenience: drink actions
    public static void drinkWater(ServerPlayerEntity player) { addThirst(player, 2.0); }
    public static void drinkJuice(ServerPlayerEntity player) { addThirst(player, 3.0); }
    public static void drinkPotion(ServerPlayerEntity player) { addThirst(player, 1.5); }
}
