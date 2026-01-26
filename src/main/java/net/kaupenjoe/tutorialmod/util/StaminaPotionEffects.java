package net.kaupenjoe.tutorialmod.util;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handles stamina modifications based on active potion effects
 */
public final class StaminaPotionEffects {

    /**
     * Calculate stamina drain multiplier based on active effects
     */
    public static double getDrainMultiplier(ServerPlayerEntity player) {
        double multiplier = 1.0;

        // Speed increases stamina drain
        if (player.hasStatusEffect(StatusEffects.SPEED)) {
            int amplifier = player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            multiplier += 0.3 * (amplifier + 1); // Speed I = 1.3x, Speed II = 1.6x
        }

        // Strength increases attack stamina cost
        if (player.hasStatusEffect(StatusEffects.STRENGTH)) {
            int amplifier = player.getStatusEffect(StatusEffects.STRENGTH).getAmplifier();
            multiplier += 0.2 * (amplifier + 1);
        }

        // Haste increases stamina drain
        if (player.hasStatusEffect(StatusEffects.HASTE)) {
            multiplier += 0.25;
        }

        return multiplier;
    }

    /**
     * Calculate stamina regen multiplier based on active effects
     */
    public static double getRegenMultiplier(ServerPlayerEntity player) {
        double multiplier = 1.0;

        // Slowness decreases stamina drain (more efficient)
        if (player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            multiplier += 0.5; // 50% more regen
        }

        // Regeneration boosts stamina regen
        if (player.hasStatusEffect(StatusEffects.REGENERATION)) {
            int amplifier = player.getStatusEffect(StatusEffects.REGENERATION).getAmplifier();
            multiplier += 0.5 * (amplifier + 1);
        }

        // Absorption gives slight stamina boost
        if (player.hasStatusEffect(StatusEffects.ABSORPTION)) {
            multiplier += 0.3;
        }

        // Hero of the Village boosts stamina regen
        if (player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            multiplier += 0.5;
        }

        return multiplier;
    }

    /**
     * Check if player should have reduced stamina costs
     */
    public static boolean hasStaminaReduction(ServerPlayerEntity player) {
        return player.hasStatusEffect(StatusEffects.SLOWNESS) ||
               player.hasStatusEffect(StatusEffects.RESISTANCE);
    }
}
