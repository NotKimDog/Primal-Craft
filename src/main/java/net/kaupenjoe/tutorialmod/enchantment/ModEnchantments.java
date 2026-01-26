package net.kaupenjoe.tutorialmod.enchantment;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.enchantment.custom.ChainLightningEnchantmentEffect;
import net.kaupenjoe.tutorialmod.enchantment.custom.FrostbiteEnchantmentEffect;
import net.kaupenjoe.tutorialmod.enchantment.custom.LightningStrikerEnchantmentEffect;
import net.kaupenjoe.tutorialmod.enchantment.custom.LifestealEnchantmentEffect;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> LIGHTNING_STRIKER =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(TutorialMod.MOD_ID, "lightning_striker"));

    public static final RegistryKey<Enchantment> LIFESTEAL =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(TutorialMod.MOD_ID, "lifesteal"));

    public static final RegistryKey<Enchantment> FROSTBITE =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(TutorialMod.MOD_ID, "frostbite"));

    public static final RegistryKey<Enchantment> CHAIN_LIGHTNING =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(TutorialMod.MOD_ID, "chain_lightning"));

    // Data-gen bootstrap (used by TutorialModDataGenerator)
    public static void bootstrap(Registerable<Enchantment> registerable) {
        var enchantments = registerable.getRegistryLookup(RegistryKeys.ENCHANTMENT);
        var items = registerable.getRegistryLookup(RegistryKeys.ITEM);

        // Lightning Striker (existing)
        register(registerable, LIGHTNING_STRIKER, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                5,
                2,
                Enchantment.leveledCost(5, 7),
                Enchantment.leveledCost(25, 9),
                2,
                AttributeModifierSlot.MAINHAND))
                .exclusiveSet(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE_SET))
                .addEffect(EnchantmentEffectComponentTypes.POST_ATTACK,
                        EnchantmentEffectTarget.ATTACKER, EnchantmentEffectTarget.VICTIM,
                        new LightningStrikerEnchantmentEffect()));

        // Lifesteal - heals attacker on hit
        register(registerable, LIFESTEAL, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                3,
                2,
                Enchantment.leveledCost(3, 5),
                Enchantment.leveledCost(10, 7),
                1,
                AttributeModifierSlot.MAINHAND))
                .exclusiveSet(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE_SET))
                .addEffect(EnchantmentEffectComponentTypes.POST_ATTACK,
                        EnchantmentEffectTarget.ATTACKER, EnchantmentEffectTarget.VICTIM,
                        new LifestealEnchantmentEffect()));

        // Frostbite - slows and chills victim
        register(registerable, FROSTBITE, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                3,
                2,
                Enchantment.leveledCost(3, 5),
                Enchantment.leveledCost(10, 7),
                1,
                AttributeModifierSlot.MAINHAND))
                .exclusiveSet(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE_SET))
                .addEffect(EnchantmentEffectComponentTypes.POST_ATTACK,
                        EnchantmentEffectTarget.ATTACKER, EnchantmentEffectTarget.VICTIM,
                        new FrostbiteEnchantmentEffect()));

        // Chain Lightning - strike multiple nearby targets
        register(registerable, CHAIN_LIGHTNING, Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                5,
                2,
                Enchantment.leveledCost(5, 8),
                Enchantment.leveledCost(20, 10),
                2,
                AttributeModifierSlot.MAINHAND))
                .exclusiveSet(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE_SET))
                .addEffect(EnchantmentEffectComponentTypes.POST_ATTACK,
                        EnchantmentEffectTarget.ATTACKER, EnchantmentEffectTarget.VICTIM,
                        new ChainLightningEnchantmentEffect()));
    }

    private static void register(Registerable<Enchantment> registry, RegistryKey<Enchantment> key, Enchantment.Builder builder) {
        registry.register(key, builder.build(key.getValue()));
    }
}
