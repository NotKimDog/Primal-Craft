package net.kimdog_studios.primal_craft.util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.network.WeatherNotificationPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Custom weather notification system with alerts and state tracking
 */
public class WeatherNotificationSystem {
    private static final Map<UUID, String> lastWeatherState = new HashMap<>();
    private static final Map<UUID, Long> lastNotificationTime = new HashMap<>();
    private static final long NOTIFICATION_COOLDOWN = 600000;
    private static int notificationsSent = 0;

    /**
     * Check and notify player of weather changes
     */
    public static void checkAndNotify(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        long currentTime = System.currentTimeMillis();

        // Get current weather info
        TemperatureSystem.WeatherInfo weather = TemperatureSystem.getWeatherInfo(player);
        String currentState = weather.state;

        // Check if state changed
        String lastState = lastWeatherState.get(playerId);
        if (lastState == null || !lastState.equals(currentState)) {
            PrimalCraft.LOGGER.debug("🌦️  [WEATHER] {} weather changed: {} → {}",
                player.getName().getString(), lastState != null ? lastState : "INITIAL", currentState);

            // Check cooldown
            Long lastTime = lastNotificationTime.get(playerId);
            if (lastTime == null || (currentTime - lastTime) > NOTIFICATION_COOLDOWN) {
                // Send notification
                sendWeatherNotification(player, weather);
                lastNotificationTime.put(playerId, currentTime);
            } else {
                long cooldownRemaining = NOTIFICATION_COOLDOWN - (currentTime - lastTime);
                PrimalCraft.LOGGER.trace("   └─ Notification on cooldown ({} seconds remaining)",
                    cooldownRemaining / 1000);
            }
            lastWeatherState.put(playerId, currentState);
        }
    }

    /**
     * Send weather notification to player
     */
    private static void sendWeatherNotification(ServerPlayerEntity player, TemperatureSystem.WeatherInfo weather) {
        notificationsSent++;

        PrimalCraft.LOGGER.debug("📢 [WEATHER_NOTIFY] Event #{} - {} weather: {}",
            notificationsSent, player.getName().getString(), weather.state);
        PrimalCraft.LOGGER.trace("   ├─ Message: {}", weather.alert);
        PrimalCraft.LOGGER.trace("   ├─ Wind Chill: {}°C", String.format("%.1f", weather.windChill));
        PrimalCraft.LOGGER.trace("   ├─ Biome Temp: {}°C", String.format("%.1f", weather.effectiveTemp));

        // Build notification message
        String message = weather.alert;
        if (weather.windChill < -2.0) {
            message += "|Wind: " + String.format("%.1f", weather.windChill) + "°C";
        }

        // Send to client for animated display
        int color = getWeatherNotificationColor(weather.state);
        PrimalCraft.LOGGER.trace("   ├─ Color: #" + Integer.toHexString(color));
        ServerPlayNetworking.send(player, new WeatherNotificationPayload(message, color));

        // Play appropriate sound based on weather state
        String soundType = "NONE";
        switch (weather.state) {
            case "Clear":
                soundType = "CLEAR_WEATHER";
                player.playSound(net.kimdog_studios.primal_craft.sound.ModSounds.CLEAR_WEATHER, 0.7f, 1.0f);
                break;
            case "Rain":
            case "Snow":
            case "Thunderstorm":
            case "Snowstorm":
                soundType = "RAIN_WEATHER";
                player.playSound(net.kimdog_studios.primal_craft.sound.ModSounds.RAIN_WEATHER, 0.7f, 1.0f);
                break;
            case "Blizzard":
            case "Heatwave":
            case "Hard Freeze":
            default:
                soundType = "THUNDER_WEATHER";
                player.playSound(net.kimdog_studios.primal_craft.sound.ModSounds.THUNDER_WEATHER, 0.6f, 1.0f);
                break;
        }
        PrimalCraft.LOGGER.trace("   └─ Sound: {}", soundType);
    }

    /**
     * Get notification color based on severity
     */
    public static int getWeatherNotificationColor(String state) {
        return switch (state) {
            case "Blizzard", "BLIZZARD" -> 0xFF88CCFF;
            case "Hard Freeze" -> 0xFF66AAFF;
            case "Heatwave", "HEATWAVE" -> 0xFFFF3333;
            case "Thunderstorm", "THUNDERSTORM" -> 0xFFFFDD00;
            case "Snowstorm" -> 0xFFCCEEFF;
            case "Rain", "RAIN" -> 0xFF6699FF;
            case "Snow" -> 0xFFDDEEFF;
            case "Dust Storm", "DUST_STORM" -> 0xFFDD8833;
            case "Windy", "WINDY" -> 0xFF99DDFF;
            case "Foggy", "FOGGY" -> 0xFFBBBBDD;
            default -> 0xFF66FF66;
        };
    }

    /**
     * Reset notification state (e.g., on player respawn)
     */
    public static void resetPlayer(UUID playerId) {
        lastWeatherState.remove(playerId);
        lastNotificationTime.remove(playerId);
    }

    /**
     * Broadcast weather command change to all players
     */
    public static void broadcastWeatherCommand(net.minecraft.server.MinecraftServer server, String weatherType) {
        if (server == null) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player == null) continue;

            // Get current temperature for this player
            double temperature = TemperatureSystem.getPlayerTemperature(player);
            TemperatureSystem.WeatherInfo weather = TemperatureSystem.getWeatherInfo(player);

            // Build message with weather type and temperature
            String message = "Weather changed to: " + weatherType;
            message += " | Temperature: " + String.format("%.1f", temperature) + "°C";

            // Add weather state if different from command
            if (!weather.state.equals(weatherType)) {
                message += " (" + weather.state + ")";
            }

            // Get color based on weather type
            int color;
            switch (weatherType) {
                case "Clear" -> color = 0xFF66FF66; // Green
                case "Rain" -> color = 0xFF6699FF; // Blue
                case "Thunder" -> color = 0xFFFFDD00; // Yellow
                default -> color = getWeatherNotificationColor(weather.state);
            }

            // Send notification
            ServerPlayNetworking.send(player, new WeatherNotificationPayload(message, color));

            // Play appropriate sound based on weather type
            switch (weatherType) {
                case "Clear":
                    player.playSound(net.kimdog_studios.primal_craft.sound.ModSounds.CLEAR_WEATHER, 0.7f, 1.0f);
                    break;
                case "Rain":
                    player.playSound(net.kimdog_studios.primal_craft.sound.ModSounds.RAIN_WEATHER, 0.7f, 1.0f);
                    break;
                case "Thunder":
                    player.playSound(net.kimdog_studios.primal_craft.sound.ModSounds.THUNDER_WEATHER, 0.7f, 1.0f);
                    break;
            }
        }
    }
}
