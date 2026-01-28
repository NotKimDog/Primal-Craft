package net.kimdog_studios.primal_craft.util;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> NEEDS_PINK_GARNET_TOOL = createTag("needs_pink_garnet_tool");
        public static final TagKey<Block> INCORRECT_FOR_PINK_GARNET_TOOL = createTag("incorrect_for_pink_garnet_tool");

        public static final TagKey<Block> INCORRECT_FOR_OBSIDIAN_TOOL = createTag("incorrect_for_obsidian_tool");
        public static final TagKey<Block> NEEDS_DIAMOND_TOOL = createTag("needs_diamond_tool");

        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(PrimalCraft.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> TRANSFORMABLE_ITEMS = createTag("transformable_items");
        public static final TagKey<Item> PINK_GARNET_REPAIR = createTag("pink_garnet_repair");

        private static TagKey<Item> createTag(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of(PrimalCraft.MOD_ID, name));
        }
    }
}
