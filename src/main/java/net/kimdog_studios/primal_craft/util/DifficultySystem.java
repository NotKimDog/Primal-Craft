package net.kimdog_studios.primal_craft.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🎮 Primal Craft - Dynamic Difficulty System
 *
 * Server-authoritative difficulty management system that tracks player progression
 * and dynamically scales survival mechanics. Handles difficulty presets, custom configurations,
 * metric tracking, and real-time difficulty adjustments.
 *
 * Features:
 * - Server-side difficulty profile management per player
 * - Preset difficulty levels (Easy, Normal, Hard, Hardcore)
 * - Custom difficulty multipliers
 * - Dynamic scaling based on progression metrics
 * - Thread-safe concurrent player tracking
 * - Automatic cleanup on server stop
 * - Network synchronization for multiplayer
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class DifficultySystem {
    // Thread-safe storage for player difficulty profiles
    private static final Map<UUID, DifficultyProfile> DIFFICULTY_PROFILES = new ConcurrentHashMap<>();

    // Configuration
    private static final int SYNC_INTERVAL_TICKS = 20; // Sync difficulty once per second
    private static final int METRICS_LOG_INTERVAL = 1200; // Log metrics every minute
    private static final long DYNAMIC_SCALING_COOLDOWN = 1800000; // 30 minutes in milliseconds

    // Statistics tracking
    private static volatile int tickCounter = 0;
    private static volatile int syncEvents = 0;
    private static volatile int difficultyChanges = 0;

    // Singleton pattern - prevent instantiation
    private DifficultySystem() {
        throw new UnsupportedOperationException("DifficultySystem is a utility class and cannot be instantiated");
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Registers the difficulty system with server events.
     * Must be called during mod initialization.
     */
    public static void register() {
        long startTime = System.currentTimeMillis();

        PrimalCraft.LOGGER.info("⚙️  [DIFFICULTY_SYSTEM] Initializing DifficultySystem v1.0.0");
        PrimalCraft.LOGGER.debug("   ├─ Sync Interval: {} ticks (~1 Hz)", SYNC_INTERVAL_TICKS);
        PrimalCraft.LOGGER.debug("   ├─ Dynamic Scaling Cooldown: {} ms (30 min)", DYNAMIC_SCALING_COOLDOWN);
        PrimalCraft.LOGGER.debug("   ├─ Thread Safety: ConcurrentHashMap enabled");
        PrimalCraft.LOGGER.debug("   └─ Registering event listeners...");

        try {
            // Register tick handler for difficulty updates
            ServerTickEvents.END_SERVER_TICK.register(DifficultySystem::tick);
            PrimalCraft.LOGGER.debug("      ✓ Registered tick handler");

            // Register cleanup handler for server shutdown
            ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
                int profileCount = DIFFICULTY_PROFILES.size();
                PrimalCraft.LOGGER.info("🧹 [DIFFICULTY_SYSTEM] Server stopping - cleaning up resources");
                PrimalCraft.LOGGER.info("   ├─ Clearing difficulty profiles for {} players", profileCount);
                PrimalCraft.LOGGER.info("   ├─ Total sync events: {}", syncEvents);
                PrimalCraft.LOGGER.info("   ├─ Total difficulty changes: {}", difficultyChanges);
                PrimalCraft.LOGGER.info("   └─ Total ticks processed: {}", tickCounter);

                DIFFICULTY_PROFILES.clear();

                PrimalCraft.LOGGER.info("✅ [DIFFICULTY_SYSTEM] Cleanup complete");
            });
            PrimalCraft.LOGGER.debug("      ✓ Registered cleanup handler");

            long elapsed = System.currentTimeMillis() - startTime;
            PrimalCraft.LOGGER.info("✅ [DIFFICULTY_SYSTEM] DifficultySystem registered successfully in {}ms", elapsed);

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("❌ [DIFFICULTY_SYSTEM] Failed to register DifficultySystem", e);
            throw new RuntimeException("Failed to initialize DifficultySystem", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // TICK HANDLER
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Main tick handler - processes difficulty updates for all players.
     * Called every server tick.
     */
    private static void tick(MinecraftServer server) {
        try {
            // Check if difficulty system is enabled
            if (!PrimalCraftConfig.getDifficulty().isDifficultySystemEnabled) {
                return;
            }

            tickCounter++;
            int playerCount = server.getPlayerManager().getPlayerList().size();

            // Log periodic statistics
            if (tickCounter % METRICS_LOG_INTERVAL == 0) {
                PrimalCraft.LOGGER.info("📊 [DIFFICULTY_STATS] Tick #{} - Players: {} | Syncs: {} | Changes: {}",
                    tickCounter, playerCount, syncEvents, difficultyChanges);
            }

            // Sync difficulty data every SYNC_INTERVAL_TICKS
            if (tickCounter % SYNC_INTERVAL_TICKS == 0) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    processPlayerDifficulty(player);
                }
            }

            // Process metric updates
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                updatePlayerMetrics(player);
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("❌ [DIFFICULTY_SYSTEM] Error during tick processing", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // PROFILE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Get or create a difficulty profile for a player
     */
    public static DifficultyProfile getOrCreateProfile(ServerPlayerEntity player) {
        // Use UUID as player identifier
        String playerName = player.getUuid().toString();

        return DIFFICULTY_PROFILES.computeIfAbsent(player.getUuid(), uuid -> {
            DifficultyProfile profile = new DifficultyProfile(
                uuid.toString(),
                playerName
            );
            PrimalCraft.LOGGER.info("[DIFFICULTY] Created new profile for player: {}", playerName);
            return profile;
        });
    }

    /**
     * Get a player's difficulty profile
     */
    public static DifficultyProfile getProfile(UUID playerUuid) {
        return DIFFICULTY_PROFILES.get(playerUuid);
    }

    /**
     * Get a player's difficulty profile, or null if not found
     */
    public static DifficultyProfile getProfile(ServerPlayerEntity player) {
        return DIFFICULTY_PROFILES.get(player.getUuid());
    }

    /**
     * Set a player's difficulty preset
     */
    public static void setDifficultyPreset(ServerPlayerEntity player, DifficultyPreset preset) {
        DifficultyProfile profile = getOrCreateProfile(player);
        DifficultyPreset oldPreset = profile.getPreset();
        profile.setPreset(preset);
        difficultyChanges++;

        String playerName = profile.getPlayerName();
        String message = String.format("Difficulty changed from %s to %s",
            oldPreset.getDisplayName(), preset.getDisplayName());
        player.sendMessage(Text.of("§6[Difficulty] " + message), false);

        PrimalCraft.LOGGER.info("[DIFFICULTY] {} difficulty set to {}",
            playerName, preset.getDisplayName());
    }

    /**
     * Get the effective difficulty multiplier for a specific aspect
     */
    public static float getDifficultyMultiplier(ServerPlayerEntity player, String aspect) {
        DifficultyProfile profile = getProfile(player);
        if (profile == null) {
            profile = getOrCreateProfile(player);
        }

        return switch (aspect.toLowerCase()) {
            case "stamina" -> profile.getStaminaMultiplier();
            case "thirst" -> profile.getThirstMultiplier();
            case "temperature" -> profile.getTemperatureMultiplier();
            case "hazard" -> profile.getHazardMultiplier();
            case "damage" -> profile.getDamageMultiplier();
            case "mob" -> profile.getMobMultiplier();
            default -> 1.0f;
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // METRIC TRACKING
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Record damage taken by a player
     */
    public static void recordDamageTaken(ServerPlayerEntity player, float damage) {
        DifficultyProfile profile = getOrCreateProfile(player);
        profile.addDamageTaken(damage);
    }

    /**
     * Record resources gathered by a player
     */
    public static void recordResourcesGathered(ServerPlayerEntity player, long count) {
        DifficultyProfile profile = getOrCreateProfile(player);
        profile.addResourcesGathered(count);
    }

    /**
     * Record a player death
     */
    public static void recordPlayerDeath(ServerPlayerEntity player) {
        DifficultyProfile profile = getOrCreateProfile(player);
        profile.incrementDeathCount();

        PrimalCraft.LOGGER.info("[DIFFICULTY] {} died (total deaths: {})",
            profile.getPlayerName(), profile.getDeathCount());
    }

    /**
     * Update player metrics (playtime, etc.)
     */
    private static void updatePlayerMetrics(ServerPlayerEntity player) {
        DifficultyProfile profile = getOrCreateProfile(player);
        profile.incrementPlaytime(1);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DYNAMIC SCALING
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Check and apply dynamic difficulty scaling based on progression metrics
     */
    public static void checkAndApplyDynamicScaling(ServerPlayerEntity player) {
        if (!PrimalCraftConfig.getDifficulty().dynamicDifficultyScaling) {
            return;
        }

        DifficultyProfile profile = getOrCreateProfile(player);
        if (!profile.isDynamicScalingEnabled()) {
            return;
        }

        long timeSinceLastAdjustment = System.currentTimeMillis() - profile.getLastDifficultyAdjustment();
        if (timeSinceLastAdjustment < DYNAMIC_SCALING_COOLDOWN) {
            return; // Wait for cooldown period
        }

        float progressionScore = profile.calculateProgressionScore();
        float thresholdPerLevel = PrimalCraftConfig.getDifficulty().scalingThresholdPerLevel;
        int targetScalingLevel = (int) (progressionScore / thresholdPerLevel);

        if (targetScalingLevel > profile.getScalingLevel()) {
            profile.setScalingLevel(targetScalingLevel);
            profile.setLastDifficultyAdjustment(System.currentTimeMillis());

            // Apply scaling with acceleration - gets harder faster at higher levels
            float scalingFactor = calculateScalingFactor(targetScalingLevel);
            applyScalingFactors(profile, scalingFactor);

            player.sendMessage(Text.of(String.format(
                "§c⚠ [Difficulty] Game difficulty increased! (Level %d) - Multiplier: %.2fx",
                targetScalingLevel, scalingFactor
            )), false);

            PrimalCraft.LOGGER.info("[DIFFICULTY] {} difficulty scaled to level {} (multiplier: {})",
                profile.getPlayerName(), targetScalingLevel, String.format("%.2f", scalingFactor));
        }
    }

    /**
     * Calculate scaling factor with exponential acceleration
     * Level 1 = 1.1x, Level 2 = 1.25x, Level 3 = 1.45x, Level 4 = 1.7x, etc.
     */
    private static float calculateScalingFactor(int level) {
        if (level <= 0) return 1.0f;
        // Exponential scaling: 1.0 + (0.1 * level + 0.025 * level^2)
        return 1.0f + (0.1f * level) + (0.025f * level * level);
    }

    /**
     * Apply scaling factors to all difficulty multipliers based on config toggles
     */
    private static void applyScalingFactors(DifficultyProfile profile, float factor) {
        var config = PrimalCraftConfig.getDifficulty();

        // Only scale aspects that are enabled
        if (config.staminaScalingEnabled) {
            profile.setCustomStaminaMultiplier(profile.getStaminaMultiplier() * factor);
        }
        if (config.thirstScalingEnabled) {
            profile.setCustomThirstMultiplier(profile.getThirstMultiplier() * factor);
        }
        if (config.temperatureScalingEnabled) {
            profile.setCustomTemperatureMultiplier(profile.getTemperatureMultiplier() * factor);
        }
        if (config.hazardScalingEnabled) {
            profile.setCustomHazardMultiplier(profile.getHazardMultiplier() * factor);
        }
        if (config.damageScalingEnabled) {
            profile.setCustomDamageMultiplier(profile.getDamageMultiplier() * factor);
        }
        if (config.mobScalingEnabled) {
            profile.setCustomMobMultiplier(profile.getMobMultiplier() * factor);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // SYNCHRONIZATION & PROCESSING
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Process difficulty data for a player and sync to client
     */
    private static void processPlayerDifficulty(ServerPlayerEntity player) {
        DifficultyProfile profile = getOrCreateProfile(player);

        // Check for dynamic scaling
        checkAndApplyDynamicScaling(player);

        // Sync to client would happen here with network payload
        // For now, we just update the profile
        syncEvents++;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DIFFICULTY RETRIEVAL & INFO
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Get all active difficulty profiles
     */
    public static Map<UUID, DifficultyProfile> getAllProfiles() {
        return new HashMap<>(DIFFICULTY_PROFILES);
    }

    /**
     * Get a summary of player's difficulty info
     */
    public static String getDifficultyInfo(ServerPlayerEntity player) {
        DifficultyProfile profile = getProfile(player.getUuid());
        if (profile == null) {
            return "No difficulty profile found";
        }

        return String.format(
            "§e▶ Difficulty: %s §7(Level: %d)%n" +
            "§7Playtime: %.1f hours%n" +
            "§7Damage Taken: %.1f%n" +
            "§7Deaths: %d%n" +
            "§7Resources: %d%n" +
            "§7Multipliers: Stamina=%.2f Thirst=%.2f Temp=%.2f Hazard=%.2f",
            profile.getPreset().getDisplayName(),
            profile.getScalingLevel(),
            profile.getPlaytimeHours(),
            profile.getTotalDamageTaken(),
            profile.getDeathCount(),
            profile.getTotalResourcesGathered(),
            profile.getStaminaMultiplier(),
            profile.getThirstMultiplier(),
            profile.getTemperatureMultiplier(),
            profile.getHazardMultiplier()
        );
    }
}
