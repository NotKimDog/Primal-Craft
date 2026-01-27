package net.kaupenjoe.tutorialmod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.kaupenjoe.tutorialmod.item.ModItems;
import net.kaupenjoe.tutorialmod.trim.ModTrimPatterns;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.data.recipe.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Extensible recipe provider with pluggable recipe generators
 * This design allows easy addition of new recipe types without modifying core logic
 */
public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            private final RecipeGeneratorContext context = new RecipeGeneratorContext(this, exporter);
            private final List<IRecipeGenerator> generators = new ArrayList<>();

            @Override
            public void generate() {
                long startTime = System.currentTimeMillis();
                TutorialMod.LOGGER.info("Starting automated recipe generation...");

                // Register all recipe generators
                registerGenerators();

                // Execute all generators
                generators.forEach(generator -> {
                    try {
                        generator.generate(context);
                    } catch (Exception e) {
                        TutorialMod.LOGGER.error("Error in recipe generator {}: {}",
                            generator.getClass().getSimpleName(), e.getMessage());
                    }
                });

                long elapsed = System.currentTimeMillis() - startTime;
                TutorialMod.LOGGER.info("Recipe generation completed in {}ms", elapsed);
            }

            /**
             * Register all available recipe generators
             * Add new generators here to extend functionality
             */
            private void registerGenerators() {
                generators.add(new OreSmeltingGenerator());
                generators.add(new ToolRecipeGenerator());
                generators.add(new ArmorRecipeGenerator());
                generators.add(new CustomRecipeGenerator());
                generators.add(new CompactingRecipeGenerator());
                generators.add(new TrimRecipeGenerator());
            }
        };
    }

    @Override
    public String getName() {
        return "TutorialMod Recipes";
    }

    // ===== RECIPE GENERATOR INTERFACE =====
    /**
     * Interface for pluggable recipe generators
     */
    private interface IRecipeGenerator {
        void generate(RecipeGeneratorContext context) throws Exception;
    }

    // ===== RECIPE GENERATOR CONTEXT =====
    /**
     * Context passed to all generators with access to exporter and helper methods
     */
    private class RecipeGeneratorContext {
        public final RecipeGenerator generator;
        public final RecipeExporter exporter;

        public RecipeGeneratorContext(RecipeGenerator generator, RecipeExporter exporter) {
            this.generator = generator;
            this.exporter = exporter;
        }

        // Helper methods available to all generators
        public ShapedRecipeJsonBuilder createShaped(RecipeCategory category, ItemConvertible output) {
            return generator.createShaped(category, output);
        }

        public ShapelessRecipeJsonBuilder createShapeless(RecipeCategory category, ItemConvertible output, int count) {
            return generator.createShapeless(category, output, count);
        }

        public void offerSmelting(List<ItemConvertible> inputs, RecipeCategory category, ItemConvertible output,
                                float experience, int cookTime, String id) {
            generator.offerSmelting(inputs, category, output, experience, cookTime, id);
        }

        public void offerBlasting(List<ItemConvertible> inputs, RecipeCategory category, ItemConvertible output,
                                 float experience, int cookTime, String id) {
            generator.offerBlasting(inputs, category, output, experience, cookTime, id);
        }

        public String hasItem(ItemConvertible item) {
            return generator.hasItem(item);
        }

        public AdvancementCriterion<?> conditionsFromItem(ItemConvertible item) {
            return generator.conditionsFromItem(item);
        }
    }

    // ===== RECIPE GENERATORS =====

    /**
     * Generates smelting and blasting recipes for ores
     */
    private class OreSmeltingGenerator implements IRecipeGenerator {
        @Override
        public void generate(RecipeGeneratorContext ctx) {
            for (var ore : DatagenHelper.getOres()) {
                String materialName = DatagenHelper.getMaterialName(ore.name());
                Item material = DatagenHelper.findMaterialItem(materialName);

                if (material != null) {
                    ctx.offerSmelting(List.of(ore.block()), RecipeCategory.MISC, material, 0.25f, 200, materialName);
                    ctx.offerBlasting(List.of(ore.block()), RecipeCategory.MISC, material, 0.25f, 100, materialName);
                    TutorialMod.LOGGER.info("  ✓ Ore recipes for: {}", ore.name());
                }
            }
        }
    }

    /**
     * Generates tool recipes (swords, pickaxes, hammers, shovels, axes, hoes)
     */
    private class ToolRecipeGenerator implements IRecipeGenerator {
        @Override
        public void generate(RecipeGeneratorContext ctx) {
            List<Item> sticks = DatagenHelper.getItemsContaining("stick").stream()
                .map(DatagenHelper.ItemEntry::item)
                .toList();
            Ingredient STICKS = Ingredient.ofItems(sticks.toArray(new Item[0]));

            generateToolSet(ctx, "sword", DatagenHelper.getSwords(), STICKS, "obsidian");
            generateToolSet(ctx, "hammer", DatagenHelper.getItemsContaining("hammer"), STICKS, null);
            generateToolSet(ctx, "shovel", DatagenHelper.getShovels(), STICKS, null);
            generateToolSet(ctx, "axe", DatagenHelper.getAxes(), STICKS, "obsidian");
            generateToolSet(ctx, "hoe", DatagenHelper.getHoes(), STICKS, null);
        }

        private void generateToolSet(RecipeGeneratorContext ctx, String toolType,
                                    List<DatagenHelper.ItemEntry> items, Ingredient stick, String excludePattern) {
            int count = 0;
            for (var entry : items) {
                if (excludePattern != null && entry.name().contains(excludePattern)) continue;

                String materialName = DatagenHelper.getMaterialName(entry.name());
                Item material = DatagenHelper.findMaterialItem(materialName);

                if (material != null) {
                    generateToolRecipe(ctx, toolType, entry.item(), entry.name(), material, stick);
                    count++;
                }
            }
            TutorialMod.LOGGER.info("  ✓ Added {} {} recipes", count, toolType);
        }

        private void generateToolRecipe(RecipeGeneratorContext ctx, String toolType, ItemConvertible output,
                                       String itemName, ItemConvertible material, Ingredient stick) {
            RecipeCategory category = toolType.equals("sword") ? RecipeCategory.COMBAT : RecipeCategory.TOOLS;
            ShapedRecipeJsonBuilder builder = ctx.createShaped(category, output);

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
            builder.criterion(ctx.hasItem(material), ctx.conditionsFromItem(material));
            builder.offerTo(ctx.exporter, itemName);
        }
    }

    /**
     * Generates armor recipes (helmets, chestplates, leggings, boots)
     */
    private class ArmorRecipeGenerator implements IRecipeGenerator {
        @Override
        public void generate(RecipeGeneratorContext ctx) {
            generateArmorSet(ctx, "helmet", DatagenHelper.getHelmets(), null);
            generateArmorSet(ctx, "chestplate", DatagenHelper.getChestplates(), "pajama");
            generateArmorSet(ctx, "leggings", DatagenHelper.getLeggings(), "pajama");
            generateArmorSet(ctx, "boots", DatagenHelper.getBoots(), null);
        }

        private void generateArmorSet(RecipeGeneratorContext ctx, String armorType,
                                     List<DatagenHelper.ItemEntry> items, String excludePattern) {
            int count = 0;
            for (var entry : items) {
                if (excludePattern != null && entry.name().contains(excludePattern)) continue;

                String materialName = DatagenHelper.getMaterialName(entry.name());
                Item material = DatagenHelper.findMaterialItem(materialName);

                if (material != null) {
                    generateArmorRecipe(ctx, armorType, entry.item(), entry.name(), material);
                    count++;
                }
            }
            TutorialMod.LOGGER.info("  ✓ Added {} {} recipes", count, armorType);
        }

        private void generateArmorRecipe(RecipeGeneratorContext ctx, String armorType,
                                        ItemConvertible output, String itemName, ItemConvertible material) {
            ShapedRecipeJsonBuilder builder = ctx.createShaped(RecipeCategory.COMBAT, output);

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
            builder.criterion(ctx.hasItem(material), ctx.conditionsFromItem(material));
            builder.offerTo(ctx.exporter, itemName);
        }
    }

    /**
     * Generates custom recipes from configuration
     */
    private class CustomRecipeGenerator implements IRecipeGenerator {
        @Override
        public void generate(RecipeGeneratorContext ctx) {
            // Shaped recipes
            RecipeConfig.SHAPED_RECIPES.forEach((recipeId, recipe) -> {
                ShapedRecipeJsonBuilder builder = ctx.createShaped(RecipeCategory.MISC, recipe.output);
                for (String line : recipe.pattern) {
                    if (!line.isEmpty()) {
                        builder.pattern(line);
                    }
                }
                recipe.ingredients.forEach((ch, item) -> builder.input(ch, item));
                builder.criterion(ctx.hasItem(recipe.criterion), ctx.conditionsFromItem(recipe.criterion));
                builder.offerTo(ctx.exporter, recipeId);
            });

            // Shapeless recipes
            RecipeConfig.SHAPELESS_RECIPES.forEach((recipeId, recipe) -> {
                ShapelessRecipeJsonBuilder builder = ctx.createShapeless(RecipeCategory.MISC, recipe.output, recipe.count);
                recipe.ingredients.forEach(builder::input);
                builder.criterion(ctx.hasItem(recipe.criterion), ctx.conditionsFromItem(recipe.criterion));
                builder.offerTo(ctx.exporter, recipeId);
            });

            int total = RecipeConfig.SHAPED_RECIPES.size() + RecipeConfig.SHAPELESS_RECIPES.size();
            TutorialMod.LOGGER.info("  ✓ Loaded {} custom recipes from RecipeConfig", total);
        }
    }

    /**
     * Generates compacting recipes
     */
    private class CompactingRecipeGenerator implements IRecipeGenerator {
        @Override
        public void generate(RecipeGeneratorContext ctx) {
            ctx.generator.offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS,
                ModItems.PINK_GARNET, RecipeCategory.DECORATIONS, ModBlocks.PINK_GARNET_BLOCK);
            TutorialMod.LOGGER.info("  ✓ Compacting recipes generated");
        }
    }

    /**
     * Generates trim recipes
     */
    private class TrimRecipeGenerator implements IRecipeGenerator {
        @Override
        public void generate(RecipeGeneratorContext ctx) {
            ctx.generator.offerSmithingTrimRecipe(ModItems.KAUPEN_SMITHING_TEMPLATE,
                ModTrimPatterns.KAUPEN,
                RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(TutorialMod.MOD_ID, "kaupen")));
            TutorialMod.LOGGER.info("  ✓ Trim recipes generated");
        }
    }
}

