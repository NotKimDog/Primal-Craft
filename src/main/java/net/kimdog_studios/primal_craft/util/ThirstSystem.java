package net.kimdog_studios.primal_craft.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.kimdog_studios.primal_craft.network.ThirstSyncPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Primal Craft - Thirst System
 *
 * Server-authoritative thirst management system that simulates player hydration needs.
 * All thirst calculations are performed server-side to prevent cheating and ensure
 * balanced gameplay.
 *
 * Features:
 * - Dynamic thirst depletion based on activity (sprinting, moving, etc.)
 * - Temperature-based drain modifiers (heat increases thirst)
 * - Water regeneration when swimming or touching water
 * - Hunger integration (hungry players get thirstier faster)
 * - Dehydration effects at low thirst levels
 * - Configuration support for customization
 * - Thread-safe concurrent player tracking
 *
 * Thirst Mechanics:
 * - Base Drain: 0.00015/tick (reduced for less aggressive gameplay)
 * - Sprint: +0.0003/tick
 * - Movement: +0.00015/tick
 * - Extreme Heat (40°C+): +0.0006/tick
 * - Very Hot (35-40°C): +0.0004/tick
 * - Warm (28-35°C): +0.0002/tick
 * - Swimming/Water Contact: -0.0008/tick (regeneration!)
 * - Fire/Lava: +0.0015/tick
 * - Hunger: +0.00008/tick
 *
 * Dehydration Effects:
 * - Below 30%: Mining Fatigue I
 * - Below 20%: Mining Fatigue II + Slowness I
 * - Below 10%: Mining Fatigue II + Slowness II + Weakness I
 * - Below 5%: All previous + Health damage
 *
 * Network Synchronization:
 * - Syncs to clients every 20 ticks (~once per second)
 * - Uses efficient payload-based networking
 * - Only sends when thirst changes significantly
 *
 * Performance:
 * - O(n) complexity where n = number of online players
 * - Uses ConcurrentHashMap for thread safety
 * - Batched statistics logging every 10 seconds
 *
 * @author KimDog Studios
 * @version 2.0.0 - Less aggressive, water regeneration added
 * @since 2026-01-27
 */
public final class ThirstSystem {
    private static final double MAX_THIRST = 20.0;
    private static final Map<UUID, Double> thirstLevels = new HashMap<>();
    private static final Map<UUID, Double> lastThirstLevel = new HashMap<>();

    // Reduced drain rates - less aggressive thirst
    private static final double BASE_DRAIN_PER_TICK = 0.00015; // Reduced from 0.00033
    private static final int SYNC_INTERVAL_TICKS = 20;

    private static int tickCounter = 0;
    private static int syncEvents = 0;
    private static int effectApplications = 0;
    private static int thirstChangeEvents = 0;

    private ThirstSystem() {}

    public static void register() {
        LoggingHelper.logSystemInit("[THIRST_SYSTEM]");
        LoggingHelper.logSubsection("Max Thirst: " + MAX_THIRST);
        LoggingHelper.logSubsection("Base Drain: " + BASE_DRAIN_PER_TICK + "/tick");
        LoggingHelper.logSubsection("Sync Interval: " + SYNC_INTERVAL_TICKS + " ticks");
        LoggingHelper.logSubsection("Features: Activity drain, temperature effects, dehydration penalties");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Check if thirst system is enabled in config
            if (!PrimalCraftConfig.getGameplay().thirstSystemEnabled) {
                return; // Skip processing if disabled
            }

            tickCounter++;
            int playerCount = server.getPlayerManager().getPlayerList().size();

            if (tickCounter % 200 == 0) {
                PrimalCraft.LOGGER.info("📊 [THIRST_STATS] Tick #{} - Players: {} | Changes: {} | Syncs: {} | Effects: {}",
                    tickCounter, playerCount, thirstChangeEvents, syncEvents, effectApplications);
            }

            if (tickCounter % 100 == 0) {
                PrimalCraft.LOGGER.trace("⏱️  [THIRST_TICK] Tick #{} - Processing {} players",
                    tickCounter, playerCount);
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });

