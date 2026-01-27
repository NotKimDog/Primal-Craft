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

                // Initialize the advanced recipe builder for cleaner code
                AdvancedRecipeBuilder builder = new AdvancedRecipeBuilder(exporter, this);

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

                // ===== AUTO-GENERATE TOOL RECIPES (Using Advanced Builder) =====
                builder.batchAddToolRecipes("sword", DatagenHelper.getSwords(), STICKS, "obsidian");
                builder.batchAddToolRecipes("pickaxe", DatagenHelper.getPickaxes(), STICKS, null);
                builder.batchAddToolRecipes("shovel", DatagenHelper.getShovels(), STICKS, null);
                builder.batchAddToolRecipes("axe", DatagenHelper.getAxes(), STICKS, "obsidian");
                builder.batchAddToolRecipes("hoe", DatagenHelper.getHoes(), STICKS, null);

                // ===== AUTO-GENERATE ARMOR RECIPES (Using Advanced Builder) =====
                builder.batchAddArmorRecipes("helmet", DatagenHelper.getHelmets(), null);
                builder.batchAddArmorRecipes("chestplate", DatagenHelper.getChestplates(), "pajama");
                builder.batchAddArmorRecipes("leggings", DatagenHelper.getLeggings(), "pajama");
                builder.batchAddArmorRecipes("boots", DatagenHelper.getBoots(), null);

                long elapsed = System.currentTimeMillis() - startTime;
                TutorialMod.LOGGER.info("Automated recipe generation completed in {}ms", elapsed);
                TutorialMod.LOGGER.info("  ✓ Total automated recipes generated: {}", builder.getTotalGenerated());

                // ===== CUSTOM RECIPES FROM CONFIGURATION =====
                // All custom recipes are now centralized in RecipeConfig.java
                // To add/remove recipes: just edit RecipeConfig.SHAPED_RECIPES or SHAPELESS_RECIPES

                int customBefore = builder.getTotalGenerated();

                // Load shaped recipes from config
                RecipeConfig.SHAPED_RECIPES.forEach((recipeId, recipe) -> {
                    ShapedRecipeJsonBuilder shapeBuilder = createShaped(RecipeCategory.MISC, recipe.output);
                    for (String line : recipe.pattern) {
                        if (!line.isEmpty()) {
                            shapeBuilder.pattern(line);
                        }
                    }
                    recipe.ingredients.forEach(shapeBuilder::input);
                    shapeBuilder.criterion(hasItem(recipe.criterion), conditionsFromItem(recipe.criterion));
                    shapeBuilder.offerTo(exporter, new Identifier(TutorialMod.MOD_ID, recipeId));
                });

                // Load shapeless recipes from config
                RecipeConfig.SHAPELESS_RECIPES.forEach((recipeId, recipe) -> {
                    ShapelessRecipeJsonBuilder builder2 = createShapeless(RecipeCategory.MISC, recipe.output, recipe.count);
                    recipe.ingredients.forEach(builder2::input);
                    builder2.criterion(hasItem(recipe.criterion), conditionsFromItem(recipe.criterion));
                    builder2.offerTo(exporter, new Identifier(TutorialMod.MOD_ID, recipeId));
                });

                int customCount = RecipeConfig.SHAPED_RECIPES.size() + RecipeConfig.SHAPELESS_RECIPES.size();
                TutorialMod.LOGGER.info("  ✓ Loaded {} custom recipes from RecipeConfig", customCount);

                // ===== COMPACTING RECIPES =====
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, ModItems.PINK_GARNET, RecipeCategory.DECORATIONS, ModBlocks.PINK_GARNET_BLOCK);

                // ===== SMITHING TRIM RECIPES =====
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

























