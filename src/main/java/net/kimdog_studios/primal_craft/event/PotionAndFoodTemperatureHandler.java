package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.kimdog_studios.primal_craft.api.TemperatureAPI;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public final class PotionAndFoodTemperatureHandler {
    private PotionAndFoodTemperatureHandler() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;

            ItemStack stack = sp.getStackInHand(hand);
            Item item = stack.getItem();

            // Potions: adjust minor effects based on temperature
            if (item == Items.POTION) {
                double temp = TemperatureAPI.estimateItemTemperature(sp, stack, findSlotIndex(sp, stack));
                double mod = tempBucketModifier(temp);
                if (mod > 1.0) {
                    sp.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, (int)(40 * (mod - 1.0)), 0, false, false));
                } else if (mod < 1.0) {
                    sp.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, (int)(20 * (1.0 - mod)), 0, false, false));
                }
                return ActionResult.PASS;
            }

            // Food: apply small status effect scaled by item temperature (temporarily disabled)
            // boolean isFood = stack.get(net.minecraft.component.DataComponentTypes.FOOD) != null;
            // if (isFood) {
            //     double temp = net.kimdog_studios.primal_craft.api.TemperatureAPI.estimateItemTemperature(sp, stack, findSlotIndex(sp, stack));
            //     double mod = tempBucketModifier(temp);
            //     int duration = (int)Math.max(0, Math.round(20 * (0.6 * mod)));
            //     if (duration > 0) {
            //         sp.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SPEED, duration, 0, false, false));
            //     }
            //     return net.minecraft.util.ActionResult.PASS;
            // }

            return ActionResult.PASS;
        });
    }

    private static int findSlotIndex(ServerPlayerEntity sp, ItemStack target) {
        var inv = sp.getInventory();
        for (int i = 0; i < inv.size(); i++) if (inv.getStack(i) == target) return i;
        return -1;
    }

    private static double tempBucketModifier(double t) {
        if (t < 10) return 1.10; // cold
        if (t < 20) return 1.05; // cool
        if (t < 28) return 1.00; // moderate
        if (t < 35) return 0.92; // warm
        return 0.80; // hot
    }
}
