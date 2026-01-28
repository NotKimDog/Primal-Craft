package net.kimdog_studios.primal_craft.web.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.kimdog_studios.primal_craft.web.server.WebServer;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * API endpoint for reading and updating config in real-time
 * GET /api/config - Get current configuration
 * POST /api/config - Update configuration
 */
public class ConfigEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                handleGetConfig(exchange);
            } else if ("POST".equals(method)) {
                handlePostConfig(exchange);
            } else if ("OPTIONS".equals(method)) {
                WebServer.setCORSHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            } else {
                WebServer.sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[CONFIG_API] Error handling request: {}", e.getMessage(), e);
            WebServer.sendError(exchange, 500, "Internal server error");
        }
    }

    private void handleGetConfig(HttpExchange exchange) throws IOException {
        JSONObject config = new JSONObject();

        try {
            // Get current config values
            var gameplay = PrimalCraftConfig.getGameplay();
            var hud = PrimalCraftConfig.getHUD();
            var difficulty = PrimalCraftConfig.getDifficulty();

            // Build gameplay section
            JSONObject gameplayObj = new JSONObject();
            gameplayObj.put("staminaSystemEnabled", gameplay.staminaSystemEnabled);
            gameplayObj.put("staminaDepletionRate", gameplay.staminaDepletionRate);
            gameplayObj.put("staminaRecoveryRate", gameplay.staminaRecoveryRate);
            gameplayObj.put("thirstSystemEnabled", gameplay.thirstSystemEnabled);
            gameplayObj.put("thirstDepletionRate", gameplay.thirstDepletionRate);
            gameplayObj.put("temperatureSystemEnabled", gameplay.temperatureSystemEnabled);
            gameplayObj.put("environmentalHazardsEnabled", gameplay.environmentalHazardsEnabled);

            // Build HUD section
            JSONObject hudObj = new JSONObject();
            hudObj.put("showStaminaBar", hud.showStaminaBar);
            hudObj.put("showThirstBar", hud.showThirstBar);
            hudObj.put("showTemperatureIndicator", hud.showTemperatureIndicator);
            hudObj.put("showWeatherNotifications", hud.showWeatherNotifications);
            hudObj.put("showBiomeNotifications", hud.showBiomeNotifications);
            hudObj.put("hudScale", hud.hudScale);
            hudObj.put("hudOpacity", hud.hudOpacity);

            // Build difficulty section
            JSONObject difficultyObj = new JSONObject();
            difficultyObj.put("staminalossDifficulty", difficulty.staminalossDifficulty);
            difficultyObj.put("thirstDifficulty", difficulty.thirstDifficulty);
            difficultyObj.put("temperatureDifficulty", difficulty.temperatureDifficulty);
            difficultyObj.put("hazardDifficulty", difficulty.hazardDifficulty);

            config.put("gameplay", gameplayObj);
            config.put("hud", hudObj);
            config.put("difficulty", difficultyObj);
            config.put("timestamp", System.currentTimeMillis());

            PrimalCraft.LOGGER.debug("[CONFIG_API] Sent configuration");
            WebServer.sendJSON(exchange, config.toString());
        } catch (JSONException e) {
            PrimalCraft.LOGGER.error("[CONFIG_API] JSON error: {}", e.getMessage());
            WebServer.sendError(exchange, 500, "JSON error");
        }
    }

    private void handlePostConfig(HttpExchange exchange) throws IOException {
        try {
            // Read request body
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            JSONObject updates = new JSONObject(body.toString());
            JSONObject response = new JSONObject();
            int updated = 0;

            // Update gameplay settings
            if (updates.has("gameplay")) {
                JSONObject gameplay = updates.getJSONObject("gameplay");
                var gameplaySettings = PrimalCraftConfig.getGameplay();

                if (gameplay.has("staminaSystemEnabled")) {
                    gameplaySettings.staminaSystemEnabled = gameplay.getBoolean("staminaSystemEnabled");
                    updated++;
                }
                if (gameplay.has("staminaDepletionRate")) {
                    gameplaySettings.staminaDepletionRate = (float) gameplay.getDouble("staminaDepletionRate");
                    updated++;
                }
                if (gameplay.has("staminaRecoveryRate")) {
                    gameplaySettings.staminaRecoveryRate = (float) gameplay.getDouble("staminaRecoveryRate");
                    updated++;
                }
                if (gameplay.has("thirstSystemEnabled")) {
                    gameplaySettings.thirstSystemEnabled = gameplay.getBoolean("thirstSystemEnabled");
                    updated++;
                }
                if (gameplay.has("thirstDepletionRate")) {
                    gameplaySettings.thirstDepletionRate = (float) gameplay.getDouble("thirstDepletionRate");
                    updated++;
                }
                if (gameplay.has("temperatureSystemEnabled")) {
                    gameplaySettings.temperatureSystemEnabled = gameplay.getBoolean("temperatureSystemEnabled");
                    updated++;
                }
                if (gameplay.has("environmentalHazardsEnabled")) {
                    gameplaySettings.environmentalHazardsEnabled = gameplay.getBoolean("environmentalHazardsEnabled");
                    updated++;
                }
            }

            // Update HUD settings
            if (updates.has("hud")) {
                JSONObject hud = updates.getJSONObject("hud");
                var hudSettings = PrimalCraftConfig.getHUD();

                if (hud.has("showStaminaBar")) {
                    hudSettings.showStaminaBar = hud.getBoolean("showStaminaBar");
                    updated++;
                }
                if (hud.has("showThirstBar")) {
                    hudSettings.showThirstBar = hud.getBoolean("showThirstBar");
                    updated++;
                }
                if (hud.has("showTemperatureIndicator")) {
                    hudSettings.showTemperatureIndicator = hud.getBoolean("showTemperatureIndicator");
                    updated++;
                }
                if (hud.has("showWeatherNotifications")) {
                    hudSettings.showWeatherNotifications = hud.getBoolean("showWeatherNotifications");
                    updated++;
                }
                if (hud.has("showBiomeNotifications")) {
                    hudSettings.showBiomeNotifications = hud.getBoolean("showBiomeNotifications");
                    updated++;
                }
                if (hud.has("hudScale")) {
                    hudSettings.hudScale = (float) hud.getDouble("hudScale");
                    updated++;
                }
                if (hud.has("hudOpacity")) {
                    hudSettings.hudOpacity = (float) hud.getDouble("hudOpacity");
                    updated++;
                }
            }

            // Update difficulty settings
            if (updates.has("difficulty")) {
                JSONObject difficulty = updates.getJSONObject("difficulty");
                var difficultySettings = PrimalCraftConfig.getDifficulty();

                if (difficulty.has("staminalossDifficulty")) {
                    difficultySettings.staminalossDifficulty = (float) difficulty.getDouble("staminalossDifficulty");
                    updated++;
                }
                if (difficulty.has("thirstDifficulty")) {
                    difficultySettings.thirstDifficulty = (float) difficulty.getDouble("thirstDifficulty");
                    updated++;
                }
                if (difficulty.has("temperatureDifficulty")) {
                    difficultySettings.temperatureDifficulty = (float) difficulty.getDouble("temperatureDifficulty");
                    updated++;
                }
                if (difficulty.has("hazardDifficulty")) {
                    difficultySettings.hazardDifficulty = (float) difficulty.getDouble("hazardDifficulty");
                    updated++;
                }
            }

            // Save config
            PrimalCraftConfig.save();

            response.put("success", true);
            response.put("updated", updated);
            response.put("message", "Configuration updated successfully");

            PrimalCraft.LOGGER.info("⚙️  [CONFIG_API] Updated {} configuration settings", updated);
            WebServer.sendJSON(exchange, response.toString());
        } catch (JSONException e) {
            PrimalCraft.LOGGER.error("[CONFIG_API] Invalid JSON: {}", e.getMessage());
            WebServer.sendError(exchange, 400, "Invalid JSON");
        }
    }
}
