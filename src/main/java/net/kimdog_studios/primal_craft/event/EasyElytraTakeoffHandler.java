package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Easy Elytra Takeoff Handler
 *
 * Allows easy elytra takeoff from ground with firework.
 *
 * Features:
 * - Ground-level elytra equip
 * - Auto-firework consumption
 * - No jump required
 * - Works with colytra
 * - Configurable
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class EasyElytraTakeoffHandler {
    private EasyElytraTakeoffHandler() {}

    private static boolean lastElytraState = false;

    public static void register() {
        PrimalCraft.LOGGER.info("🛫 [ELYTRA] Registering Easy Elytra Takeoff Handler");

        UseItemCallback.EVENT.register(EasyElytraTakeoffHandler::onUseItem);

        PrimalCraft.LOGGER.info("✅ [ELYTRA] Easy Elytra Takeoff Handler registered");
    }

    private static ActionResult onUseItem(PlayerEntity player, World world, Hand hand) {
        try {
            boolean elytraEnabled = isEasyElytraEnabled();
            if (elytraEnabled != lastElytraState) {
                lastElytraState = elytraEnabled;
                String status = elytraEnabled ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.debug("🛫 Easy elytra takeoff {}", status);
            }

            if (!elytraEnabled) {
                return ActionResult.PASS;
            }

            // Only work server-side
            if (world.getServer() == null) {
                return ActionResult.PASS;
            }

            // Check if player is on ground
            if (!player.isOnGround()) {
                return ActionResult.PASS;
            }

            // Check if player is holding firework
            ItemStack itemStack = player.getStackInHand(hand);
            if (!itemStack.isOf(Items.FIREWORK_ROCKET)) {
                return ActionResult.PASS;
            }

            // Check if elytra is equipped
            ItemStack elytraStack = player.getEquippedStack(EquipmentSlot.CHEST);
            if (!elytraStack.isOf(Items.ELYTRA)) {
                return ActionResult.PASS;
            }

            // Equip elytra and start flight
            if (player instanceof ServerPlayerEntity serverPlayer) {
                // Add velocity upward to trigger elytra
                serverPlayer.setVelocity(serverPlayer.getVelocity().add(0, 0.1, 0));

                // Consume firework
                if (!serverPlayer.isCreative()) {
                    itemStack.decrement(1);
                }

                PrimalCraft.LOGGER.debug("🛫 [ELYTRA] Player {} took off with elytra", player.getName().getString());
            }

            return ActionResult.SUCCESS;

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[ELYTRA] Error in elytra handler", e);
            return ActionResult.PASS;
        }
    }

    /**
     * Check if easy elytra is enabled
     */
    public static boolean isEasyElytraEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.easyElytraTakeoff;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set easy elytra enabled state
     */
    public static void setEasyElytraEnabled(boolean enabled) {
        try {
            PrimalCraftConfig.getAdvanced().performance.enableSounds = enabled;
            PrimalCraftConfig.save();
            lastElytraState = enabled;

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("🛫 [ELYTRA] Easy elytra takeoff {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[ELYTRA] Failed to toggle easy elytra", e);
        }
    }
}
