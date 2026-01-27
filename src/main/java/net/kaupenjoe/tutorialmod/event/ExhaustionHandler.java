package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Exhaustion System - Movement and activity cause fatigue that affects performance
 */
public class ExhaustionHandler {
    private static final Map<UUID, Double> EXHAUSTION = new HashMap<>();
    private static int exhaustionTicks = 0;

    public static void register() {
        TutorialMod.LOGGER.info("⚙️  [EXHAUSTION] Registering ExhaustionHandler");
        TutorialMod.LOGGER.debug("   ├─ Exhaustion system initialized");
        TutorialMod.LOGGER.debug("   └─ Tracking player movement and activity");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            exhaustionTicks++;
            if (exhaustionTicks % 100 == 0) {
                TutorialMod.LOGGER.trace("⏱️  [EXHAUSTION_TICK] Tick #{} - Processing {} players",
                    exhaustionTicks, server.getPlayerManager().getPlayerList().size());
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickExhaustion(player);
            }
        });

        TutorialMod.LOGGER.info("✅ [EXHAUSTION] ExhaustionHandler registered");
    }

    private static void tickExhaustion(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        double exhaustion = EXHAUSTION.getOrDefault(id, 0.0);

        // Activity accumulation
        double activityLevel = 0.0;

        if (player.isSprinting()) {
            activityLevel += 0.05;
        }
        if (player.getVelocity().horizontalLengthSquared() > 0.02) {
            activityLevel += 0.02;
        }
        if (player.isSwimming()) {
            activityLevel += 0.03;
        }
        if (player.isClimbing()) {
            activityLevel += 0.04;
        }

        double newExhaustion = exhaustion + activityLevel;
        newExhaustion = Math.max(0.0, Math.min(100.0, newExhaustion));

        if (exhaustionTicks % 100 == 0 && activityLevel > 0.01) {
            TutorialMod.LOGGER.trace("😩 [EXHAUSTION] {} exhaustion: {}",
                player.getName().getString(), String.format("%.1f", newExhaustion));
            TutorialMod.LOGGER.trace("   ├─ Activity: {}", String.format("%.2f", activityLevel));
            TutorialMod.LOGGER.trace("   ├─ Sprinting: {}", player.isSprinting() ? "YES (+0.05)" : "NO");
            TutorialMod.LOGGER.trace("   ├─ Moving: {}", player.getVelocity().horizontalLengthSquared() > 0.02 ? "YES (+0.02)" : "NO");
            TutorialMod.LOGGER.trace("   └─ Exhaustion: {} → {}",
                String.format("%.1f", exhaustion), String.format("%.1f", newExhaustion));
        }

        EXHAUSTION.put(id, newExhaustion);
    }

    public static double getExhaustion(ServerPlayerEntity player) {
        return EXHAUSTION.getOrDefault(player.getUuid(), 0.0);
    }
}
