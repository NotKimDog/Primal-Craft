package net.kimdog_studios.primal_craft.web.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.kimdog_studios.primal_craft.web.server.WebServer;
import net.minecraft.server.MinecraftServer;
import org.json.JSONObject;

import java.io.IOException;

/**
 * API endpoint for server status information
 * GET /api/status - Get current server status
 */
public class StatusEndpoint implements HttpHandler {
    private static MinecraftServer server;

    public static void setServer(MinecraftServer minecraftServer) {
        server = minecraftServer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("GET".equals(method)) {
            handleGetStatus(exchange);
        } else if ("OPTIONS".equals(method)) {
            WebServer.setCORSHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        } else {
            WebServer.sendError(exchange, 405, "Method not allowed");
        }
    }

    private void handleGetStatus(HttpExchange exchange) throws IOException {
        JSONObject status = new JSONObject();

        try {
            if (server != null && !server.isStopped()) {
                int playerCount = server.getPlayerManager().getPlayerList().size();
                int maxPlayers = 20; // Default

                status.put("serverRunning", true);
                status.put("playerCount", playerCount);
                status.put("maxPlayers", maxPlayers);
                status.put("ticks", server.getTicks());

                // Add player list
                org.json.JSONArray players = new org.json.JSONArray();
                for (var player : server.getPlayerManager().getPlayerList()) {
                    org.json.JSONObject playerObj = new org.json.JSONObject();
                    playerObj.put("name", player.getName().getString());
                    playerObj.put("health", (int) player.getHealth());
                    playerObj.put("maxHealth", (int) player.getMaxHealth());
                    playerObj.put("food", player.getHungerManager().getFoodLevel());
                    playerObj.put("level", player.experienceLevel);
                    players.put(playerObj);
                }
                status.put("players", players);
            } else {
                status.put("serverRunning", false);
                status.put("message", "Server is not running");
            }

            status.put("timestamp", System.currentTimeMillis());
            WebServer.sendJSON(exchange, status.toString());
        } catch (Exception e) {
            WebServer.sendError(exchange, 500, "Error retrieving status");
        }
    }
}
