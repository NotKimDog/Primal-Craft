package net.kimdog_studios.primal_craft.web.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.web.api.ChatEndpoint;
import net.kimdog_studios.primal_craft.web.api.ConfigEndpoint;
import net.kimdog_studios.primal_craft.web.api.DashboardEndpoint;
import net.kimdog_studios.primal_craft.web.api.StatusEndpoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight HTTP server for Primal Craft web dashboard
 * Runs on port 8888 by default
 */
public class WebServer {
    private static HttpServer server;
    private static final int PORT = 8888;
    private static boolean isRunning = false;

    public static void start() {
        if (isRunning) {
            PrimalCraft.LOGGER.warn("[WEB_SERVER] Server already running on port {}", PORT);
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

            // Register API endpoints
            server.createContext("/api/config", new ConfigEndpoint());
            server.createContext("/api/chat", new ChatEndpoint());
            server.createContext("/api/status", new StatusEndpoint());
            server.createContext("/", new DashboardEndpoint());

            server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
            server.start();
            isRunning = true;

            PrimalCraft.LOGGER.info("🌐 [WEB_SERVER] Dashboard started on http://localhost:{}/", PORT);
            PrimalCraft.LOGGER.info("   └─ Open this URL in your browser to access the dashboard");
        } catch (IOException e) {
            PrimalCraft.LOGGER.error("[WEB_SERVER] Failed to start server: {}", e.getMessage(), e);
        }
    }

    public static void stop() {
        if (server != null && isRunning) {
            server.stop(0);
            isRunning = false;
            PrimalCraft.LOGGER.info("✅ [WEB_SERVER] Dashboard stopped");
        }
    }

    public static boolean isRunning() {
        return isRunning;
    }

    /**
     * Send CORS headers to allow cross-origin requests
     */
    public static void setCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    /**
     * Send JSON response
     */
    public static void sendJSON(HttpExchange exchange, String json) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        setCORSHeaders(exchange);

        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    /**
     * Send HTML response
     */
    public static void sendHTML(HttpExchange exchange, String html) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        setCORSHeaders(exchange);

        byte[] response = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    /**
     * Send error response
     */
    public static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        setCORSHeaders(exchange);

        String json = String.format("{\"error\": \"%s\"}", message);
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
