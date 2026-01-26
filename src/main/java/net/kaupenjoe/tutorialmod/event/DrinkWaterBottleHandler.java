package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.kaupenjoe.tutorialmod.util.ThirstSystem;
import net.kaupenjoe.tutorialmod.util.WaterCarryTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public final class DrinkWaterBottleHandler {
    private DrinkWaterBottleHandler() {}

    public static void register() {
        // Track carried water bottles every server tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                WaterCarryTracker.tick(player);
            }
        });

        // Drink handler
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ItemStack stack = serverPlayer.getStackInHand(hand);
            if (stack.isOf(Items.POTION)) {
                String name = stack.getName().getString().toLowerCase();
                boolean isWater = name.contains("water");

                if (isWater) {
                    // Temperature-aware hydration - MORE GENEROUS
                    double waterTemp = WaterCarryTracker.estimateWaterTemperature(serverPlayer, stack);
                    double modifier = WaterCarryTracker.hydrationModifierFor(waterTemp);
                    double baseRestore = 6.0; // Increased from 4.0 - restores 30% of max thirst
                    double restore = baseRestore * modifier;

                    ThirstSystem.addThirst(serverPlayer, restore);

                    // Consume item: replace with empty bottle in survival; keep in creative
                    if (!serverPlayer.isCreative()) {
                        stack.decrement(1);
                        serverPlayer.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
                    }
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }
}
