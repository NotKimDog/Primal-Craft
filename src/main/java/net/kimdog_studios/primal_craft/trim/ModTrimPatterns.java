package net.kimdog_studios.primal_craft.trim;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.item.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ModTrimPatterns {
    public static final RegistryKey<ArmorTrimPattern> KAUPEN = RegistryKey.of(RegistryKeys.TRIM_PATTERN,
            Identifier.of(PrimalCraft.MOD_ID, "kaupen"));

    public static void bootstrap(Registerable<ArmorTrimPattern> context) {
        register(context, ModItems.KAUPEN_SMITHING_TEMPLATE, KAUPEN);
    }

    private static void register(Registerable<ArmorTrimPattern> context, Item item, RegistryKey<ArmorTrimPattern> key) {
        ArmorTrimPattern trimPattern = new ArmorTrimPattern(key.getValue(),
                Text.translatable(Util.createTranslationKey("trim_pattern", key.getValue())), false);

        context.register(key, trimPattern);
    }
}