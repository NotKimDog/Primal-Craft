package net.kimdog_studios.primal_craft.enchantment;

import com.mojang.serialization.MapCodec;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.enchantment.custom.ChainLightningEnchantmentEffect;
import net.kimdog_studios.primal_craft.enchantment.custom.LightningStrikerEnchantmentEffect;
import net.kimdog_studios.primal_craft.enchantment.custom.FrostbiteEnchantmentEffect;
import net.kimdog_studios.primal_craft.enchantment.custom.LifestealEnchantmentEffect;
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
        return Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.of(PrimalCraft.MOD_ID, name), codec);
    }

    public static void registerEnchantmentEffects() {
        PrimalCraft.LOGGER.info("Registering Mod Enchantment Effects for " + PrimalCraft.MOD_ID);
    }
}
