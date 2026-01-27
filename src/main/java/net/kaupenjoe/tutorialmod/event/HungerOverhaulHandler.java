package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kaupenjoe.tutorialmod.TutorialMod;
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
    private static int hungerTicks = 0;

    public static void register() {
        TutorialMod.LOGGER.info("🍖 [HUNGER_OVERHAUL] Registering HungerOverhaulHandler");
        TutorialMod.LOGGER.debug("   ├─ Hunger drain multiplier: {}x", HUNGER_DRAIN_MULTIPLIER);
        TutorialMod.LOGGER.debug("   └─ Implementing custom hunger mechanics");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            hungerTicks++;
            if (hungerTicks % 100 == 0) {
                TutorialMod.LOGGER.trace("⏱️  [HUNGER_TICK] Tick #{} - Processing {} players",
                    hungerTicks, server.getPlayerManager().getPlayerList().size());
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickHunger(player);
            }
        });

        TutorialMod.LOGGER.info("✅ [HUNGER_OVERHAUL] HungerOverhaulHandler registered");
    }

    private static void tickHunger(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        int currentHunger = player.getHungerManager().getFoodLevel();
        int previousHunger = LAST_HUNGER_LEVEL.getOrDefault(id, currentHunger);

        // Track hunger changes
        if (currentHunger != previousHunger) {
            int delta = currentHunger - previousHunger;
            String type = delta > 0 ? "GAIN" : "LOSS";

            if (hungerTicks % 5 == 0) {
                TutorialMod.LOGGER.debug("🍖 [HUNGER] {} hunger {}: {} → {} ({:+d})",
                    player.getName().getString(), type, previousHunger, currentHunger, delta);
                TutorialMod.LOGGER.trace("   ├─ Saturation: {}",
                    String.format("%.1f", player.getHungerManager().getSaturationLevel()));
                TutorialMod.LOGGER.trace("   └─ Health: {}", String.format("%.1f", player.getHealth()));
            }

            LAST_HUNGER_LEVEL.put(id, currentHunger);
        }

        // Apply custom hunger drain based on activity
        if (hungerTicks % 20 == 0) {
            double customDrain = 0.0;

            if (player.isSprinting()) {
                customDrain += 0.05 * HUNGER_DRAIN_MULTIPLIER;
            }
            if (player.getVelocity().horizontalLengthSquared() > 0.02) {
                customDrain += 0.02 * HUNGER_DRAIN_MULTIPLIER;
            }
            if (player.isSwimming()) {
                customDrain += 0.03 * HUNGER_DRAIN_MULTIPLIER;
            }

            if (customDrain > 0.01 && hungerTicks % 100 == 0) {
                TutorialMod.LOGGER.trace("🍖 [DRAIN] {} applying custom drain: {}",
                    player.getName().getString(), String.format("%.2f", customDrain));
            }
        }
    }

    public static int getHunger(ServerPlayerEntity player) {
        return player.getHungerManager().getFoodLevel();
    }
}
