package net.kimdog_studios.primal_craft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.block.ModBlocks;
import net.kimdog_studios.primal_craft.item.ModItems;
import net.kimdog_studios.primal_craft.trim.ModTrimPatterns;
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
                PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
                PrimalCraft.LOGGER.info("║  STARTING RECIPE GENERATION                                ║");
                PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");

                int smeltingRecipes = 0;
                int blastingRecipes = 0;
                int toolRecipes = 0;
                int armorRecipes = 0;

                // ===== AUTO-DETECT ORES AND CREATE SMELTING RECIPES =====
                var ores = DatagenHelper.getOres();
                for (var oreEntry : ores) {
                    String materialName = DatagenHelper.getMaterialName(oreEntry.name());
                    Item materialItem = DatagenHelper.findMaterialItem(materialName);
                    if (materialItem != null) {
                        offerSmelting(List.of(oreEntry.block()), RecipeCategory.MISC, materialItem, 0.25f, 200, materialName);
                        smeltingRecipes++;
                        offerBlasting(List.of(oreEntry.block()), RecipeCategory.MISC, materialItem, 0.25f, 100, materialName);
                        blastingRecipes++;
                    }
                }
                PrimalCraft.LOGGER.info("  ✓ Smelting recipes: {} recipes", smeltingRecipes);
                PrimalCraft.LOGGER.info("  ✓ Blasting recipes: {} recipes", blastingRecipes);

                // ===== AUTO-GENERATE STICK INGREDIENT =====
                List<Item> allSticks = DatagenHelper.getItemsContaining("stick").stream()
                    .map(DatagenHelper.ItemEntry::item)
                    .toList();
                Ingredient STICKS = Ingredient.ofItems(allSticks.toArray(new Item[0]));
                PrimalCraft.LOGGER.debug("  • Detected {} stick variants", allSticks.size());

                // ===== AUTO-GENERATE TOOL RECIPES =====
                var swordCount = generateToolRecipes("sword", DatagenHelper.getSwords(), STICKS, "obsidian");
                toolRecipes += swordCount;
                // Skip pickaxe - hammer is included in pickaxes list causing duplicates
                // generateToolRecipes("pickaxe", DatagenHelper.getPickaxes(), STICKS, "hammer");
                var hammerCount = generateToolRecipes("hammer", DatagenHelper.getItemsContaining("hammer"), STICKS, null);
                toolRecipes += hammerCount;
                var shovelCount = generateToolRecipes("shovel", DatagenHelper.getShovels(), STICKS, null);
                toolRecipes += shovelCount;
                var axeCount = generateToolRecipes("axe", DatagenHelper.getAxes(), STICKS, "obsidian");
                toolRecipes += axeCount;
                var hoeCount = generateToolRecipes("hoe", DatagenHelper.getHoes(), STICKS, null);
                toolRecipes += hoeCount;

                PrimalCraft.LOGGER.info("  ✓ Tool recipes: {} recipes (swords: {}, hammers: {}, shovels: {}, axes: {}, hoes: {})",
                    toolRecipes, swordCount, hammerCount, shovelCount, axeCount, hoeCount);

                // ===== AUTO-GENERATE ARMOR RECIPES =====
                var helmetCount = generateArmorRecipes("helmet", DatagenHelper.getHelmets(), null);
                armorRecipes += helmetCount;
                var chestplateCount = generateArmorRecipes("chestplate", DatagenHelper.getChestplates(), "pajama");
                armorRecipes += chestplateCount;
                var leggingsCount = generateArmorRecipes("leggings", DatagenHelper.getLeggings(), "pajama");
                armorRecipes += leggingsCount;
                var bootsCount = generateArmorRecipes("boots", DatagenHelper.getBoots(), null);
                armorRecipes += bootsCount;

                PrimalCraft.LOGGER.info("  ✓ Armor recipes: {} recipes (helmets: {}, chestplates: {}, leggings: {}, boots: {})",
                    armorRecipes, helmetCount, chestplateCount, leggingsCount, bootsCount);

                // ===== CUSTOM RECIPES FROM CONFIGURATION =====
                // Load shaped recipes from config
                RecipeConfig.SHAPED_RECIPES.forEach((recipeId, recipe) -> {
                    ShapedRecipeJsonBuilder shapeBuilder = createShaped(RecipeCategory.MISC, recipe.output);
                    for (String line : recipe.pattern) {
                        if (!line.isEmpty()) {
                            shapeBuilder.pattern(line);
                        }
                    }
                    recipe.ingredients.forEach((ch, item) -> shapeBuilder.input(ch, item));
                    shapeBuilder.criterion(hasItem(recipe.criterion), conditionsFromItem(recipe.criterion));
                    shapeBuilder.offerTo(exporter, recipeId);
                });

                // Load shapeless recipes from config
                RecipeConfig.SHAPELESS_RECIPES.forEach((recipeId, recipe) -> {
                    ShapelessRecipeJsonBuilder shapelessBuilder = createShapeless(RecipeCategory.MISC, recipe.output, recipe.count);
                    recipe.ingredients.forEach(shapelessBuilder::input);
                    shapelessBuilder.criterion(hasItem(recipe.criterion), conditionsFromItem(recipe.criterion));
                    shapelessBuilder.offerTo(exporter, recipeId);
                });

                int customCount = RecipeConfig.SHAPED_RECIPES.size() + RecipeConfig.SHAPELESS_RECIPES.size();
                PrimalCraft.LOGGER.info("  ✓ Custom recipes: {} recipes (shaped: {}, shapeless: {})",
                    customCount, RecipeConfig.SHAPED_RECIPES.size(), RecipeConfig.SHAPELESS_RECIPES.size());

                // ===== COMPACTING RECIPES =====
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, ModItems.PINK_GARNET, RecipeCategory.DECORATIONS, ModBlocks.PINK_GARNET_BLOCK);
                PrimalCraft.LOGGER.debug("  • Added compacting recipes");

                // ===== SMITHING TRIM RECIPES =====
                offerSmithingTrimRecipe(ModItems.KAUPEN_SMITHING_TEMPLATE, ModTrimPatterns.KAUPEN,
                        RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(PrimalCraft.MOD_ID, "kaupen")));
                PrimalCraft.LOGGER.debug("  • Added smithing trim recipes");

                long elapsed = System.currentTimeMillis() - startTime;
                int totalRecipes = smeltingRecipes + blastingRecipes + toolRecipes + armorRecipes + customCount + 2;
                PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
                PrimalCraft.LOGGER.info("║  RECIPE GENERATION COMPLETE                                ║");
                PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");
                PrimalCraft.LOGGER.info("  📊 Total Recipes Generated: {}", totalRecipes);
                PrimalCraft.LOGGER.info("  ⏱️  Execution Time: {}ms", elapsed);
            }

            /**
             * Generate tool recipes with batch processing
             */
            private int generateToolRecipes(String toolType, List<DatagenHelper.ItemEntry> items, Ingredient stick, String excludePattern) {
                int count = 0;
                for (var entry : items) {
                    if (excludePattern != null && entry.name().contains(excludePattern)) continue;

                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);

                    if (material != null) {
                        generateToolRecipe(toolType, entry.item(), entry.name(), material, stick);
                        count++;
                    }
                }
                return count;
            }

            /**
             * Generate a single tool recipe with explicit item name
             */
            private void generateToolRecipe(String toolType, ItemConvertible output, String itemName, ItemConvertible material, Ingredient stick) {
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
                    case "hammer" -> {
                        builder.pattern("MM ");
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
                builder.offerTo(exporter, itemName);
            }

            /**
             * Generate armor recipes with batch processing
             */
            private int generateArmorRecipes(String armorType, List<DatagenHelper.ItemEntry> items, String excludePattern) {
                int count = 0;
                for (var entry : items) {
                    if (excludePattern != null && entry.name().contains(excludePattern)) continue;

                    String materialName = DatagenHelper.getMaterialName(entry.name());
                    Item material = DatagenHelper.findMaterialItem(materialName);

                    if (material != null) {
                        generateArmorRecipe(armorType, entry.item(), entry.name(), material);
                        count++;
                    }
                }
                return count;
            }

            /**
             * Generate a single armor recipe with explicit item name
             */
            private void generateArmorRecipe(String armorType, ItemConvertible output, String itemName, ItemConvertible material) {
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
                builder.offerTo(exporter, itemName);
            }
        };
    }

    @Override
    public String getName() {
        return "TutorialMod Recipes";
    }
}

























