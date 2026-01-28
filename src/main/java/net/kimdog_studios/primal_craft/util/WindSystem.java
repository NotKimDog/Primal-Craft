package net.kimdog_studios.primal_craft.util;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.registry.tag.BiomeTags;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Dynamic wind system with directional force, gusts, and visual effects
 */
public class WindSystem {
    // Global wind state per world
    private static final Map<String, WindData> worldWindData = new HashMap<>();
    private static final Random random = new Random();

    // Wind update frequency
    private static final int WIND_UPDATE_INTERVAL = 100;
    private static int windTickCounter = 0;
    private static int windUpdates = 0;

    /**
     * Custom weather types with specific behaviors
     */
    public enum CustomWeatherType {
        CLEAR(0.1, 0.4, false, 20.0),
        WINDY(1.2, 2.0, false, 15.0),
        RAIN(1.0, 1.8, false, 10.0),
        THUNDERSTORM(2.0, 3.5, true, 8.0),
        BLIZZARD(2.5, 4.5, true, -15.0),
        HEATWAVE(0.3, 1.0, false, 40.0),
        DUST_STORM(3.5, 5.0, false, 35.0),
        FOGGY(0.05, 0.2, false, 12.0);

        public final double minStrength;
        public final double maxStrength;
        public final boolean isStorm;
        public final double baseTemp;

        CustomWeatherType(double minStrength, double maxStrength, boolean isStorm, double baseTemp) {
            this.minStrength = minStrength;
            this.maxStrength = maxStrength;
            this.isStorm = isStorm;
            this.baseTemp = baseTemp;
        }
    }

    public static class WindData {
        public Vec3d direction;
        public double baseStrength;
        public double gustStrength;
        public long lastUpdate;
        public double temperature;
        public boolean stormy;
        public CustomWeatherType customWeather;

        public WindData() {
            this.direction = new Vec3d(1, 0, 0);
            this.baseStrength = 0.3;
            this.gustStrength = 1.0;
            this.lastUpdate = System.currentTimeMillis();
            this.temperature = 20.0;
            this.stormy = false;
            this.customWeather = CustomWeatherType.CLEAR;
        }

        public double getEffectiveStrength() {
            double effective = baseStrength * gustStrength;
            if (stormy) effective *= 3.0;
            if (customWeather == CustomWeatherType.BLIZZARD || customWeather == CustomWeatherType.DUST_STORM) {
                effective *= 2.5;
            }
            return Math.min(effective, 15.0);
        }

        public Vec3d getWindForce() {
            return direction.multiply(getEffectiveStrength());
        }
    }

    /**
     * Update global wind for a world
     */
    public static void updateWind(ServerWorld world, int serverTick) {
        String worldKey = world.getRegistryKey().getValue().toString();
        WindData wind = worldWindData.computeIfAbsent(worldKey, k -> new WindData());

        // Update base strength (weather-dependent)
        boolean raining = world.isRaining();
        boolean thundering = world.isThundering();
        wind.stormy = thundering;

        updateCustomWeather(wind, raining, thundering);

        windTickCounter++;
        if (windTickCounter >= WIND_UPDATE_INTERVAL) {
            windTickCounter = 0;
            windUpdates++;

            PrimalCraft.LOGGER.debug("💨 [WIND_UPDATE] Event #{} - World: {}", windUpdates, worldKey);
            PrimalCraft.LOGGER.trace("   ├─ Weather: Rain={}, Thunder={}, Custom={}",
                raining, thundering, wind.customWeather.name());
            PrimalCraft.LOGGER.trace("   ├─ Strength: {} (base: {}, gust: {})",
                String.format("%.2f", wind.getEffectiveStrength()),
                String.format("%.2f", wind.baseStrength),
                String.format("%.2f", wind.gustStrength));
            PrimalCraft.LOGGER.trace("   └─ Temperature: {}°C", String.format("%.1f", wind.temperature));

            // Gradual wind direction shift
            double angleShift = (random.nextDouble() - 0.5) * 0.3;
            Vec3d current = wind.direction;
            double currentAngle = Math.atan2(current.z, current.x);
            double newAngle = currentAngle + angleShift;

            wind.direction = new Vec3d(
                Math.cos(newAngle),
                (random.nextDouble() - 0.5) * 0.1,
                Math.sin(newAngle)
            ).normalize();
        }

        // Update gusts every tick (creates turbulence)
        if (serverTick % 20 == 0) {
            // Update weather-based strength based on custom weather
            CustomWeatherType weather = wind.customWeather;
            wind.baseStrength = weather.minStrength + random.nextDouble() * (weather.maxStrength - weather.minStrength);

            // Smooth gust transitions - INCREASED RANGE
            double targetGust = 0.5 + random.nextDouble() * 2.0;
            wind.gustStrength += (targetGust - wind.gustStrength) * 0.2;
        }

        wind.lastUpdate = System.currentTimeMillis();
    }

