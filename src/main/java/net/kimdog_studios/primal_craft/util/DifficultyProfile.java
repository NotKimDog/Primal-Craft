package net.kimdog_studios.primal_craft.util;

/**
 * 🎮 Primal Craft - Player Difficulty Profile
 *
 * Per-player difficulty configuration and metrics tracking.
 * Stores individual player difficulty settings and progression metrics.
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public class DifficultyProfile {
    private String playerUuid;
    private String playerName;
    private DifficultyPreset preset;

    // Custom multipliers (used when preset is custom)
    private float customStaminaMultiplier = 1.0f;
    private float customThirstMultiplier = 1.0f;
    private float customTemperatureMultiplier = 1.0f;
    private float customHazardMultiplier = 1.0f;
    private float customDamageMultiplier = 1.0f;
    private float customMobMultiplier = 1.0f;

    // Progression metrics
    private long playtimeTicks = 0;
    private float totalDamageTaken = 0.0f;
    private long totalResourcesGathered = 0;
    private int deathCount = 0;
    private float totalStaminaDrained = 0.0f;

    // Dynamic scaling state
    private boolean dynamicScalingEnabled = true;
    private long lastDifficultyAdjustment = 0;
    private int scalingLevel = 0; // 0 = base, 1+ = scaled up

    // Timestamps
    private long createdAt = System.currentTimeMillis();
    private long lastUpdated = System.currentTimeMillis();

    public DifficultyProfile() {
        this.preset = DifficultyPreset.NORMAL;
    }

    public DifficultyProfile(String playerUuid, String playerName) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.preset = DifficultyPreset.NORMAL;
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - Basic Info
    // ═══════════════════════════════════════════════════════════════════════════════

    public String getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(String playerUuid) {
        this.playerUuid = playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public DifficultyPreset getPreset() {
        return preset;
    }

    public void setPreset(DifficultyPreset preset) {
        this.preset = preset;
        this.lastUpdated = System.currentTimeMillis();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // MULTIPLIER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Get the effective stamina multiplier (preset or custom)
     */
    public float getStaminaMultiplier() {
        return preset == DifficultyPreset.NORMAL && customStaminaMultiplier != 1.0f
            ? customStaminaMultiplier
            : preset.getStaminaMultiplier();
    }

    /**
     * Get the effective thirst multiplier (preset or custom)
     */
    public float getThirstMultiplier() {
        return preset == DifficultyPreset.NORMAL && customThirstMultiplier != 1.0f
            ? customThirstMultiplier
            : preset.getThirstMultiplier();
    }

    /**
     * Get the effective temperature multiplier (preset or custom)
     */
    public float getTemperatureMultiplier() {
        return preset == DifficultyPreset.NORMAL && customTemperatureMultiplier != 1.0f
            ? customTemperatureMultiplier
            : preset.getTemperatureMultiplier();
    }

    /**
     * Get the effective hazard multiplier (preset or custom)
     */
    public float getHazardMultiplier() {
        return preset == DifficultyPreset.NORMAL && customHazardMultiplier != 1.0f
            ? customHazardMultiplier
            : preset.getHazardMultiplier();
    }

    /**
     * Get the effective damage multiplier (preset or custom)
     */
    public float getDamageMultiplier() {
        return preset == DifficultyPreset.NORMAL && customDamageMultiplier != 1.0f
            ? customDamageMultiplier
            : preset.getDamageMultiplier();
    }

    /**
     * Get the effective mob multiplier (preset or custom)
     */
    public float getMobMultiplier() {
        return preset == DifficultyPreset.NORMAL && customMobMultiplier != 1.0f
            ? customMobMultiplier
            : preset.getMobMultiplier();
    }

    // Custom multiplier setters
    public void setCustomStaminaMultiplier(float value) {
        this.customStaminaMultiplier = Math.max(0.1f, Math.min(5.0f, value));
    }

    public void setCustomThirstMultiplier(float value) {
        this.customThirstMultiplier = Math.max(0.1f, Math.min(5.0f, value));
    }

    public void setCustomTemperatureMultiplier(float value) {
        this.customTemperatureMultiplier = Math.max(0.1f, Math.min(5.0f, value));
    }

    public void setCustomHazardMultiplier(float value) {
        this.customHazardMultiplier = Math.max(0.1f, Math.min(5.0f, value));
    }

    public void setCustomDamageMultiplier(float value) {
        this.customDamageMultiplier = Math.max(0.1f, Math.min(5.0f, value));
    }

    public void setCustomMobMultiplier(float value) {
        this.customMobMultiplier = Math.max(0.1f, Math.min(5.0f, value));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // METRICS TRACKING
    // ═══════════════════════════════════════════════════════════════════════════════

    public long getPlaytimeTicks() {
        return playtimeTicks;
    }

    public void incrementPlaytime(long ticks) {
        this.playtimeTicks += ticks;
        this.lastUpdated = System.currentTimeMillis();
    }

    public float getTotalDamageTaken() {
        return totalDamageTaken;
    }

    public void addDamageTaken(float damage) {
        this.totalDamageTaken += damage;
        this.lastUpdated = System.currentTimeMillis();
    }

    public long getTotalResourcesGathered() {
        return totalResourcesGathered;
    }

    public void addResourcesGathered(long count) {
        this.totalResourcesGathered += count;
        this.lastUpdated = System.currentTimeMillis();
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void incrementDeathCount() {
        this.deathCount++;
        this.lastUpdated = System.currentTimeMillis();
    }

    public float getTotalStaminaDrained() {
        return totalStaminaDrained;
    }

    public void addStaminaDrained(float amount) {
        this.totalStaminaDrained += amount;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DYNAMIC SCALING
    // ═══════════════════════════════════════════════════════════════════════════════

    public boolean isDynamicScalingEnabled() {
        return dynamicScalingEnabled;
    }

    public void setDynamicScalingEnabled(boolean enabled) {
        this.dynamicScalingEnabled = enabled;
        this.lastUpdated = System.currentTimeMillis();
    }

    public long getLastDifficultyAdjustment() {
        return lastDifficultyAdjustment;
    }

    public void setLastDifficultyAdjustment(long timestamp) {
        this.lastDifficultyAdjustment = timestamp;
    }

    public int getScalingLevel() {
        return scalingLevel;
    }

    public void setScalingLevel(int level) {
        this.scalingLevel = Math.max(0, level);
        this.lastUpdated = System.currentTimeMillis();
    }

    public void incrementScalingLevel() {
        this.scalingLevel++;
        this.lastUpdated = System.currentTimeMillis();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // TIMESTAMPS
    // ═══════════════════════════════════════════════════════════════════════════════

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Get playtime in minutes
     */
    public double getPlaytimeMinutes() {
        return playtimeTicks / 1200.0; // 20 ticks/second * 60 seconds/minute
    }

    /**
     * Get playtime in hours
     */
    public double getPlaytimeHours() {
        return playtimeTicks / 72000.0; // 20 ticks/second * 60 seconds * 60 minutes
    }

    /**
     * Calculate a composite progression score with customizable weighting
     */
    public float calculateProgressionScore() {
        var config = net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig.getDifficulty();

        if (!config.enableMetricsWeighting) {
            // Simple calculation: (playtime_hours × 10) + (damage × 0.5) + (resources × 0.01) + (deaths × 5)
            return (float) getPlaytimeHours() * 10 +
                   totalDamageTaken * 0.5f +
                   totalResourcesGathered * 0.01f +
                   deathCount * 5;
        }

        // Weighted calculation
        float playtimeScore = (float) getPlaytimeHours() * config.playtimeWeight * 25; // Scale factor
        float damageScore = totalDamageTaken * config.damageWeight * 2;
        float deathScore = deathCount * config.deathWeight * 25;
        float resourceScore = totalResourcesGathered * config.resourceWeight * 0.01f;

        return playtimeScore + damageScore + deathScore + resourceScore;
    }

    @Override
    public String toString() {
        return String.format(
            "DifficultyProfile{player=%s, preset=%s, scaling=%d, playtime=%.1fh, damage=%.1f, deaths=%d}",
            playerName, preset.getDisplayName(), scalingLevel, getPlaytimeHours(), totalDamageTaken, deathCount
        );
    }
}
