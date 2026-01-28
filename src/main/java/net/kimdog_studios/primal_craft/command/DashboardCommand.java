package net.kimdog_studios.primal_craft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.web.server.WebServer;

/**
 * Command to manage the web dashboard
 * /dashboard - Show dashboard status and help
 * /dashboard start - Start the web server
 * /dashboard stop - Stop the web server
 * /dashboard status - Show current status
 * /dashboard info - Show detailed server info
 * /dashboard reload - Reload configuration
 */
public class DashboardCommand {
    private static int commandExecutions = 0;
    private static long lastStartTime = 0;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] Registering dashboard command dispatcher");

        dispatcher.register(
            net.minecraft.server.command.CommandManager.literal("dashboard")
                .executes(DashboardCommand::showHelp)
                .then(
                    net.minecraft.server.command.CommandManager.literal("start")
                        .executes(DashboardCommand::startDashboard)
                )
                .then(
                    net.minecraft.server.command.CommandManager.literal("stop")
                        .executes(DashboardCommand::stopDashboard)
                )
                .then(
                    net.minecraft.server.command.CommandManager.literal("status")
                        .executes(DashboardCommand::showStatus)
                )
                .then(
                    net.minecraft.server.command.CommandManager.literal("info")
                        .executes(DashboardCommand::showInfo)
                )
                .then(
                    net.minecraft.server.command.CommandManager.literal("reload")
                        .executes(DashboardCommand::reloadConfig)
                )
        );

        PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] Dashboard command registered successfully");
    }

    private static int showHelp(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        commandExecutions++;

        PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] #{} Help displayed by {}", commandExecutions, source.getName());
        PrimalCraft.LOGGER.debug("   └─ Player: {}", source.getName());

        source.sendFeedback(() -> Text.literal("").append(
            Text.literal("╔════════════════════════════════════════╗\n")
                .formatted(Formatting.GOLD)
        ).append(
            Text.literal("║  🌐 Primal Craft Dashboard             ║\n")
                .formatted(Formatting.YELLOW)
        ).append(
            Text.literal("╚════════════════════════════════════════╝\n")
                .formatted(Formatting.GOLD)
        ).append(
            Text.literal("  ").formatted(Formatting.RESET)
        ).append(
            Text.literal("/dashboard start")
                .formatted(Formatting.AQUA)
        ).append(
            Text.literal(" - Start the web dashboard\n")
                .formatted(Formatting.GRAY)
        ).append(
            Text.literal("  ").formatted(Formatting.RESET)
        ).append(
            Text.literal("/dashboard stop")
                .formatted(Formatting.RED)
        ).append(
            Text.literal(" - Stop the web dashboard\n")
                .formatted(Formatting.GRAY)
        ).append(
            Text.literal("  ").formatted(Formatting.RESET)
        ).append(
            Text.literal("/dashboard status")
                .formatted(Formatting.LIGHT_PURPLE)
        ).append(
            Text.literal(" - Check dashboard status\n")
                .formatted(Formatting.GRAY)
        ).append(
            Text.literal("  ").formatted(Formatting.RESET)
        ).append(
            Text.literal("/dashboard info")
                .formatted(Formatting.AQUA)
        ).append(
            Text.literal(" - Show detailed server info\n")
                .formatted(Formatting.GRAY)
        ).append(
            Text.literal("  ").formatted(Formatting.RESET)
        ).append(
            Text.literal("/dashboard reload")
                .formatted(Formatting.GREEN)
        ).append(
            Text.literal(" - Reload configuration\n")
                .formatted(Formatting.GRAY)
        ).append(
            Text.literal("\n  ").formatted(Formatting.RESET)
        ).append(
            Text.literal("📍 Dashboard URL: ")
                .formatted(Formatting.GOLD)
        ).append(
            Text.literal("http://localhost:8888")
                .formatted(Formatting.GREEN)
        ), false);

        return 1;
    }

    private static int startDashboard(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        commandExecutions++;

        PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] #{} Start request from {}", commandExecutions, source.getName());

        if (WebServer.isRunning()) {
            PrimalCraft.LOGGER.warn("[DASHBOARD_COMMAND] Start requested but server already running");
            source.sendFeedback(() -> Text.literal("❌ Dashboard is already running!")
                .formatted(Formatting.RED), false);
            return 0;
        }

        try {
            long startTime = System.currentTimeMillis();
            PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] ├─ Starting web server...");

            WebServer.start();

            long elapsed = System.currentTimeMillis() - startTime;
            lastStartTime = System.currentTimeMillis();

            PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] ├─ ✅ Web server started successfully");
            PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] ├─ Startup time: {}ms", elapsed);
            PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] └─ URL: http://localhost:8888");

            source.sendFeedback(() -> Text.literal("")
                .append(Text.literal("✅ Dashboard started successfully!\n").formatted(Formatting.GREEN))
                .append(Text.literal("🌐 Open: ").formatted(Formatting.GOLD))
                .append(Text.literal("http://localhost:8888").formatted(Formatting.AQUA))
                .append(Text.literal("\n⏱️  Startup time: ").formatted(Formatting.GRAY))
                .append(Text.literal(elapsed + "ms").formatted(Formatting.YELLOW))
                , false);

            return 1;
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DASHBOARD_COMMAND] ❌ Failed to start dashboard", e);
            PrimalCraft.LOGGER.error("[DASHBOARD_COMMAND] └─ Error: {}", e.getMessage());

            source.sendFeedback(() -> Text.literal("❌ Failed to start dashboard: " + e.getMessage())
                .formatted(Formatting.RED), false);
            return 0;
        }
    }

    private static int stopDashboard(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        commandExecutions++;

        PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] #{} Stop request from {}", commandExecutions, source.getName());

        if (!WebServer.isRunning()) {
            PrimalCraft.LOGGER.warn("[DASHBOARD_COMMAND] Stop requested but server not running");
            source.sendFeedback(() -> Text.literal("❌ Dashboard is not running!")
                .formatted(Formatting.RED), false);
            return 0;
        }

        try {
            long uptime = System.currentTimeMillis() - lastStartTime;
            PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] ├─ Stopping web server...");
            PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] ├─ Uptime: {}s", uptime / 1000);

            WebServer.stop();

            PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] ├─ ✅ Web server stopped successfully");
            PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] └─ Total uptime: {}s", uptime / 1000);

            source.sendFeedback(() -> Text.literal("")
                .append(Text.literal("✅ Dashboard stopped successfully!\n").formatted(Formatting.GREEN))
                .append(Text.literal("⏱️  Uptime: ").formatted(Formatting.GRAY))
                .append(Text.literal((uptime / 1000) + "s").formatted(Formatting.YELLOW))
                , false);

            return 1;
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DASHBOARD_COMMAND] ❌ Failed to stop dashboard", e);
            PrimalCraft.LOGGER.error("[DASHBOARD_COMMAND] └─ Error: {}", e.getMessage());

            source.sendFeedback(() -> Text.literal("❌ Failed to stop dashboard: " + e.getMessage())
                .formatted(Formatting.RED), false);
            return 0;
        }
    }

    private static int showStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        commandExecutions++;

        PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] #{} Status request from {}", commandExecutions, source.getName());

        boolean running = WebServer.isRunning();
        String status = running ? "🟢 RUNNING" : "🔴 STOPPED";
        Formatting color = running ? Formatting.GREEN : Formatting.RED;

        PrimalCraft.LOGGER.debug("[DASHBOARD_COMMAND] ├─ Status: {}", running ? "RUNNING" : "STOPPED");
        if (running) {
            long uptime = System.currentTimeMillis() - lastStartTime;
            PrimalCraft.LOGGER.debug("[DASHBOARD_COMMAND] └─ Uptime: {}s", uptime / 1000);
        }

        source.sendFeedback(() -> Text.literal("")
            .append(Text.literal("Dashboard Status: ").formatted(Formatting.GOLD))
            .append(Text.literal(status).formatted(color))
            .append(Text.literal("\n"))
            .append(running ? Text.literal("📍 URL: http://localhost:8888").formatted(Formatting.AQUA)
                           : Text.literal("Use /dashboard start to enable").formatted(Formatting.GRAY))
            , false);

        return 1;
    }

    private static int showInfo(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        commandExecutions++;

        PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] #{} Info request from {}", commandExecutions, source.getName());

        boolean running = WebServer.isRunning();
        long uptime = running ? (System.currentTimeMillis() - lastStartTime) : 0;
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;

        PrimalCraft.LOGGER.debug("[DASHBOARD_COMMAND] ├─ Dashboard running: {}", running);
        PrimalCraft.LOGGER.debug("[DASHBOARD_COMMAND] ├─ Uptime: {}s", uptime / 1000);
        PrimalCraft.LOGGER.debug("[DASHBOARD_COMMAND] ├─ Memory: {}/{}MB", usedMemory, maxMemory);
        PrimalCraft.LOGGER.debug("[DASHBOARD_COMMAND] └─ Total commands: {}", commandExecutions);

        source.sendFeedback(() -> Text.literal("")
            .append(Text.literal("╔═══════════════════════════════════╗\n").formatted(Formatting.GOLD))
            .append(Text.literal("║  📊 Dashboard Information         ║\n").formatted(Formatting.YELLOW))
            .append(Text.literal("╚═══════════════════════════════════╝\n").formatted(Formatting.GOLD))
            .append(Text.literal("Status: ").formatted(Formatting.GRAY))
            .append(Text.literal(running ? "🟢 RUNNING" : "🔴 STOPPED").formatted(running ? Formatting.GREEN : Formatting.RED))
            .append(Text.literal("\nUptime: ").formatted(Formatting.GRAY))
            .append(Text.literal((uptime / 1000) + "s").formatted(Formatting.YELLOW))
            .append(Text.literal("\nMemory: ").formatted(Formatting.GRAY))
            .append(Text.literal(usedMemory + "/" + maxMemory + "MB").formatted(Formatting.YELLOW))
            .append(Text.literal("\nCommands: ").formatted(Formatting.GRAY))
            .append(Text.literal(commandExecutions + "").formatted(Formatting.YELLOW))
            , false);

        return 1;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        commandExecutions++;

        PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] #{} Reload request from {}", commandExecutions, source.getName());

        try {
            PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] ├─ Reloading configuration...");

            // Reload configuration
            net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig.load();

            PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] ├─ ✅ Configuration reloaded successfully");
            PrimalCraft.LOGGER.info("[DASHBOARD_COMMAND] └─ All settings updated");

            source.sendFeedback(() -> Text.literal("")
                .append(Text.literal("✅ Configuration reloaded successfully!\n").formatted(Formatting.GREEN))
                .append(Text.literal("📝 All settings have been updated").formatted(Formatting.GRAY))
                , false);

            return 1;
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DASHBOARD_COMMAND] ❌ Failed to reload configuration", e);
            PrimalCraft.LOGGER.error("[DASHBOARD_COMMAND] └─ Error: {}", e.getMessage());

            source.sendFeedback(() -> Text.literal("❌ Failed to reload configuration: " + e.getMessage())
                .formatted(Formatting.RED), false);

            return 0;
        }
    }
}