    /**
     * Auto-detect and set custom weather type based on vanilla weather + temperature
     */
    private static void updateCustomWeather(WindData wind, boolean raining, boolean thundering) {
        double temp = wind.temperature;

        if (thundering) {
            // Thunder + cold = blizzard
            if (temp < 5.0) {
                wind.customWeather = CustomWeatherType.BLIZZARD;
            } else {
                wind.customWeather = CustomWeatherType.THUNDERSTORM;
            }
        } else if (raining) {
            // Rain + cold = snow/blizzard
            if (temp < 1.0) {
                wind.customWeather = CustomWeatherType.BLIZZARD;
            } else {
                wind.customWeather = CustomWeatherType.RAIN;
            }
        } else {
            // Clear weather - check temperature
            if (temp > 38.0) {
                wind.customWeather = CustomWeatherType.HEATWAVE;
            } else if (temp < 8.0) {
                wind.customWeather = CustomWeatherType.FOGGY;
            } else {
                wind.customWeather = CustomWeatherType.CLEAR;
            }
        }

        wind.stormy = wind.customWeather.isStorm;
    }

    /**
     * Manually set custom weather for a world
     */
    public static void setCustomWeather(ServerWorld world, CustomWeatherType weatherType) {
        String worldKey = world.getRegistryKey().getValue().toString();
        WindData wind = worldWindData.computeIfAbsent(worldKey, k -> new WindData());

        wind.customWeather = weatherType;
        wind.baseStrength = weatherType.minStrength + random.nextDouble() * (weatherType.maxStrength - weatherType.minStrength);
        wind.stormy = weatherType.isStorm;
        wind.temperature = weatherType.baseTemp;

        // Set vanilla weather to match
        switch (weatherType) {
            case THUNDERSTORM, BLIZZARD -> {
                world.setWeather(0, 6000, true, true);
            }
            case RAIN -> {
                world.setWeather(0, 6000, true, false);
            }
            default -> {
                world.setWeather(6000, 0, false, false);
            }
        }

        wind.lastUpdate = System.currentTimeMillis();
    }

    /**
     * Force immediate wind update for a world (useful for weather command responses)
     */
    public static void forceWindUpdate(ServerWorld world) {
        String worldKey = world.getRegistryKey().getValue().toString();
        WindData wind = worldWindData.computeIfAbsent(worldKey, k -> new WindData());

        boolean raining = world.isRaining();
        boolean thundering = world.isThundering();
        wind.stormy = thundering;

        if (thundering) {
            wind.baseStrength = 0.8;
        } else if (raining) {
            wind.baseStrength = 0.6;
        } else {
            wind.baseStrength = 0.3;
        }

        wind.lastUpdate = System.currentTimeMillis();
    }

    /**
     * Get wind data for a world
     */
    public static WindData getWindData(World world) {
        String worldKey = world.getRegistryKey().getValue().toString();
        return worldWindData.computeIfAbsent(worldKey, k -> new WindData());
    }

    /**
     * Get wind force at a specific position (altitude and exposure affect strength)
     */
    public static Vec3d getWindForceAtPosition(World world, BlockPos pos, boolean isExposed) {
        WindData wind = getWindData(world);
        Vec3d baseForce = wind.getWindForce();

        // Altitude amplifies wind (stronger at height)
        int y = pos.getY();
        double altitudeMultiplier = 1.0;
        if (y > 100) {
            altitudeMultiplier = 1.0 + Math.min((y - 100) * 0.01, 1.5); // Up to 2.5x at y=250
        } else {
            int sea = world.getSeaLevel();
            if (y < sea - 15) {
                altitudeMultiplier *= 0.7; // Deep underground = calmer air pockets
            } else if (y < sea) {
                altitudeMultiplier *= 0.85; // Slightly damped below sea level
            }
        }

        // Coastal/ocean proximity boosts wind like open seas
        double oceanMultiplier = isNearOceanOrCoast(world, pos) ? 1.35 : 1.0;

        // Exposure: sheltered areas get less wind using structural scan instead of binary flag
        double exposureMultiplier = computeExposureFactor(world, pos);
        if (!isExposed) {
            exposureMultiplier *= 0.35; // Sky not visible but still some drafts
        }

        // Terrain shielding and funneling based on upwind obstacles
        double terrainMultiplier = computeTerrainShelterMultiplier(world, pos, wind.direction);

        // Thermal updrafts/downdrafts from nearby blocks (lava/fire up, ice/snow down)
        double thermalLift = computeThermalDraft(world, pos);
        baseForce = baseForce.add(0, thermalLift, 0);

        return baseForce.multiply(altitudeMultiplier * exposureMultiplier * oceanMultiplier * terrainMultiplier);
    }

