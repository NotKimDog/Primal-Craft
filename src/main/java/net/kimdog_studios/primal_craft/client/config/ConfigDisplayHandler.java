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

        PrimalCraftConfig.MasterConfig config = PrimalCraftConfig.getConfig();

        client.player.sendMessage(
            Text.literal("⚙️ Primal Craft Configuration Status:").styled(s -> s.withColor(Formatting.GOLD).withBold(true)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Stamina: ").append(
                Text.literal(config.gameplay.stamina.enabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.gameplay.stamina.enabled ? Formatting.GREEN : Formatting.RED))
            ).append(Text.literal("  Recovery: " + String.format("%.2fx", config.gameplay.stamina.recoveryRate)))
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Thirst: ").append(
                Text.literal(config.gameplay.thirst.enabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.gameplay.thirst.enabled ? Formatting.GREEN : Formatting.RED))
            ).append(Text.literal("  Depletion: " + String.format("%.2fx", config.gameplay.thirst.depletionRate)))
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Temperature: ").append(
                Text.literal(config.gameplay.temperature.enabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.gameplay.temperature.enabled ? Formatting.GREEN : Formatting.RED))
            ).append(Text.literal("  Damage: " + String.format("%.2fx", config.gameplay.temperature.coldDamage)))
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Hazards: ").append(
                Text.literal(config.gameplay.hazards.enabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.gameplay.hazards.enabled ? Formatting.GREEN : Formatting.RED))
            ).append(Text.literal("  Intensity: " + String.format("%.2fx", config.gameplay.hazards.weatherIntensity)))
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        client.player.sendMessage(
            Text.literal("  • Hunger: ").append(
                Text.literal(config.gameplay.hunger.enabled ? "✓ ENABLED" : "✗ DISABLED")
                    .styled(s -> s.withColor(config.gameplay.hunger.enabled ? Formatting.GREEN : Formatting.RED))
            ).append(Text.literal("  Rate: " + String.format("%.2fx", config.gameplay.hunger.depletionMultiplier)))
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        client.player.sendMessage(
            Text.literal("🎮 Difficulty: " + config.difficulty.master.currentPreset)
                .styled(s -> s.withColor(Formatting.AQUA)),
            false
        );

        client.player.sendMessage(
            Text.literal("🎨 HUD: " + (config.hud.visibility.showStamina ? "Visible" : "Hidden"))
                .styled(s -> s.withColor(Formatting.LIGHT_PURPLE)),
            false
        );

        client.player.sendMessage(
            Text.literal("📁 Config files: config/primal-craft/")
                .styled(s -> s.withColor(Formatting.GRAY)),
            false
        );

        // Log to console as well
        PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        PrimalCraft.LOGGER.info("║  CONFIGURATION STATUS                                      ║");
        PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");
        PrimalCraft.LOGGER.info("Gameplay Systems:");
        PrimalCraft.LOGGER.info("  Stamina: {} (Recovery: {:.2f}x)",
            config.gameplay.stamina.enabled ? "✓" : "✗",
            config.gameplay.stamina.recoveryRate
        );
        PrimalCraft.LOGGER.info("  Thirst: {} (Depletion: {:.2f}x)",
            config.gameplay.thirst.enabled ? "✓" : "✗",
            config.gameplay.thirst.depletionRate
        );
        PrimalCraft.LOGGER.info("  Temperature: {} (Change Rate: {:.2f}x)",
            config.gameplay.temperature.enabled ? "✓" : "✗",
            config.gameplay.temperature.coldDamage
        );
        PrimalCraft.LOGGER.info("Difficulty: {}", config.difficulty.master.currentPreset);
        PrimalCraft.LOGGER.info("Systems - Zoom: {}, Veinminer: {}",
            config.systems.zoom.enabled ? "✓" : "✗",
            config.systems.veinminer.enabled ? "✓" : "✗"
        );
        PrimalCraft.LOGGER.info("═══════════════════════════════════════════════════════════════");
    }
}
