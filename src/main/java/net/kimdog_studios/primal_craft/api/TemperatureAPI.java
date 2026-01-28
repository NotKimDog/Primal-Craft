package net.kimdog_studios.primal_craft.api;

import net.kimdog_studios.primal_craft.util.InventoryTemperatureSystem;
import net.kimdog_studios.primal_craft.util.TemperatureSystem;
import net.kimdog_studios.primal_craft.util.WaterCarryTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Public API for temperature systems. Use this to integrate mechanics without touching internals.
 */
public final class TemperatureAPI {
    private TemperatureAPI() {}

    // Query inventory/armor heat modifiers
    public static double getInventoryHeatModifier(ServerPlayerEntity player) {
        return InventoryTemperatureSystem.getInventoryHeatModifier(player);
    }

    public static double getArmorHeatModifier(ServerPlayerEntity player) {
        return InventoryTemperatureSystem.getArmorHeatModifier(player);
    }

    // Estimate temperatures
    public static double estimateItemTemperature(ServerPlayerEntity player, ItemStack stack, int slotIndex) {
        return InventoryTemperatureSystem.estimateItemTemperature(player, stack, slotIndex);
    }

    public static double estimateWaterTemperature(ServerPlayerEntity player, ItemStack stack) {
        return WaterCarryTracker.estimateWaterTemperature(player, stack);
    }

    // Hydration modifier based on water temperature
    public static double hydrationModifierFor(double waterTemp) {
        return WaterCarryTracker.hydrationModifierFor(waterTemp);
    }

    // Player temperature queries
    public static double getPlayerTemperature(ServerPlayerEntity player) {
        return TemperatureSystem.getPlayerTemperature(player);
    }

    public static double getEffectiveTemperature(ServerPlayerEntity player) {
        return TemperatureSystem.getEffectiveTemperature(player);
    }

    /**
     * Apply external heat delta (±°C) to the player's temperature cache.
     * This nudges the smoothed temperature toward the adjusted value.
     */
    public static void applyExternalHeat(ServerPlayerEntity player, double deltaC) {
        TemperatureSystem.applyExternalHeat(player, deltaC);
    }
}
