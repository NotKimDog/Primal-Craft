package net.kimdog_studios.primal_craft.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-slot item temperatures and computes a player inventory heat contribution.
 * Heuristics are mapping-safe (by item names) and use ambient temperature trend.
 */
public final class InventoryTemperatureSystem {
    private static final Map<UUID, long[]> slotFirstSeen = new HashMap<>(); // per-player per-slot carry start

    private InventoryTemperatureSystem() {}

    /** Update tracking for player inventory once per tick. */
    public static void tick(ServerPlayerEntity player) {
        var inv = player.getInventory();
        long now = player.getEntityWorld().getTime();
        UUID id = player.getUuid();
        long[] seen = slotFirstSeen.computeIfAbsent(id, k -> new long[inv.size()]);

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) { seen[i] = 0L; continue; }
            // Track once a non-empty item appears in the slot
            if (seen[i] == 0L) seen[i] = now;
        }
    }

    /** Estimate temperature of an item stack based on carry duration and ambient temperature. */
    public static double estimateItemTemperature(ServerPlayerEntity player, ItemStack stack, int slotIndex) {
        double ambient = TemperatureSystem.getPlayerTemperature(player);
        UUID id = player.getUuid();
        long[] seen = slotFirstSeen.get(id);
        long now = player.getEntityWorld().getTime();
        long carryTicks = (seen != null && slotIndex >= 0 && slotIndex < seen.length) ? Math.max(0L, now - seen[slotIndex]) : 0L;

        // Base initial temps: liquids start cooler, metals start near ambient, food moderate
        double startTemp = initialItemTempGuess(stack);

        // Approach ambient exponentially; half-life ~ 4 minutes (4800 ticks)
        double tau = 4800.0 / Math.log(2.0);
        double factor = Math.exp(-carryTicks / tau);
        double itemTemp = startTemp * factor + ambient * (1.0 - factor);

        // Special cases: lava bucket is extremely hot
        if (stack.isOf(Items.LAVA_BUCKET)) itemTemp = 900.0;
        if (stack.isOf(Items.WATER_BUCKET)) itemTemp = Math.min(itemTemp, ambient); // water tends to ambient or cooler
        return clamp(itemTemp, -20.0, 950.0);
    }

    /** Compute total inventory heat modifier to apply to player temperature. */
    public static double getInventoryHeatModifier(ServerPlayerEntity player) {
        var inv = player.getInventory();
        double ambient = TemperatureSystem.getPlayerTemperature(player);
        double total = 0.0;
        int count = 0;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            double temp = estimateItemTemperature(player, stack, i);
            double heatContribution = itemHeatContribution(stack, temp, ambient);
            total += heatContribution;
            count++;
        }

        // Normalize: average effect, then dampen for balance
        if (count > 0) total = (total / count) * 0.4;
        return total;
    }

    /** Armor heat contribution: heavier/metal armor warms more in heat and cools less in cold. */
    public static double getArmorHeatModifier(ServerPlayerEntity player) {
        double worldAmbient = TemperatureSystem.getWorldTemperature(player);
        double diffAboveComfort = Math.max(0.0, worldAmbient - 25.0); // starts affecting at 25C instead of 20C
        double diffBelowCool = Math.max(0.0, 12.0 - worldAmbient); // starts affecting at 12C instead of 15C
        double heatScale = Math.min(1.0, diffAboveComfort / 30.0); // ramps from 0 at 25C to 1 at 55C+
        double coolScale = Math.min(1.0, diffBelowCool / 12.0);    // ramps from 0 at 12C to 1 at 0C

        double maxHeat = 0.0;
        for (int slot = 36; slot < 40; slot++) {
            var stack = player.getInventory().getStack(slot);
            if (stack.isEmpty()) continue;
            String name = stack.getItem().toString().toLowerCase();
            double baseHeat; // per-piece max heat contribution at heatScale=1
            if (name.contains("netherite")) baseHeat = 0.12;
            else if (name.contains("diamond")) baseHeat = 0.10;
            else if (name.contains("iron")) baseHeat = 0.08;
            else if (name.contains("gold")) baseHeat = 0.07;
            else if (name.contains("chain")) baseHeat = 0.06;
            else if (name.contains("leather")) baseHeat = 0.04;
            else baseHeat = 0.06;

            double heat = baseHeat * heatScale;

            // In cold ambients let metal conduct a tiny bit of chill instead of heat
            if (heat == 0.0 && diffBelowCool > 0.0) {
                if (!name.contains("leather")) {
                    heat = -0.04 * coolScale; // slight cooling from metal conduction
                } else {
                    heat = -0.02 * coolScale; // leather insulates a bit, less cooling
                }
            }
            // Take max single piece contribution instead of summing all pieces
            if (Math.abs(heat) > Math.abs(maxHeat)) maxHeat = heat;
        }
        // Cap at ±1.0°C max
        return Math.max(-1.0, Math.min(1.0, maxHeat));
    }

    private static double itemHeatContribution(ItemStack stack, double itemTemp, double ambient) {
        double delta = itemTemp - ambient;
        String name = stack.getItem().toString().toLowerCase();
        double weight = 1.0;
        // Heavier and metal/lava items contribute more
        if (name.contains("lava") || stack.isOf(Items.LAVA_BUCKET)) weight = 3.0;
        else if (name.contains("iron") || name.contains("gold") || name.contains("netherite") || name.contains("diamond")) weight = 1.8;
        else if (name.contains("potion") || name.contains("water")) weight = 1.2;
        else if (name.contains("bucket")) weight = 1.5;
        else weight = 0.9;

        return delta * 0.03 * weight; // small per-item contribution
    }

    private static double initialItemTempGuess(ItemStack stack) {
        String name = stack.getItem().toString().toLowerCase();
        if (name.contains("potion") || name.contains("water")) return 15.0;
        if (name.contains("food") || name.contains("meat") || name.contains("berry")) return 20.0;
        if (name.contains("iron") || name.contains("gold") || name.contains("netherite") || name.contains("diamond") || name.contains("chain")) return 22.0;
        if (name.contains("bucket")) return 18.0;
        return 20.0;
    }

    private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
}
