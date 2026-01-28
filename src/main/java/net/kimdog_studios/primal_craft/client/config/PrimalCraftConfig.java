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
 * 🎮 Primal Craft Configuration Manager (v3.0 - Modular)
 * Each config section has its own JSON file in config/primal-craft/
 */
public class PrimalCraftConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get("config/primal-craft");

    private static final File GAMEPLAY_FILE = CONFIG_DIR.resolve("gameplay.json").toFile();
    private static final File HUD_FILE = CONFIG_DIR.resolve("hud.json").toFile();
    private static final File SYSTEMS_FILE = CONFIG_DIR.resolve("systems.json").toFile();
    private static final File DIFFICULTY_FILE = CONFIG_DIR.resolve("difficulty.json").toFile();
    private static final File ADVANCED_FILE = CONFIG_DIR.resolve("advanced.json").toFile();

    // GAMEPLAY SETTINGS (gameplay.json)
    public static class GameplaySettings {
        public StaminaSettings stamina = new StaminaSettings();
        public ThirstSettings thirst = new ThirstSettings();
        public TemperatureSettings temperature = new TemperatureSettings();
        public HazardSettings hazards = new HazardSettings();
        public HungerSettings hunger = new HungerSettings();

        // Compatibility properties for direct access
        public boolean staminaSystemEnabled;
        public float staminaDepletionRate;
        public float staminaRecoveryRate;
        public boolean thirstSystemEnabled;
        public float thirstDepletionRate;
        public float thirstRecoveryRate;
        public boolean temperatureSystemEnabled;
        public boolean environmentalHazardsEnabled;

        public static class StaminaSettings {
            public boolean enabled = true;
            public float depletionRate = 1.0f;
            public float recoveryRate = 1.0f;
            public float recoveryDelay = 2.0f;
            public int maxStamina = 100;
            public boolean regenWhileSprinting = false;
            public float sprintCost = 1.0f;
            public float jumpCost = 1.0f;
        }

        public static class ThirstSettings {
            public boolean enabled = true;
            public float depletionRate = 1.0f;
            public float recoveryRate = 1.0f;
            public int maxThirst = 20;
            public float hotBiomeMultiplier = 2.0f;
            public float desertMultiplier = 3.0f;
        }

        public static class TemperatureSettings {
            public boolean enabled = true;
            public float changeRate = 1.0f;
            public float coldDamage = 1.0f;
            public float heatDamage = 1.0f;
        }

        public static class HazardSettings {
            public boolean enabled = true;
            public float weatherIntensity = 1.0f;
            public boolean lightningDanger = true;
            public float hazardDamage = 1.0f;
        }

        public static class HungerSettings {
            public boolean enabled = true;
            public float depletionMultiplier = 1.0f;
            public float saturationMultiplier = 1.0f;
        }
    }

    // HUD SETTINGS (hud.json)
    public static class HUDSettings {
        public BarVisibility visibility = new BarVisibility();
        public BarStyling styling = new BarStyling();
        public ColorSettings colors = new ColorSettings();
        public AnimationSettings animations = new AnimationSettings();

        // Compatibility properties for direct access
        public boolean showStaminaBar;
        public boolean showThirstBar;
        public boolean showTemperatureIndicator;
        public boolean showWeatherNotifications;
        public boolean showBiomeNotifications;
        public float hudScale;
        public float hudOpacity;

        public static class BarVisibility {
            public boolean showStamina = true;
            public boolean showThirst = true;
            public boolean showTemperature = true;
            public boolean showWeatherNotifications = true;
            public boolean showBiomeNotifications = true;
            public boolean showDebugInfo = false;
        }

        public static class BarStyling {
            public float scale = 1.0f;
            public float opacity = 1.0f;
            public int xOffset = 0;
            public int yOffset = 0;
        }

        public static class ColorSettings {
            public int staminaColor = 0xFFFFD700;
            public int thirstColor = 0xFF00BFFF;
            public int temperatureColor = 0xFFFF6347;
        }

        public static class AnimationSettings {
            public boolean enabled = true;
            public float speed = 1.0f;
        }
    }

    // SYSTEM SETTINGS (systems.json)
    public static class SystemSettings {
        public ZoomSettings zoom = new ZoomSettings();
        public VeinminerSettings veinminer = new VeinminerSettings();

        public static class ZoomSettings {
            public boolean enabled = true;
            public float sensitivity = 1.0f;
            public float maxZoom = 10.0f;
        }

        public static class VeinminerSettings {
            public boolean enabled = true;
            public int maxBlocksPerVein = 64;
            public float speed = 1.0f;
        }
    }

    // DIFFICULTY SETTINGS (difficulty.json)
    public static class DifficultySettings {
        public MasterDifficulty master = new MasterDifficulty();
        public CoreMultipliers core = new CoreMultipliers();
        public DamageScaling damage = new DamageScaling();
        public DimensionMultipliers dimensions = new DimensionMultipliers();
        public MobAndResourceScaling mobResources = new MobAndResourceScaling();

        // Compatibility fields
        public float thirstDifficulty = 1.0f;
        public float temperatureDifficulty = 1.0f;
        public float hazardDifficulty = 1.0f;
        public boolean enableDimensionMultipliers = true;
        public boolean difficultyAffectsMobBehavior = true;
        public boolean difficultyAffectsResourceScarcity = false;

        // Metric weighting fields
        public boolean enableMetricsWeighting = true;
        public float playtimeWeight = 0.4f;
        public float damageWeight = 0.3f;
        public float deathWeight = 0.2f;
        public float resourceWeight = 0.1f;

        // System flags
        public boolean isDifficultySystemEnabled = true;
        public boolean dynamicDifficultyScaling = true;
        public float scalingThresholdPerLevel = 100.0f;

        // Scaling enables
        public boolean staminaScalingEnabled = true;
        public boolean thirstScalingEnabled = true;
        public boolean temperatureScalingEnabled = true;
        public boolean hazardScalingEnabled = true;
        public boolean damageScalingEnabled = true;
        public boolean mobScalingEnabled = true;

        // Additional stamina field
        public float staminalossDifficulty = 1.0f;

        public static class MasterDifficulty {
            public boolean enabled = true;
            public String currentPreset = "NORMAL";
            public boolean dynamicScaling = true;
        }

        public static class CoreMultipliers {
            public float stamina = 1.0f;
            public float thirst = 1.0f;
            public float temperature = 1.0f;
            public float hazards = 1.0f;
        }

        public static class DamageScaling {
            public float environmental = 1.0f;
            public float dehydration = 1.0f;
        }

        public static class DimensionMultipliers {
            public boolean enabled = true;
            public float nether = 1.5f;
            public float end = 2.5f;
        }

        public static class MobAndResourceScaling {
            public boolean mobBehavior = true;
            public float mobDamage = 1.0f;
            public float mobHealth = 1.0f;
        }
    }

    // ADVANCED SETTINGS (advanced.json)
    public static class AdvancedSettings {
        public IntegrationSettings integrations = new IntegrationSettings();
        public PerformanceSettings performance = new PerformanceSettings();
        public DeveloperSettings developer = new DeveloperSettings();
        public FeatureToggles features = new FeatureToggles();
        public WindowSettings window = new WindowSettings();

        public static class IntegrationSettings {
            public boolean webDashboardEnabled = false;
            public int webDashboardPort = 8080;
        }

        public static class PerformanceSettings {
            public boolean enableParticles = true;
            public boolean enableSounds = true;
            public int updateFrequency = 20;
        }

        public static class DeveloperSettings {
            public boolean debugMode = false;
            public String logLevel = "INFO";
        }

        public static class FeatureToggles {
            // Phase 1: Critical Fixes
            public boolean hardcoreDifficulty = true;
            public boolean presetSystem = true;
            public boolean debugHudRemoval = true;
            public boolean sleepSystemToggle = true;

            // Phase 2: Gameplay Features
            public boolean difficultyColors = true;
            public boolean mobAggression = true;
            public boolean itemDropParticles = true;
            public boolean dayTransitionAnimation = true;
            public boolean thirdPersonNames = true;
            public boolean rightClickHarvester = true;

            // Phase 3: Content Creator & Window
            public boolean fpsAndPingGUI = false;
            public boolean fullscreenAutoLaunch = false;
            public boolean customWindowTitle = true;
            public boolean customWindowIcon = false;

            // Phase 4: Quality of Life
            public boolean dynamicFpsOptimizer = false;
            public boolean doubleDoors = true;
            public boolean infiniteTrading = false;
            public boolean dropConfirmation = false;
            public boolean easyElytraTakeoff = true;
            public boolean dynamicLights = false;

            // Phase 5: Major Overhauls
            public boolean dragonRedesign = true;
            public boolean netherOverhaul = true;

            // Meta Feature
            public boolean hytaleFeelEnabled = true;
        }

        public static class WindowSettings {
            public String customTitle = "Primal Craft";
        }
    }

    // MASTER CONFIG
    public static class MasterConfig {
        public GameplaySettings gameplay = new GameplaySettings();
        public HUDSettings hud = new HUDSettings();
        public SystemSettings systems = new SystemSettings();
        public DifficultySettings difficulty = new DifficultySettings();
        public AdvancedSettings advanced = new AdvancedSettings();
        public String version = "3.0";
    }

    private static MasterConfig masterConfig = new MasterConfig();
    private static final List<Consumer<MasterConfig>> CONFIG_CHANGE_LISTENERS = new ArrayList<>();

    public static void registerConfigChangeListener(Consumer<MasterConfig> listener) {
        CONFIG_CHANGE_LISTENERS.add(listener);
        PrimalCraft.LOGGER.info("✓ Config listener registered");
    }

    private static void notifyConfigChange() {
        for (Consumer<MasterConfig> listener : CONFIG_CHANGE_LISTENERS) {
            try {
                listener.accept(masterConfig);
            } catch (Exception e) {
                PrimalCraft.LOGGER.error("[CONFIG] Error notifying listener", e);
            }
        }
    }

    public static void init() {
        try {
            PrimalCraft.LOGGER.info("🎮 Initializing Modular Config (v3.0)...");
            Files.createDirectories(CONFIG_DIR);
            loadGameplay();
            loadHUD();
            loadSystems();
            loadDifficulty();
            loadAdvanced();
            PrimalCraft.LOGGER.info("✓ All configs loaded");
        } catch (IOException e) {
            PrimalCraft.LOGGER.error("❌ Config init failed", e);
        }
    }

    private static void loadGameplay() {
        if (GAMEPLAY_FILE.exists()) {
            try (FileReader reader = new FileReader(GAMEPLAY_FILE)) {
                GameplaySettings loaded = GSON.fromJson(reader, GameplaySettings.class);
                if (loaded != null) masterConfig.gameplay = loaded;
            } catch (IOException e) {
                PrimalCraft.LOGGER.error("Failed to load gameplay config", e);
                saveGameplay();
            }
        } else {
            saveGameplay();
        }
    }

    private static void loadHUD() {
        if (HUD_FILE.exists()) {
            try (FileReader reader = new FileReader(HUD_FILE)) {
                HUDSettings loaded = GSON.fromJson(reader, HUDSettings.class);
                if (loaded != null) masterConfig.hud = loaded;
            } catch (IOException e) {
                saveHUD();
            }
        } else {
            saveHUD();
        }
    }

    private static void loadSystems() {
        if (SYSTEMS_FILE.exists()) {
            try (FileReader reader = new FileReader(SYSTEMS_FILE)) {
                SystemSettings loaded = GSON.fromJson(reader, SystemSettings.class);
                if (loaded != null) masterConfig.systems = loaded;
            } catch (IOException e) {
                saveSystems();
            }
        } else {
            saveSystems();
        }
    }

    private static void loadDifficulty() {
        if (DIFFICULTY_FILE.exists()) {
            try (FileReader reader = new FileReader(DIFFICULTY_FILE)) {
                DifficultySettings loaded = GSON.fromJson(reader, DifficultySettings.class);
                if (loaded != null) masterConfig.difficulty = loaded;
            } catch (IOException e) {
                saveDifficulty();
            }
        } else {
            saveDifficulty();
        }
    }

    private static void loadAdvanced() {
        if (ADVANCED_FILE.exists()) {
            try (FileReader reader = new FileReader(ADVANCED_FILE)) {
                AdvancedSettings loaded = GSON.fromJson(reader, AdvancedSettings.class);
                if (loaded != null) masterConfig.advanced = loaded;
            } catch (IOException e) {
                saveAdvanced();
            }
        } else {
            saveAdvanced();
        }
    }

    public static void save() {
        saveGameplay();
        saveHUD();
        saveSystems();
        saveDifficulty();
        saveAdvanced();
        PrimalCraft.LOGGER.info("💾 All configs saved");
        notifyConfigChange();
    }

    private static void saveGameplay() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (FileWriter writer = new FileWriter(GAMEPLAY_FILE)) {
                GSON.toJson(masterConfig.gameplay, writer);
            }
        } catch (IOException e) {
            PrimalCraft.LOGGER.error("Failed to save gameplay config", e);
        }
    }

    private static void saveHUD() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (FileWriter writer = new FileWriter(HUD_FILE)) {
                GSON.toJson(masterConfig.hud, writer);
            }
        } catch (IOException e) {
            PrimalCraft.LOGGER.error("Failed to save HUD config", e);
        }
    }

    private static void saveSystems() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (FileWriter writer = new FileWriter(SYSTEMS_FILE)) {
                GSON.toJson(masterConfig.systems, writer);
            }
        } catch (IOException e) {
            PrimalCraft.LOGGER.error("Failed to save systems config", e);
        }
    }

    private static void saveDifficulty() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (FileWriter writer = new FileWriter(DIFFICULTY_FILE)) {
                GSON.toJson(masterConfig.difficulty, writer);
            }
        } catch (IOException e) {
            PrimalCraft.LOGGER.error("Failed to save difficulty config", e);
        }
    }

    private static void saveAdvanced() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (FileWriter writer = new FileWriter(ADVANCED_FILE)) {
                GSON.toJson(masterConfig.advanced, writer);
            }
        } catch (IOException e) {
            PrimalCraft.LOGGER.error("Failed to save advanced config", e);
        }
    }

    public static MasterConfig getConfig() {
        return masterConfig;
    }

    public static GameplaySettings getGameplay() {
        return masterConfig.gameplay;
    }

    public static HUDSettings getHUD() {
        return masterConfig.hud;
    }

    public static SystemSettings getSystems() {
        return masterConfig.systems;
    }

    public static DifficultySettings getDifficulty() {
        return masterConfig.difficulty;
    }

    public static AdvancedSettings getAdvanced() {
        return masterConfig.advanced;
    }

    public static void load() {
        init();
    }
}
