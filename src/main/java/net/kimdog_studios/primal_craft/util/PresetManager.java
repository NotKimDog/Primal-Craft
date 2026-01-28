package net.kimdog_studios.primal_craft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 🎮 Primal Craft - Difficulty Preset Manager
 *
 * Manages difficulty presets that apply pre-configured multiplier sets
 * to all difficulty-related settings at once. Includes built-in presets
 * and support for custom presets.
 *
 * Presets include:
 * - PEACEFUL: 0.0x - No challenge
 * - EASY: 0.5x - Casual gameplay
 * - NORMAL: 1.0x - Balanced gameplay
 * - HARD: 1.5x - Challenging gameplay
 * - EXPERT: 2.0x - Hardcore challenge
 * - NIGHTMARE: 3.0x - Extreme difficulty
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class PresetManager {
    private PresetManager() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get("config/primal-craft");
    private static final File PRESETS_FILE = CONFIG_DIR.resolve("presets.json").toFile();

    private static Map<String, DifficultyPresetConfig> presets = new HashMap<>();

    public static void init() {
        PrimalCraft.LOGGER.info("📋 [PRESET_MANAGER] Initializing Preset Manager");
        try {
            Files.createDirectories(CONFIG_DIR);
            loadPresets();
            createDefaultPresetsIfMissing();
            PrimalCraft.LOGGER.info("✅ [PRESET_MANAGER] Presets loaded successfully");
        } catch (IOException e) {
            PrimalCraft.LOGGER.error("❌ [PRESET_MANAGER] Failed to initialize presets", e);
        }
    }

    private static void loadPresets() {
        if (PRESETS_FILE.exists()) {
            try (FileReader reader = new FileReader(PRESETS_FILE)) {
                PresetsData data = GSON.fromJson(reader, PresetsData.class);
                if (data != null && data.presets != null) {
                    presets = data.presets;
                    PrimalCraft.LOGGER.info("📋 Loaded {} presets from config", presets.size());
                }
            } catch (IOException e) {
                PrimalCraft.LOGGER.error("Failed to load presets config", e);
                createDefaultPresets();
            }
        } else {
            createDefaultPresets();
        }
    }

    private static void createDefaultPresetsIfMissing() {
        boolean needsSave = false;

        if (!presets.containsKey("PEACEFUL")) {
            presets.put("PEACEFUL", new DifficultyPresetConfig(
                "PEACEFUL", 0.0f, 0xFF00FF00, true
            ));
            needsSave = true;
        }

        if (!presets.containsKey("EASY")) {
            presets.put("EASY", new DifficultyPresetConfig(
                "EASY", 0.5f, 0xFF00CC00, true
            ));
            needsSave = true;
        }

        if (!presets.containsKey("NORMAL")) {
            presets.put("NORMAL", new DifficultyPresetConfig(
                "NORMAL", 1.0f, 0xFFFFFF00, true
            ));
            needsSave = true;
        }

        if (!presets.containsKey("HARD")) {
            presets.put("HARD", new DifficultyPresetConfig(
                "HARD", 1.5f, 0xFFFF6600, true
            ));
            needsSave = true;
        }

        if (!presets.containsKey("EXPERT")) {
            presets.put("EXPERT", new DifficultyPresetConfig(
                "EXPERT", 2.0f, 0xFFFF3300, true
            ));
            needsSave = true;
        }

        if (!presets.containsKey("NIGHTMARE")) {
            presets.put("NIGHTMARE", new DifficultyPresetConfig(
                "NIGHTMARE", 3.0f, 0xFFFF0000, true
            ));
            needsSave = true;
        }

        if (needsSave) {
            savePresets();
        }
    }

    private static void createDefaultPresets() {
        PrimalCraft.LOGGER.info("📋 Creating default presets...");
        presets.put("PEACEFUL", new DifficultyPresetConfig("PEACEFUL", 0.0f, 0xFF00FF00, true));
        presets.put("EASY", new DifficultyPresetConfig("EASY", 0.5f, 0xFF00CC00, true));
        presets.put("NORMAL", new DifficultyPresetConfig("NORMAL", 1.0f, 0xFFFFFF00, true));
        presets.put("HARD", new DifficultyPresetConfig("HARD", 1.5f, 0xFFFF6600, true));
        presets.put("EXPERT", new DifficultyPresetConfig("EXPERT", 2.0f, 0xFFFF3300, true));
        presets.put("NIGHTMARE", new DifficultyPresetConfig("NIGHTMARE", 3.0f, 0xFFFF0000, true));
        savePresets();
    }

    public static void savePresets() {
        try {
            Files.createDirectories(CONFIG_DIR);
            PresetsData data = new PresetsData();
            data.presets = presets;
            try (FileWriter writer = new FileWriter(PRESETS_FILE)) {
                GSON.toJson(data, writer);
            }
            PrimalCraft.LOGGER.info("💾 Presets saved successfully");
        } catch (IOException e) {
            PrimalCraft.LOGGER.error("Failed to save presets", e);
        }
    }

    /**
     * Apply a preset to the current configuration
     */
    public static void applyPreset(String presetName) {
        if (!PrimalCraftConfig.getAdvanced().features.presetSystem) {
            PrimalCraft.LOGGER.warn("⚠️  Preset system is disabled by config");
            return;
        }
        DifficultyPresetConfig preset = presets.get(presetName.toUpperCase());
        if (preset == null) {
            PrimalCraft.LOGGER.warn("⚠️  Preset not found: {}", presetName);
            return;
        }

        try {
            var config = PrimalCraftConfig.getDifficulty();

            // Apply multiplier to all relevant systems
            config.core.stamina = preset.multiplier;
            config.core.thirst = preset.multiplier;
            config.core.temperature = preset.multiplier;
            config.core.hazards = preset.multiplier;
            config.damage.environmental = preset.multiplier;
            config.damage.dehydration = preset.multiplier;
            config.mobResources.mobDamage = preset.multiplier;
            config.mobResources.mobHealth = preset.multiplier;

            // Apply to gameplay systems if they exist
            var gameplayConfig = PrimalCraftConfig.getGameplay();
            if (gameplayConfig != null) {
                gameplayConfig.stamina.depletionRate *= preset.multiplier;
                gameplayConfig.thirst.depletionRate *= preset.multiplier;
                gameplayConfig.temperature.changeRate *= preset.multiplier;
            }

            // Set master difficulty flag
            config.master.currentPreset = presetName.toUpperCase();

            // Save the configuration
            PrimalCraftConfig.save();

            PrimalCraft.LOGGER.info("✅ [PRESET] Applied preset: {} (Multiplier: {:.2f}x, Color: #{:08X})",
                presetName.toUpperCase(), preset.multiplier, preset.color);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("❌ Failed to apply preset: {}", presetName, e);
        }
    }

    /**
     * Get a preset by name
     */
    public static DifficultyPresetConfig getPreset(String name) {
        return presets.get(name.toUpperCase());
    }

    /**
     * Get all presets
     */
    public static Map<String, DifficultyPresetConfig> getAllPresets() {
        return new HashMap<>(presets);
    }

    /**
     * Get the display color for a preset
     */
    public static int getPresetColor(String presetName) {
        DifficultyPresetConfig preset = presets.get(presetName.toUpperCase());
        return preset != null ? preset.color : 0xFFFFFFFF;
    }

    /**
     * Get the multiplier for a preset
     */
    public static float getPresetMultiplier(String presetName) {
        DifficultyPresetConfig preset = presets.get(presetName.toUpperCase());
        return preset != null ? preset.multiplier : 1.0f;
    }

    /**
     * Check if a preset exists
     */
    public static boolean hasPreset(String presetName) {
        return presets.containsKey(presetName.toUpperCase());
    }

    /**
     * Get all preset names
     */
    public static String[] getPresetNames() {
        return presets.keySet().toArray(new String[0]);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // INNER CLASSES
    // ═══════════════════════════════════════════════════════════════════════════════

    public static class DifficultyPresetConfig {
        public String name;
        public float multiplier;
        public int color;  // Hex color for UI display (e.g., 0xFFFF0000 = red)
        public boolean enabled;

        public DifficultyPresetConfig() {}

        public DifficultyPresetConfig(String name, float multiplier, int color, boolean enabled) {
            this.name = name;
            this.multiplier = multiplier;
            this.color = color;
            this.enabled = enabled;
        }
    }

    private static class PresetsData {
        Map<String, DifficultyPresetConfig> presets = new HashMap<>();
    }
}
