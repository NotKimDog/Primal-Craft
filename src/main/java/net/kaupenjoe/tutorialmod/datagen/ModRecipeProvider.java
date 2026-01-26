package net.kaupenjoe.tutorialmod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.kaupenjoe.tutorialmod.item.ModItems;
import net.kaupenjoe.tutorialmod.trim.ModTrimPatterns;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.Blocks;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            @Override
            public void generate() {
                long startTime = System.currentTimeMillis();
                TutorialMod.LOGGER.info("Starting automated recipe generation...");
                int generated = 0;
                Set<String> recipeIds = new HashSet<>();

                // ===== AUTO-DETECT ORES AND CREATE SMELTING RECIPES =====
                for (var oreEntry : DatagenHelper.getOres()) {
                    String materialName = DatagenHelper.getMaterialName(oreEntry.name());
                    Item materialItem = DatagenHelper.findMaterialItem(materialName);

                    if (materialItem != null) {
                        offerSmelting(List.of(oreEntry.block()), RecipeCategory.MISC, materialItem, 0.25f, 200, materialName);
                        offerBlasting(List.of(oreEntry.block()), RecipeCategory.MISC, materialItem, 0.25f, 100, materialName);
                        generated += 2;
                        TutorialMod.LOGGER.info("  ✓ Smelting recipes for: {}", oreEntry.name());
                    }
                }

                // ===== AUTO-GENERATE STICK INGREDIENT =====
                List<Item> allSticks = DatagenHelper.getItemsContaining("stick").stream()
                    .map(DatagenHelper.ItemEntry::item)
                    .toList();
                Ingredient STICKS = Ingredient.ofItems(allSticks.toArray(new Item[0]));

                // ===== AUTO-GENERATE TOOL RECIPES =====
                // Swords
                for (var entry : DatagenHelper.getSwords()) {
                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);
                    if (material != null && !entry.name().contains("obsidian")) { // Skip special cases
                        Identifier id = Identifier.of(TutorialMod.MOD_ID, entry.name());
                        if (!recipeIds.add(id.toString())) {
                            TutorialMod.LOGGER.warn("  ⚠ Skipping duplicate recipe {}", id);
                            continue;
                        }
                        createShaped(RecipeCategory.COMBAT, entry.item())
                            .pattern(" M ")
                            .pattern(" M ")
                            .pattern(" S ")
                            .input('M', material)
                            .input('S', STICKS)
                            .criterion(hasItem(material), conditionsFromItem(material))
                            .offerTo(exporter);
                        generated++;
                    }
                }
                TutorialMod.LOGGER.info("  ✓ Sword recipes: {}", DatagenHelper.getSwords().size());

                // Pickaxes & Hammers
                for (var entry : DatagenHelper.getPickaxes()) {
                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);
                    if (material != null) {
                        Identifier id = Identifier.of(TutorialMod.MOD_ID, entry.name());
                        if (!recipeIds.add(id.toString())) {
                            TutorialMod.LOGGER.warn("  ⚠ Skipping duplicate recipe {}", id);
                            continue;
                        }
                        createShaped(RecipeCategory.TOOLS, entry.item())
                            .pattern("MMM")
                            .pattern(" S ")
                            .pattern(" S ")
                            .input('M', material)
                            .input('S', STICKS)
                            .criterion(hasItem(material), conditionsFromItem(material))
                            .offerTo(exporter);
                        generated++;
                    }
                }
                TutorialMod.LOGGER.info("  ✓ Pickaxe recipes: {}", DatagenHelper.getPickaxes().size());

                // Shovels
                for (var entry : DatagenHelper.getShovels()) {
                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);
                    if (material != null) {
                        Identifier id = Identifier.of(TutorialMod.MOD_ID, entry.name());
                        if (!recipeIds.add(id.toString())) {
                            TutorialMod.LOGGER.warn("  ⚠ Skipping duplicate recipe {}", id);
                            continue;
                        }
                        createShaped(RecipeCategory.TOOLS, entry.item())
                            .pattern(" M ")
                            .pattern(" S ")
                            .pattern(" S ")
                            .input('M', material)
                            .input('S', STICKS)
                            .criterion(hasItem(material), conditionsFromItem(material))
                            .offerTo(exporter);
                        generated++;
                    }
                }
                TutorialMod.LOGGER.info("  ✓ Shovel recipes: {}", DatagenHelper.getShovels().size());

                // Axes
                for (var entry : DatagenHelper.getAxes()) {
                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);
                    if (material != null && !entry.name().contains("obsidian")) {
                        String id = Identifier.of(TutorialMod.MOD_ID, entry.name()).toString();
                        if (!recipeIds.add(id)) {
                            TutorialMod.LOGGER.warn("  ⚠ Skipping duplicate recipe {}", id);
                            continue;
                        }
                        createShaped(RecipeCategory.TOOLS, entry.item())
                            .pattern("MM ")
                            .pattern("MS ")
                            .pattern(" S ")
                            .input('M', material)
                            .input('S', STICKS)
                            .criterion(hasItem(material), conditionsFromItem(material))
                            .offerTo(exporter);
                        generated++;
                    }
                }
                TutorialMod.LOGGER.info("  ✓ Axe recipes: {}", DatagenHelper.getAxes().size());

                // Hoes
                for (var entry : DatagenHelper.getHoes()) {
                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);
                    if (material != null) {
                        String id = Identifier.of(TutorialMod.MOD_ID, entry.name()).toString();
                        if (!recipeIds.add(id)) {
                            TutorialMod.LOGGER.warn("  ⚠ Skipping duplicate recipe {}", id);
                            continue;
                        }
                        createShaped(RecipeCategory.TOOLS, entry.item())
                            .pattern("MM ")
                            .pattern(" S ")
                            .pattern(" S ")
                            .input('M', material)
                            .input('S', STICKS)
                            .criterion(hasItem(material), conditionsFromItem(material))
                            .offerTo(exporter);
                        generated++;
                    }
                }
                TutorialMod.LOGGER.info("  ✓ Hoe recipes: {}", DatagenHelper.getHoes().size());

                // ===== AUTO-GENERATE ARMOR RECIPES =====
                // Helmets
                for (var entry : DatagenHelper.getHelmets()) {
                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);
                    if (material != null) {
                        String id = Identifier.of(TutorialMod.MOD_ID, entry.name()).toString();
                        if (!recipeIds.add(id)) {
                            TutorialMod.LOGGER.warn("  ⚠ Skipping duplicate recipe {}", id);
                            continue;
                        }
                        createShaped(RecipeCategory.COMBAT, entry.item())
                            .pattern("MMM")
                            .pattern("M M")
                            .pattern("   ")
                            .input('M', material)
                            .criterion(hasItem(material), conditionsFromItem(material))
                            .offerTo(exporter);
                        generated++;
                    }
                }
                TutorialMod.LOGGER.info("  ✓ Helmet recipes: {}", DatagenHelper.getHelmets().size());

                // Chestplates (skip pajama top - custom recipe needed)
                for (var entry : DatagenHelper.getChestplates()) {
                    if (entry.name().contains("pajama")) continue; // Already has custom recipe
                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);
                    if (material != null) {
                        String id = Identifier.of(TutorialMod.MOD_ID, entry.name()).toString();
                        if (!recipeIds.add(id)) {
                            TutorialMod.LOGGER.warn("  ⚠ Skipping duplicate recipe {}", id);
                            continue;
                        }
                        createShaped(RecipeCategory.COMBAT, entry.item())
                            .pattern("M M")
                            .pattern("MMM")
                            .pattern("MMM")
                            .input('M', material)
                            .criterion(hasItem(material), conditionsFromItem(material))
                            .offerTo(exporter);
                        generated++;
                    }
                }
                TutorialMod.LOGGER.info("  ✓ Chestplate recipes");

                // Leggings (skip pajama bottoms - custom recipe needed)
                for (var entry : DatagenHelper.getLeggings()) {
                    if (entry.name().contains("pajama")) continue; // Already has custom recipe
                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);
                    if (material != null) {
                        String id = Identifier.of(TutorialMod.MOD_ID, entry.name()).toString();
                        if (!recipeIds.add(id)) {
                            TutorialMod.LOGGER.warn("  ⚠ Skipping duplicate recipe {}", id);
                            continue;
                        }
                        createShaped(RecipeCategory.COMBAT, entry.item())
                            .pattern("MMM")
                            .pattern("M M")
                            .pattern("M M")
                            .input('M', material)
                            .criterion(hasItem(material), conditionsFromItem(material))
                            .offerTo(exporter);
                        generated++;
                    }
                }
                TutorialMod.LOGGER.info("  ✓ Leggings recipes");

                // Boots
                for (var entry : DatagenHelper.getBoots()) {
                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);
                    if (material != null) {
                        String id = Identifier.of(TutorialMod.MOD_ID, entry.name()).toString();
                        if (!recipeIds.add(id)) {
                            TutorialMod.LOGGER.warn("  ⚠ Skipping duplicate recipe {}", id);
                            continue;
                        }
                        createShaped(RecipeCategory.COMBAT, entry.item())
                            .pattern("   ")
                            .pattern("M M")
                            .pattern("M M")
                            .input('M', material)
                            .criterion(hasItem(material), conditionsFromItem(material))
                            .offerTo(exporter);
                        generated++;
                    }
                }
                TutorialMod.LOGGER.info("  ✓ Boot recipes: {}", DatagenHelper.getBoots().size());

                long elapsed = System.currentTimeMillis() - startTime;
                TutorialMod.LOGGER.info("Automated recipe generation completed in {}ms", elapsed);
                TutorialMod.LOGGER.info("  ✓ Total automated recipes generated: {}", generated);

                // ===== MANUAL/CUSTOM RECIPES (Keep existing special ones) =====
                // Compacting block recipes
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, ModItems.PINK_GARNET, RecipeCategory.DECORATIONS, ModBlocks.PINK_GARNET_BLOCK);

                // Raw block <-> raw item
                createShaped(RecipeCategory.MISC, ModBlocks.RAW_PINK_GARNET_BLOCK)
                        .pattern("RRR")
                        .pattern("RRR")
                        .pattern("RRR")
                        .input('R', ModItems.RAW_PINK_GARNET)
                        .criterion(hasItem(ModItems.RAW_PINK_GARNET), conditionsFromItem(ModItems.RAW_PINK_GARNET))
                        .offerTo(exporter);

                createShapeless(RecipeCategory.MISC, ModItems.RAW_PINK_GARNET, 9)
                        .input(ModBlocks.RAW_PINK_GARNET_BLOCK)
                        .criterion(hasItem(ModBlocks.RAW_PINK_GARNET_BLOCK), conditionsFromItem(ModBlocks.RAW_PINK_GARNET_BLOCK))
                        .offerTo(exporter);

                createShapeless(RecipeCategory.MISC, ModItems.RAW_PINK_GARNET, 32)
                        .input(ModBlocks.MAGIC_BLOCK)
                        .criterion(hasItem(ModBlocks.MAGIC_BLOCK), conditionsFromItem(ModBlocks.MAGIC_BLOCK))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "raw_pink_garnet_from_magic_block")));

                // Pajamas - comfortable sleepwear crafted from wool (NOT auto-generated)
                createShaped(RecipeCategory.COMBAT, ModItems.PAJAMA_TOP)
                        .pattern("B B")
                        .pattern("BBB")
                        .pattern("BBB")
                        .input('B', Items.BLACK_WOOL)
                        .criterion(hasItem(Items.BLACK_WOOL), conditionsFromItem(Items.BLACK_WOOL))
                        .offerTo(exporter);

                createShaped(RecipeCategory.COMBAT, ModItems.PAJAMA_BOTTOMS)
                        .pattern("RRR")
                        .pattern("R R")
                        .pattern("R R")
                        .input('R', Items.RED_WOOL)
                        .criterion(hasItem(Items.RED_WOOL), conditionsFromItem(Items.RED_WOOL))
                        .offerTo(exporter);

                // Horse armor (make craftable)
                createShaped(RecipeCategory.MISC, ModItems.PINK_GARNET_HORSE_ARMOR)
                        .pattern("PPP")
                        .pattern("PPP")
                        .pattern("P P")
                        .input('P', ModItems.PINK_GARNET)
                        .criterion(hasItem(ModItems.PINK_GARNET), conditionsFromItem(ModItems.PINK_GARNET))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "pink_garnet_horse_armor")));

                // Special tools (not standard pattern)
                createShaped(RecipeCategory.TOOLS, ModItems.TOMAHAWK)
                        .pattern("PP ")
                        .pattern("PS ")
                        .pattern(" S ")
                        .input('P', ModItems.PINK_GARNET)
                        .input('S', STICKS)
                        .criterion(hasItem(ModItems.PINK_GARNET), conditionsFromItem(ModItems.PINK_GARNET))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "tomahawk")));

                createShaped(RecipeCategory.TOOLS, ModItems.OBSIDIAN_AXE)
                        .pattern("OO ")
                        .pattern("OS ")
                        .pattern(" S ")
                        .input('O', Items.OBSIDIAN)
                        .input('S', STICKS)
                        .criterion(hasItem(Items.OBSIDIAN), conditionsFromItem(Items.OBSIDIAN))
                        .offerTo(exporter);

                // Bow (simple shapeless recipe using strings + garnet for durability)
                createShapeless(RecipeCategory.COMBAT, ModItems.KAUPEN_BOW)
                        .input(Items.STRING)
                        .input(Items.STRING)
                        .input(Items.STRING)
                        .input(ModItems.PINK_GARNET)
                        .criterion(hasItem(ModItems.PINK_GARNET), conditionsFromItem(ModItems.PINK_GARNET))
                        .offerTo(exporter);

                // Special items
                createShapeless(RecipeCategory.COMBAT, ModItems.SPECTRE_STAFF)
                        .input(ModItems.PINK_GARNET)
                        .input(Items.BLAZE_ROD)
                        .criterion(hasItem(ModItems.PINK_GARNET), conditionsFromItem(ModItems.PINK_GARNET))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "spectre_staff")));

                createShapeless(RecipeCategory.MISC, ModItems.BAR_BRAWL_MUSIC_DISC)
                        .input(Items.MUSIC_DISC_13)
                        .input(ModItems.PINK_GARNET)
                        .criterion(hasItem(ModItems.PINK_GARNET), conditionsFromItem(ModItems.PINK_GARNET))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "bar_brawl_music_disc")));

                // Utility blocks & items
                createShaped(RecipeCategory.DECORATIONS, ModBlocks.PINK_GARNET_LAMP)
                        .pattern("PPP")
                        .pattern("PRP")
                        .pattern("PPP")
                        .input('P', ModItems.PINK_GARNET)
                        .input('R', Items.REDSTONE)
                        .criterion(hasItem(ModItems.PINK_GARNET), conditionsFromItem(ModItems.PINK_GARNET))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "pink_garnet_lamp")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.PINK_GARNET_STAIRS)
                        .pattern("P  ")
                        .pattern("PP ")
                        .pattern("PPP")
                        .input('P', ModItems.PINK_GARNET)
                        .criterion(hasItem(ModItems.PINK_GARNET), conditionsFromItem(ModItems.PINK_GARNET))
                        .offerTo(exporter);

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.PINK_GARNET_SLAB)
                        .pattern("PPP")
                        .input('P', ModItems.PINK_GARNET)
                        .criterion(hasItem(ModItems.PINK_GARNET), conditionsFromItem(ModItems.PINK_GARNET))
                        .offerTo(exporter);

                // Chair and pedestal from driftwood planks
                createShaped(RecipeCategory.DECORATIONS, ModBlocks.CHAIR)
                        .pattern(" P ")
                        .pattern("PS ")
                        .pattern("   ")
                        .input('P', ModBlocks.DRIFTWOOD_PLANKS)
                        .input('S', STICKS)
                        .criterion(hasItem(ModBlocks.DRIFTWOOD_PLANKS), conditionsFromItem(ModBlocks.DRIFTWOOD_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "chair")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.PEDESTAL)
                        .pattern(" P ")
                        .pattern("P P")
                        .pattern(" P ")
                        .input('P', ModBlocks.DRIFTWOOD_PLANKS)
                        .criterion(hasItem(ModBlocks.DRIFTWOOD_PLANKS), conditionsFromItem(ModBlocks.DRIFTWOOD_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "pedestal")));

                // Cauliflower seeds from cauliflower
                createShapeless(RecipeCategory.MISC, ModItems.CAULIFLOWER_SEEDS)
                        .input(ModItems.CAULIFLOWER)
                        .criterion(hasItem(ModItems.CAULIFLOWER), conditionsFromItem(ModItems.CAULIFLOWER))
                        .offerTo(exporter);

                // Honey berries (allow crafting two from bush) - optional convenience
                createShapeless(RecipeCategory.FOOD, ModItems.HONEY_BERRIES, 2)
                        .input(ModBlocks.HONEY_BERRY_BUSH)
                        .criterion(hasItem(ModBlocks.HONEY_BERRY_BUSH), conditionsFromItem(ModBlocks.HONEY_BERRY_BUSH))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "honey_berries_from_bush")));

                // Growth chamber: craftable with magic blocks and pistons/observers (example)
                createShaped(RecipeCategory.MISC, ModBlocks.GROWTH_CHAMBER)
                        .pattern("MMM")
                        .pattern("RCR")
                        .pattern("OOO")
                        .input('M', ModBlocks.MAGIC_BLOCK)
                        .input('R', ModItems.PINK_GARNET)
                        .input('C', Items.CHEST)
                        .input('O', Items.OBSERVER)
                        .criterion(hasItem(ModBlocks.MAGIC_BLOCK), conditionsFromItem(ModBlocks.MAGIC_BLOCK))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "growth_chamber")));

                // MAGIC_BLOCK: very hard to get - requires Nether Star + starlight ashes + pink garnet block
                createShaped(RecipeCategory.MISC, ModBlocks.MAGIC_BLOCK)
                        .pattern("GSG")
                        .pattern("SNS")
                        .pattern("GGG")
                        .input('G', ModBlocks.PINK_GARNET_BLOCK)
                        .input('S', ModItems.STARLIGHT_ASHES)
                        .input('N', Items.NETHER_STAR)
                        .criterion(hasItem(ModItems.STARLIGHT_ASHES), conditionsFromItem(ModItems.STARLIGHT_ASHES))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "magic_block_hard")));

                // Custom crafting tables: 2x2 of matching planks -> crafting table
                createShaped(RecipeCategory.DECORATIONS, ModBlocks.ACACIA_CRAFTING_TABLE)
                        .pattern("PP ")
                        .pattern("PP ")
                        .pattern("   ")
                        .input('P', Blocks.ACACIA_PLANKS)
                        .criterion(hasItem(Blocks.ACACIA_PLANKS), conditionsFromItem(Blocks.ACACIA_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "acacia_crafting_table")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.BAMBOO_CRAFTING_TABLE)
                        .pattern("PP ")
                        .pattern("PP ")
                        .pattern("   ")
                        .input('P', Blocks.BAMBOO_PLANKS)
                        .criterion(hasItem(Blocks.BAMBOO_PLANKS), conditionsFromItem(Blocks.BAMBOO_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "bamboo_crafting_table")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.BIRCH_CRAFTING_TABLE)
                        .pattern("PP ")
                        .pattern("PP ")
                        .pattern("   ")
                        .input('P', Blocks.BIRCH_PLANKS)
                        .criterion(hasItem(Blocks.BIRCH_PLANKS), conditionsFromItem(Blocks.BIRCH_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "birch_crafting_table")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.CHERRY_CRAFTING_TABLE)
                        .pattern("PP ")
                        .pattern("PP ")
                        .pattern("   ")
                        .input('P', Blocks.CHERRY_PLANKS)
                        .criterion(hasItem(Blocks.CHERRY_PLANKS), conditionsFromItem(Blocks.CHERRY_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "cherry_crafting_table")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.CRIMSON_CRAFTING_TABLE)
                        .pattern("PP ")
                        .pattern("PP ")
                        .pattern("   ")
                        .input('P', Blocks.CRIMSON_PLANKS)
                        .criterion(hasItem(Blocks.CRIMSON_PLANKS), conditionsFromItem(Blocks.CRIMSON_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "crimson_crafting_table")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.DARK_OAK_CRAFTING_TABLE)
                        .pattern("PP ")
                        .pattern("PP ")
                        .pattern("   ")
                        .input('P', Blocks.DARK_OAK_PLANKS)
                        .criterion(hasItem(Blocks.DARK_OAK_PLANKS), conditionsFromItem(Blocks.DARK_OAK_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "dark_oak_crafting_table")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.JUNGLE_CRAFTING_TABLE)
                        .pattern("PP ")
                        .pattern("PP ")
                        .pattern("   ")
                        .input('P', Blocks.JUNGLE_PLANKS)
                        .criterion(hasItem(Blocks.JUNGLE_PLANKS), conditionsFromItem(Blocks.JUNGLE_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "jungle_crafting_table")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.MANGROVE_CRAFTING_TABLE)
                        .pattern("PP ")
                        .pattern("PP ")
                        .pattern("   ")
                        .input('P', Blocks.MANGROVE_PLANKS)
                        .criterion(hasItem(Blocks.MANGROVE_PLANKS), conditionsFromItem(Blocks.MANGROVE_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "mangrove_crafting_table")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.PALEOAK_CRAFTING_TABLE)
                        .pattern("PP ")
                        .pattern("PP ")
                        .pattern("   ")
                        .input('P', Blocks.PALE_OAK_PLANKS)
                        .criterion(hasItem(Blocks.PALE_OAK_PLANKS), conditionsFromItem(Blocks.PALE_OAK_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "pale_oak_crafting_table")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.SPRUCE_CRAFTING_TABLE)
                        .pattern("PP ")
                        .pattern("PP ")
                        .pattern("   ")
                        .input('P', Blocks.SPRUCE_PLANKS)
                        .criterion(hasItem(Blocks.SPRUCE_PLANKS), conditionsFromItem(Blocks.SPRUCE_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "spruce_crafting_table")));

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.WARPED_CRAFTING_TABLE)
                        .pattern("PP ")
                        .pattern("PP ")
                        .pattern("   ")
                        .input('P', Blocks.WARPED_PLANKS)
                        .criterion(hasItem(Blocks.WARPED_PLANKS), conditionsFromItem(Blocks.WARPED_PLANKS))
                        .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "warped_crafting_table")));

                offerSmithingTrimRecipe(ModItems.KAUPEN_SMITHING_TEMPLATE, ModTrimPatterns.KAUPEN,
                        RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "kaupen")));
            }
        };
    }

    @Override
    public String getName() {
        return "TutorialMod Recipes";
    }
}

























