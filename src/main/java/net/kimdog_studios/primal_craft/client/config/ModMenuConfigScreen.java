package net.kimdog_studios.primal_craft.client.config;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.text.Text;

/**
 * 🎮 Primal Craft Configuration System
 * Provides configuration management for all mod settings
 *
 * Configuration is stored in JSON format at: config/primal-craft/config.json
 * Players can edit this file directly or install ModMenu for a GUI
 */
public class ModMenuConfigScreen {

    /**
     * Initialize the configuration system
     * Called from TutorialModClient during startup
     */
    public static void initialize() {
        PrimalCraft.LOGGER.info("🎮 Initializing Primal Craft Configuration System...");
        PrimalCraftConfig.init();
        PrimalCraft.LOGGER.info("✓ Configuration system initialized successfully");
    }

    /**
     * Get a formatted summary of current configuration
     */
    public static String getConfigSummary() {
        PrimalCraftConfig.MasterConfig config = PrimalCraftConfig.getConfig();
        return String.format(
            "═══════════════════════════════════════════════════════════\n" +
            "🎮 PRIMAL CRAFT CONFIGURATION SUMMARY\n" +
            "═══════════════════════════════════════════════════════════\n" +
            "\n" +
            "⚙️  GAMEPLAY SYSTEMS:\n" +
            "  • Stamina System: %s\n" +
            "  • Thirst System: %s\n" +
            "  • Temperature System: %s\n" +
            "  • Environmental Hazards: %s\n" +
            "\n" +
            "🎨 HUD & DISPLAY:\n" +
            "  • Stamina Bar: %s\n" +
            "  • Thirst Bar: %s\n" +
            "  • HUD Scale: %.2fx\n" +
            "  • HUD Opacity: %.0f%%\n" +
            "\n" +
            "⚔️  DIFFICULTY MULTIPLIERS:\n" +
            "  • Stamina: %.2fx\n" +
            "  • Thirst: %.2fx\n" +
            "  • Temperature: %.2fx\n" +
            "  • Hazards: %.2fx\n" +
            "\n" +
            "🔧 ADVANCED:\n" +
            "  • Debug Mode: %s\n" +
            "\n" +
            "📝 Config Files: config/primal-craft/\n" +
            "═══════════════════════════════════════════════════════════\n",

            config.gameplay.stamina.enabled ? "✓ ENABLED" : "✗ DISABLED",
            config.gameplay.thirst.enabled ? "✓ ENABLED" : "✗ DISABLED",
            config.gameplay.temperature.enabled ? "✓ ENABLED" : "✗ DISABLED",
            config.gameplay.hazards.enabled ? "✓ ENABLED" : "✗ DISABLED",

            config.hud.visibility.showStamina ? "✓ SHOWN" : "✗ HIDDEN",
            config.hud.visibility.showThirst ? "✓ SHOWN" : "✗ HIDDEN",
            config.hud.styling.scale,
            config.hud.styling.opacity * 100,

            config.difficulty.core.stamina,
            config.difficulty.core.thirst,
            config.difficulty.core.temperature,
            config.difficulty.core.hazards,

            config.advanced.developer.debugMode ? "✓ ENABLED" : "✗ DISABLED"
        );
    }
}
