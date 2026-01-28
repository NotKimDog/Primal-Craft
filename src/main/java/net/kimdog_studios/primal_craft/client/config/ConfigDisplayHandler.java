package net.kimdog_studios.primal_craft.client.config;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Displays configuration settings to the player when they join a world
 */
public class ConfigDisplayHandler {
    private static boolean hasDisplayedConfig = false;
    private static int ticksUntilDisplay = 40; // 2 seconds at 20 ticks/sec

    /**
     * Called when client starts - register world join listener
     */
    public static void onClientStart() {
        hasDisplayedConfig = false;
        ticksUntilDisplay = 40;

        // Register tick listener to detect world join
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null && !hasDisplayedConfig) {
                ticksUntilDisplay--;
                if (ticksUntilDisplay <= 0) {
                    displayConfigToPlayer();
                    hasDisplayedConfig = true;
                }
            }
        });
    }

    /**
     * Display configuration settings to the player
     */
    private static void displayConfigToPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        var config = PrimalCraftConfig.getConfig();

        // Send header
        client.player.sendMessage(
            Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").styled(style ->
                style.withColor(Formatting.GOLD)
            ),
            false
        );

        client.player.sendMessage(
            Text.literal("⚙️  PRIMAL CRAFT CONFIG APPLIED").styled(style ->
                style.withColor(Formatting.YELLOW).withBold(true)
            ),
            false
        );

        client.player.sendMessage(
            Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").styled(style ->
                style.withColor(Formatting.GOLD)
            ),
            false
        );

        // === GAMEPLAY SYSTEMS ===
        client.player.sendMessage(
            Text.literal("🎮 Gameplay Systems:").styled(style -> style.withColor(Formatting.AQUA).withBold(true)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Stamina: ").append(
                Text.literal(config.gameplay.staminaSystemEnabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.gameplay.staminaSystemEnabled ? Formatting.GREEN : Formatting.RED))
            ).append(Text.literal("  Recovery: " + String.format("%.2fx", config.gameplay.staminaRecoveryRate)))
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Thirst: ").append(
                Text.literal(config.gameplay.thirstSystemEnabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.gameplay.thirstSystemEnabled ? Formatting.GREEN : Formatting.RED))
            ).append(Text.literal("  Depletion: " + String.format("%.2fx", config.gameplay.thirstDepletionRate)))
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Temperature: ").append(
                Text.literal(config.gameplay.temperatureSystemEnabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.gameplay.temperatureSystemEnabled ? Formatting.GREEN : Formatting.RED))
            ).append(Text.literal("  Change Rate: " + String.format("%.2fx", config.gameplay.temperatureChangeRate)))
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Environmental Hazards: ").append(
                Text.literal(config.gameplay.environmentalHazardsEnabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.gameplay.environmentalHazardsEnabled ? Formatting.GREEN : Formatting.RED))
            ),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Hunger Overhaul: ").append(
                Text.literal(config.gameplay.hungerOverhaulEnabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.gameplay.hungerOverhaulEnabled ? Formatting.GREEN : Formatting.RED))
            ),
            false
        );

        // === DIFFICULTY ===
        client.player.sendMessage(Text.literal(""), false);
        client.player.sendMessage(
            Text.literal("⚔️  Difficulty Multipliers:").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withBold(true)),
            false
        );

        String difficultyLabel = getDifficultyLabel(
            config.difficulty.staminalossDifficulty,
            config.difficulty.thirstDifficulty,
            config.difficulty.temperatureDifficulty,
            config.difficulty.hazardDifficulty
        );
        client.player.sendMessage(
            Text.literal("  • ").append(
                Text.literal(difficultyLabel).styled(s -> s.withColor(getMultiplierColor(config.difficulty.staminalossDifficulty)))
            ),
            false
        );

        // === SYSTEMS (Zoom & Veinminer) ===
        client.player.sendMessage(Text.literal(""), false);
        client.player.sendMessage(
            Text.literal("🔬 Systems:").styled(style -> style.withColor(Formatting.GREEN).withBold(true)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Zoom: ").append(
                Text.literal(config.systems.zoomEnabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.systems.zoomEnabled ? Formatting.GREEN : Formatting.RED))
            ).append(Text.literal("  Sensitivity: " + String.format("%.2fx", config.systems.zoomSensitivity)))
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Veinminer: ").append(
                Text.literal(config.systems.veinminerEnabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.systems.veinminerEnabled ? Formatting.GREEN : Formatting.RED))
            ).append(Text.literal("  Max Blocks: " + config.systems.veinminerMaxBlocks))
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        // === HUD ===
        client.player.sendMessage(Text.literal(""), false);
        client.player.sendMessage(
            Text.literal("🎨 HUD Display:").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withBold(true)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Scale: ").append(
                Text.literal(String.format("%.2fx", config.hud.hudScale)).styled(s -> s.withColor(Formatting.YELLOW))
            ).append(Text.literal("  Opacity: " + String.format("%.0f%%", config.hud.hudOpacity * 100)))
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Bars: ").append(
                Text.literal(config.hud.showStaminaBar ? "Stamina " : "").styled(s -> s.withColor(Formatting.GOLD))
            ).append(
                Text.literal(config.hud.showThirstBar ? "Thirst " : "").styled(s -> s.withColor(Formatting.AQUA))
            ).append(
                Text.literal(config.hud.showTemperatureIndicator ? "Temperature " : "").styled(s -> s.withColor(Formatting.RED))
            ),
            false
        );

        // Footer
        client.player.sendMessage(
            Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").styled(style ->
                style.withColor(Formatting.GOLD)
            ),
            false
        );

        client.player.sendMessage(
            Text.literal("💡 Tip: Open mod menu for full configuration").styled(style ->
                style.withColor(Formatting.GRAY).withItalic(true)
            ),
            false
        );

        // Log to console as well
        PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        PrimalCraft.LOGGER.info("║  CONFIGURATION STATUS                                      ║");
        PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");
        PrimalCraft.LOGGER.info("Gameplay Systems:");
        PrimalCraft.LOGGER.info("  Stamina: {} (Recovery: {:.2f}x)",
            config.gameplay.staminaSystemEnabled ? "✓" : "✗",
            config.gameplay.staminaRecoveryRate
        );
        PrimalCraft.LOGGER.info("  Thirst: {} (Depletion: {:.2f}x)",
            config.gameplay.thirstSystemEnabled ? "✓" : "✗",
            config.gameplay.thirstDepletionRate
        );
        PrimalCraft.LOGGER.info("  Temperature: {} (Change Rate: {:.2f}x)",
            config.gameplay.temperatureSystemEnabled ? "✓" : "✗",
            config.gameplay.temperatureChangeRate
        );
        PrimalCraft.LOGGER.info("Difficulty: {}", difficultyLabel);
        PrimalCraft.LOGGER.info("Systems - Zoom: {}, Veinminer: {}",
            config.systems.zoomEnabled ? "✓" : "✗",
            config.systems.veinminerEnabled ? "✓" : "✗"
        );
        PrimalCraft.LOGGER.info("═══════════════════════════════════════════════════════════════");
    }

    /**
     * Get difficulty label based on multipliers
     */
    private static String getDifficultyLabel(float stamina, float thirst, float temp, float hazard) {
        float avg = (stamina + thirst + temp + hazard) / 4f;

        if (Math.abs(avg - 0.5f) < 0.1f) return "😊 Easy (0.5x)";
        if (Math.abs(avg - 1.0f) < 0.1f) return "⚖️  Normal (1.0x)";
        if (Math.abs(avg - 1.5f) < 0.1f) return "😰 Hard (1.5x)";
        if (Math.abs(avg - 2.0f) < 0.1f) return "💀 Very Hard (2.0x)";
        if (Math.abs(avg - 2.5f) < 0.1f) return "☠️  Nightmare (2.5x)";
        return "⚙️  Custom (" + String.format("%.2f", avg) + "x)";
    }

    /**
     * Get color based on multiplier value
     */
    private static Formatting getMultiplierColor(float multiplier) {
        if (multiplier < 0.7f) return Formatting.GREEN;
        if (multiplier < 1.2f) return Formatting.YELLOW;
        if (multiplier < 1.8f) return Formatting.GOLD;
        if (multiplier < 2.2f) return Formatting.RED;
        return Formatting.DARK_RED;
    }
}
