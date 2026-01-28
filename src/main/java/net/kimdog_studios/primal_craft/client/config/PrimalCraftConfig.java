package net.kimdog_studios.primal_craft.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kimdog_studios.primal_craft.PrimalCraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 🎮 Primal Craft Configuration Manager
 * Handles all mod configuration settings with persistent JSON storage
 */
public class PrimalCraftConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get("config/primal-craft");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("config.json").toFile();

    // ═══════════════════════════════════════════════════════════════════════════════
    // GAMEPLAY SETTINGS
    // ═══════════════════════════════════════════════════════════════════════════════

    public static class GameplaySettings {
        public boolean staminaSystemEnabled = true;
        public boolean thirstSystemEnabled = true;
        public boolean temperatureSystemEnabled = true;
        public boolean environmentalHazardsEnabled = true;
        public boolean hungerOverhaulEnabled = true;
        public boolean exhaustionEnabled = true;

        public float staminaDepletionRate = 1.0f;
        public float staminaRecoveryRate = 1.0f;
        public float thirstDepletionRate = 1.0f;
        public float thirstRecoveryRate = 1.0f;

        @Override
        public String toString() {
            return String.format(
                "GameplaySettings{stamina=%s, thirst=%s, temperature=%s, hazards=%s, hunger=%s, exhaustion=%s}",
                staminaSystemEnabled, thirstSystemEnabled, temperatureSystemEnabled,
                environmentalHazardsEnabled, hungerOverhaulEnabled, exhaustionEnabled
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // HUD SETTINGS
    // ═══════════════════════════════════════════════════════════════════════════════

    public static class HUDSettings {
        public boolean showStaminaBar = true;
        public boolean showThirstBar = true;
        public boolean showTemperatureIndicator = true;
        public boolean showWeatherNotifications = true;
        public boolean showBiomeNotifications = true;

        public float hudScale = 1.0f;
        public float hudOpacity = 1.0f;

        @Override
        public String toString() {
            return String.format(
                "HUDSettings{stamina=%s, thirst=%s, temp=%s, opacity=%.2f}",
                showStaminaBar, showThirstBar, showTemperatureIndicator, hudOpacity
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DIFFICULTY SETTINGS
    // ═══════════════════════════════════════════════════════════════════════════════

    public static class DifficultySettings {
        public float staminalossDifficulty = 1.0f;
        public float thirstDifficulty = 1.0f;
        public float temperatureDifficulty = 1.0f;
        public float hazardDifficulty = 1.0f;

        @Override
        public String toString() {
            return String.format(
                "DifficultySettings{stamina=%.2f, thirst=%.2f, temp=%.2f, hazard=%.2f}",
                staminalossDifficulty, thirstDifficulty, temperatureDifficulty, hazardDifficulty
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // ROOT CONFIG
    // ═══════════════════════════════════════════════════════════════════════════════

    public static class ConfigData {
        public GameplaySettings gameplay = new GameplaySettings();
        public HUDSettings hud = new HUDSettings();
        public DifficultySettings difficulty = new DifficultySettings();

        // Advanced Features
        public boolean webDashboardEnabled = false;
        public boolean autoSaveConfig = true;
        public boolean showDetailedTooltips = true;
        public boolean debugMode = false;

        @Override
        public String toString() {
            return String.format(
                "ConfigData{%s, %s, %s, webDashboard=%s, autoSave=%s, tooltips=%s, debug=%s}",
                gameplay, hud, difficulty, webDashboardEnabled, autoSaveConfig, showDetailedTooltips, debugMode
            );
        }
    }

    private static ConfigData config = new ConfigData();

    // ═══════════════════════════════════════════════════════════════════════════════
    // INITIALIZATION & FILE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Initialize configuration system
     * Creates config directory and loads or creates config file
     */
    public static void init() {
        try {
            PrimalCraft.LOGGER.info("🎮 Initializing Primal Craft Configuration...");

            // Create config directory if it doesn't exist
            Files.createDirectories(CONFIG_DIR);
            PrimalCraft.LOGGER.info("✓ Config directory ready: {}", CONFIG_DIR.toAbsolutePath());

            // Load config if exists, otherwise create default
            if (CONFIG_FILE.exists()) {
                load();
                PrimalCraft.LOGGER.info("✓ Configuration loaded successfully");
            } else {
                save();
                PrimalCraft.LOGGER.info("✓ New default configuration created");
            }

            PrimalCraft.LOGGER.info("✓ Configuration initialized: {}", config);

        } catch (IOException e) {
            PrimalCraft.LOGGER.error("❌ Failed to initialize configuration", e);
        }
    }

    /**
     * Load configuration from JSON file
     */
    public static void load() {
        try {
            if (CONFIG_FILE.exists()) {
                try (FileReader reader = new FileReader(CONFIG_FILE)) {
                    config = GSON.fromJson(reader, ConfigData.class);
                    if (config == null) {
                        config = new ConfigData();
                    }
                    PrimalCraft.LOGGER.info("✓ Config loaded from: {}", CONFIG_FILE.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            PrimalCraft.LOGGER.error("❌ Failed to load configuration", e);
            config = new ConfigData();
        }
    }

    /**
     * Save configuration to JSON file
     */
    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
                PrimalCraft.LOGGER.info("✓ Configuration saved successfully");
            }
        } catch (IOException e) {
            PrimalCraft.LOGGER.error("❌ Failed to save configuration", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════

    public static ConfigData getConfig() {
        return config;
    }

    public static GameplaySettings getGameplay() {
        return config.gameplay;
    }

    public static HUDSettings getHUD() {
        return config.hud;
    }

    public static DifficultySettings getDifficulty() {
        return config.difficulty;
    }

    public static boolean isDebugMode() {
        return config.debugMode;
    }

    public static void setDebugMode(boolean enabled) {
        config.debugMode = enabled;
        PrimalCraft.LOGGER.info("🔍 Debug mode: {}", enabled ? "ENABLED" : "DISABLED");
        save();
    }
}
