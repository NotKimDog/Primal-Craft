package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Client-side wind line renderer with anime-style streaks
 */
public class WindLineRenderer {
    private static final List<WindLine> windLines = new ArrayList<>();
    private static final Random random = new Random();
    private static Vec3d windDirection = new Vec3d(1, 0, 0);
    private static double windStrength = 0.5;
    private static boolean stormy = false;
    private static int spawnTimer = 0;

    public static class WindLine {
        public Vec3d startPos;
        public Vec3d velocity;
        public float alpha;
        public float length;
        public int age;
        public int maxAge;
        public int color;

        public WindLine(Vec3d startPos, Vec3d velocity, float length, int maxAge, int color) {
            this.startPos = startPos;
            this.velocity = velocity;
            this.alpha = 0f;
            this.length = length;
            this.age = 0;
            this.maxAge = maxAge;
            this.color = color;
        }

        public boolean update() {
            age++;

            // Move line
            startPos = startPos.add(velocity);

            // Fade in/out
            if (age < 10) {
                alpha = age / 10f;
            } else if (age > maxAge - 10) {
                alpha = (maxAge - age) / 10f;
            } else {
                alpha = 1f;
            }

            return age < maxAge;
        }

        public Vec3d getEndPos() {
            return startPos.add(velocity.normalize().multiply(length));
        }
    }

    public static void register() {
        // Update and render on HUD (runs every frame)
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            updateWindLines(client);
            renderWindLines(client);
        });
    }

    public static void updateWindData(Vec3d direction, double strength, boolean isStormy) {
        windDirection = direction;
        windStrength = strength;
        stormy = isStormy;
    }

    private static void updateWindLines(MinecraftClient client) {
        // Update existing lines
        Iterator<WindLine> iterator = windLines.iterator();
        while (iterator.hasNext()) {
            WindLine line = iterator.next();
            if (!line.update()) {
                iterator.remove();
            }
        }

        // Spawn new wind lines
        spawnTimer++;
        int spawnRate = stormy ? 2 : (int)(6 - windStrength * 3); // More lines when windier

        if (spawnTimer >= Math.max(1, spawnRate)) {
            spawnTimer = 0;
            spawnWindLines(client);
        }
    }

    private static void spawnWindLines(MinecraftClient client) {
        Vec3d playerPos = client.player.getEyePos();

        // Spawn multiple lines in a radius around player
        int count = stormy ? 8 : (int)(3 + windStrength * 5);

        for (int i = 0; i < count; i++) {
            // Random spawn position around player (30 block radius)
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 10 + random.nextDouble() * 20;
            double height = (random.nextDouble() - 0.5) * 20;

            Vec3d spawnPos = playerPos.add(
                Math.cos(angle) * distance,
                height,
                Math.sin(angle) * distance
            );

            // Wind velocity (based on global wind direction)
            Vec3d velocity = windDirection
                .multiply(0.3 + windStrength * 0.5)
                .add(
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.05,
                    (random.nextDouble() - 0.5) * 0.1
                );

            // Line properties
            float length = (float)(1.5 + windStrength * 2.5);
            int maxAge = (int)(20 + random.nextInt(40));

            // Color based on weather
            int color;
            if (stormy) {
                color = 0xCCEEFF; // Bright white-blue for storms
            } else {
                color = 0xDDEEFF; // Soft white for normal wind
            }

            windLines.add(new WindLine(spawnPos, velocity, length, maxAge, color));
        }
    }

    private static void renderWindLines(MinecraftClient client) {
        // TODO: Implement custom wind particle renderer with proper 1.21 rendering API
        // For now wind physics work, visual lines disabled until proper particle system
        if (windLines.isEmpty()) return;

        // Rendering temporarily disabled - wind physics still active
        // Will be implemented with custom particles in next update
    }

    public static void clear() {
        windLines.clear();
    }
}
