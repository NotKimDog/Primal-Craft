package net.kaupenjoe.tutorialmod.datagen;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.data.recipe.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Advanced automated recipe builder for future-proof recipe generation
 * Handles all common recipe patterns in one place
 */
public class AdvancedRecipeBuilder {
    private final RecipeExporter exporter;
    private final Set<String> generatedRecipeIds = new HashSet<>();
    private int totalGenerated = 0;
    private final Map<String, ToolRecipePattern> toolPatterns = new HashMap<>();
    private final Map<String, ArmorRecipePattern> armorPatterns = new HashMap<>();
    private final RecipeProvider provider;

    public AdvancedRecipeBuilder(RecipeExporter exporter, RecipeProvider provider) {
        this.exporter = exporter;
        this.provider = provider;
        initializePatterns();
    }

    /**
     * Initialize standard tool and armor patterns
     */
    private void initializePatterns() {
        toolPatterns.put("sword", new ToolRecipePattern(
            new String[]{"M", "M", "S"},
            new char[]{'M', 'S'}
        ));

        toolPatterns.put("pickaxe", new ToolRecipePattern(
            new String[]{"MMM", "MS", "MS"},
            new char[]{'M', 'S'}
        ));

        toolPatterns.put("hammer", new ToolRecipePattern(
            new String[]{"MM", "MS", "MS"},
            new char[]{'M', 'S'}
        ));

        toolPatterns.put("shovel", new ToolRecipePattern(
            new String[]{"M", "S", "S"},
            new char[]{'M', 'S'}
        ));

        toolPatterns.put("axe", new ToolRecipePattern(
            new String[]{"MM", "MS", "S"},
            new char[]{'M', 'S'}
        ));

        toolPatterns.put("hoe", new ToolRecipePattern(
            new String[]{"MM", "S", "S"},
            new char[]{'M', 'S'}
        ));

        // Armor patterns
        armorPatterns.put("helmet", new ArmorRecipePattern(
            new String[]{"MMM", "M M"},
            new char[]{'M'}
        ));

        armorPatterns.put("chestplate", new ArmorRecipePattern(
            new String[]{"M M", "MMM", "MMM"},
            new char[]{'M'}
        ));

        armorPatterns.put("leggings", new ArmorRecipePattern(
            new String[]{"MMM", "M M", "M M"},
            new char[]{'M'}
        ));

        armorPatterns.put("boots", new ArmorRecipePattern(
            new String[]{"", "M M", "M M"},
            new char[]{'M'}
        ));
    }

    /**
     * Add a simple shaped tool recipe with duplicate prevention
     */
    public void addToolRecipe(String toolType, ItemConvertible output, ItemConvertible material, Ingredient stick) {
        ToolRecipePattern pattern = toolPatterns.get(toolType.toLowerCase());
        if (pattern == null) {
            TutorialMod.LOGGER.warn("Unknown tool type: {}", toolType);
            return;
        }

        String materialName = DatagenHelper.getMaterialName(output.asItem().toString());
        String recipeId = materialName + "_" + toolType;

        if (!canAddRecipe(recipeId)) {
            TutorialMod.LOGGER.warn("Duplicate recipe prevented: {}", recipeId);
            return;
        }

        RecipeCategory category = toolType.equals("sword") ? RecipeCategory.COMBAT : RecipeCategory.TOOLS;

        ShapedRecipeJsonBuilder builder = provider.createShaped(category, output);
        for (String line : pattern.shape) {
            builder.pattern(line);
        }

        builder.input('M', material);
        builder.input('S', stick);
        builder.criterion(provider.hasItem(material), provider.conditionsFromItem(material));

        try {
            builder.offerTo(exporter);
            recordRecipe(recipeId);
            totalGenerated++;
        } catch (Exception e) {
            TutorialMod.LOGGER.warn("Failed to offer tool recipe {}: {}", recipeId, e.getMessage());
        }
    }

    /**
     * Add a simple shaped armor recipe
     */
    public void addArmorRecipe(String armorType, ItemConvertible output, ItemConvertible material) {
        ArmorRecipePattern pattern = armorPatterns.get(armorType.toLowerCase());
        if (pattern == null) {
            TutorialMod.LOGGER.warn("Unknown armor type: {}", armorType);
            return;
        }

        String materialName = DatagenHelper.getMaterialName(output.asItem().toString());
        String recipeId = materialName;

        if (!canAddRecipe(recipeId)) {
            TutorialMod.LOGGER.warn("Duplicate recipe prevented: {}", recipeId);
            return;
        }

        ShapedRecipeJsonBuilder builder = provider.createShaped(RecipeCategory.COMBAT, output);
        for (String line : pattern.shape) {
            if (!line.isEmpty()) {
                builder.pattern(line);
            }
        }

        builder.input('M', material);
        builder.criterion(provider.hasItem(material), provider.conditionsFromItem(material));

        try {
            builder.offerTo(exporter);
            recordRecipe(recipeId);
            totalGenerated++;
        } catch (Exception e) {
            TutorialMod.LOGGER.warn("Failed to offer armor recipe {}: {}", recipeId, e.getMessage());
        }
    }

    /**
     * Batch add recipes from criteria
     */
    public void batchAddToolRecipes(String toolType, List<DatagenHelper.ItemEntry> items,
                                   net.minecraft.recipe.Ingredient stickIngredient, String excludePattern) {
        int before = totalGenerated;

        for (var entry : items) {
            if (excludePattern != null && entry.name().contains(excludePattern)) continue;

            String materialName = DatagenHelper.getMaterialName(entry.name());
            Item material = DatagenHelper.findMaterialItem(materialName);

            if (material != null) {
                addToolRecipe(toolType, entry.item(), material, stickIngredient);
            }
        }

        TutorialMod.LOGGER.info("  ✓ Added {} {} recipes", totalGenerated - before, toolType);
    }

    /**
     * Batch add armor recipes
     */
    public void batchAddArmorRecipes(String armorType, List<DatagenHelper.ItemEntry> items, String excludePattern) {
        int before = totalGenerated;

        for (var entry : items) {
            if (excludePattern != null && entry.name().contains(excludePattern)) continue;

            String materialName = DatagenHelper.getMaterialName(entry.name());
            Item material = DatagenHelper.findMaterialItem(materialName);

            if (material != null) {
                addArmorRecipe(armorType, entry.item(), material);
            }
        }

        TutorialMod.LOGGER.info("  ✓ Added {} {} recipes", totalGenerated - before, armorType);
    }

    /**
     * Check if a recipe can be added (no duplicates)
     */
    private boolean canAddRecipe(String recipeId) {
        return generatedRecipeIds.add(recipeId);
    }

    /**
     * Record a generated recipe
     */
    private void recordRecipe(String recipeId) {
        generatedRecipeIds.add(recipeId);
    }

    /**
     * Get total recipes generated
     */
    public int getTotalGenerated() {
        return totalGenerated;
    }

    /**
     * Get list of generated recipe IDs
     */
    public Set<String> getGeneratedRecipeIds() {
        return new HashSet<>(generatedRecipeIds);
    }

    /**
     * Internal pattern representation for tools
     */
    public static class ToolRecipePattern {
        public final String[] shape;
        public final char[] inputs;

        public ToolRecipePattern(String[] shape, char[] inputs) {
            this.shape = shape;
            this.inputs = inputs;
        }
    }

    /**
     * Internal pattern representation for armor
     */
    public static class ArmorRecipePattern {
        public final String[] shape;
        public final char[] inputs;

        public ArmorRecipePattern(String[] shape, char[] inputs) {
            this.shape = shape;
            this.inputs = inputs;
        }
    }
}
