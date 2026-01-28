package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.kimdog_studios.primal_craft.item.ModItems;

/**
 * Sleep Overhaul - custom rules: must wear pajamas (top and bottoms) and have an empty inventory to sleep.
 */
public class SleepOverhaulHandler {
    public static void register() {
        // Enforce rules before sleep begins
        EntitySleepEvents.ALLOW_SLEEPING.register((entity, pos) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return null;

            if (!isWearingSleepPajamas(player)) {
                player.sendMessage(Text.literal("You must wear pajamas to sleep (top and bottoms)."), true);
                return PlayerEntity.SleepFailureReason.NOT_SAFE;
            }

            if (!isInventoryEmptyForSleep(player)) {
                player.sendMessage(Text.literal("Empty your inventory (only pajamas may be worn)."), true);
                return PlayerEntity.SleepFailureReason.NOT_SAFE;
            }

            return null; // allow sleep
        });

        // Failsafe: if player somehow starts sleeping without meeting rules, kick them out
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.isSleeping() && (!isWearingSleepPajamas(player) || !isInventoryEmptyForSleep(player))) {
                    player.wakeUp();
                    player.sendMessage(Text.literal("Sleep cancelled: wear pajamas and empty inventory."), true);
                }
            }
        });
    }

    private static boolean isWearingHeadItem(ServerPlayerEntity player) {
        return !player.getEquippedStack(EquipmentSlot.HEAD).isEmpty();
    }

    private static boolean isWearingSleepPajamas(ServerPlayerEntity player) {
        var chest = player.getEquippedStack(EquipmentSlot.CHEST);
        var legs = player.getEquippedStack(EquipmentSlot.LEGS);
        return chest.isOf(ModItems.PAJAMA_TOP) && legs.isOf(ModItems.PAJAMA_BOTTOMS);
    }

    // Only pajamas allowed in armor (chest/legs), head/feet empty, all inventory/offhand empty
    private static boolean isInventoryEmptyForSleep(ServerPlayerEntity player) {
        var inv = player.getInventory();
        int size = inv.size();

        for (int slot = 0; slot < size; slot++) {
            var stack = inv.getStack(slot);
            boolean isHeadSlot = (slot == 39);
            boolean isChestSlot = (slot == 38);
            boolean isLegsSlot = (slot == 37);
            boolean isFeetSlot = (slot == 36);

            if (isChestSlot) {
                if (!stack.isOf(ModItems.PAJAMA_TOP)) return false;
                continue;
            }
            if (isLegsSlot) {
                if (!stack.isOf(ModItems.PAJAMA_BOTTOMS)) return false;
                continue;
            }
            if (isHeadSlot || isFeetSlot) {
                if (!stack.isEmpty()) return false; // head/feet must be empty
                continue;
            }

            if (!stack.isEmpty()) return false; // all other slots must be empty
        }
        return true;
    }
}
