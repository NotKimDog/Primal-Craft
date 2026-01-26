package net.kaupenjoe.tutorialmod.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.network.StaminaSyncPayload;
// import net.kaupenjoe.tutorialmod.event.MountStaminaHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight stamina system: server-only, global map keyed by player UUID.
 * - Regens every server tick.
 * - Provides tryConsume for gameplay hooks.
 * - Resets on server stop (no persistence yet).
 */
public final class StaminaSystem {
    private static final double MAX_STAMINA = 100.0;
    private static final double REGEN_PER_TICK = 1.0; // Increased from 0.2 - ~20/sec instead of 4/sec
    private static final Map<UUID, Double> STAMINA = new ConcurrentHashMap<>();
    private static final Identifier STAMINA_SYNC_ID = Identifier.of(TutorialMod.MOD_ID, "stamina_sync");
    private static int tickCounter = 0;

    private StaminaSystem() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(StaminaSystem::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> STAMINA.clear());
    }

    private static void tick(MinecraftServer server) {
        tickCounter++;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID id = player.getUuid();
            double value = STAMINA.getOrDefault(id, MAX_STAMINA);

            // Calculate player temperature
            double temperature = TemperatureSystem.getPlayerTemperature(player);

            // Calculate regen multipliers from all sources
            double regenMultiplier = 1.0;
            regenMultiplier *= StaminaPotionEffects.getRegenMultiplier(player);
            regenMultiplier *= TemperatureSystem.getTemperatureRegenMultiplier(temperature);
            // regenMultiplier *= EnvironmentalStaminaEffects.getEnvironmentRegenMultiplier(player);
            // regenMultiplier *= ArmorWeightSystem.getArmorRegenMultiplier(player);

            // Check for pet bonus
            // if (MountStaminaHandler.hasPetNearby(player)) {
            //     regenMultiplier += 0.3;
            // }

            double next = Math.min(MAX_STAMINA, value + (REGEN_PER_TICK * regenMultiplier));
            STAMINA.put(id, next);

            // Check for adrenaline rush
            // if (AdrenalineRushSystem.shouldTriggerRush(player, value)) {
            //     AdrenalineRushSystem.triggerAdrenalineRush(player);
            //     // Restore 30 stamina
            //     STAMINA.put(id, Math.min(MAX_STAMINA, value + 30.0));
            // }

            // Sync to client every 5 ticks (~4 per second)
            if (tickCounter % 5 == 0) {
                ServerPlayNetworking.send(player, new StaminaSyncPayload(
                    STAMINA.get(id),
                    MAX_STAMINA
                ));

                // Sync temperature
                ServerPlayNetworking.send(player, new net.kaupenjoe.tutorialmod.network.TemperatureSyncPayload(temperature));
                // Sync world/biome temperature
                double worldTemp = TemperatureSystem.getWorldTemperature(player);
                ServerPlayNetworking.send(player, new net.kaupenjoe.tutorialmod.network.WorldTemperatureSyncPayload(worldTemp));
            }
        }
    }

    /** Attempt to consume stamina. Returns true if successful. */
    public static boolean tryConsume(ServerPlayerEntity player, double amount) {
        UUID id = player.getUuid();
        double current = STAMINA.getOrDefault(id, MAX_STAMINA);
        if (current < amount) {
            return false;
        }
        STAMINA.put(id, current - amount);
        return true;
    }

    public static double get(ServerPlayerEntity player) {
        return STAMINA.getOrDefault(player.getUuid(), MAX_STAMINA);
    }

    public static double getMax() {
        return MAX_STAMINA;
    }

    public static Identifier getSyncId() { return STAMINA_SYNC_ID; } // legacy callers; unused now
}
