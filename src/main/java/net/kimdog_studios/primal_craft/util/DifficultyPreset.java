package net.kimdog_studios.primal_craft.util;

/**
 * 🎮 Primal Craft - Difficulty Preset Enum
 *
 * Predefined difficulty configurations for different playstyles.
 * Each preset defines specific multipliers for various survival mechanics.
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public enum DifficultyPreset {
    EASY("Easy", 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.7f),
    NORMAL("Normal", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f),
    HARD("Hard", 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.3f),
    HARDCORE("Hardcore", 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 1.6f);

    private final String displayName;
    private final float staminaMultiplier;
    private final float thirstMultiplier;
    private final float temperatureMultiplier;
    private final float hazardMultiplier;
    private final float damageMultiplier;
    private final float mobMultiplier;

    DifficultyPreset(String displayName, float stamina, float thirst, float temperature,
                     float hazard, float damage, float mob) {
        this.displayName = displayName;
        this.staminaMultiplier = stamina;
        this.thirstMultiplier = thirst;
        this.temperatureMultiplier = temperature;
        this.hazardMultiplier = hazard;
        this.damageMultiplier = damage;
        this.mobMultiplier = mob;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getStaminaMultiplier() {
        return staminaMultiplier;
    }

    public float getThirstMultiplier() {
        return thirstMultiplier;
    }

    public float getTemperatureMultiplier() {
        return temperatureMultiplier;
    }

    public float getHazardMultiplier() {
        return hazardMultiplier;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public float getMobMultiplier() {
        return mobMultiplier;
    }

    /**
     * Get difficulty by name (case-insensitive)
     */
    public static DifficultyPreset fromString(String name) {
        try {
            return DifficultyPreset.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NORMAL; // Default to normal
        }
    }
}
