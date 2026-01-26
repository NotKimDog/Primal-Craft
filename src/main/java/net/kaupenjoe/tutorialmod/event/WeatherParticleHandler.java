package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kaupenjoe.tutorialmod.util.WindSystem;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * Spawns ambient particles for custom weather states.
 */
public final class WeatherParticleHandler {
    private static final Random RANDOM = new Random();

    private WeatherParticleHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int tick = server.getTicks();
            // Light frequency: every 5 ticks to reduce load
            if ((tick % 5) != 0) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerWorld world = (ServerWorld) player.getEntityWorld();
                WindSystem.WindData wind = WindSystem.getWindData(world);
                spawnParticlesForWeather(world, player, wind);
            }
        });
    }

    private static void spawnParticlesForWeather(ServerWorld world, ServerPlayerEntity player, WindSystem.WindData wind) {
        WindSystem.CustomWeatherType weather = wind.customWeather;
        ParticleEffect particle;
        int count;
        double speed;

        switch (weather) {
            case BLIZZARD -> {
                particle = ParticleTypes.ITEM_SNOWBALL;
                count = 6;
                speed = 0.02;
            }
            case THUNDERSTORM -> {
                particle = ParticleTypes.CLOUD;
                count = 4;
                speed = 0.03;
            }
            case RAIN -> {
                particle = ParticleTypes.SPLASH;
                count = 4;
                speed = 0.02;
            }
            case WINDY -> {
                particle = ParticleTypes.CLOUD;
                count = 3;
                speed = 0.02;
            }
            case HEATWAVE -> {
                particle = ParticleTypes.ASH;
                count = 3;
                speed = 0.015;
            }
            case DUST_STORM -> {
                particle = ParticleTypes.ASH;
                count = 6;
                speed = 0.03;
            }
            case FOGGY -> {
                particle = ParticleTypes.CLOUD;
                count = 2;
                speed = 0.01;
            }
            default -> {
                return; // CLEAR: no particles
            }
        }

        Vec3d base = new Vec3d(player.getX(), player.getY() + 1.0, player.getZ());
        Vec3d dir = wind.direction.normalize();

        for (int i = 0; i < count; i++) {
            double ox = (RANDOM.nextDouble() - 0.5) * 1.5;
            double oy = (RANDOM.nextDouble()) * 1.2;
            double oz = (RANDOM.nextDouble() - 0.5) * 1.5;
            double dx = dir.x * speed;
            double dy = dir.y * speed * 0.5;
            double dz = dir.z * speed;
            world.spawnParticles(particle,
                    base.x + ox,
                    base.y + oy,
                    base.z + oz,
                    1,
                    0.1, 0.1, 0.1,
                    0.0);
            // Apply a slight motion by re-spawning with speed values as deltas
            world.spawnParticles(particle,
                    base.x + ox,
                    base.y + oy,
                    base.z + oz,
                    0,
                    dx, dy, dz,
                    speed);
        }
    }
}
