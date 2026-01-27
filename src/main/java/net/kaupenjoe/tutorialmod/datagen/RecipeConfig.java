package net.kaupenjoe.tutorialmod.datagen;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.kaupenjoe.tutorialmod.item.ModItems;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import java.util.*;

/**
 * Centralized recipe configuration for all custom/special recipes.
 * This is the ONLY place you need to modify to add new special recipes!
 * No need to touch Java code - just add to the lists below.
 *
 * Format:
 * new CustomRecipeConfig()
 *   .withShapedRecipe("recipe_id", output, pattern, ingredients...)
 *   .withShapelessRecipe("recipe_id", output, count, ingredients...)
 *   .etc
 */
public class RecipeConfig {

    public static final Map<String, CustomShapedRecipe> SHAPED_RECIPES = new LinkedHashMap<>();
    public static final Map<String, CustomShapelessRecipe> SHAPELESS_RECIPES = new LinkedHashMap<>();
    public static final List<CompactingRecipe> COMPACTING_RECIPES = new ArrayList<>();

    static {
        long startTime = System.currentTimeMillis();
        TutorialMod.LOGGER.debug("📝 Initializing RecipeConfig...");

        initializeShapedRecipes();
        initializeShapelessRecipes();
        initializeCompactingRecipes();

        long elapsed = System.currentTimeMillis() - startTime;
        TutorialMod.LOGGER.debug("  ✓ RecipeConfig initialized with {} shaped, {} shapeless, {} compacting recipes in {}ms",
            SHAPED_RECIPES.size(), SHAPELESS_RECIPES.size(), COMPACTING_RECIPES.size(), elapsed);
    }

