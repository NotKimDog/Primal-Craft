package net.kimdog_studios.primal_craft.web.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.web.server.WebServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * API endpoint for sending chat messages to the game
 * POST /api/chat - Send a message to chat
 */
public class ChatEndpoint implements HttpHandler {
    private static MinecraftServer server;

    public static void setServer(MinecraftServer minecraftServer) {
        server = minecraftServer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            if ("POST".equals(method)) {
                handlePostChat(exchange);
            } else if ("OPTIONS".equals(method)) {
                WebServer.setCORSHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            } else {
                WebServer.sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[CHAT_API] Error handling request: {}", e.getMessage(), e);
            WebServer.sendError(exchange, 500, "Internal server error");
        }
    }

    private void handlePostChat(HttpExchange exchange) throws IOException {
        if (server == null) {
            WebServer.sendError(exchange, 503, "Server not ready");
            return;
        }

        try {
            // Read request body
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            JSONObject request = new JSONObject(body.toString());
            String message = request.optString("message", "");
            String sender = request.optString("sender", "Dashboard");

            if (message.isEmpty()) {
                WebServer.sendError(exchange, 400, "Message cannot be empty");
                return;
            }

            // Limit message length
            if (message.length() > 256) {
                message = message.substring(0, 256);
            }

            // Broadcast message to all players
            Text chatMessage = Text.of("[" + sender + "] " + message);
            server.getPlayerManager().broadcast(chatMessage, false);

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Message sent to chat");
            response.put("playerCount", server.getPlayerManager().getPlayerList().size());

            PrimalCraft.LOGGER.info("💬 [CHAT_API] Message from {}: {}", sender, message);
            WebServer.sendJSON(exchange, response.toString());
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[CHAT_API] Error sending message: {}", e.getMessage());
            WebServer.sendError(exchange, 400, "Invalid request");
        }
    }
}
