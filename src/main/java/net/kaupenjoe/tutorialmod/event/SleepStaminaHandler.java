package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.kaupenjoe.tutorialmod.util.EnhancedStaminaManager;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Handles stamina restoration and buffs from sleeping
 */
public final class SleepStaminaHandler {

    public static void register() {
        // When player wakes up from sleep
        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayerEntity player) {
                // Fully restore stamina
                double maxStamina = EnhancedStaminaManager.getMaxStamina(player);
                EnhancedStaminaManager.restoreStamina(player, maxStamina * 2); // Restore double to ensure full

                // Remove all fatigue
                // This will be handled in EnhancedStaminaManager

                // Give "Well Rested" buff
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.REGENERATION,
                    6000, // 5 minutes
                    0,
                    false,
                    false,
                    true
                ));

                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SATURATION,
                    200, // 10 seconds
                    0,
                    false,
                    false,
                    true
                ));

                // Send message
                player.sendMessage(Text.literal("§a✨ Well Rested! Stamina fully restored! ✨"), true);
            }
        });
    }
}
