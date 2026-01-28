package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Sleep System Toggle Handler
 *
 * Allows toggling the custom sleep system on/off to return to vanilla behavior.
 * When disabled, sleep works like vanilla (no stamina penalties, normal time advance).
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class SleepSystemToggleHandler {
    private SleepSystemToggleHandler() {}

    private static boolean lastSleepSystemState = true;

    public static void register() {
        PrimalCraft.LOGGER.info("🛏️  [SLEEP_SYSTEM] Registering Sleep System Toggle Handler");

        ServerTickEvents.END_SERVER_TICK.register(SleepSystemToggleHandler::onServerTick);

        PrimalCraft.LOGGER.info("✅ [SLEEP_SYSTEM] Sleep System Toggle Handler registered");
    }

    private static void onServerTick(MinecraftServer server) {
        try {
            boolean isSleepSystemEnabled = PrimalCraftConfig.getAdvanced().features.sleepSystemToggle;

            // Log changes
            if (isSleepSystemEnabled != lastSleepSystemState) {
                lastSleepSystemState = isSleepSystemEnabled;

                String statusMessage = isSleepSystemEnabled
                    ? "Custom sleep system ENABLED (stamina penalties active)"
                    : "Custom sleep system DISABLED (vanilla sleep restored)";

                PrimalCraft.LOGGER.info("🛏️  [SLEEP_SYSTEM] {}", statusMessage);

                // Notify all players
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    player.sendMessage(
                        net.minecraft.text.Text.of("§6[Sleep System] " + statusMessage),
                        false
                    );
                }
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[SLEEP_SYSTEM] Error updating sleep system state", e);
        }
    }

    /**
     * Check if the custom sleep system is currently enabled
     */
    public static boolean isSleepSystemEnabled() {
        return PrimalCraftConfig.getAdvanced().features.sleepSystemToggle;
    }

    /**
     * Enable or disable the sleep system
     */
    public static void setSleepSystemEnabled(boolean enabled) {
        try {
            PrimalCraftConfig.getAdvanced().features.sleepSystemToggle = enabled;
            PrimalCraftConfig.save();
            lastSleepSystemState = !enabled;

            String message = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("🛏️  [SLEEP_SYSTEM] Sleep system {}", message);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[SLEEP_SYSTEM] Failed to toggle sleep system", e);
        }
    }

    /**
     * Get vanilla-compatible sleep info for display
     */
    public static String getSleepSystemStatus() {
        return isSleepSystemEnabled()
            ? "§cCustom Sleep System (Stamina Penalties Active)"
            : "§aVanilla Sleep (No Penalties)";
    }
}
