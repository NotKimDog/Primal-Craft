package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.util.LoggingHelper;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Exhaustion System - Movement and activity cause fatigue that affects performance
 */
public class ExhaustionHandler {
    private static final Map<UUID, Double> EXHAUSTION = new HashMap<>();
    private static final Map<UUID, Double> LAST_EXHAUSTION = new HashMap<>();
    private static final Map<UUID, Integer> THRESHOLD_WARNINGS = new HashMap<>();
    private static int exhaustionTicks = 0;
    private static int exhaustionUpdates = 0;
    private static int thresholdBreaches = 0;

    public static void register() {
        LoggingHelper.logSystemInit("[EXHAUSTION]");
        LoggingHelper.logSubsection("Exhaustion system initialized");
        LoggingHelper.logSubsection("Tracking player movement and activity fatigue");
        LoggingHelper.logSubsection("Warning thresholds: 33%, 66%, 100%");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            exhaustionTicks++;
            int playerCount = server.getPlayerManager().getPlayerList().size();

            if (exhaustionTicks % 200 == 0) {
                PrimalCraft.LOGGER.info("📊 [EXHAUSTION_STATS] Tick #{} - Players: {} | Updates: {} | Breaches: {}",
                    exhaustionTicks, playerCount, exhaustionUpdates, thresholdBreaches);
            }

            if (exhaustionTicks % 100 == 0) {
                PrimalCraft.LOGGER.trace("⏱️  [EXHAUSTION_TICK] Tick #{} - Processing {} players",
                    exhaustionTicks, playerCount);
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickExhaustion(player);
            }
        });

        PrimalCraft.LOGGER.info("✅ [EXHAUSTION] ExhaustionHandler registered successfully");
    }

    private static void tickExhaustion(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        double exhaustion = EXHAUSTION.getOrDefault(id, 0.0);
        double lastExhaustion = LAST_EXHAUSTION.getOrDefault(id, 0.0);

        // Activity accumulation
        double activityLevel = 0.0;
        StringBuilder activities = new StringBuilder();

        if (player.isSprinting()) {
            activityLevel += 0.05;
            activities.append("Sprint(+0.05) ");
        }
        if (player.getVelocity().horizontalLengthSquared() > 0.02) {
            activityLevel += 0.02;
            activities.append("Move(+0.02) ");
        }
        if (player.isSwimming()) {
            activityLevel += 0.03;
            activities.append("Swim(+0.03) ");
        }
        if (player.isClimbing()) {
            activityLevel += 0.04;
            activities.append("Climb(+0.04) ");
        }

        double newExhaustion = exhaustion + activityLevel;
        newExhaustion = Math.max(0.0, Math.min(100.0, newExhaustion));

        // Check for threshold breaches
        int lastThreshold = (int)(lastExhaustion / 33);
        int newThreshold = (int)(newExhaustion / 33);

        if (newThreshold > lastThreshold && newThreshold < 4) {
            thresholdBreaches++;
            PrimalCraft.LOGGER.warn("⚠️  [EXHAUSTION_WARNING] Event #{}: {} reached {}% exhaustion!",
                thresholdBreaches, player.getName().getString(), (newThreshold * 33));
            LoggingHelper.trackStateChange(
                "exhaustion_" + id,
                String.format("%.1f%%", lastExhaustion),
                String.format("%.1f%%", newExhaustion)
            );
        }

        // Log significant changes
        if (Math.abs(newExhaustion - lastExhaustion) > 0.5) {
            exhaustionUpdates++;
            String emoji = newExhaustion > lastExhaustion ? "📈" : "📉";

            PrimalCraft.LOGGER.debug("{} [EXHAUSTION_CHANGE] Event #{}: {} | {} → {} ({:+.2f})",
                emoji, exhaustionUpdates, player.getName().getString(),
                String.format("%.1f", lastExhaustion), String.format("%.1f", newExhaustion),
                newExhaustion - lastExhaustion);

            if (activityLevel > 0.01) {
                PrimalCraft.LOGGER.trace("   ├─ Activities: {}", activities.toString().isEmpty() ? "None" : activities.toString());
                PrimalCraft.LOGGER.trace("   ├─ Total Activity: {}", String.format("%.3f", activityLevel));
                PrimalCraft.LOGGER.trace("   └─ Health Impact: {}",
                    String.format("%.1f", player.getHealth()) + "/" + String.format("%.1f", player.getMaxHealth()));
            }
        }

        // Periodic comprehensive status
        if (exhaustionTicks % 300 == 0 && newExhaustion > 0.1) {
            PrimalCraft.LOGGER.trace("😩 [EXHAUSTION_STATUS] {} - Level: {} | Status: {}",
                player.getName().getString(),
                String.format("%.1f%%", newExhaustion),
                exhaustionLevel(newExhaustion));
        }

        EXHAUSTION.put(id, newExhaustion);
        LAST_EXHAUSTION.put(id, newExhaustion);
    }

    private static String exhaustionLevel(double exhaustion) {
        if (exhaustion < 25) return "FRESH";
        if (exhaustion < 50) return "FATIGUED";
        if (exhaustion < 75) return "VERY_FATIGUED";
        return "EXHAUSTED";
    }

    public static double getExhaustion(ServerPlayerEntity player) {
        return EXHAUSTION.getOrDefault(player.getUuid(), 0.0);
    }
}
