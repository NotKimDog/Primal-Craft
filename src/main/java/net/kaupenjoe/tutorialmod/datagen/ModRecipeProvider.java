package net.kaupenjoe.tutorialmod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.kaupenjoe.tutorialmod.item.ModItems;
import net.kaupenjoe.tutorialmod.trim.ModTrimPatterns;
import net.minecraft.data.recipe.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import java.util.List;
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

                // ===== AUTO-DETECT ORES AND CREATE SMELTING RECIPES =====
                for (var oreEntry : DatagenHelper.getOres()) {
                    String materialName = DatagenHelper.getMaterialName(oreEntry.name());
                    Item materialItem = DatagenHelper.findMaterialItem(materialName);
                    if (materialItem != null) {
                        offerSmelting(List.of(oreEntry.block()), RecipeCategory.MISC, materialItem, 0.25f, 200, materialName);
                        offerBlasting(List.of(oreEntry.block()), RecipeCategory.MISC, materialItem, 0.25f, 100, materialName);
                        TutorialMod.LOGGER.info("  ✓ Smelting recipes for: {}", oreEntry.name());
                    }
                }

                // ===== AUTO-GENERATE STICK INGREDIENT =====
                List<Item> allSticks = DatagenHelper.getItemsContaining("stick").stream()
                    .map(DatagenHelper.ItemEntry::item)
                    .toList();
                Ingredient STICKS = Ingredient.ofItems(allSticks.toArray(new Item[0]));

                // ===== AUTO-GENERATE TOOL RECIPES =====
                generateToolRecipes("sword", DatagenHelper.getSwords(), STICKS, "obsidian");
                generateToolRecipes("pickaxe", DatagenHelper.getPickaxes(), STICKS, null);
                generateToolRecipes("shovel", DatagenHelper.getShovels(), STICKS, null);
                generateToolRecipes("axe", DatagenHelper.getAxes(), STICKS, "obsidian");
                generateToolRecipes("hoe", DatagenHelper.getHoes(), STICKS, null);

                // ===== AUTO-GENERATE ARMOR RECIPES =====
                generateArmorRecipes("helmet", DatagenHelper.getHelmets(), null);
                generateArmorRecipes("chestplate", DatagenHelper.getChestplates(), "pajama");
                generateArmorRecipes("leggings", DatagenHelper.getLeggings(), "pajama");
                generateArmorRecipes("boots", DatagenHelper.getBoots(), null);

                // ===== CUSTOM RECIPES FROM CONFIGURATION =====
                // Load shaped recipes from config
                RecipeConfig.SHAPED_RECIPES.forEach((recipeId, recipe) -> {
                    createShaped(RecipeCategory.MISC, recipe.output)
                        .apply(builder -> {
                            for (String line : recipe.pattern) {
                                if (!line.isEmpty()) {
                                    builder.pattern(line);
                                }
                            }
                            recipe.ingredients.forEach(builder::input);
                            builder.criterion(hasItem(recipe.criterion), conditionsFromItem(recipe.criterion));
                        })
                        .offerTo(exporter, Identifier.of(TutorialMod.MOD_ID, recipeId));
                });

                // Load shapeless recipes from config
                RecipeConfig.SHAPELESS_RECIPES.forEach((recipeId, recipe) -> {
                    createShapeless(RecipeCategory.MISC, recipe.output, recipe.count)
                        .apply(builder -> {
                            recipe.ingredients.forEach(builder::input);
                            builder.criterion(hasItem(recipe.criterion), conditionsFromItem(recipe.criterion));
                        })
                        .offerTo(exporter, Identifier.of(TutorialMod.MOD_ID, recipeId));
                });

                int customCount = RecipeConfig.SHAPED_RECIPES.size() + RecipeConfig.SHAPELESS_RECIPES.size();
                TutorialMod.LOGGER.info("  ✓ Loaded {} custom recipes from RecipeConfig", customCount);

                // ===== COMPACTING RECIPES =====
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, ModItems.PINK_GARNET, RecipeCategory.DECORATIONS, ModBlocks.PINK_GARNET_BLOCK);

                // ===== SMITHING TRIM RECIPES =====
                offerSmithingTrimRecipe(ModItems.KAUPEN_SMITHING_TEMPLATE, ModTrimPatterns.KAUPEN,
                        RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "kaupen")));

                long elapsed = System.currentTimeMillis() - startTime;
                TutorialMod.LOGGER.info("Recipe generation completed in {}ms", elapsed);
            }

            /**
             * Generate tool recipes with batch processing
             */
            private void generateToolRecipes(String toolType, List<DatagenHelper.ItemEntry> items, Ingredient stick, String excludePattern) {
                int count = 0;
                for (var entry : items) {
                    if (excludePattern != null && entry.name().contains(excludePattern)) continue;

                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);

                    if (material != null) {
                        generateToolRecipe(toolType, entry.item(), material, stick);
                        count++;
                    }
                }
                TutorialMod.LOGGER.info("  ✓ Added {} {} recipes", count, toolType);
            }

            /**
             * Generate a single tool recipe
             */
            private void generateToolRecipe(String toolType, ItemConvertible output, ItemConvertible material, Ingredient stick) {
                String materialName = DatagenHelper.getMaterialName(output.asItem().toString());
                String patternId = materialName + "_" + toolType;
                RecipeCategory category = toolType.equals("sword") ? RecipeCategory.COMBAT : RecipeCategory.TOOLS;

                ShapedRecipeJsonBuilder builder = createShaped(category, output);

                // Apply the correct pattern
                switch (toolType.toLowerCase()) {
                    case "sword" -> {
                        builder.pattern("M");
                        builder.pattern("M");
                        builder.pattern("S");
                    }
                    case "pickaxe" -> {
                        builder.pattern("MMM");
                        builder.pattern("MS ");
                        builder.pattern("MS ");
                    }
                    case "shovel" -> {
                        builder.pattern("M");
                        builder.pattern("S");
                        builder.pattern("S");
                    }
                    case "axe" -> {
                        builder.pattern("MM ");
                        builder.pattern("MS ");
                        builder.pattern(" S ");
                    }
                    case "hoe" -> {
                        builder.pattern("MM ");
                        builder.pattern(" S ");
                        builder.pattern(" S ");
                    }
                }

                builder.input('M', material);
                builder.input('S', stick);
                builder.criterion(hasItem(material), conditionsFromItem(material));
                builder.offerTo(exporter, Identifier.of(TutorialMod.MOD_ID, patternId));
            }

            /**
             * Generate armor recipes with batch processing
             */
            private void generateArmorRecipes(String armorType, List<DatagenHelper.ItemEntry> items, String excludePattern) {
                int count = 0;
                for (var entry : items) {
                    if (excludePattern != null && entry.name().contains(excludePattern)) continue;

                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);

                    if (material != null) {
                        generateArmorRecipe(armorType, entry.item(), material);
                        count++;
                    }
                }
                TutorialMod.LOGGER.info("  ✓ Added {} {} recipes", count, armorType);
            }

            /**
             * Generate a single armor recipe
             */
            private void generateArmorRecipe(String armorType, ItemConvertible output, ItemConvertible material) {
                String materialName = DatagenHelper.getMaterialName(output.asItem().toString());
                ShapedRecipeJsonBuilder builder = createShaped(RecipeCategory.COMBAT, output);

                // Apply the correct pattern
                switch (armorType.toLowerCase()) {
                    case "helmet" -> {
                        builder.pattern("MMM");
                        builder.pattern("M M");
                    }
                    case "chestplate" -> {
                        builder.pattern("M M");
                        builder.pattern("MMM");
                        builder.pattern("MMM");
                    }
                    case "leggings" -> {
                        builder.pattern("MMM");
                        builder.pattern("M M");
                        builder.pattern("M M");
                    }
                    case "boots" -> {
                        builder.pattern("M M");
                        builder.pattern("M M");
                    }
                }

                builder.input('M', material);
                builder.criterion(hasItem(material), conditionsFromItem(material));
                builder.offerTo(exporter, Identifier.of(TutorialMod.MOD_ID, materialName));
            }
        };
    }

    @Override
    public String getName() {
        return "TutorialMod Recipes";
    }
}

























