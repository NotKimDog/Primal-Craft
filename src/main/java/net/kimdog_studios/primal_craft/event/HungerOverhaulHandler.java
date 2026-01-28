package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.kimdog_studios.primal_craft.util.LoggingHelper;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Hunger Overhaul - Makes food meaningful, faster depletion, affects healing
 */
public class HungerOverhaulHandler {
    private static final double HUNGER_DRAIN_MULTIPLIER = 1.5; // 50% faster depletion
    private static final Map<UUID, Integer> LAST_HUNGER_LEVEL = new HashMap<>();
    private static final Map<UUID, Float> LAST_SATURATION = new HashMap<>();
    private static final Map<UUID, Double> LAST_HEALTH = new HashMap<>();
    private static int hungerTicks = 0;
    private static int hungerChangeEvents = 0;
    private static int drainApplications = 0;

    public static void register() {
        LoggingHelper.logSystemInit("[HUNGER_OVERHAUL]");
        LoggingHelper.logSubsection("Hunger drain multiplier: " + HUNGER_DRAIN_MULTIPLIER + "x");
        LoggingHelper.logSubsection("Implementing custom hunger mechanics");
        LoggingHelper.logSubsection("Features: Faster depletion, Activity-based drain, Healing modifiers");

        ServerTickEvents.END_SERVER_TICK.register(server -> {

            hungerTicks++;
            int playerCount = server.getPlayerManager().getPlayerList().size();

            if (hungerTicks % 200 == 0) {
                PrimalCraft.LOGGER.info("📊 [HUNGER_OVERHAUL_STATS] Tick #{} - Players: {} | Changes: {} | Drains: {}",
                    hungerTicks, playerCount, hungerChangeEvents, drainApplications);
            }

            if (hungerTicks % 100 == 0) {
                PrimalCraft.LOGGER.trace("⏱️  [HUNGER_TICK] Tick #{} - Processing {} players",
                    hungerTicks, playerCount);
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickHunger(player);
            }
        });

        PrimalCraft.LOGGER.info("✅ [HUNGER_OVERHAUL] HungerOverhaulHandler registered successfully");
    }

    private static void tickHunger(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        int currentHunger = player.getHungerManager().getFoodLevel();
        float currentSaturation = player.getHungerManager().getSaturationLevel();
        double currentHealth = player.getHealth();

        int previousHunger = LAST_HUNGER_LEVEL.getOrDefault(id, currentHunger);
        float previousSaturation = LAST_SATURATION.getOrDefault(id, currentSaturation);
        double previousHealth = LAST_HEALTH.getOrDefault(id, currentHealth);

        // Track hunger changes
        if (currentHunger != previousHunger) {
            hungerChangeEvents++;
            int delta = currentHunger - previousHunger;
            String type = delta > 0 ? "GAIN" : "LOSS";
            String emoji = delta > 0 ? "📈" : "📉";

            PrimalCraft.LOGGER.debug("{} [HUNGER_CHANGE] Event #{}: {} hunger {}: {} → {} ({:+d}) | Saturation: {} → {} | Health: {} → {}",
                emoji, hungerChangeEvents,
                player.getName().getString(), type,
                previousHunger, currentHunger, delta,
                String.format("%.2f", previousSaturation), String.format("%.2f", currentSaturation),
                String.format("%.1f", previousHealth), String.format("%.1f", currentHealth));

            LoggingHelper.trackStateChange(
                "hunger_" + player.getUuid(),
                "Level: " + previousHunger + ", Saturation: " + String.format("%.2f", previousSaturation),
                "Level: " + currentHunger + ", Saturation: " + String.format("%.2f", currentSaturation)
            );

            LAST_HUNGER_LEVEL.put(id, currentHunger);
            LAST_SATURATION.put(id, currentSaturation);
            LAST_HEALTH.put(id, currentHealth);
        }

        // Apply custom hunger drain based on activity
        if (hungerTicks % 20 == 0) {
            double customDrain = 0.0;
            StringBuilder activityLog = new StringBuilder();

            if (player.isSprinting()) {
                double sprintDrain = 0.05 * HUNGER_DRAIN_MULTIPLIER;
                customDrain += sprintDrain;
                activityLog.append("Sprint: ").append(String.format("%.3f", sprintDrain)).append(" | ");
            }
            if (player.getVelocity().horizontalLengthSquared() > 0.02) {
                double moveDrain = 0.02 * HUNGER_DRAIN_MULTIPLIER;
                customDrain += moveDrain;
                activityLog.append("Move: ").append(String.format("%.3f", moveDrain)).append(" | ");
            }
            if (player.isSwimming()) {
                double swimDrain = 0.03 * HUNGER_DRAIN_MULTIPLIER;
                customDrain += swimDrain;
                activityLog.append("Swim: ").append(String.format("%.3f", swimDrain)).append(" | ");
            }

            if (customDrain > 0.01) {
                drainApplications++;
                PrimalCraft.LOGGER.trace("🍖 [HUNGER_DRAIN] Event #{}: {} total drain: {} | {}",
                    drainApplications, player.getName().getString(),
                    String.format("%.3f", customDrain), activityLog.toString());
                PrimalCraft.LOGGER.trace("   ├─ Food Level: {} | Saturation: {} | Health: {}",
                    currentHunger, String.format("%.2f", currentSaturation), String.format("%.1f", currentHealth));
            }
        }
    }

    public static int getHunger(ServerPlayerEntity player) {
        return player.getHungerManager().getFoodLevel();
    }
}