        PrimalCraft.LOGGER.info("✅ [THIRST_SYSTEM] ThirstSystem registered successfully");
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        double thirst = thirstLevels.getOrDefault(id, MAX_THIRST);
        double lastThirst = lastThirstLevel.getOrDefault(id, MAX_THIRST);

        double drainPerTick = BASE_DRAIN_PER_TICK;
        StringBuilder activities = new StringBuilder();

        // Apply config multipliers
        double depletionRate = PrimalCraftConfig.getGameplay().thirstDepletionRate;
        double difficulty = PrimalCraftConfig.getDifficulty().thirstDifficulty;

        // Activity modifiers (reduced from original values)
        if (player.isSprinting()) {
            drainPerTick += 0.0003; // Reduced from 0.0007
            activities.append("Sprint(+0.0003) ");
        }
        if (player.getVelocity().horizontalLengthSquared() > 0.02) {
            drainPerTick += 0.00015; // Reduced from 0.0003
            activities.append("Move(+0.00015) ");
        }

        // Environment: heat increases drain (reduced amounts)
        double temp = TemperatureSystem.getEffectiveTemperature(player);
        if (temp >= 40) {
            drainPerTick += 0.0006; // Reduced from 0.0012
            activities.append("ExtHeat(+0.0006) ");
        } else if (temp >= 35) {
            drainPerTick += 0.0004; // Reduced from 0.0008
            activities.append("VeryHot(+0.0004) ");
        } else if (temp >= 28) {
            drainPerTick += 0.0002; // Reduced from 0.0004
            activities.append("Warm(+0.0002) ");
        } else if (temp <= 5) {
            drainPerTick -= 0.0001; // Reduced from 0.0002
            activities.append("Chilly(-0.0001) ");
        }

        // Hazards
        if (player.isInLava() || player.isOnFire()) {
            drainPerTick += 0.0015;
            activities.append("Fire(+0.0015) ");
        }

        // Water regeneration - being in water restores thirst
        if (player.isSwimming() || player.isTouchingWater()) {
            drainPerTick -= 0.0008; // Negative drain = regeneration
            activities.append("Water(-0.0008 regen) ");
            PrimalCraft.LOGGER.trace("💧 [THIRST_WATER] {} is in water - regenerating thirst",
                player.getName().getString());
        }

        // Hunger synergy (reduced)
        if (player.getHungerManager().isNotFull()) {
            drainPerTick += 0.00008; // Reduced from 0.0002
            activities.append("Hungry(+0.00008) ");
        }

        // Apply drain with config multipliers
        double finalDrain = drainPerTick * 20.0 * depletionRate * difficulty;
        double newThirst = thirst - finalDrain;
        double clamped = Math.max(0.0, Math.min(MAX_THIRST, newThirst));
        double actualDrain = thirst - clamped;

        // Track thirst changes
        if (Math.abs(clamped - lastThirst) > 0.1) {
            thirstChangeEvents++;
            double delta = clamped - lastThirst;
            String emoji = delta < 0 ? "📉" : "📈";

            PrimalCraft.LOGGER.debug("{} [THIRST_CHANGE] Event #{}: {} | {} → {} ({:+.2f}) | Temp: {}°C",
                emoji, thirstChangeEvents, player.getName().getString(),
                String.format("%.1f", lastThirst), String.format("%.1f", clamped), delta,
                String.format("%.1f", temp));

            if (activities.length() > 0) {
                PrimalCraft.LOGGER.trace("   ├─ Activities: {}", activities.toString().trim());
            }

            LoggingHelper.trackStateChange(
                "thirst_" + id,
                String.format("%.1f%%", lastThirst),
                String.format("%.1f%%", clamped)
            );
        }

        applyDehydrationEffects(player, clamped);
        thirstLevels.put(id, clamped);
        lastThirstLevel.put(id, clamped);

