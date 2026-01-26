package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.network.WindSyncPayload;
import net.kaupenjoe.tutorialmod.util.WindSystem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Server-side wind system handler
 */
public class WindHandler {
    private static int syncTimer = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int serverTick = server.getTicks();

            // Update wind for each world
            server.getWorlds().forEach(world -> {
                WindSystem.updateWind(world, serverTick);
            });

            // Apply wind physics to players
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                WindSystem.applyWindToPlayer(player);
            }

            // Sync wind data to clients every second
            syncTimer++;
            if (syncTimer >= 20) {
                syncTimer = 0;
                syncWindToClients(server);
            }
        });
    }

    private static void syncWindToClients(net.minecraft.server.MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            WindSystem.WindData wind = WindSystem.getWindData(player.getEntityWorld());
            Vec3d dir = wind.direction;

            // Check if player is exposed to sky - if not, send 0 wind
            boolean skyVisible = player.getEntityWorld().isSkyVisible(player.getBlockPos());
            double windStrength = skyVisible ? wind.getEffectiveStrength() : 0.0;

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