package net.kaupenjoe.tutorialmod.enchantment;

import com.mojang.serialization.MapCodec;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.enchantment.custom.ChainLightningEnchantmentEffect;
import net.kaupenjoe.tutorialmod.enchantment.custom.LightningStrikerEnchantmentEffect;
import net.kaupenjoe.tutorialmod.enchantment.custom.FrostbiteEnchantmentEffect;
import net.kaupenjoe.tutorialmod.enchantment.custom.LifestealEnchantmentEffect;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEnchantmentEffects {
    public static final MapCodec<? extends EnchantmentEntityEffect> LIGHTNING_STRIKER =
            registerEntityEffect("lightning_striker", LightningStrikerEnchantmentEffect.CODEC);

    // Register the codecs for custom enchantment entity effects so data-gen can serialize them
    public static final MapCodec<? extends EnchantmentEntityEffect> LIFESTEAL =
            registerEntityEffect("lifesteal", LifestealEnchantmentEffect.CODEC);

    public static final MapCodec<? extends EnchantmentEntityEffect> FROSTBITE =
            registerEntityEffect("frostbite", FrostbiteEnchantmentEffect.CODEC);

    public static final MapCodec<? extends EnchantmentEntityEffect> CHAIN_LIGHTNING =
            registerEntityEffect("chain_lightning", ChainLightningEnchantmentEffect.CODEC);


    private static MapCodec<? extends EnchantmentEntityEffect> registerEntityEffect(String name,
                                                                                    MapCodec<? extends EnchantmentEntityEffect> codec) {
        return Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.of(TutorialMod.MOD_ID, name), codec);
    }

    public static void registerEnchantmentEffects() {
        TutorialMod.LOGGER.info("Registering Mod Enchantment Effects for " + TutorialMod.MOD_ID);
    }
}
