package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.network.WindSyncPayload;
import net.kimdog_studios.primal_craft.util.LoggingHelper;
import net.kimdog_studios.primal_craft.util.WindSystem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

/**
 * Server-side wind system handler with detailed logging
 */
public class WindHandler {
    private static int syncTimer = 0;
    private static int windUpdates = 0;
    private static int windSyncs = 0;
    private static int windApplications = 0;

    public static void register() {
        LoggingHelper.logSystemInit("[WIND_SYSTEM]");
        LoggingHelper.logSubsection("Wind physics updates per tick");
        LoggingHelper.logSubsection("Wind syncs every 20 ticks (1 second)");
        LoggingHelper.logSubsection("Wind effects based on sky exposure and weather");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int serverTick = server.getTicks();
            List<net.minecraft.server.world.ServerWorld> worlds = new ArrayList<>();
            server.getWorlds().forEach(worlds::add);
            int worldCount = worlds.size();
            int playerCount = server.getPlayerManager().getPlayerList().size();

            if (serverTick % 200 == 0) {
                PrimalCraft.LOGGER.info("📊 [WIND_STATS] Tick #{} - Worlds: {} | Players: {} | Updates: {} | Syncs: {} | Applications: {}",
                    serverTick, worldCount, playerCount, windUpdates, windSyncs, windApplications);
            }

            if (serverTick % 100 == 0) {
                PrimalCraft.LOGGER.trace("💨 [WIND_TICK] Tick #{} - Processing {} worlds with {} players",
                    serverTick, worldCount, playerCount);
            }

            // Update wind for each world
            server.getWorlds().forEach(world -> {
                windUpdates++;
                WindSystem.updateWind(world, serverTick);

                if (serverTick % 200 == 0) {
                    WindSystem.WindData wind = WindSystem.getWindData(world);
                    PrimalCraft.LOGGER.trace("💨 [WIND_UPDATE] World: {} | Direction: ({}, {}, {}) | Strength: {} | Stormy: {}",
                        world.getRegistryKey().getValue(),
                        String.format("%.2f", wind.direction.x),
                        String.format("%.2f", wind.direction.y),
                        String.format("%.2f", wind.direction.z),
                        String.format("%.2f", wind.getEffectiveStrength()),
                        wind.stormy);
                }
            });

            // Apply wind physics to players
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                windApplications++;
                WindSystem.applyWindToPlayer(player);
            }

            // Sync wind data to clients every second
            syncTimer++;
            if (syncTimer >= 20) {
                syncTimer = 0;
                syncWindToClients(server);
            }
        });

        PrimalCraft.LOGGER.info("✅ [WIND_SYSTEM] WindHandler registered successfully");
    }

    private static void syncWindToClients(net.minecraft.server.MinecraftServer server) {
        int syncCount = 0;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            windSyncs++;
            syncCount++;

            WindSystem.WindData wind = WindSystem.getWindData(player.getEntityWorld());
            Vec3d dir = wind.direction;

            // Check if player is exposed to sky - if not, send 0 wind
            boolean skyVisible = player.getEntityWorld().isSkyVisible(player.getBlockPos());
            double windStrength = skyVisible ? wind.getEffectiveStrength() : 0.0;

            String exposure = skyVisible ? "EXPOSED" : "SHELTERED";

            if (windSyncs % 50 == 0) {
                PrimalCraft.LOGGER.trace("🌐 [WIND_SYNC] Event #{}: {} | Exposure: {} | Wind: ({}, {}, {}) | Strength: {} | Stormy: {}",
                    windSyncs, player.getName().getString(), exposure,
                    String.format("%.2f", dir.x), String.format("%.2f", dir.y), String.format("%.2f", dir.z),
                    String.format("%.2f", windStrength), wind.stormy);
            }

            ServerPlayNetworking.send(player, new WindSyncPayload(
                dir.x,
                dir.y,
                dir.z,
                windStrength,
                wind.stormy
            ));
        }
    }
}