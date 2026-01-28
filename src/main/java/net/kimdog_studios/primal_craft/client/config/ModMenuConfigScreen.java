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
        PrimalCraftConfig.ConfigData config = PrimalCraftConfig.getConfig();
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
            "📝 Config File: config/primal-craft/config.json\n" +
            "═══════════════════════════════════════════════════════════\n",

            config.gameplay.staminaSystemEnabled ? "✓ ENABLED" : "✗ DISABLED",
            config.gameplay.thirstSystemEnabled ? "✓ ENABLED" : "✗ DISABLED",
            config.gameplay.temperatureSystemEnabled ? "✓ ENABLED" : "✗ DISABLED",
            config.gameplay.environmentalHazardsEnabled ? "✓ ENABLED" : "✗ DISABLED",

            config.hud.showStaminaBar ? "✓ SHOWN" : "✗ HIDDEN",
            config.hud.showThirstBar ? "✓ SHOWN" : "✗ HIDDEN",
            config.hud.hudScale,
            config.hud.hudOpacity * 100,

            config.difficulty.staminalossDifficulty,
            config.difficulty.thirstDifficulty,
            config.difficulty.temperatureDifficulty,
            config.difficulty.hazardDifficulty,

            config.debugMode ? "✓ ENABLED" : "✗ DISABLED"
        );
    }
}