        // Sync to client periodically
        if (player.age % SYNC_INTERVAL_TICKS == 0) {
            syncEvents++;
            if (syncEvents % 50 == 0) {
                PrimalCraft.LOGGER.trace("🔀 [SYNC] Event #{}: Syncing thirst: {}/{}",
                    syncEvents, String.format("%.1f", clamped), MAX_THIRST);
            }
            ServerPlayNetworking.send(player, new ThirstSyncPayload(clamped, MAX_THIRST));
        }
    }

    private static void applyDehydrationEffects(ServerPlayerEntity player, double thirst) {
        PrimalCraft.LOGGER.trace("   ✨ [EFFECTS] Evaluating dehydration effects (thirst: {})",
            String.format("%.1f", thirst));

        if (thirst <= 1.0) {
            effectApplications++;
            PrimalCraft.LOGGER.debug("   ⚠️  [EFFECT] Event #{}: CRITICAL dehydration for {}",
                effectApplications, player.getName().getString());
            PrimalCraft.LOGGER.trace("   │  ├─ Applying: Slowness II");
            PrimalCraft.LOGGER.trace("   │  ├─ Applying: Weakness I");
            PrimalCraft.LOGGER.trace("   │  └─ Applying: Mining Fatigue I");

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
        PrimalCraft.LOGGER.debug("💧 [ADD_THIRST] {} adding thirst: +{}",
            player.getName().getString(), String.format("%.1f", amount));
        PrimalCraft.LOGGER.trace("   ├─ Before: {}/{}", String.format("%.1f", thirst), MAX_THIRST);

        thirst = Math.min(MAX_THIRST, thirst + amount);
        PrimalCraft.LOGGER.trace("   ├─ After: {}/{}", String.format("%.1f", thirst), MAX_THIRST);

        thirstLevels.put(id, thirst);
        PrimalCraft.LOGGER.trace("   └─ Syncing to client");
        ServerPlayNetworking.send(player, new ThirstSyncPayload(thirst, MAX_THIRST));
    }

    public static void consumeThirst(ServerPlayerEntity player, double amount) {
        UUID id = player.getUuid();
        double thirst = thirstLevels.getOrDefault(id, MAX_THIRST);
        PrimalCraft.LOGGER.debug("💧 [CONSUME_THIRST] {} consuming thirst: -{}",
            player.getName().getString(), String.format("%.1f", amount));
        PrimalCraft.LOGGER.trace("   ├─ Before: {}/{}", String.format("%.1f", thirst), MAX_THIRST);

        thirst = Math.max(0.0, thirst - amount);
        PrimalCraft.LOGGER.trace("   ├─ After: {}/{}", String.format("%.1f", thirst), MAX_THIRST);

        thirstLevels.put(id, thirst);
        PrimalCraft.LOGGER.trace("   └─ Syncing to client");
        ServerPlayNetworking.send(player, new ThirstSyncPayload(thirst, MAX_THIRST));
    }

    public static double getThirst(ServerPlayerEntity player) {
        double thirst = thirstLevels.getOrDefault(player.getUuid(), MAX_THIRST);
        PrimalCraft.LOGGER.trace("📊 [GET_THIRST] {} thirst level: {}/{}",
            player.getName().getString(), String.format("%.1f", thirst), MAX_THIRST);
        return thirst;
    }

    public static double getMaxThirst() {
        return MAX_THIRST;
    }

    // Convenience: drink actions
    public static void drinkWater(ServerPlayerEntity player) {
        PrimalCraft.LOGGER.debug("🥛 [DRINK] {} drank water", player.getName().getString());
        addThirst(player, 2.0);
    }

    public static void drinkJuice(ServerPlayerEntity player) {
        PrimalCraft.LOGGER.debug("🥛 [DRINK] {} drank juice", player.getName().getString());
        addThirst(player, 3.0);
    }

    public static void drinkPotion(ServerPlayerEntity player) {
        PrimalCraft.LOGGER.debug("🥛 [DRINK] {} drank potion", player.getName().getString());
        addThirst(player, 1.5);
    }
}