    /**
     * Initialize all shaped recipes - easily add new ones here!
     */
    private static void initializeShapedRecipes() {
        // ===== COMPACTING BLOCKS =====
        SHAPED_RECIPES.put("raw_pink_garnet_block", new CustomShapedRecipe(
            ModBlocks.RAW_PINK_GARNET_BLOCK,
            new String[]{"RRR", "RRR", "RRR"},
            Map.of('R', ModItems.RAW_PINK_GARNET),
            ModItems.RAW_PINK_GARNET
        ));

        // ===== PAJAMAS =====
        SHAPED_RECIPES.put("pajama_top", new CustomShapedRecipe(
            ModItems.PAJAMA_TOP,
            new String[]{"B B", "BBB", "BBB"},
            Map.of('B', Items.BLACK_WOOL),
            Items.BLACK_WOOL
        ));

        SHAPED_RECIPES.put("pajama_bottoms", new CustomShapedRecipe(
            ModItems.PAJAMA_BOTTOMS,
            new String[]{"RRR", "R R", "R R"},
            Map.of('R', Items.RED_WOOL),
            Items.RED_WOOL
        ));

        // ===== HORSE ARMOR =====
        SHAPED_RECIPES.put("pink_garnet_horse_armor", new CustomShapedRecipe(
            ModItems.PINK_GARNET_HORSE_ARMOR,
            new String[]{"PPP", "PPP", "P P"},
            Map.of('P', ModItems.PINK_GARNET),
            ModItems.PINK_GARNET
        ));

        // ===== DECORATIVE BLOCKS =====
        SHAPED_RECIPES.put("pink_garnet_lamp", new CustomShapedRecipe(
            ModBlocks.PINK_GARNET_LAMP,
            new String[]{"PPP", "PRP", "PPP"},
            Map.of('P', ModItems.PINK_GARNET, 'R', Items.REDSTONE),
            ModItems.PINK_GARNET
        ));

        SHAPED_RECIPES.put("pink_garnet_stairs", new CustomShapedRecipe(
            ModBlocks.PINK_GARNET_STAIRS,
            new String[]{"P  ", "PP ", "PPP"},
            Map.of('P', ModItems.PINK_GARNET),
            ModItems.PINK_GARNET
        ));

        SHAPED_RECIPES.put("pink_garnet_slab", new CustomShapedRecipe(
            ModBlocks.PINK_GARNET_SLAB,
            new String[]{"PPP"},
            Map.of('P', ModItems.PINK_GARNET),
            ModItems.PINK_GARNET
        ));

        // ===== DRIFTWOOD FURNITURE =====
        SHAPED_RECIPES.put("chair", new CustomShapedRecipe(
            ModBlocks.CHAIR,
            new String[]{" P ", "PS ", "   "},
            Map.of('P', ModBlocks.DRIFTWOOD_PLANKS, 'S', Items.STICK),
            ModBlocks.DRIFTWOOD_PLANKS
        ));

        SHAPED_RECIPES.put("pedestal", new CustomShapedRecipe(
            ModBlocks.PEDESTAL,
            new String[]{" P ", "P P", " P "},
            Map.of('P', ModBlocks.DRIFTWOOD_PLANKS),
            ModBlocks.DRIFTWOOD_PLANKS
        ));

        // ===== GROWTH CHAMBER =====
        SHAPED_RECIPES.put("growth_chamber", new CustomShapedRecipe(
            ModBlocks.GROWTH_CHAMBER,
            new String[]{"MMM", "RCR", "OOO"},
            Map.of('M', ModBlocks.MAGIC_BLOCK, 'R', ModItems.PINK_GARNET, 'C', Items.CHEST, 'O', Items.OBSERVER),
            ModBlocks.MAGIC_BLOCK
        ));

        // ===== MAGIC BLOCK (HARD RECIPE) =====
        SHAPED_RECIPES.put("magic_block_hard", new CustomShapedRecipe(
            ModBlocks.MAGIC_BLOCK,
            new String[]{"GSG", "SNS", "GGG"},
            Map.of('G', ModBlocks.PINK_GARNET_BLOCK, 'S', ModItems.STARLIGHT_ASHES, 'N', Items.NETHER_STAR),
            ModItems.STARLIGHT_ASHES
        ));

        // ===== CUSTOM CRAFTING TABLES =====
        List<Map.Entry<ItemConvertible, String>> woodTypes = List.of(
            Map.entry(Blocks.ACACIA_PLANKS, "acacia"),
            Map.entry(Blocks.BAMBOO_PLANKS, "bamboo"),
            Map.entry(Blocks.BIRCH_PLANKS, "birch"),
            Map.entry(Blocks.CHERRY_PLANKS, "cherry"),
            Map.entry(Blocks.CRIMSON_PLANKS, "crimson"),
            Map.entry(Blocks.DARK_OAK_PLANKS, "dark_oak"),
            Map.entry(Blocks.JUNGLE_PLANKS, "jungle"),
            Map.entry(Blocks.MANGROVE_PLANKS, "mangrove"),
            Map.entry(Blocks.PALE_OAK_PLANKS, "pale_oak"),
            Map.entry(Blocks.SPRUCE_PLANKS, "spruce"),
            Map.entry(Blocks.WARPED_PLANKS, "warped")
        );

        // Add crafting table recipes dynamically
        woodTypes.forEach(entry -> {
            ItemConvertible planks = entry.getKey();
            String woodName = entry.getValue();
            ItemConvertible table = getCraftingTableForWood(woodName);
            if (table != null) {
                SHAPED_RECIPES.put(woodName + "_crafting_table", new CustomShapedRecipe(
                    table,
                    new String[]{"PP ", "PP ", "   "},
                    Map.of('P', planks),
                    planks
                ));
            }
        });
    }

