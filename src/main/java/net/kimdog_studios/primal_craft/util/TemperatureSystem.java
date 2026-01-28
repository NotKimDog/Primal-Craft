package net.kimdog_studios.primal_craft.util;

import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.state.property.Properties;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

/**
 * Comprehensive temperature system with realistic mechanics - IMPROVED VERSION
 * Enhanced for stability, precision, and performance with detailed logging
 */
public final class TemperatureSystem {
    private static final double MIN_TEMPERATURE = -40.0;
    private static final double MAX_TEMPERATURE = 1200.0; // Lava reaches 1200°C

    // Statistics tracking
    private static int temperatureCalculations = 0;
    private static int cacheHits = 0;
    private static int cacheMisses = 0;
    private static int playerTemperatureQueries = 0;
    private static long lastStatsLog = System.currentTimeMillis();

    // Cache configuration
    private static final int CACHE_UPDATE_INTERVAL_MS = 500; // 500ms = ~10 ticks at 20 TPS
    private static final int MAX_CACHE_SIZE = 1000; // Prevent unbounded memory growth
    private static final int CLEANUP_INTERVAL_MS = 60000; // Cleanup every 60 seconds

    // Player temperature cache with automatic cleanup
    private static final java.util.Map<java.util.UUID, Double> cachedTemperature = new java.util.LinkedHashMap<>(16, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };
    private static final java.util.Map<java.util.UUID, Long> lastCalcTime = new java.util.LinkedHashMap<>(16, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    // Wet player tracking (players stay cold after being in water)
    private static final java.util.Map<java.util.UUID, Long> lastWaterTime = new java.util.LinkedHashMap<>(16, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };
    private static final long WET_DURATION = 45000; // Stay wet for 45 seconds after water

    // Cached world temperature smoothing (per world key)
    private static final java.util.Map<String, Double> worldCachedTemperature = new java.util.LinkedHashMap<>(16, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
            return size() > 100; // Limit world cache
        }
    };
    private static final java.util.Map<String, Long> worldLastCalcTime = new java.util.LinkedHashMap<>(16, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
            return size() > 100;
        }
    };

    // Last cleanup time for cache maintenance
    private static long lastCleanupTime = System.currentTimeMillis();

    // Temperature trend tracking (for HUD display - is temp rising or falling?)
    private static final java.util.Map<java.util.UUID, Double> temperatureTrend = new java.util.LinkedHashMap<>(16, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    // Player comfort zone memory (what temps do they prefer?)
    private static final java.util.Map<java.util.UUID, Double> comfortZonePreference = new java.util.LinkedHashMap<>(16, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    // Initialize logging
    static {
        PrimalCraft.LOGGER.info("TemperatureSystem initialized");
    }

    /**
     * Calculate comprehensive player temperature with all environmental factors
     * Uses exponential smoothing for stability and caching for performance
     */
    public static double getPlayerTemperature(ServerPlayerEntity player) {
        // Return neutral temperature if temperature system is disabled
        if (!PrimalCraftConfig.getGameplay().temperatureSystemEnabled) {
            return 20.0; // Comfortable neutral temperature
        }

        long currentTime = System.currentTimeMillis();
        java.util.UUID playerId = player.getUuid();

        // Periodic cache cleanup (every 60 seconds)
        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            synchronized (cachedTemperature) {
                // Remove entries older than 5 minutes
                cachedTemperature.entrySet().removeIf(entry -> {
                    Long calcTime = lastCalcTime.get(entry.getKey());
                    return calcTime != null && (currentTime - calcTime) > 300000;
                });
                lastWaterTime.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > WET_DURATION);
            }
            lastCleanupTime = currentTime;
        }

        // Use cached temp if calculated recently
        Long lastTime = lastCalcTime.get(playerId);
        if (lastTime != null && currentTime - lastTime < CACHE_UPDATE_INTERVAL_MS) {
            Double cached = cachedTemperature.get(playerId);
            if (cached != null) return cached;
        }

        // Calculate fresh temperature
        double freshTemp = calculateTemperatureInternal(player, currentTime);

        // Apply exponential smoothing for stability
        Double lastTemp = cachedTemperature.get(playerId);
        double smoothedTemp = (lastTemp == null) ? freshTemp : smooth(freshTemp, lastTemp, lastTime == null ? CACHE_UPDATE_INTERVAL_MS : (currentTime - lastTime));
        smoothedTemp = Math.max(MIN_TEMPERATURE, Math.min(MAX_TEMPERATURE, smoothedTemp));

        // Store in cache
        lastCalcTime.put(playerId, currentTime);
        cachedTemperature.put(playerId, smoothedTemp);

        // Track temperature trend (delta from last calculation)
        Double lastCachedTemp = temperatureTrend.get(playerId);
        temperatureTrend.put(playerId, lastCachedTemp != null ? (smoothedTemp - lastCachedTemp) : 0.0);

        return smoothedTemp;
    }

    // Adaptive smoothing: alpha scales with time delta up to a cap
    private static double smooth(double fresh, double last, long dtMs) {
        double baseAlpha = 0.7;
        double maxAlpha = 0.9;
        double minAlpha = 0.5;
        double factor = Math.max(0.0, Math.min(1.0, dtMs / 1000.0)); // normalize up to 1s
        double alpha = Math.max(minAlpha, Math.min(maxAlpha, baseAlpha + 0.2 * factor));
        return alpha * fresh + (1.0 - alpha) * last;
    }

    /**
     * Internal temperature calculation (cached)
     */
    private static double calculateTemperatureInternal(ServerPlayerEntity player, long currentTime) {
        BlockPos pos = player.getBlockPos();
        RegistryEntry<Biome> biome = player.getEntityWorld().getBiome(pos);
        float biomeTemp = biome.value().getTemperature();

        // DIMENSION overrides
        String dimension = player.getEntityWorld().getRegistryKey().getValue().getPath();
        double temperature;
        if (dimension.contains("nether")) {
            temperature = 90.0; // Fixed nether temp (no random variation)
        } else if (dimension.contains("end")) {
            temperature = -12.0; // Fixed end temp (no random variation)
        } else {
            temperature = convertBiomeToRealTemp(biomeTemp);
        }

        // TIME OF DAY - realistic day/night ambient temperatures
        long timeOfDay = player.getEntityWorld().getTimeOfDay() % 24000;
        double ambientTemp = getAmbientTempForTime(timeOfDay);
        temperature += ambientTemp;

        // ALTITUDE (realistic atmospheric lapse rate: ~6.5°C per 1000m, in Minecraft: ~1°C per 10 blocks)
        int y = pos.getY();
        if (y > 64) {
            temperature -= (y - 64) * 0.065; // -0.065°C per block up
        } else if (y < 64) {
            temperature += (64 - y) * 0.02; // +0.02°C per block down (geothermal gradient)
        }

        // CAVE temperature (underground is stable ~10-15°C)
        if (!player.getEntityWorld().isSkyVisible(pos)) {
            int depth = 64 - y;
            if (depth > 0) {
                double caveTemp = 10.0 + (Math.min(depth, 50) / 50.0 * 5.0); // 10-15°C range
                double blendFactor = Math.min(depth / 40.0, 1.0); // Blend over 40 blocks
                temperature = temperature * (1 - blendFactor) + caveTemp * blendFactor;
            }
        }

        // SUN EXPOSURE - most impactful factor
        boolean skyVisible = player.getEntityWorld().isSkyVisible(pos);
        if (skyVisible) {
            if (timeOfDay > 5000 && timeOfDay < 19000) { // Dawn to dusk
                double sunIntensity = getSunIntensity(timeOfDay);
                double sunWarmth = sunIntensity * 30.0; // Up to +30°C from direct sun!
                temperature += sunWarmth;
            }
        } else {
            // In shade/indoors: tiny night cooling only
            if (timeOfDay <= 5000 || timeOfDay >= 19000) {
                temperature -= 1.5; // Night shade
            }
        }

        // SHELTER factor (0 exposed -> 1 fully sheltered)
        double shelter = getShelterFactor(player, pos);

        // WEATHER + WIND CHILL
        boolean raining = player.getEntityWorld().isRaining();
        boolean thundering = player.getEntityWorld().isThundering();
        if (raining) {
            temperature -= 10.0; // rain cooling
            if (skyVisible) {
                // Getting rained/snowed on makes you wet
                lastWaterTime.put(player.getUuid(), currentTime);
            }
        }
        if (thundering) {
            temperature -= 12.0; // thunder colder
            if (skyVisible) {
                lastWaterTime.put(player.getUuid(), currentTime);
            }
        }
        // Wind chill scales with exposure and altitude; blocked by shelter
        double windChill = 0.0;
        if (skyVisible) {
            // Calculate wind strength estimate based on weather and altitude
            // Stormy = strong wind, clear = light wind
            double baseWindStrength = (raining || thundering) ? 0.6 : 0.3;
            if (thundering) baseWindStrength = 0.9;

            // Use calculated wind strength for chill
            double windStrength = baseWindStrength;

            // Base wind chill from actual wind
            windChill -= windStrength * 3.0; // Up to -9°C from strong wind

            if (raining) windChill -= 4.0;
            if (thundering) windChill -= 3.0;
            // Altitude wind
            if (y > 90) {
                windChill -= Math.min(6.0, (y - 90) * 0.12);
            }
            windChill *= (1.0 - shelter); // shelter cuts wind
            temperature += windChill;
        }

        // HUMIDITY (water proximity) - only check if not in sun (prevents spam)
        if (timeOfDay <= 5000 || timeOfDay >= 19000) { // Only at night/dawn/dusk
            temperature += getWaterProximityEffect(player, pos);
        }

        // NEARBY HEAT SOURCES (campfires, lava, fire, furnaces) - calculate once
        double heatFromSources = getNearbyBlockTemp(player, pos);
        temperature += heatFromSources;

        // Entity heat/cold contribution
        temperature += getNearbyEntityTemp(player);

        // WATER IMMERSION (overrides other temps)
        if (player.isTouchingWater() && !player.isInLava()) {
            temperature = getWaterTemperature(biomeTemp, y);
            // Mark player as wet
            lastWaterTime.put(player.getUuid(), currentTime);
        }

        // WET PLAYER DEBUFF (stays cold after water until near heat source or sun)
        boolean isWet = false;
        if (lastWaterTime.containsKey(player.getUuid())) {
            long timeSinceWater = currentTime - lastWaterTime.get(player.getUuid());
            if (timeSinceWater < WET_DURATION) {
                isWet = true;
                boolean inDirectSun = player.getEntityWorld().isSkyVisible(pos) && timeOfDay > 5000 && timeOfDay < 19000;
                double cap;
                if (heatFromSources >= 15.0) {
                    cap = 24.0; // Strong heat nearby, can dry quickly
                } else if (heatFromSources >= 5.0) {
                    cap = 14.0; // Moderate heat source, slow warm
                } else if (inDirectSun) {
                    cap = 12.0; // Sun will slowly dry, still capped low
                } else {
                    cap = 8.0; // Cold night shade: stay very cold
                }
                temperature = Math.min(temperature, cap);
            } else {
                lastWaterTime.remove(player.getUuid());
            }
        }

        // LAVA (800-1200°C - extreme!)
        if (player.isInLava()) {
            temperature = 1000.0; // Fixed lava temp (no random variation)
        }

        // ON FIRE
        if (player.isOnFire()) {
            temperature += 250.0;
        }

        // POWDER SNOW (forces very cold)
        if (player.inPowderSnow) {
            temperature = -15.0;
        }

        // ARMOR INSULATION (moderates temperature)
        temperature = applyArmorInsulation(player, temperature);

        // SPRINTING generates body heat (less effective when soaked)
        if (player.isSprinting()) {
            temperature += isWet ? 1.0 : 3.0; // Dampened heat gain when wet
        }

        // MOVEMENT (activity generates heat)
        if (player.getVelocity().horizontalLengthSquared() > 0.01) {
            temperature += isWet ? 0.5 : 1.5; // Less heat from movement when soaked
        }

        // Night radiative cooling when exposed to sky
        if (skyVisible && (timeOfDay >= 19000 || timeOfDay <= 5000)) {
            temperature -= 3.0; // clear-sky night loss
        }

        return Math.max(MIN_TEMPERATURE, Math.min(MAX_TEMPERATURE, temperature));
    }

    /**
     * Convert Minecraft biome temp (0-2 scale) to realistic Celsius
     * More accurate mapping with better interpolation
     */
    private static double convertBiomeToRealTemp(float biomeTemp) {
        // Realistic mapping with piecewise linear interpolation:
        // 0.0   = Frozen (-20°C) - frozen peaks
        // 0.1   = Snowy (-18°C)
        // 0.15  = Very Cold (-15°C) - taiga/snowy forests
        // 0.25  = Cold Mountain (-10°C)
        // 0.3   = Cold (0°C) - plains, forests
        // 0.5   = Cool (5°C) - birch, oak forests
        // 0.8   = Temperate (12°C)
        // 1.0   = Warm (15°C) - temperate biomes
        // 1.2   = Warm+ (18°C)
        // 1.5   = Hot (25°C) - desert, savanna
        // 1.8   = Very Hot (32°C)
        // 2.0   = Scorching (40°C) - badlands

        if (biomeTemp <= 0.0f) {
            return -20.0; // Absolute zero
        } else if (biomeTemp <= 0.15f) {
            // -20 to -15°C
            return -20.0 + (biomeTemp / 0.15f) * 5.0;
        } else if (biomeTemp <= 0.3f) {
            // -15 to 0°C
            return -15.0 + ((biomeTemp - 0.15f) / 0.15f) * 15.0;
        } else if (biomeTemp <= 0.5f) {
            // 0 to 5°C
            return 0.0 + ((biomeTemp - 0.3f) / 0.2f) * 5.0;
        } else if (biomeTemp <= 1.0f) {
            // 5 to 15°C
            return 5.0 + ((biomeTemp - 0.5f) / 0.5f) * 10.0;
        } else if (biomeTemp <= 1.5f) {
            // 15 to 25°C
            return 15.0 + ((biomeTemp - 1.0f) / 0.5f) * 10.0;
        } else if (biomeTemp <= 2.0f) {
            // 25 to 40°C
            return 25.0 + ((biomeTemp - 1.5f) / 0.5f) * 15.0;
        } else {
            return 40.0; // Maximum
        }
    }

    /**
     * Get ambient temperature change based on time of day
     * Uses smooth curves for realistic transitions
     */
    private static double getAmbientTempForTime(long timeOfDay) {
        // Minecraft day is 24000 ticks
        // Sunrise: 5000, Noon: 12000, Sunset: 19000
        //
        // Temperature curve:
        // 0-5000:    Deep night to cold dawn (-14 to -12°C change)
        // 5000-8000: Morning warming (-12 to -2°C)
        // 8000-13000: Daytime peak (-2 to +5°C)
        // 13000-18000: Afternoon cooling (+5 to -8°C)
        // 18000-24000: Night time (-8 to -14°C)

        if (timeOfDay < 5000) {
            // Deep night: stay cold and stable
            double progress = timeOfDay / 5000.0;
            return -14.0 + progress * 2.0; // -14 to -12
        } else if (timeOfDay < 8000) {
            // Morning warming
            double progress = (timeOfDay - 5000) / 3000.0;
            return -12.0 + progress * 10.0; // -12 to -2
        } else if (timeOfDay < 13000) {
            // Warm daytime - peak around 12000
            double progress = (timeOfDay - 8000) / 5000.0;
            // Smooth curve up
            return -2.0 + Math.sin(progress * Math.PI / 2.0) * 7.0;
        } else if (timeOfDay < 18000) {
            // Afternoon cooling
            double progress = (timeOfDay - 13000) / 5000.0;
            return 5.0 - Math.sin(progress * Math.PI / 2.0) * 13.0; // +5 to -8
        } else {
            // Evening to night
            double progress = (timeOfDay - 18000) / 6000.0;
            return -8.0 - progress * 6.0; // -8 to -14
        }
    }

    /**
     * Calculate sun intensity based on realistic solar angle
     * Uses smooth cosine curve for natural transitions
     */
    private static double getSunIntensity(long timeOfDay) {
        // Peak at 12000 (noon), drops to 0 at sunrise (5000) and sunset (19000)
        // Smooth bell curve for natural sun intensity progression
        double peakTime = 12000.0;
        double distFromPeak = Math.abs(timeOfDay - peakTime);

        // Sharper cutoff outside sunrise/sunset times
        if (distFromPeak > 7500) return 0.0; // Before sunrise or after sunset

        // Smooth cosine curve (more intensity mid-day, less near sunrise/sunset)
        double normalized = distFromPeak / 7500.0; // 0 at peak, 1 at edges
        return Math.pow(Math.cos(normalized * Math.PI / 2.0), 1.2); // Slightly sharper curve
    }

    /**
     * Water proximity effect (humidity) - optimized to prevent spam
     * Checks a coarse grid to avoid excessive calculations
     */
    private static double getWaterProximityEffect(ServerPlayerEntity player, BlockPos pos) {
        double humidity = 0.0;
        int checkRadius = 5; // Check in 5-block radius

        // Check for nearby water blocks (coarser grid: every 2 blocks)
        for (int x = -checkRadius; x <= checkRadius; x += 2) {
            for (int y = -2; y <= 2; y += 2) {
                for (int z = -checkRadius; z <= checkRadius; z += 2) {
                    BlockPos checkPos = pos.add(x, y, z);
                    Block block = player.getEntityWorld().getBlockState(checkPos).getBlock();

                    // Check if it's water
                    if (block == Blocks.WATER) {
                        double dist = Math.sqrt(x * x + y * y + z * z);
                        // Smooth falloff using quadratic decay (more realistic)
                        double effect = Math.max(0.0, 1.0 - (dist * dist) / (checkRadius * checkRadius * 1.5));
                        humidity -= effect * effect * 2.5; // Water cools efficiently
                    }
                }
            }
        }

        return Math.max(-6.0, humidity);
    }

    /**
     * Get water temperature based on biome
     */
    private static double getWaterTemperature(float biomeTemp, int y) {
        double waterTemp;

        if (biomeTemp < 0.2f) waterTemp = 0.0; // Frozen biome = near freezing
        else if (biomeTemp < 0.5f) waterTemp = 5.0; // Cold biome
        else if (biomeTemp < 1.0f) waterTemp = 12.0; // Cool biome
        else if (biomeTemp < 1.5f) waterTemp = 18.0; // Temperate
        else waterTemp = 25.0; // Warm biome

        // Deep water is colder (thermocline effect)
        int depth = 64 - y;
        if (depth > 20) {
            waterTemp -= Math.min(10.0, (depth - 20) * 0.1);
        }

        return waterTemp;
    }

    /**
     * Armor provides insulation from temperature extremes
     * Better material detection and more realistic modulation
     */
    private static double applyArmorInsulation(ServerPlayerEntity player, double temperature) {
        double comfortTemp = 20.0;
        double insulation = 0.0;
        int armorCount = 0;
        for (int slot = 36; slot < 40; slot++) {
            var stack = player.getInventory().getStack(slot);
            if (stack.isEmpty()) continue;
            armorCount++;
            String itemName = stack.getItem().toString().toLowerCase();
            if (itemName.contains("netherite")) insulation += 0.14; // was 0.22
            else if (itemName.contains("diamond")) insulation += 0.12; // was 0.18
            else if (itemName.contains("leather")) insulation += 0.18; // was 0.25
            else if (itemName.contains("iron")) insulation += 0.08; // was 0.12
            else if (itemName.contains("gold")) insulation += 0.07; // was 0.10
            else if (itemName.contains("chain")) insulation += 0.05; // was 0.08
            else insulation += 0.06; // was 0.10
        }
        insulation *= (1.0 + (armorCount * 0.10)); // was 0.15
        insulation = Math.min(0.55, insulation); // was cap 0.8
        return temperature + (comfortTemp - temperature) * insulation;
    }

    // Stronger interior seal: scan multiple Y layers and percentage of solid coverage
    private static double getShelterFactor(ServerPlayerEntity player, BlockPos pos) {
        var world = player.getEntityWorld();
        if (!world.isSkyVisible(pos)) return 1.0; // fully sheltered underground/indoors
        int roofHits = 0;
        int roofChecks = 0;
        for (int dy = 1; dy <= 6; dy++) {
            BlockState state = world.getBlockState(pos.up(dy));
            if (!state.isAir()) roofHits++;
            roofChecks++;
        }
        int wallHits = 0;
        int wallChecks = 0;
        int radius = 3;
        for (int dy = -1; dy <= 2; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                        BlockState s = world.getBlockState(pos.add(dx, dy, dz));
                        if (!s.isAir()) wallHits++;
                        wallChecks++;
                    }
                }
            }
        }
        double roofCover = roofChecks == 0 ? 0.0 : (double) roofHits / roofChecks; // 0..1
        double wallCover = wallChecks == 0 ? 0.0 : (double) wallHits / wallChecks; // 0..1
        double cover = Math.max(roofCover, wallCover);
        return Math.max(0.0, Math.min(1.0, cover));
    }

    /**
     * Check nearby blocks for heat/cold sources (optimized to prevent spam)
     * More accurate falloff and better heat values for realism
     */
    private static double getNearbyBlockTemp(ServerPlayerEntity player, BlockPos center) {
        double heat = 0.0;
        var world = player.getEntityWorld();
        for (int x = -6; x <= 6; x += 2) {
            for (int y = -3; y <= 3; y += 2) {
                for (int z = -6; z <= 6; z += 2) {
                    BlockPos checkPos = center.add(x, y, z);
                    BlockState state = world.getBlockState(checkPos);
                    Block block = state.getBlock();
                    double distSq = x*x + y*y + z*z;
                    double dist = Math.sqrt(distSq);
                    if (dist <= 0.1) continue;
                    double falloff = Math.max(0, 1.0 - (distSq / 144.0));
                    double falloffSq = falloff * falloff;

                    // HEAT SOURCES (realistic temperature values)
                    if (block == Blocks.LAVA || block == Blocks.LAVA_CAULDRON) {
                        heat += 120.0 * falloffSq * falloffSq; // Extreme heat
                    } else if (block == Blocks.FIRE || block == Blocks.SOUL_FIRE) {
                        heat += 65.0 * falloffSq * falloffSq; // Fire is hot
                    } else if (block instanceof CampfireBlock && state.get(CampfireBlock.LIT)) {
                        heat += 50.0 * falloffSq * falloffSq; // Campfire is warm
                    } else if (block == Blocks.MAGMA_BLOCK) {
                        heat += 60.0 * falloffSq * falloffSq; // Magma is very hot
                    } else if (state.getBlock() instanceof AbstractFurnaceBlock && state.contains(Properties.LIT) && state.get(Properties.LIT)) {
                        heat += 35.0 * falloff; // Lit furnace gives moderate heat
                    } else if (block == Blocks.LANTERN || block == Blocks.SOUL_LANTERN) {
                        heat += 15.0 * falloff; // Lanterns provide gentle warmth
                    } else if (block == Blocks.TORCH || block == Blocks.SOUL_TORCH || block == Blocks.WALL_TORCH) {
                        heat += 10.0 * falloff; // Torches provide slight warmth
                    } else if (block == Blocks.CANDLE) {
                        heat += 6.0 * falloff; // Candles provide minimal warmth
                    } else if (block == Blocks.SCULK_SENSOR) {
                        heat += 2.0 * falloff; // Sculk slightly cool (ambient magic)
                    }

                    // COLD SOURCES (realistic values)
                    else if (block == Blocks.BLUE_ICE) {
                        heat -= 30.0 * falloffSq * falloffSq; // Extreme cold
                    } else if (block == Blocks.ICE || block == Blocks.PACKED_ICE) {
                        heat -= 20.0 * falloffSq * falloffSq; // Strong cold
                    } else if (block == Blocks.SNOW_BLOCK || block == Blocks.POWDER_SNOW) {
                        heat -= 12.0 * falloff; // Moderate cold
                    } else if (block == Blocks.SNOW) {
                        heat -= 8.0 * falloff; // Light cold from snow
                    } else if (block == Blocks.OAK_PLANKS || block == Blocks.SPRUCE_PLANKS || block == Blocks.BIRCH_PLANKS || block == Blocks.DARK_OAK_PLANKS || block == Blocks.JUNGLE_PLANKS || block == Blocks.ACACIA_PLANKS || block == Blocks.CHERRY_PLANKS || block == Blocks.BAMBOO_PLANKS) {
                        heat += 4.0 * falloff; // wood interior warmth
                    } else if (block == Blocks.STONE || block == Blocks.COBBLESTONE || block == Blocks.SMOOTH_STONE || block == Blocks.ANDESITE || block == Blocks.DIORITE || block == Blocks.GRANITE) {
                        heat -= 3.0 * falloff; // stone cools slightly
                    } else if (block instanceof net.minecraft.block.LeavesBlock) {
                        heat -= 2.0 * falloff; // leafy canopy cools a bit
                    } else if (block == Blocks.GLASS || block == Blocks.GLASS_PANE) {
                        heat += 2.0 * falloffSq; // sun through glass warms slightly
                    }
                }
            }
        }
        // Clamp heat within reasonable bounds
        return Math.max(-60.0, Math.min(120.0, heat));
    }

    /**
     * Get stamina drain multiplier based on temperature
     * COLDER = MORE DRAIN (harder to move), HOT = MORE DRAIN (exhausting)
     * COMFORTABLE RANGE = LESS DRAIN (efficient)
     * More granular ranges for better gameplay feel
     */
    public static double getTemperatureStaminaMultiplier(double temperature) {
        // FREEZING (<0°C) - Very hard to move
        if (temperature < -20) {
            return 2.5; // Extreme cold = 2.5x stamina drain
        } else if (temperature < -15) {
            return 2.3; // Severe cold
        } else if (temperature < -10) {
            return 2.0; // Freezing = 2x drain
        } else if (temperature < -5) {
            return 1.8; // Hard freeze
        } else if (temperature < 0) {
            return 1.6; // Very cold = 1.6x drain
        }

        // COLD (0-15°C) - Harder to move
        else if (temperature < 5) {
            return 1.4; // Cold
        } else if (temperature < 10) {
            return 1.2; // Cool
        } else if (temperature < 15) {
            return 1.05; // Cool to comfortable transition
        }

        // COMFORTABLE (15-30°C) - Most efficient!
        else if (temperature < 20) {
            return 0.85; // Perfect comfort = 0.85x drain (best efficiency!)
        } else if (temperature < 25) {
            return 0.95; // Still excellent
        } else if (temperature < 30) {
            return 1.0; // Warm = 1x drain (normal)
        }

        // HOT (30-45°C) - Exhausting
        else if (temperature < 35) {
            return 1.15; // Hot = 1.15x drain
        } else if (temperature < 40) {
            return 1.4; // Very hot = 1.4x drain
        } else if (temperature < 45) {
            return 1.7; // Scorching = 1.7x drain
        }

        // EXTREME HEAT (45-55°C) - Dangerous
        else if (temperature < 50) {
            return 2.0; // Dangerous heat = 2.0x drain
        } else if (temperature < 55) {
            return 2.3; // Extreme heat
        }

        // DEADLY HEAT (>55°C)
        return 2.8; // Heat stroke = 2.8x drain
    }

    /**
     * Get stamina regen multiplier based on temperature
     * WARMER = FASTER REGEN, COLDER = SLOWER REGEN
     * More granular ranges for balanced gameplay
     */
    public static double getTemperatureRegenMultiplier(double temperature) {
        // COLD TEMPS (<10°C) - Slow regen
        if (temperature < -20) {
            return 0.15; // Freezing = 15% regen (very slow)
        } else if (temperature < -15) {
            return 0.2; // Severe cold
        } else if (temperature < -10) {
            return 0.25; // Hard freeze
        } else if (temperature < -5) {
            return 0.35; // Very cold
        } else if (temperature < 0) {
            return 0.45; // Cold
        } else if (temperature < 5) {
            return 0.6; // Still cold
        } else if (temperature < 10) {
            return 0.75; // Cool
        }

        // COMFORTABLE TEMPS (10-30°C) - Normal to excellent regen
        else if (temperature < 15) {
            return 0.95; // Cool to comfortable transition
        } else if (temperature < 20) {
            return 1.0; // Perfect = 100% regen (normal)
        } else if (temperature < 25) {
            return 1.25; // Warm = 125% regen (great!)
        } else if (temperature < 30) {
            return 1.4; // Hot = 140% regen (better regen in warmth)
        }

        // HOT TEMPS (30-40°C) - Still good regen!
        else if (temperature < 35) {
            return 1.6; // Very hot = 160% regen
        } else if (temperature < 40) {
            return 1.7; // Scorching = 170% regen
        }

        // EXTREME HOT (40-50°C) - Starts reducing again
        else if (temperature < 45) {
            return 1.6; // Too hot = back to 160%
        } else if (temperature < 50) {
            return 1.3; // Very dangerous heat
        } else if (temperature < 55) {
            return 1.0; // Dangerous heat = 100%
        }

        // DEADLY HOT (>55°C) - Heat exhaustion
        else if (temperature < 60) {
            return 0.7; // Heat stroke range = 70% regen
        }

        return 0.5; // Extreme heat stress = 50% regen
    }

    /**
     * Get temperature status message
     */
    public static String getTemperatureStatus(double temperature) {
        if (temperature < -10) return "Freezing";
        if (temperature < 5) return "Very Cold";
        if (temperature < 15) return "Cold";
        if (temperature < 25) return "Comfortable";
        if (temperature < 35) return "Warm";
        if (temperature < 45) return "Hot";
        return "Extremely Hot";
    }

    /**
     * Get temperature color for HUD display
     */
    public static int getTemperatureColor(double temperature) {
        if (temperature < 0) return 0xFF88CCFF; // Icy blue
        if (temperature < 15) return 0xFF66AAFF; // Light blue
        if (temperature < 25) return 0xFF66FF66; // Green (comfortable)
        if (temperature < 35) return 0xFFFFDD00; // Yellow
        if (temperature < 45) return 0xFFFF8800; // Orange
        return 0xFFFF3333; // Red (dangerous heat)
    }

    /**
     * Get base world/biome temperature (without player-specific modifiers)
     * Used for HUD display to show environmental conditions
     * This is the "ambient" temperature - what the environment itself is
     */
    public static double getWorldTemperature(ServerPlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        RegistryEntry<Biome> biome = player.getEntityWorld().getBiome(pos);
        float biomeTemp = biome.value().getTemperature();

        String worldKey = player.getEntityWorld().getRegistryKey().getValue().toString();
        long now = System.currentTimeMillis();
        if (worldLastCalcTime.containsKey(worldKey)) {
            long dt = now - worldLastCalcTime.get(worldKey);
            if (dt < 1000 && worldCachedTemperature.containsKey(worldKey)) { // 1s window to further damp jitter
                return worldCachedTemperature.get(worldKey);
            }
        }

        // Base biome temp
        double temperature = convertBiomeToRealTemp(biomeTemp);

        // Dimension overrides
        String dimension = player.getEntityWorld().getRegistryKey().getValue().getPath();
        if (dimension.contains("nether")) {
            return 90.0; // Average nether temp
        } else if (dimension.contains("end")) {
            return -12.0; // Average end temp
        }

        // Time of day ambient (affects world temperature)
        long timeOfDay = player.getEntityWorld().getTimeOfDay() % 24000;
        temperature += getAmbientTempForTime(timeOfDay);

        // Altitude affects world temp
        int y = pos.getY();
        if (y > 64) {
            temperature -= (y - 64) * 0.065;
        } else if (y < 64) {
            temperature += (64 - y) * 0.02;
        }

        // Weather affects world temp
        if (player.getEntityWorld().isRaining()) {
            temperature -= 10.0;
        }
        if (player.getEntityWorld().isThundering()) {
            temperature -= 12.0;
        }

        // Sun exposure (world gets warmer during day even in shade)
        if (timeOfDay > 5000 && timeOfDay < 19000) {
            double sunIntensity = getSunIntensity(timeOfDay);
            // World temp only gets partial sun benefit (ambient heating)
            temperature += sunIntensity * 15.0; // Half of direct sun warmth
        }

        // Smooth abrupt changes to reduce jitter when moving
        double lastWorld = worldCachedTemperature.getOrDefault(worldKey, temperature);
        double maxWorldStep = 0.5; // tighter smoothing per window
        temperature = lastWorld + Math.max(-maxWorldStep, Math.min(maxWorldStep, temperature - lastWorld));

        worldLastCalcTime.put(worldKey, now);
        worldCachedTemperature.put(worldKey, temperature);

        return temperature;
    }

    // Quick helper: weather alert text derived from temp + weather
    public static String getWeatherAlert(ServerPlayerEntity player) {
        double temp = getPlayerTemperature(player);
        boolean raining = player.getEntityWorld().isRaining();
        boolean thundering = player.getEntityWorld().isThundering();
        long timeOfDay = player.getEntityWorld().getTimeOfDay() % 24000;
        boolean skyVisible = player.getEntityWorld().isSkyVisible(player.getBlockPos());

        // Precipitation type
        boolean snowing = raining && temp <= 1.0;

        // Heat/cold alerts take priority
        if (temp >= 40) {
            return thundering ? "Severe heat + storm" : "Heat wave";
        }
        if (temp <= -10) {
            return snowing ? "Blizzard" : "Hard freeze";
        }

        // Storm states
        if (thundering) return snowing ? "Snowstorm" : "Thunderstorm";
        if (raining) return snowing ? "Snow" : "Rain";

        // Night clear and cold
        if (skyVisible && (timeOfDay >= 19000 || timeOfDay <= 5000) && temp < 8) {
            return "Cold night";
        }

        // Calm/clear
        if (temp < 15) return "Cool & clear";
        if (temp < 28) return "Mild & clear";
        return "Warm & clear";
    }

    // Lightweight custom weather info based on our temperature model
    public static class WeatherInfo {
        public final String state;      // e.g., Heatwave, Blizzard, Rain, Snowstorm, Cool & Clear
        public final String alert;      // short alert string
        public final boolean wet;       // player is or will get wet
        public final boolean snowing;   // precipitation is snow
        public final double effectiveTemp; // current player temp (°C)
        public final double windChill;  // computed wind chill component

        public WeatherInfo(String state, String alert, boolean wet, boolean snowing, double effectiveTemp, double windChill) {
            this.state = state;
            this.alert = alert;
            this.wet = wet;
            this.snowing = snowing;
            this.effectiveTemp = effectiveTemp;
            this.windChill = windChill;
        }
    }

    public static WeatherInfo getWeatherInfo(ServerPlayerEntity player) {
        double temp = getPlayerTemperature(player);
        boolean raining = player.getEntityWorld().isRaining();
        boolean thundering = player.getEntityWorld().isThundering();
        long timeOfDay = player.getEntityWorld().getTimeOfDay() % 24000;
        BlockPos pos = player.getBlockPos();
        boolean skyVisible = player.getEntityWorld().isSkyVisible(pos);
        int y = pos.getY();
        double shelter = getShelterFactor(player, pos);

        boolean snowing = raining && temp <= 1.0;

        // approximate wind chill similar to main calc
        double windChill = 0.0;
        if (skyVisible) {
            if (raining) windChill -= 4.0;
            if (thundering) windChill -= 3.0;
            if (y > 90) {
                windChill -= Math.min(6.0, (y - 90) * 0.12);
            }
            windChill *= (1.0 - shelter);
        }

        String state;
        String alert;

        // Priority: extreme temps
        if (temp >= 45) {
            state = "Heatwave";
            alert = thundering ? "Severe heat + storm" : "Extreme heat";
        } else if (temp <= -10) {
            if (snowing) {
                state = "Blizzard";
                alert = "Blizzard conditions";
            } else {
                state = "Hard Freeze";
                alert = "Hard freeze";
            }
        }
        // Storm states
        else if (thundering) {
            state = snowing ? "Snowstorm" : "Thunderstorm";
            alert = state;
        } else if (raining) {
            state = snowing ? "Snow" : "Rain";
            alert = state;
        }
        // Clear but cold/hot bands
        else if (temp < 5) {
            state = "Cold";
            alert = "Cold & clear";
        } else if (temp < 15) {
            state = "Cool";
            alert = "Cool & clear";
        } else if (temp < 28) {
            state = "Mild";
            alert = "Mild & clear";
        } else {
            state = "Warm";
            alert = "Warm & clear";
        }

        // Night-time special note
        if (skyVisible && (timeOfDay >= 19000 || timeOfDay <= 5000) && temp < 8 && !raining && !thundering) {
            alert = "Cold night";
        }

        boolean wet = snowing || raining && skyVisible;

        return new WeatherInfo(state, alert, wet, snowing, temp, windChill);
    }

    /**
     * Effective temperature for gameplay decisions: player temp plus biome modifier.
     */
    public static double getEffectiveTemperature(ServerPlayerEntity player) {
        double base = getPlayerTemperature(player);
        double biomeMod = getBiomeTemperatureModifier(player);
        return base + biomeMod;
    }

    // ============================================================================
    // NEW FEATURES: Temperature Trends, Damage, Biome Effects, Enchantments
    // ============================================================================

    /**
     * Get temperature trend (rising/falling)
     *
     * @return positive = getting warmer, negative = getting colder, 0 = stable
     */
    public static double getTemperatureTrend(ServerPlayerEntity player) {
        return temperatureTrend.getOrDefault(player.getUuid(), 0.0);
    }

    /**
     * Get temperature trend as percentage string for HUD
     *
     * @return "↑ +2.5°C/min" or "↓ -1.2°C/min" or "→ Stable"
     */
    public static String getTemperatureTrendString(ServerPlayerEntity player) {
        double trend = getTemperatureTrend(player);
        if (Math.abs(trend) < 0.1) {
            return "→ Stable";
        } else if (trend > 0) {
            return String.format("↑ +%.1f°C/update", trend);
        } else {
            return String.format("↓ %.1f°C/update", trend);
        }
    }

    /**
     * Calculate temperature damage to player
     * Extreme cold/heat causes damage over time
     *
     * @return damage amount per tick (0 = no damage)
     */
    public static double getTemperatureDamage(double temperature) {
        // No damage in comfortable range
        if (temperature >= -10 && temperature <= 50) {
            return 0.0;
        }

        // Extreme cold damage (-10 and below)
        if (temperature < -35) {
            return 0.5; // -35°C and colder = severe damage (half heart per tick)
        } else if (temperature < -25) {
            return 0.25; // -25°C = moderate damage
        } else if (temperature < -10) {
            return 0.05; // Mild cold damage
        }

        // Extreme heat damage (50°C and above)
        if (temperature > 80) {
            return 0.5; // Extreme heat = severe damage
        } else if (temperature > 65) {
            return 0.25; // Very hot damage
        } else if (temperature > 50) {
            return 0.05; // Heat damage starts mild
        }

        return 0.0;
    }

    /**
     * Biome-specific temperature modifiers
     * Some biomes have special properties
     */
    public static double getBiomeTemperatureModifier(ServerPlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        RegistryEntry<Biome> biome = player.getEntityWorld().getBiome(pos);
        String biomeName = biome.getKey().orElse(null) != null ?
                biome.getKey().get().getValue().getPath() : "unknown";

        // Biome-specific adjustments
        switch (biomeName) {
            case "badlands":
            case "wooded_badlands":
            case "eroded_badlands":
                return 3.0; // Badlands are extra hot due to sun reflection
            case "jungle":
            case "sparse_jungle":
            case "bamboo_jungle":
                return -2.0; // Jungle canopy provides cooling
            case "mangrove_swamp":
                return -1.5; // Swamp humidity cools
            case "windswept_hills":
            case "windswept_gravelly_hills":
                return -2.5; // High altitude wind chill
            case "deep_dark":
                return -5.0; // Underground is very cool
            case "mushroom_fields":
                return 1.0; // Mushroom islands are slightly warmer
            default:
                return 0.0; // No modifier
        }
    }

    /**
     * Enhanced armor insulation with enchantment detection
     * Accounts for enchantment levels (Protection, Frost Walker, etc.)
     */
    private static double applyArmorInsulationWithEnchantments(ServerPlayerEntity player, double temperature) {
        double comfortTemp = 20.0;
        double insulation = 0.0;
        int armorCount = 0;
        double enchantmentBonus = 0.0;

        // Check all armor slots
        for (int slot = 36; slot < 40; slot++) {
            var stack = player.getInventory().getStack(slot);
            if (stack.isEmpty()) continue;

            armorCount++;
            String itemName = stack.getItem().toString().toLowerCase();

            // Material-based insulation values
            if (itemName.contains("netherite")) {
                insulation += 0.22;
            } else if (itemName.contains("diamond")) {
                insulation += 0.18;
            } else if (itemName.contains("leather")) {
                insulation += 0.25;
            } else if (itemName.contains("iron")) {
                insulation += 0.12;
            } else if (itemName.contains("gold")) {
                insulation += 0.10;
            } else if (itemName.contains("chain")) {
                insulation += 0.08;
            } else {
                insulation += 0.10;
            }

            // Check for enchantments that improve temperature stability
            // Protection enchantment helps with temperature extremes
            var enchantments = stack.getEnchantments();
            if (enchantments != null) {
                // Rough enchantment detection
                String enchStr = enchantments.toString().toLowerCase();
                if (enchStr.contains("protection")) {
                    enchantmentBonus += 0.08; // Each enchantment level = more resistance
                }
                if (enchStr.contains("frost")) {
                    enchantmentBonus += 0.15; // Frost Walker/Thorns helps with cold
                }
                if (enchStr.contains("fire")) {
                    enchantmentBonus += 0.10; // Fire Protection helps with heat
                }
            }
        }

        // Apply multiplier for armor count + enchantments
        insulation *= (1.0 + (armorCount * 0.15));
        insulation += enchantmentBonus;
        insulation = Math.min(0.9, insulation); // Cap at 0.9

        return temperature + (comfortTemp - temperature) * insulation;
    }

    /**
     * Apply an external heat delta (±°C) to the player's cached temperature.
     */
    public static void applyExternalHeat(net.minecraft.server.network.ServerPlayerEntity player, double deltaC) {
        java.util.UUID id = player.getUuid();
        Double cached = cachedTemperature.get(id);
        long now = System.currentTimeMillis();
        double current = (cached != null) ? cached : calculateTemperatureInternal(player, now);
        double adjusted = clampTemp(current + deltaC);
        // Smooth: 50% toward adjusted to prevent jumps
        double smoothed = current * 0.5 + adjusted * 0.5;
        cachedTemperature.put(id, smoothed);
        lastCalcTime.put(id, now);
    }

    private static double clampTemp(double t) {
        return Math.max(MIN_TEMPERATURE, Math.min(MAX_TEMPERATURE, t));
    }

    private static double getNearbyEntityTemp(ServerPlayerEntity player) {
        double delta = 0.0;
        var world = player.getEntityWorld();
        var box = player.getBoundingBox().expand(6);
        for (Entity e : world.getOtherEntities(player, box)) {
            EntityType<?> t = e.getType();
            if (t == EntityType.BLAZE) delta += 8.0;
            else if (t == EntityType.STRAY) delta -= 6.0;
            else if (t == EntityType.MAGMA_CUBE) delta += 5.0;
            else if (t == EntityType.ENDERMAN) delta -= 2.0;
            else if (t == EntityType.WITHER) delta += 6.0;
            else if (t == EntityType.POLAR_BEAR) delta -= 4.0;
            else if (t == EntityType.HOGLIN) delta += 2.0;
            else if (t == EntityType.ZOGLIN) delta += 3.0;
            else if (t == EntityType.ZOMBIFIED_PIGLIN) delta += 1.5;
        }
        return Math.max(-15.0, Math.min(15.0, delta));
    }

    // Adaptation-aware multipliers
    public static double getAdaptedStaminaDrainMultiplier(ServerPlayerEntity player) {
        double eff = getEffectiveTemperature(player);
        return ClimateAdaptationSystem.adjustStaminaDrain(eff, player);
    }
    public static double getAdaptedStaminaRegenMultiplier(ServerPlayerEntity player) {
        double eff = getEffectiveTemperature(player);
        return ClimateAdaptationSystem.adjustStaminaRegen(eff, player);
    }
}
