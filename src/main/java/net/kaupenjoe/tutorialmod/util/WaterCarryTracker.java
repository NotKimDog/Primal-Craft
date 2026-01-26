package net.kaupenjoe.tutorialmod.util;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks carried water bottles per player and estimates their current temperature
 * based on ambient temperature and carry duration.
 */
public final class WaterCarryTracker {
    private static final Map<UUID, long[]> slotFirstSeen = new HashMap<>(); // per-player per-slot timestamp

    private WaterCarryTracker() {}

    /**
     * Call every tick to update carry times for water bottles in inventory.
     */
    public static void tick(ServerPlayerEntity player) {
        var inv = player.getInventory();
        UUID id = player.getUuid();
        long[] seen = slotFirstSeen.computeIfAbsent(id, k -> new long[inv.size()]);
        long now = player.getEntityWorld().getTime(); // server time in ticks

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(Items.POTION)) {
                // Fallback detection by display name (mapping-safe)
                String name = stack.getName().getString().toLowerCase();
                if (name.contains("water")) {
                    if (seen[i] == 0L) seen[i] = now; // mark first time we saw this slot holding water
                } else {
                    seen[i] = 0L; // not water
                }
            } else {
                seen[i] = 0L; // empty or non-potion
            }
        }
    }

    /**
     * Estimate the water bottle temperature when drinking from a hand stack.
     * If the stack came from a slot tracked for some time, use carry duration; otherwise fall back to ambient.
     */
    public static double estimateWaterTemperature(ServerPlayerEntity player, ItemStack stack) {
        double ambient = TemperatureSystem.getPlayerTemperature(player);
        UUID id = player.getUuid();
        long[] seen = slotFirstSeen.get(id);
        long now = player.getEntityWorld().getTime();
        long carryTicks = 0L;

        if (seen != null) {
            // Try to match the stack by searching inventory for same object reference
            var inv = player.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i) == stack && seen[i] > 0L) {
                    carryTicks = now - seen[i];
                    break;
                }
            }
        }

        // Model: water tends toward ambient temp over time – exponential approach
        // Start at 15°C (cool bottle), approach ambient with half-life ~ 5 minutes (~6000 ticks)
        double startTemp = 15.0;
        double tau = 6000.0 / Math.log(2.0); // time constant from half-life
        double factor = Math.exp(-carryTicks / tau);
        double waterTemp = startTemp * factor + ambient * (1.0 - factor);

        // Environmental influence: ice under water cools, lava/magma nearby heats
        var world = player.getEntityWorld();
        BlockPos pos = player.getBlockPos();

        // If player is submerged in water and standing above ice variants, cool strongly
        if (player.isSubmergedInWater()) {
            BlockPos below = pos.down();
            var belowState = world.getBlockState(below);
            if (belowState.isOf(Blocks.ICE) || belowState.isOf(Blocks.PACKED_ICE) || belowState.isOf(Blocks.BLUE_ICE) || belowState.isOf(Blocks.FROSTED_ICE)) {
                waterTemp -= 12.0; // very cold due to ice under water
            }
        }

        // Heat from nearby lava or magma within a small radius
        int radius = 4;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 2; dy++) { // around feet level
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos check = pos.add(dx, dy, dz);
                    var state = world.getBlockState(check);
                    if (state.isOf(Blocks.LAVA)) {
                        waterTemp += 18.0; // strong heating near lava
                    } else if (state.isOf(Blocks.MAGMA_BLOCK)) {
                        waterTemp += 6.0; // mild heating near magma
                    }
                }
            }
        }

        return clamp(waterTemp, -20.0, 95.0);
    }

    private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }

    /**
     * Compute thirst restoration modifier based on water temperature.
     * Cooler (10–20°C) hydrates best; very hot (>35°C) hydrates worse.
     */
    public static double hydrationModifierFor(double waterTemp) {
        if (waterTemp < 5) return 1.05; // very cold – slight bonus
        if (waterTemp < 10) return 1.10; // cold – bonus
        if (waterTemp < 20) return 1.15; // cool – best hydration
        if (waterTemp < 28) return 1.00; // moderate – normal
        if (waterTemp < 35) return 0.90; // warm – slight penalty
        if (waterTemp < 45) return 0.75; // hot – penalty
        return 0.60; // very hot – poor hydration
    }
}
