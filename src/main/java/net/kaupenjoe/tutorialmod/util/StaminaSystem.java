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
    private static final double REGEN_PER_TICK = 1.0;
    private static final Map<UUID, Double> STAMINA = new ConcurrentHashMap<>();
    private static final Identifier STAMINA_SYNC_ID = Identifier.of(TutorialMod.MOD_ID, "stamina_sync");
    private static int tickCounter = 0;
    private static int consumptionEvents = 0;
    private static int syncEvents = 0;

    private StaminaSystem() {}

    public static void register() {
        TutorialMod.LOGGER.info("⚙️  [STAMINA_SYSTEM] Initializing StaminaSystem");
        TutorialMod.LOGGER.debug("   ├─ Max Stamina: {}", MAX_STAMINA);
        TutorialMod.LOGGER.debug("   ├─ Regen Per Tick: {}", REGEN_PER_TICK);
        TutorialMod.LOGGER.debug("   └─ Registering event listeners...");

        ServerTickEvents.END_SERVER_TICK.register(StaminaSystem::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            TutorialMod.LOGGER.info("🧹 [STAMINA_SYSTEM] Server stopping - clearing all stamina data ({} players)", STAMINA.size());
            STAMINA.clear();
        });

        TutorialMod.LOGGER.info("✅ [STAMINA_SYSTEM] StaminaSystem registered successfully");
    }

    private static void tick(MinecraftServer server) {
        tickCounter++;
        int playerCount = server.getPlayerManager().getPlayerList().size();

        if (tickCounter % 100 == 0) {
            TutorialMod.LOGGER.trace("📍 [STAMINA_TICK] Tick #{} - Processing {} players", tickCounter, playerCount);
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID id = player.getUuid();
            double value = STAMINA.getOrDefault(id, MAX_STAMINA);

            // Calculate player temperature
            double temperature = TemperatureSystem.getPlayerTemperature(player);

            // Calculate regen multipliers from all sources
            double regenMultiplier = 1.0;
            double potionMult = StaminaPotionEffects.getRegenMultiplier(player);
            double tempMult = TemperatureSystem.getTemperatureRegenMultiplier(temperature);

            regenMultiplier *= potionMult;
            regenMultiplier *= tempMult;

            if (tickCounter % 100 == 0) {
                TutorialMod.LOGGER.trace("   ├─ [REGEN] {}: Potion: {}, Temp: {} ({}°C)",
                    player.getName().getString(), String.format("%.2f", potionMult),
                    String.format("%.2f", tempMult), String.format("%.1f", temperature));
            }

            double regenAmount = REGEN_PER_TICK * regenMultiplier;
            double next = Math.min(MAX_STAMINA, value + regenAmount);
            STAMINA.put(id, next);

            if (tickCounter % 100 == 0 && regenAmount > 0.01) {
                TutorialMod.LOGGER.trace("   │  └─ Regenerated {}: {} → {} (+{})",
                    player.getName().getString(), String.format("%.1f", value),
                    String.format("%.1f", next), String.format("%.2f", regenAmount));
            }

            // Sync to client every 5 ticks (~4 per second)
            if (tickCounter % 5 == 0) {
                syncEvents++;
                TutorialMod.LOGGER.trace("   ├─ [SYNC] Event #{}: Syncing stamina to {}: {}/{}",
                    syncEvents, player.getName().getString(), String.format("%.1f", next), MAX_STAMINA);

                ServerPlayNetworking.send(player, new StaminaSyncPayload(next, MAX_STAMINA));
                ServerPlayNetworking.send(player, new net.kaupenjoe.tutorialmod.network.TemperatureSyncPayload(temperature));

                double worldTemp = TemperatureSystem.getWorldTemperature(player);
                ServerPlayNetworking.send(player, new net.kaupenjoe.tutorialmod.network.WorldTemperatureSyncPayload(worldTemp));
            }
        }
    }

    /** Attempt to consume stamina. Returns true if successful. */
    public static boolean tryConsume(ServerPlayerEntity player, double amount) {
        UUID id = player.getUuid();
        double current = STAMINA.getOrDefault(id, MAX_STAMINA);
        consumptionEvents++;

        TutorialMod.LOGGER.trace("💸 [CONSUME] Event #{}: {} attempting to consume {}",
            consumptionEvents, player.getName().getString(), String.format("%.2f", amount));
        TutorialMod.LOGGER.trace("   ├─ Current: {}, Required: {}",
            String.format("%.1f", current), String.format("%.2f", amount));

        if (current < amount) {
            TutorialMod.LOGGER.trace("   └─ ✗ FAILED - Insufficient stamina (deficit: {})",
                String.format("%.2f", amount - current));
            return false;
        }

        double newAmount = current - amount;
        STAMINA.put(id, newAmount);

        TutorialMod.LOGGER.trace("   └─ ✓ SUCCESS - {} → {}",
            String.format("%.1f", current), String.format("%.1f", newAmount));

        return true;
    }

    public static double get(ServerPlayerEntity player) {
        double stamina = STAMINA.getOrDefault(player.getUuid(), MAX_STAMINA);
        TutorialMod.LOGGER.trace("📊 [STAMINA_GET] {} stamina: {}/{}",
            player.getName().getString(), String.format("%.1f", stamina), MAX_STAMINA);
        return stamina;
    }

    public static double getMax() {
        return MAX_STAMINA;
    }

    public static Identifier getSyncId() { return STAMINA_SYNC_ID; } // legacy callers; unused now
}
