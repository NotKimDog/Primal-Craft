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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 🎮 Primal Craft Configuration Manager
 * Handles all mod configuration settings with persistent JSON storage
 *
 * FEATURES:
 * ✓ Real-time Config Sync: All systems notified immediately when config changes
 * ✓ Listener-based Updates: Systems register listeners for config changes
 * ✓ Server Support: Ready for multiplayer config sync via network packets
 *
 * USAGE:
 * 1. Register listeners via registerConfigChangeListener()
 * 2. Modify config settings directly
 * 3. Call save() to persist and notify all listeners
 *
 * SERVER SYNC:
 * When on a server, config changes sync via network packets to all players.
 * The CONFIG_CHANGE_LISTENERS system ensures all client-side systems stay synchronized.
 */
public class PrimalCraftConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get("config/primal-craft");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("config.json").toFile();

    // ═══════════════════════════════════════════════════════════════════════════════
    // GAMEPLAY SETTINGS
    // ═══════════════════════════════════════════════════════════════════════════════

    public static class GameplaySettings {
        // System Toggles
        public boolean staminaSystemEnabled = true;
        public boolean thirstSystemEnabled = true;
        public boolean temperatureSystemEnabled = true;
        public boolean environmentalHazardsEnabled = true;
        public boolean hungerOverhaulEnabled = true;
        public boolean exhaustionEnabled = true;

        // Stamina Settings
        public float staminaDepletionRate = 1.0f;
        public float staminaRecoveryRate = 1.0f;
        public float sprintStaminaCost = 1.0f;
        public float jumpStaminaCost = 1.0f;
        public float swimStaminaCost = 1.0f;
        public float attackStaminaCost = 1.0f;
        public int maxStamina = 100;
        public boolean staminaRegenWhileSprinting = false;
        public float staminaRegenDelay = 2.0f; // seconds

        // Thirst Settings
        public float thirstDepletionRate = 1.0f;
        public float thirstRecoveryRate = 1.0f;
        public float sprintThirstMultiplier = 1.5f;
        public float hotBiomeThirstMultiplier = 2.0f;
        public int maxThirst = 20;
        public boolean thirstAffectsHealth = true;
        public float dehydrationDamage = 1.0f;

        // Temperature Settings
        public float temperatureChangeRate = 1.0f;
        public boolean temperatureAffectsHealth = true;
        public float coldDamage = 1.0f;
        public float heatDamage = 1.0f;
        public boolean clothingAffectsTemperature = true;

        // Hunger Settings
        public float hungerDepletionMultiplier = 1.0f;
        public float hungerHealingCost = 1.0f;
        public boolean naturalRegeneration = true;
        public float saturationMultiplier = 1.0f;

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
        // Bar Visibility
        public boolean showStaminaBar = true;
        public boolean showThirstBar = true;
        public boolean showTemperatureIndicator = true;
        public boolean showWeatherNotifications = true;
        public boolean showBiomeNotifications = true;
        public boolean showEffectIcons = true;
        public boolean showDebugInfo = false;

        // Bar Appearance
        public float hudScale = 1.0f;
        public float hudOpacity = 1.0f;
        public int hudXOffset = 0;
        public int hudYOffset = 0;

        // Bar Colors (ARGB hex)
        public int staminaBarColor = 0xFFFFFF00; // Yellow
        public int thirstBarColor = 0xFF00BFFF;  // Deep Sky Blue
        public int lowStaminaColor = 0xFFFF4500; // Orange Red
        public int lowThirstColor = 0xFFDC143C;  // Crimson

        // Animations
        public boolean enableBarAnimations = true;
        public boolean enableWarningFlash = true;
        public float animationSpeed = 1.0f;

        // Text Display
        public boolean showNumericValues = true;
        public boolean showPercentages = false;
        public boolean showLabels = true;

        @Override
        public String toString() {
            return String.format(
                "HUDSettings{stamina=%s, thirst=%s, temp=%s, opacity=%.2f}",
                showStaminaBar, showThirstBar, showTemperatureIndicator, hudOpacity
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // SYSTEM SETTINGS (Zoom, Veinminer, etc.)
    // ═══════════════════════════════════════════════════════════════════════════════

    public static class SystemSettings {
        // Zoom Settings
        public boolean zoomEnabled = true;
        public float zoomSensitivity = 1.0f;
        public float zoomSpeed = 1.0f;
        public float maxZoom = 10.0f;

        // Veinminer Settings
        public boolean veinminerEnabled = true;
        public int veinminerMaxBlocks = 64;
        public float veinminerSpeed = 1.0f;
        public boolean veinminerOresOnly = true;

        @Override
        public String toString() {
            return String.format(
                "SystemSettings{zoom=%s, veinminer=%s}",
                zoomEnabled, veinminerEnabled
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DIFFICULTY SETTINGS
    // ═══════════════════════════════════════════════════════════════════════════════

    public static class DifficultySettings {
        // Core Difficulty Multipliers
        public float staminalossDifficulty = 1.0f;
        public float thirstDifficulty = 1.0f;
        public float temperatureDifficulty = 1.0f;
        public float hazardDifficulty = 1.0f;

        // Damage Multipliers
        public float environmentalDamageMultiplier = 1.0f;
        public float dehydrationDamageMultiplier = 1.0f;
        public float freezingDamageMultiplier = 1.0f;
        public float heatstrokeDamageMultiplier = 1.0f;

        // Survival Challenges
        public boolean enableHardcoreTemperature = false;
        public boolean enableRealisticThirst = false;
        public boolean enableExtremeWeather = false;
        public boolean enableSeasonalDifficulty = false;

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
        public SystemSettings systems = new SystemSettings();
        public DifficultySettings difficulty = new DifficultySettings();

        // Advanced Features
        public boolean webDashboardEnabled = false;
        public boolean autoSaveConfig = true;
        public boolean showDetailedTooltips = true;
        public boolean debugMode = false;

        // Performance
        public boolean enableParticleEffects = true;
        public boolean enableSoundEffects = true;
        public int updateFrequency = 20; // ticks

        // Compatibility
        public boolean compatibilityMode = false;
        public boolean disableVanillaConflicts = true;

        // Notifications
        public boolean enableChatNotifications = true;
        public boolean enableScreenNotifications = true;
        public boolean enableSoundNotifications = true;

        @Override
        public String toString() {
            return String.format(
                "ConfigData{%s, %s, %s, %s, webDashboard=%s, autoSave=%s, tooltips=%s, debug=%s}",
                gameplay, hud, systems, difficulty, webDashboardEnabled, autoSaveConfig, showDetailedTooltips, debugMode
            );
        }
    }

    private static ConfigData config = new ConfigData();

    // ═══════════════════════════════════════════════════════════════════════════════
    // CONFIG CHANGE LISTENERS - Real-time system updates
    // ═══════════════════════════════════════════════════════════════════════════════

    private static final List<Consumer<ConfigData>> CONFIG_CHANGE_LISTENERS = new ArrayList<>();

    /**
     * Register a listener to be notified when config changes
     * Listeners are called immediately with the updated config
     */
    public static void registerConfigChangeListener(Consumer<ConfigData> listener) {
        CONFIG_CHANGE_LISTENERS.add(listener);
        PrimalCraft.LOGGER.info("✓ Config change listener registered (total: {})", CONFIG_CHANGE_LISTENERS.size());
    }

    /**
     * Notify all listeners that config has changed
     * Call this after any config modification
     */
    private static void notifyConfigChange() {
        PrimalCraft.LOGGER.info("[CONFIG_SYNC] Notifying {} listeners of config change", CONFIG_CHANGE_LISTENERS.size());
        for (Consumer<ConfigData> listener : CONFIG_CHANGE_LISTENERS) {
            try {
                listener.accept(config);
            } catch (Exception e) {
                PrimalCraft.LOGGER.error("[CONFIG_SYNC] Error notifying listener", e);
            }
        }
    }

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
                // Notify all listeners that config has changed
                notifyConfigChange();
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

    public static SystemSettings getSystems() {
        return config.systems;
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

    /**
     * Convert ConfigData to JSON string
     */
    public static String toJson(ConfigData data) {
        return GSON.toJson(data);
    }

    /**
     * Load ConfigData from JSON string
     */
    public static void fromJson(String json, ConfigData data) {
        try {
            ConfigData loaded = GSON.fromJson(json, ConfigData.class);
            if (loaded != null) {
                data.gameplay = loaded.gameplay;
                data.hud = loaded.hud;
                data.systems = loaded.systems;
                data.difficulty = loaded.difficulty;
                data.webDashboardEnabled = loaded.webDashboardEnabled;
                data.autoSaveConfig = loaded.autoSaveConfig;
                data.showDetailedTooltips = loaded.showDetailedTooltips;
                data.debugMode = loaded.debugMode;
                PrimalCraft.LOGGER.info("✓ Config loaded from JSON");
                notifyConfigChange();
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("❌ Failed to parse JSON config", e);
        }
    }
}