    // Lightweight check to see if player is in/near ocean or beach
    private static boolean isNearOceanOrCoast(World world, BlockPos pos) {
        var biomeEntry = world.getBiome(pos);
        if (biomeEntry.isIn(BiomeTags.IS_OCEAN) || biomeEntry.isIn(BiomeTags.IS_BEACH)) {
            return true;
        }

        int seaY = world.getSeaLevel();
        int radius = 12; // short reach to detect coastline without heavy scans
        // Sample four cardinal points at sea level for water
        if (!world.getFluidState(new BlockPos(pos.getX() + radius, seaY, pos.getZ())).isEmpty()) return true;
        if (!world.getFluidState(new BlockPos(pos.getX() - radius, seaY, pos.getZ())).isEmpty()) return true;
        if (!world.getFluidState(new BlockPos(pos.getX(), seaY, pos.getZ() + radius)).isEmpty()) return true;
        if (!world.getFluidState(new BlockPos(pos.getX(), seaY, pos.getZ() - radius)).isEmpty()) return true;

        return false;
    }

    // Estimate how exposed a position is: 1 = open sky, 0.1 = well-sealed room
    private static double computeExposureFactor(World world, BlockPos pos) {
        int samples = 0;
        int blocked = 0;

        // Scan a small 3x3 column for solid blocks up to 3 blocks above player
        for (int dy = 0; dy <= 3; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos check = pos.add(dx, dy, dz);
                    samples++;
                    if (world.getBlockState(check).isOpaqueFullCube()) {
                        blocked++;
                    }
                }
            }
        }

        double openFraction = samples == 0 ? 1.0 : 1.0 - ((double) blocked / samples);
        openFraction = Math.max(0.1, Math.min(1.0, openFraction));

        // Extra roof penalty if a solid block sits directly above within 3 blocks
        boolean roofed = false;
        for (int dy = 1; dy <= 3 && !roofed; dy++) {
            BlockPos above = pos.up(dy);
            if (world.getBlockState(above).isOpaqueFullCube()) {
                roofed = true;
            }
        }

        if (roofed) {
            openFraction *= 0.35; // Porches feel breezy, sealed rooms mostly block wind
        }

        return openFraction;
    }

    // Upwind shelter (lee side) and canyon funnel detection
    private static double computeTerrainShelterMultiplier(World world, BlockPos pos, Vec3d windDir) {
        Vec3d dir = windDir.normalize();
        BlockPos.Mutable probe = new BlockPos.Mutable();

        // Check a short line upwind for solid obstacles at head height
        int hits = 0;
        for (int i = 1; i <= 6; i++) {
            probe.set(pos.getX() - Math.round((float)(dir.x * i)), pos.getY() + 1, pos.getZ() - Math.round((float)(dir.z * i)));
            if (world.getBlockState(probe).isOpaqueFullCube()) {
                hits++;
            }
        }
        double leeMultiplier = Math.max(0.5, 1.0 - hits * 0.08); // more hits = more shelter

        // Canyon/funnel boost: walls on both sides channel wind
        Vec3d left = new Vec3d(-dir.z, 0, dir.x);
        int wallPairs = 0;
        for (int d = 1; d <= 3; d++) {
            probe.set(pos.getX() + Math.round((float)(left.x * d)), pos.getY() + 1, pos.getZ() + Math.round((float)(left.z * d)));
            boolean leftWall = world.getBlockState(probe).isOpaqueFullCube();
            probe.set(pos.getX() - Math.round((float)(left.x * d)), pos.getY() + 1, pos.getZ() - Math.round((float)(left.z * d)));
            boolean rightWall = world.getBlockState(probe).isOpaqueFullCube();
            if (leftWall && rightWall) {
                wallPairs++;
            }
        }
        double funnelMultiplier = 1.0 + Math.min(wallPairs * 0.12, 0.30); // up to +30%

        return leeMultiplier * funnelMultiplier;
    }

    // Thermal/lift from nearby heat or cold sources
    private static double computeThermalDraft(World world, BlockPos pos) {
        double lift = 0.0;

        // Hot sources below -> updraft
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos below = pos.down(dy);
            var state = world.getBlockState(below);
            if (state.isOf(Blocks.LAVA) || state.isOf(Blocks.LAVA_CAULDRON)) lift += 0.08;
            if (state.isOf(Blocks.FIRE) || state.isOf(Blocks.CAMPFIRE) || state.isOf(Blocks.SOUL_CAMPFIRE)) lift += 0.05;
            if (state.isOf(Blocks.MAGMA_BLOCK)) lift += 0.04;
        }

        // Cold sources around -> downdraft
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos check = pos.up(dy);
            var state = world.getBlockState(check);
            if (state.isOf(Blocks.SNOW_BLOCK) || state.isOf(Blocks.PACKED_ICE) || state.isOf(Blocks.BLUE_ICE) || state.isOf(Blocks.ICE)) {
                lift -= 0.03;
            }
        }

        return Math.max(-0.12, Math.min(0.12, lift));
    }

    /**
     * Apply wind force to player with enhanced effects
     */
    public static void applyWindToPlayer(ServerPlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        boolean skyVisible = player.getEntityWorld().isSkyVisible(pos);

        // Only apply if exposed to sky and not in water/lava
        if (!skyVisible || player.isTouchingWater() || player.isInLava()) {
            return;
        }

        WindData wind = getWindData(player.getEntityWorld());
        double exposureFactor = computeExposureFactor(player.getEntityWorld(), pos);
        Vec3d windForce = getWindForceAtPosition(player.getEntityWorld(), pos, true);
        double windStrength = wind.getEffectiveStrength();

        // Base wind push strength - scales with wind speed (faster wind = stronger push)
        double baseScale = 0.10;
        double scale = baseScale * (1.0 + windStrength * 0.3);

        // State-based multipliers (also scale with wind)
        if (!player.isOnGround()) {
            baseScale = 0.35;
            scale = baseScale * (1.0 + windStrength * 0.4);
        } else if (player.isSneaking()) {
            baseScale = 0.015;
            scale = baseScale * (1.0 + windStrength * 0.2);
        } else if (player.isSprinting()) {
            baseScale = 0.15;
            scale = baseScale * (1.0 + windStrength * 0.35);
        }

        // Armor drag reduces push but costs stamina when fighting wind
        double drag = computeArmorDragFactor(player);
        scale *= drag;

        // Strong winds (>1.5) cause additional effects
        if (windStrength > 1.5) {
            // Stamina drain from fighting wind (every tick in strong wind)
            if (player.isSprinting() || !player.isOnGround()) {
                Vec3d playerVel = player.getVelocity();
                if (playerVel.lengthSquared() > 0) { // Check if moving
                    double dotProduct = playerVel.normalize().dotProduct(windForce.normalize());
                    if (dotProduct < -0.3) { // Moving against wind
                        double staminaDrain = (windStrength - 1.5) * 0.3;
                        // Heavier armor -> more effort against wind
                        staminaDrain *= (1.0 + (1.0 - drag));
                        StaminaSystem.tryConsume(player, staminaDrain);
                    }
                }
            }

            // Hunger/exhaustion from fighting wind
            if (player.age % 40 == 0 && player.isSprinting()) { // Every 2 seconds
                player.addExhaustion((float)(windStrength * 0.05));
            }

            // Extreme winds (>2.0) cause more severe effects
            if (windStrength > 2.0) {
                // Occasional knockback from wind gusts - VERY FREQUENT
                if (random.nextDouble() < 0.12 && !player.isSneaking()) { // Increased from 0.05
                    Vec3d knockback = windForce.normalize().multiply(windStrength * 0.40); // Increased from 0.25
                    player.addVelocity(knockback.x, Math.min(knockback.y + 0.15, 0.5), knockback.z); // Increased Y component
                    player.velocityDirty = true;
                }

                // Slower health regeneration in extreme wind
                if (player.age % 100 == 0) {
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.HUNGER, 60, 0, true, false));
                }
            }


            // Temperature effect: wind chill in cold weather
            double currentTemp = TemperatureSystem.getPlayerTemperature(player);
            if (currentTemp < 15.0 && player.age % 20 == 0) { // Every second
                double windChill = -(windStrength - 1.5) * 0.4;
                TemperatureSystem.applyExternalHeat(player, windChill);
            }

            // Heatwave wind increases temperature
            if (wind.customWeather == CustomWeatherType.HEATWAVE && player.age % 20 == 0) {
                double heatGain = windStrength * 0.3;
                TemperatureSystem.applyExternalHeat(player, heatGain);
            }
        }

        // Direction-based effects
        Vec3d playerFacing = player.getRotationVector();
        double facingDot = playerFacing.dotProduct(windForce.normalize());

        // Headwind slows, tailwind speeds up
        if (facingDot < -0.5 && player.isSprinting()) {
            // Strong headwind - slow down
            scale *= 1.8; // Increased from 1.5
        } else if (facingDot > 0.5 && player.isSprinting()) {
            // Tailwind - speed boost
            scale *= 0.7; // Increased from 0.8 (less resistance)
            player.addVelocity(windForce.x * 0.05, 0, windForce.z * 0.05); // Increased from 0.02
        }

        // Apply main wind push
        Vec3d push = windForce.multiply(scale);
        player.setVelocity(player.getVelocity().add(push));
        player.velocityDirty = true;

        // Gusty winds cause random wobble - MUCH MORE AGGRESSIVE
        if (windStrength > 1.0 && random.nextDouble() < 0.15) { // Increased from 0.08
            double gustPush = windStrength * 0.08 * (random.nextDouble() - 0.5); // Increased from 0.04
            player.addVelocity(gustPush, 0, gustPush);
        }

        // Play wind sound effects based on strength
        if (player.age % 60 == 0) { // Every 3 seconds
            ServerWorld world = (ServerWorld) player.getEntityWorld();
            if (windStrength > 2.0) {
                world.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sound.SoundEvents.ENTITY_BREEZE_WIND_BURST,
                    net.minecraft.sound.SoundCategory.WEATHER,
                    (float)(windStrength * 0.3),
                    0.8f + random.nextFloat() * 0.4f
                );
            } else if (windStrength > 1.0) {
                world.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sound.SoundEvents.ITEM_ELYTRA_FLYING,
                    net.minecraft.sound.SoundCategory.WEATHER,
                    (float)(windStrength * 0.2),
                    1.0f + random.nextFloat() * 0.2f
                );
            }
        }

        // Weather-specific effects
        if (wind.customWeather == CustomWeatherType.DUST_STORM) {
            // Dust storm: reduced visibility and occasional blindness
            if (random.nextDouble() < 0.15) {
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.BLINDNESS, 60, 0, true, false));
            }
            // Nausea from dust
            if (random.nextDouble() < 0.05) {
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.NAUSEA, 80, 0, true, false));
            }
        }

        if (wind.customWeather == CustomWeatherType.BLIZZARD) {
            // Blizzard: slowness and mining fatigue from extreme cold + wind
            if (windStrength > 1.5 && random.nextDouble() < 0.1) {
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.SLOWNESS, 100, 1, true, false));
            }
            if (windStrength > 2.0 && random.nextDouble() < 0.08) {
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.MINING_FATIGUE, 100, 0, true, false));
            }
        }
    }

    // Armor drag helper: heavier gear resists push but tires you
    private static double computeArmorDragFactor(ServerPlayerEntity player) {
        int pieces = 0;
        if (!player.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) pieces++;
        if (!player.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) pieces++;
        if (!player.getEquippedStack(EquipmentSlot.LEGS).isEmpty()) pieces++;
        if (!player.getEquippedStack(EquipmentSlot.FEET).isEmpty()) pieces++;

        double drag = 1.0 - pieces * 0.08; // each piece trims push a bit
        return Math.max(0.55, drag);
    }

    /**
     * Get wind particle spawn data for rendering
     */
    public static WindParticleData getWindParticleData(World world, Vec3d playerPos) {
        WindData wind = getWindData(world);
        return new WindParticleData(
            wind.direction,
            wind.getEffectiveStrength(),
            wind.stormy
        );
    }

    public static class WindParticleData {
        public final Vec3d direction;
        public final double strength;
        public final boolean stormy;

        public WindParticleData(Vec3d direction, double strength, boolean stormy) {
            this.direction = direction;
            this.strength = strength;
            this.stormy = stormy;
        }
    }
}

