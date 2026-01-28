package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kimdog_studios.primal_craft.network.BiomeNotificationPayload;
import net.kimdog_studios.primal_craft.util.TemperatureSystem;
import net.kimdog_studios.primal_craft.util.WindSystem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Sends a simple yellow text notification to players when they enter a new biome.
 * Displays in the middle of the screen.
 * Optimized to reduce biome checks and prevent spam.
 */
public class BiomeNotificationHandler {
    private static final Map<ServerPlayerEntity, Identifier> LAST_BIOME = new WeakHashMap<>();
    private static final Map<ServerPlayerEntity, Long> NOTIFICATION_COOLDOWN = new WeakHashMap<>();
    private static final long COOLDOWN_MS = 5000L; // 5 second cooldown between notifications (increased from 2s)
    private static int checkCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Only check biome changes every 20 ticks (1 second) to reduce server load and false positives
            checkCounter++;
            if (checkCounter % 20 != 0) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        try {
            ServerWorld world = (ServerWorld) player.getEntityWorld();
            RegistryEntry<Biome> biomeEntry = world.getBiome(player.getBlockPos());

            // Safely get the biome ID
            var keyOpt = biomeEntry.getKey();
            if (keyOpt.isEmpty()) return;

            Identifier biomeId = keyOpt.get().getValue();

            // Check if biome has changed from last NOTIFIED biome
            Identifier lastBiome = LAST_BIOME.get(player);
            if (biomeId.equals(lastBiome)) return;

            // Check cooldown to prevent spam - BEFORE updating anything
            long now = System.currentTimeMillis();
            Long lastNotification = NOTIFICATION_COOLDOWN.get(player);
            if (lastNotification != null && (now - lastNotification) < COOLDOWN_MS) {
                // Still in cooldown, do NOT update last biome - skip notification completely
                return;
            }

            // Only update last biome and cooldown AFTER passing all checks and sending notification
            LAST_BIOME.put(player, biomeId);
            NOTIFICATION_COOLDOWN.put(player, now);

            // Build simple biome name
            String display = toDisplayName(biomeId);

            // Collect concise environment info
            double temp = TemperatureSystem.getWorldTemperature(player);
            String tempInfo = String.format("%.1f°C", temp);

            WindSystem.WindData wind = WindSystem.getWindData(world);
            double windMph = wind.getEffectiveStrength() * 2.23694; // blocks/sec -> mph
            String windInfo = String.format("%.1f mph", windMph);

            String weatherInfo = getWeatherStatus(world);

            String message = String.format("%s | %s | %s | %s", display, tempInfo, windInfo, weatherInfo);

            // Send to client HUD via payload (client will animate and play sound)
            ServerPlayNetworking.send(player, new BiomeNotificationPayload(message, 0xFFFF00));

        } catch (Exception e) {
            // Silently catch exceptions to prevent server issues
            System.err.println("Error in BiomeNotificationHandler: " + e.getMessage());
        }
    }

    private static String getWeatherStatus(ServerWorld world) {
        if (world.isThundering()) return "Storm";
        if (world.isRaining()) return "Rain";
        return "Clear";
    }

    private static String toDisplayName(Identifier id) {
        String path = id.getPath().replace('_', ' ');
        String[] parts = path.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            sb.append(' ');
        }
        return sb.toString().trim();
    }
}
