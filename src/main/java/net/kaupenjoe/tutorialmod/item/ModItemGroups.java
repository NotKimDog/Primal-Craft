package net.kaupenjoe.tutorialmod.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    @SuppressWarnings("unused")
    public static final ItemGroup PINK_GARNET_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(TutorialMod.MOD_ID, "pink_garnet_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.PINK_GARNET))
                    .displayName(Text.translatable("itemgroup.tutorialmod.pink_garnet_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.PINK_GARNET);

                        entries.add(ModItems.RAW_PINK_GARNET);

                        entries.add(ModItems.CHISEL);

                        entries.add(ModItems.CAULIFLOWER);

                        entries.add(ModItems.STARLIGHT_ASHES);

                        entries.add(ModItems.PINK_GARNET_SWORD);

                        entries.add(ModItems.PINK_GARNET_PICKAXE);

                        entries.add(ModItems.PINK_GARNET_SHOVEL);

                        entries.add(ModItems.PINK_GARNET_AXE);

                        entries.add(ModItems.PINK_GARNET_HOE);

                        entries.add(ModItems.PINK_GARNET_HAMMER);

                        entries.add(ModItems.PINK_GARNET_HELMET);

                        entries.add(ModItems.PINK_GARNET_CHESTPLATE);

                        entries.add(ModItems.PINK_GARNET_LEGGINGS);

                        entries.add(ModItems.PINK_GARNET_BOOTS);

                        entries.add(ModItems.PINK_GARNET_HORSE_ARMOR);

                        entries.add(ModItems.KAUPEN_SMITHING_TEMPLATE);

                        entries.add(ModItems.KAUPEN_BOW);

                        entries.add(ModItems.BAR_BRAWL_MUSIC_DISC);

                        entries.add(ModItems.CAULIFLOWER_SEEDS);

                        entries.add(ModItems.HONEY_BERRIES);

                        entries.add(ModItems.TOMAHAWK);

                        // Sticks
                        entries.add(ModItems.ACACIA_STICK);
                        entries.add(ModItems.BIRCH_STICK);
                        entries.add(ModItems.CHERRY_STICK);
                        entries.add(ModItems.CRIMSON_STICK);
                        entries.add(ModItems.DARK_OAK_STICK);
                        entries.add(ModItems.JUNGLE_STICK);
                        entries.add(ModItems.MANGROVE_STICK);
                        entries.add(ModItems.PALE_OAK_STICK);
                        entries.add(ModItems.SPRUCE_STICK);
                        entries.add(ModItems.WARPED_STICK);

                        entries.add(ModBlocks.PINK_GARNET_BLOCK);

                        entries.add(ModBlocks.RAW_PINK_GARNET_BLOCK);

                        entries.add(ModBlocks.PINK_GARNET_ORE);

                        entries.add(ModBlocks.PINK_GARNET_DEEPSLATE_ORE);

                        entries.add(ModBlocks.MAGIC_BLOCK);

                        entries.add(ModBlocks.PINK_GARNET_STAIRS);

                        entries.add(ModBlocks.PINK_GARNET_SLAB);

                        entries.add(ModBlocks.PINK_GARNET_BUTTON);

                        entries.add(ModBlocks.PINK_GARNET_PRESSURE_PLATE);

                        entries.add(ModBlocks.PINK_GARNET_FENCE);

                        entries.add(ModBlocks.PINK_GARNET_FENCE_GATE);

                        entries.add(ModBlocks.PINK_GARNET_WALL);

                        entries.add(ModBlocks.PINK_GARNET_DOOR);

                        entries.add(ModBlocks.PINK_GARNET_TRAPDOOR);

                        entries.add(ModBlocks.PINK_GARNET_LAMP);

                        entries.add(ModBlocks.DRIFTWOOD_LOG);

                        entries.add(ModBlocks.DRIFTWOOD_WOOD);

                        entries.add(ModBlocks.STRIPPED_DRIFTWOOD_LOG);

                        entries.add(ModBlocks.STRIPPED_DRIFTWOOD_WOOD);

                        entries.add(ModBlocks.DRIFTWOOD_PLANKS);

                        entries.add(ModBlocks.DRIFTWOOD_LEAVES);

                        entries.add(ModBlocks.DRIFTWOOD_SAPLING);

                        entries.add(ModBlocks.CHAIR);

                        entries.add(ModBlocks.PEDESTAL);

                        entries.add(ModBlocks.GROWTH_CHAMBER);

                        entries.add(ModBlocks.ACACIA_CRAFTING_TABLE);

                        entries.add(ModBlocks.BAMBOO_CRAFTING_TABLE);

                        entries.add(ModBlocks.BIRCH_CRAFTING_TABLE);

                        entries.add(ModBlocks.DARK_OAK_CRAFTING_TABLE);

                        entries.add(ModBlocks.JUNGLE_CRAFTING_TABLE);

                        entries.add(ModBlocks.MANGROVE_CRAFTING_TABLE);

                        entries.add(ModBlocks.PALEOAK_CRAFTING_TABLE);

                        entries.add(ModBlocks.SPRUCE_CRAFTING_TABLE);

                        entries.add(ModBlocks.WARPED_CRAFTING_TABLE);

                        entries.add(ModItems.OBSIDIAN_AXE);

                        entries.add(ModItems.PAJAMA_BOTTOMS);

                        entries.add(ModItems.PAJAMA_TOP);
                    }).build());


    public static void registerItemGroups() {
        TutorialMod.LOGGER.info("Registering Item Groups for " + TutorialMod.MOD_ID);
    }
}