    /**
     * Initialize all shapeless recipes - easily add new ones here!
     */
    private static void initializeShapelessRecipes() {
        // ===== RAW MATERIALS =====
        SHAPELESS_RECIPES.put("raw_pink_garnet_from_block", new CustomShapelessRecipe(
            ModItems.RAW_PINK_GARNET, 9,
            List.of(ModBlocks.RAW_PINK_GARNET_BLOCK),
            ModBlocks.RAW_PINK_GARNET_BLOCK
        ));

        SHAPELESS_RECIPES.put("raw_pink_garnet_from_magic_block", new CustomShapelessRecipe(
            ModItems.RAW_PINK_GARNET, 32,
            List.of(ModBlocks.MAGIC_BLOCK),
            ModBlocks.MAGIC_BLOCK
        ));

        // ===== SPECIAL ITEMS =====
        SHAPELESS_RECIPES.put("spectre_staff", new CustomShapelessRecipe(
            ModItems.SPECTRE_STAFF, 1,
            List.of(ModItems.PINK_GARNET, Items.BLAZE_ROD),
            ModItems.PINK_GARNET
        ));

        SHAPELESS_RECIPES.put("bar_brawl_music_disc", new CustomShapelessRecipe(
            ModItems.BAR_BRAWL_MUSIC_DISC, 1,
            List.of(Items.MUSIC_DISC_13, ModItems.PINK_GARNET),
            ModItems.PINK_GARNET
        ));

        SHAPELESS_RECIPES.put("kaupen_bow", new CustomShapelessRecipe(
            ModItems.KAUPEN_BOW, 1,
            List.of(Items.STRING, Items.STRING, Items.STRING, ModItems.PINK_GARNET),
            ModItems.PINK_GARNET
        ));

        // ===== SEEDS & PLANTS =====
        SHAPELESS_RECIPES.put("cauliflower_seeds", new CustomShapelessRecipe(
            ModItems.CAULIFLOWER_SEEDS, 1,
            List.of(ModItems.CAULIFLOWER),
            ModItems.CAULIFLOWER
        ));

        SHAPELESS_RECIPES.put("honey_berries_from_bush", new CustomShapelessRecipe(
            ModItems.HONEY_BERRIES, 2,
            List.of(ModBlocks.HONEY_BERRY_BUSH),
            ModBlocks.HONEY_BERRY_BUSH
        ));

        // ===== TOOLS (NON-STANDARD) =====
        SHAPELESS_RECIPES.put("tomahawk", new CustomShapelessRecipe(
            ModItems.TOMAHAWK, 1,
            List.of(ModItems.PINK_GARNET, ModItems.PINK_GARNET, Items.STICK),
            ModItems.PINK_GARNET
        ));

        SHAPELESS_RECIPES.put("obsidian_axe", new CustomShapelessRecipe(
            ModItems.OBSIDIAN_AXE, 1,
            List.of(Items.OBSIDIAN, Items.OBSIDIAN, Items.STICK),
            Items.OBSIDIAN
        ));
    }

    /**
     * Initialize compacting recipes (block <-> items)
     */
    private static void initializeCompactingRecipes() {
        // Example: COMPACTING_RECIPES.add(new CompactingRecipe(ModItems.PINK_GARNET, ModBlocks.PINK_GARNET_BLOCK, 9));
    }

    /**
     * Get the crafting table for a given wood type
     */
    private static ItemConvertible getCraftingTableForWood(String woodName) {
        return switch(woodName) {
            case "acacia" -> ModBlocks.ACACIA_CRAFTING_TABLE;
            case "bamboo" -> ModBlocks.BAMBOO_CRAFTING_TABLE;
            case "birch" -> ModBlocks.BIRCH_CRAFTING_TABLE;
            case "cherry" -> ModBlocks.CHERRY_CRAFTING_TABLE;
            case "crimson" -> ModBlocks.CRIMSON_CRAFTING_TABLE;
            case "dark_oak" -> ModBlocks.DARK_OAK_CRAFTING_TABLE;
            case "jungle" -> ModBlocks.JUNGLE_CRAFTING_TABLE;
            case "mangrove" -> ModBlocks.MANGROVE_CRAFTING_TABLE;
            case "pale_oak" -> ModBlocks.PALEOAK_CRAFTING_TABLE;
            case "spruce" -> ModBlocks.SPRUCE_CRAFTING_TABLE;
            case "warped" -> ModBlocks.WARPED_CRAFTING_TABLE;
            default -> null;
        };
    }

    // ===== RECIPE CLASSES =====

    public static class CustomShapedRecipe {
        public final ItemConvertible output;
        public final String[] pattern;
        public final Map<Character, ItemConvertible> ingredients;
        public final ItemConvertible criterion;

        public CustomShapedRecipe(ItemConvertible output, String[] pattern,
                                 Map<Character, ItemConvertible> ingredients,
                                 ItemConvertible criterion) {
            this.output = output;
            this.pattern = pattern;
            this.ingredients = ingredients;
            this.criterion = criterion;
        }
    }

    public static class CustomShapelessRecipe {
        public final ItemConvertible output;
        public final int count;
        public final List<ItemConvertible> ingredients;
        public final ItemConvertible criterion;

        public CustomShapelessRecipe(ItemConvertible output, int count,
                                   List<ItemConvertible> ingredients,
                                   ItemConvertible criterion) {
            this.output = output;
            this.count = count;
            this.ingredients = ingredients;
            this.criterion = criterion;
        }
    }

    public static class CompactingRecipe {
        public final ItemConvertible item;
        public final ItemConvertible block;
        public final int neededForBlock;

        public CompactingRecipe(ItemConvertible item, ItemConvertible block, int neededForBlock) {
            this.item = item;
            this.block = block;
            this.neededForBlock = neededForBlock;
        }
    }
}
