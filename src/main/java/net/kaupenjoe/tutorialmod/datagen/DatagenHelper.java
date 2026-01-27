package net.kaupenjoe.tutorialmod.datagen;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.kaupenjoe.tutorialmod.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Automated datagen helper that uses reflection to discover all registered items and blocks.
 * Provides utilities for pattern matching and automatic categorization with optimized caching.
 *
 * Performance optimizations:
 * - Multi-level caching (items, blocks, filtered results)
 * - Lazy initialization of category caches
 * - Efficient reflection scanning with field filtering
 * - Thread-safe concurrent caching
 *
 * @author KimDog Studios
 */
public class DatagenHelper {

    // Cached collections for performance
    private static volatile List<ItemEntry> cachedItems = null;
    private static volatile List<BlockEntry> cachedBlocks = null;

    // Category caches - populated on first access
    private static final Map<String, List<ItemEntry>> itemCategoryCache = new ConcurrentHashMap<>();
    private static final Map<String, List<BlockEntry>> blockCategoryCache = new ConcurrentHashMap<>();

    // Suffix optimization
    private static final String[] TOOL_SUFFIXES = {"_sword", "_pickaxe", "_shovel", "_axe", "_hoe", "_hammer"};
    private static final String[] ARMOR_SUFFIXES = {"_helmet", "_chestplate", "_leggings", "_boots"};
    private static final String[] BLOCK_SUFFIXES = {"_block", "_ore", "_stairs", "_slab", "_wall", "_fence", "_door",
                                                     "_trapdoor", "_button", "_pressure_plate"};

    // Combined set for faster lookups
    private static final Set<String> ALL_SUFFIXES = new HashSet<>();
    static {
        ALL_SUFFIXES.addAll(Arrays.asList(TOOL_SUFFIXES));
        ALL_SUFFIXES.addAll(Arrays.asList(ARMOR_SUFFIXES));
        ALL_SUFFIXES.addAll(Arrays.asList(BLOCK_SUFFIXES));
    }

    // ===== CENTRALIZED MATERIAL SUFFIXES =====
    public static class MaterialSuffixes {
        public static final String[] ALL_SUFFIXES = {
            "_sword", "_pickaxe", "_shovel", "_axe", "_hoe", "_hammer",
            "_helmet", "_chestplate", "_leggings", "_boots",
            "_block", "_ore", "_stairs", "_slab", "_wall", "_fence", "_door",
            "_trapdoor", "_button", "_pressure_plate"
        };
    }

    /**
     * Get all registered items with their names (cached)
     */
    public static List<ItemEntry> getAllItems() {
        if (cachedItems != null) return cachedItems;

        synchronized (DatagenHelper.class) {
            if (cachedItems != null) return cachedItems;

            List<ItemEntry> items = scanItems(ModItems.class);
            cachedItems = Collections.unmodifiableList(items);
            return cachedItems;
        }
    }

    /**
     * Get all registered blocks with their names (cached)
     */
    public static List<BlockEntry> getAllBlocks() {
        if (cachedBlocks != null) return cachedBlocks;

        synchronized (DatagenHelper.class) {
            if (cachedBlocks != null) return cachedBlocks;

            List<BlockEntry> blocks = scanBlocks(ModBlocks.class);
            cachedBlocks = Collections.unmodifiableList(blocks);
            return cachedBlocks;
        }
    }

    /**
     * Generic reflection scanner - eliminates code duplication
     */
    private static <T> List<String[]> scanGeneric(Class<?> modClass, Class<T> typeClass) {
        List<String[]> results = new ArrayList<>();

        try {
            Field[] fields = modClass.getDeclaredFields();
            for (Field field : fields) {
                // Optimize: check modifiers first (fastest)
                int mods = field.getModifiers();
                if ((mods & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) !=
                    (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) {
                    continue;
                }

                // Then check type
                if (!typeClass.isAssignableFrom(field.getType())) {
                    continue;
                }

                // Finally get value
                field.setAccessible(true);
                Object value = field.get(null);
                String name = field.getName().toLowerCase();
                results.add(new String[]{name, String.valueOf(System.identityHashCode(value))});
            }
        } catch (Exception e) {
            TutorialMod.LOGGER.error("Failed to scan {} for {}: {}", modClass.getSimpleName(), typeClass.getSimpleName(), e.getMessage());
        }

        return results;
    }

    /**
     * Scan for items using reflection
     */
    private static List<ItemEntry> scanItems(Class<?> modClass) {
        List<ItemEntry> results = new ArrayList<>();

        try {
            Field[] fields = modClass.getDeclaredFields();
            for (Field field : fields) {
                // Optimize: check modifiers first (fastest)
                int mods = field.getModifiers();
                if ((mods & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) !=
                    (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) {
                    continue;
                }

                // Then check type
                if (!Item.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                // Finally get value
                field.setAccessible(true);
                Item value = (Item) field.get(null);
                String name = field.getName().toLowerCase();
                results.add(new ItemEntry(name, value));
            }
        } catch (Exception e) {
            TutorialMod.LOGGER.error("Failed to scan {} for items: {}", modClass.getSimpleName(), e.getMessage());
        }

        return results;
    }

    /**
     * Scan for blocks using reflection
     */
    private static List<BlockEntry> scanBlocks(Class<?> modClass) {
        List<BlockEntry> results = new ArrayList<>();

        try {
            Field[] fields = modClass.getDeclaredFields();
            for (Field field : fields) {
                // Optimize: check modifiers first (fastest)
                int mods = field.getModifiers();
                if ((mods & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) !=
                    (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) {
                    continue;
                }

                // Then check type
                if (!Block.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                // Finally get value
                field.setAccessible(true);
                Block value = (Block) field.get(null);
                String name = field.getName().toLowerCase();
                results.add(new BlockEntry(name, value));
            }
        } catch (Exception e) {
            TutorialMod.LOGGER.error("Failed to scan {} for blocks: {}", modClass.getSimpleName(), e.getMessage());
        }

        return results;
    }

    // ===== ITEM FILTERING METHODS =====

    /**
     * Filter items by keywords (cached) - UNIFIED METHOD
     */
    public static List<ItemEntry> getItemsContaining(String... keywords) {
        String cacheKey = String.join("|", keywords);
        return itemCategoryCache.computeIfAbsent(cacheKey, key ->
            getAllItems().stream()
                .filter(entry -> containsAny(entry.name(), keywords))
                .collect(Collectors.toUnmodifiableList())
        );
    }

    /**
     * Filter items by keywords with exclusions (cached) - UNIFIED METHOD
     */
    public static List<ItemEntry> getItemsMatching(String[] exclusions, String... inclusions) {
        String cacheKey = "items_" + String.join("|", inclusions) +
            (exclusions != null ? "_ex_" + String.join("|", exclusions) : "");
        return itemCategoryCache.computeIfAbsent(cacheKey, key ->
            getAllItems().stream()
                .filter(e -> containsAny(e.name(), inclusions))
                .filter(e -> exclusions == null || !containsAny(e.name(), exclusions))
                .collect(Collectors.toUnmodifiableList())
        );
    }

    // ===== AUTO-GENERATED CONVENIENCE METHODS (via unified getItemsMatching) =====
    public static List<ItemEntry> getSwords() { return getItemsContaining("sword"); }
    public static List<ItemEntry> getPickaxes() { return getItemsMatching(new String[]{"hammer"}, "pickaxe"); }
    public static List<ItemEntry> getHammers() { return getItemsContaining("hammer"); }
    public static List<ItemEntry> getShovels() { return getItemsContaining("shovel"); }
    public static List<ItemEntry> getAxes() { return getItemsContaining("axe"); }
    public static List<ItemEntry> getHoes() { return getItemsContaining("hoe"); }
    public static List<ItemEntry> getHelmets() { return getItemsContaining("helmet"); }
    public static List<ItemEntry> getChestplates() { return getItemsContaining("chestplate"); }
    public static List<ItemEntry> getLeggings() { return getItemsContaining("legging", "bottoms"); }
    public static List<ItemEntry> getBoots() { return getItemsContaining("boots"); }
    public static List<ItemEntry> getTools() { return getItemsContaining("sword", "pickaxe", "shovel", "axe", "hoe", "hammer"); }
    public static List<ItemEntry> getArmor() { return getItemsContaining("helmet", "chestplate", "legging", "boots"); }
    public static List<ItemEntry> getTrimmableArmor() { return getItemsMatching(new String[]{"pajama"}, "helmet", "chestplate", "legging", "boots"); }

    // ===== BLOCK FILTERING METHODS =====

    /**
     * Filter blocks by keywords (cached) - UNIFIED METHOD
     */
    public static List<BlockEntry> getBlocksContaining(String... keywords) {
        String cacheKey = "blocks_" + String.join("|", keywords);
        return blockCategoryCache.computeIfAbsent(cacheKey, key ->
            getAllBlocks().stream()
                .filter(entry -> containsAny(entry.name(), keywords))
                .collect(Collectors.toUnmodifiableList())
        );
    }

    /**
     * Filter blocks by keywords with exclusions (cached) - UNIFIED METHOD
     */
    public static List<BlockEntry> getBlocksMatching(String[] exclusions, String... inclusions) {
        String cacheKey = "blocks_" + String.join("|", inclusions) +
            (exclusions != null ? "_ex_" + String.join("|", exclusions) : "");
        return blockCategoryCache.computeIfAbsent(cacheKey, key ->
            getAllBlocks().stream()
                .filter(e -> containsAny(e.name(), inclusions))
                .filter(e -> exclusions == null || !containsAny(e.name(), exclusions))
                .collect(Collectors.toUnmodifiableList())
        );
    }

    // ===== AUTO-GENERATED CONVENIENCE METHODS (via unified getBlocksMatching) =====
    public static List<BlockEntry> getOres() { return getBlocksContaining("ore"); }
    public static List<BlockEntry> getStairs() { return getBlocksContaining("stairs"); }
    public static List<BlockEntry> getSlabs() { return getBlocksContaining("slab"); }
    public static List<BlockEntry> getFences() { return getBlocksContaining("fence"); }
    public static List<BlockEntry> getWalls() { return getBlocksContaining("wall"); }
    public static List<BlockEntry> getDoors() { return getBlocksContaining("door"); }
    public static List<BlockEntry> getTrapdoors() { return getBlocksContaining("trapdoor"); }
    public static List<BlockEntry> getButtons() { return getBlocksContaining("button"); }
    public static List<BlockEntry> getPressurePlates() { return getBlocksContaining("pressure_plate"); }
    public static List<BlockEntry> getLogs() { return getBlocksContaining("log", "wood"); }
    public static List<BlockEntry> getPlanks() { return getBlocksContaining("planks"); }

    // ===== UTILITY METHODS =====

    /**
     * Get material name from item/block name (e.g., "pink_garnet_sword" -> "pink_garnet")
     */
    public static String getMaterialName(String fullName) {
        // Use central suffix list from configuration
        for (String suffix : MaterialSuffixes.ALL_SUFFIXES) {
            if (fullName.endsWith(suffix)) {
                return fullName.substring(0, fullName.length() - suffix.length());
            }
        }
        return fullName;
    }

    /**
     * Find material item by name (e.g., "pink_garnet" -> ModItems.PINK_GARNET)
     */
    public static Item findMaterialItem(String materialName) {
        return getAllItems().stream()
            .filter(entry -> entry.name().equals(materialName))
            .map(ItemEntry::item)
            .findFirst()
            .orElse(null);
    }

    /**
     * Find material block by name
     */
    public static Block findMaterialBlock(String materialName) {
        return getAllBlocks().stream()
            .filter(entry -> entry.name().equals(materialName))
            .map(BlockEntry::block)
            .findFirst()
            .orElse(null);
    }

    /**
     * Check if item is a specific type using configuration
     */
    public static boolean isCraftable(String name) {
        return isTool(name) || isArmor(name);
    }

    /**
     * Check if item is a tool
     */
    public static boolean isTool(String name) {
        return containsAny(name, "sword", "pickaxe", "shovel", "axe", "hoe", "hammer");
    }

    /**
     * Check if item is armor
     */
    public static boolean isArmor(String name) {
        return containsAny(name, "helmet", "chestplate", "legging", "boots");
    }

    /**
     * Get count of discovered items
     */
    public static int getItemCount() {
        return getAllItems().size();
    }

    /**
     * Get count of discovered blocks
     */
    public static int getBlockCount() {
        return getAllBlocks().size();
    }

    /**
     * Clear all caches (useful for testing)
     */
    public static void clearCaches() {
        itemCategoryCache.clear();
        blockCategoryCache.clear();
        cachedItems = null;
        cachedBlocks = null;
    }

    /**
     * Get cache statistics
     */
    public static String getCacheStats() {
        return String.format("Items: %d cached, %d in cache | Blocks: %d cached, %d in cache",
            getAllItems().size(), itemCategoryCache.size(),
            getAllBlocks().size(), blockCategoryCache.size());
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // ===== UNIFIED TAG CONFIGURATION =====
    /**
     * Centralized tag generation configuration
     */
    public static class TagConfig {
        // Block tag keywords
        public static final Map<String, List<String>> BLOCK_TAG_KEYWORDS = Map.ofEntries(
            Map.entry("pickaxe_mineable", List.of("block", "ore", "lamp", "door", "trapdoor",
                                                   "button", "pressure_plate", "fence", "wall",
                                                   "stairs", "slab")),
            Map.entry("axe_mineable", List.of("log", "wood", "planks", "driftwood")),
            Map.entry("needs_iron_tool", List.of("deepslate")),
            Map.entry("needs_diamond_tool", List.of("magic")),
            Map.entry("fences", List.of("fence")),
            Map.entry("walls", List.of("wall")),
            Map.entry("logs", List.of("log", "wood")),
            Map.entry("planks", List.of("planks"))
        );

        // Item tag keywords
        public static final Map<String, List<String>> ITEM_TAG_KEYWORDS = Map.ofEntries(
            Map.entry("swords", List.of("sword")),
            Map.entry("pickaxes", List.of("pickaxe")),
            Map.entry("axes", List.of("axe")),
            Map.entry("shovels", List.of("shovel")),
            Map.entry("hoes", List.of("hoe")),
            Map.entry("tools", List.of("sword", "pickaxe", "shovel", "axe", "hoe", "hammer")),
            Map.entry("helmets", List.of("helmet")),
            Map.entry("chestplates", List.of("chestplate")),
            Map.entry("leggings", List.of("leggings", "bottoms")),
            Map.entry("boots", List.of("boots"))
        );

        // Tag exclusions
        public static final Map<String, List<String>> TAG_EXCLUSIONS = Map.ofEntries(
            Map.entry("pickaxe_mineable", List.of("driftwood", "leaves", "sapling")),
            Map.entry("armor", List.of("pajama"))
        );
    }

    /**
     * Get tags for a specific block
     */
    public static List<String> getBlockTags(String blockName) {
        return getTagsForName(blockName, TagConfig.BLOCK_TAG_KEYWORDS, TagConfig.TAG_EXCLUSIONS);
    }

    /**
     * Get tags for a specific item
     */
    public static List<String> getItemTags(String itemName) {
        return getTagsForName(itemName, TagConfig.ITEM_TAG_KEYWORDS, TagConfig.TAG_EXCLUSIONS);
    }

    /**
     * Generic tag matching logic (no duplication)
     */
    private static List<String> getTagsForName(String name, Map<String, List<String>> tagKeywords, Map<String, List<String>> tagExclusions) {
        List<String> tags = new ArrayList<>();

        for (Map.Entry<String, List<String>> tagEntry : tagKeywords.entrySet()) {
            String tagName = tagEntry.getKey();
            List<String> keywords = tagEntry.getValue();
            List<String> exclusions = tagExclusions.getOrDefault(tagName, List.of());

            boolean hasKeyword = keywords.stream().anyMatch(k -> name.contains(k));
            boolean hasExclusion = exclusions.stream().anyMatch(e -> name.contains(e));

            if (hasKeyword && !hasExclusion) {
                tags.add(tagName);
            }
        }

        return tags;
    }

    // ===== UNIFIED DATAGEN CONFIGURATION =====

    /**
     * Central configuration for ALL datagen operations
     * Consolidated from: ModRecipeProvider, ModBlockTagProvider, ModItemTagProvider, etc.
     */
    public static class DatagenConfig {
        // Recipe patterns by tool type
        public static final Map<String, String[]> TOOL_PATTERNS = Map.ofEntries(
            Map.entry("sword", new String[]{"M", "M", "S"}),
            Map.entry("pickaxe", new String[]{"MMM", "MS ", "MS "}),
            Map.entry("hammer", new String[]{"MM ", "MS ", "MS "}),
            Map.entry("shovel", new String[]{"M", "S", "S"}),
            Map.entry("axe", new String[]{"MM ", "MS ", " S "}),
            Map.entry("hoe", new String[]{"MM ", " S ", " S "})
        );

        // Armor patterns by type
        public static final Map<String, String[]> ARMOR_PATTERNS = Map.ofEntries(
            Map.entry("helmet", new String[]{"MMM", "M M"}),
            Map.entry("chestplate", new String[]{"M M", "MMM", "MMM"}),
            Map.entry("leggings", new String[]{"MMM", "M M", "M M"}),
            Map.entry("boots", new String[]{"M M", "M M"})
        );

        // Smelting/blasting configurations
        public static final float ORE_EXPERIENCE = 0.25f;
        public static final int SMELTING_TIME = 200;
        public static final int BLASTING_TIME = 100;

        // Tool generation exclusions
        public static final Map<String, String> TOOL_EXCLUSIONS = Map.ofEntries(
            Map.entry("sword", "obsidian"),
            Map.entry("axe", "obsidian"),
            Map.entry("hammer", null),
            Map.entry("shovel", null),
            Map.entry("hoe", null)
        );
    }

    /**
     * Get tool recipe pattern by type
     */
    public static String[] getToolPattern(String toolType) {
        return DatagenConfig.TOOL_PATTERNS.getOrDefault(toolType, new String[]{});
    }

    /**
     * Get armor recipe pattern by type
     */
    public static String[] getArmorPattern(String armorType) {
        return DatagenConfig.ARMOR_PATTERNS.getOrDefault(armorType, new String[]{});
    }

    /**
     * Get exclusion pattern for tool type
     */
    public static String getToolExclusion(String toolType) {
        return DatagenConfig.TOOL_EXCLUSIONS.get(toolType);
    }

    // ===== UNIFIED LOOT TABLE CONFIGURATION =====
    /**
     * Centralized loot table configuration
     * Consolidated from: ModLootTableProvider
     */
    public static class LootTableConfig {
        // Loot table type by block keywords
        public static final Map<String, String> LOOT_TABLE_TYPES = Map.ofEntries(
            Map.entry("ore", "drop_self"),
            Map.entry("block", "drop_self"),
            Map.entry("door", "single_block"),
            Map.entry("trapdoor", "single_block"),
            Map.entry("button", "drop_self"),
            Map.entry("pressure_plate", "drop_self"),
            Map.entry("fence", "drop_self"),
            Map.entry("gate", "drop_self"),
            Map.entry("wall", "drop_self"),
            Map.entry("stairs", "drop_self"),
            Map.entry("slab", "double_block")
        );

        // Experience drop rates
        public static final Map<String, Float> EXPERIENCE_RATES = Map.ofEntries(
            Map.entry("ore", 0.25f),
            Map.entry("deepslate", 0.25f),
            Map.entry("block", 0.1f)
        );

        // Silk touch requirements
        public static final List<String> SILK_TOUCH_BLOCKS = List.of(
            "leaves", "sapling", "ice", "glass"
        );
    }

    /**
     * Get loot table type for a block
     */
    public static String getLootTableType(String blockName) {
        for (Map.Entry<String, String> entry : LootTableConfig.LOOT_TABLE_TYPES.entrySet()) {
            if (blockName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "drop_self";
    }

    /**
     * Get experience drop rate for a block
     */
    public static float getExperienceRate(String blockName) {
        for (Map.Entry<String, Float> entry : LootTableConfig.EXPERIENCE_RATES.entrySet()) {
            if (blockName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return 0.0f;
    }

    /**
     * Check if block requires silk touch
     */
    public static boolean requiresSilkTouch(String blockName) {
        return LootTableConfig.SILK_TOUCH_BLOCKS.stream()
            .anyMatch(blockName::contains);
    }

    // ===== UNIFIED MODEL CONFIGURATION =====
    /**
     * Centralized model generation configuration
     * Consolidated from: ModModelProvider
     */
    public static class ModelConfig {
        // Model type by item/block keywords
        public static final Map<String, String> MODEL_TYPES = Map.ofEntries(
            Map.entry("sword", "handheld"),
            Map.entry("pickaxe", "handheld"),
            Map.entry("shovel", "handheld"),
            Map.entry("axe", "handheld"),
            Map.entry("hoe", "handheld"),
            Map.entry("hammer", "handheld"),
            Map.entry("ore", "cube_all"),
            Map.entry("block", "cube_all"),
            Map.entry("lamp", "cube_all"),
            Map.entry("door", "door_bottom"),
            Map.entry("fence", "fence_side"),
            Map.entry("wall", "wall_side"),
            Map.entry("stairs", "stairs"),
            Map.entry("slab", "slab"),
            Map.entry("button", "button"),
            Map.entry("pressure_plate", "pressure_plate")
        );

        // Parent model for different types
        public static final Map<String, String> PARENT_MODELS = Map.ofEntries(
            Map.entry("handheld", "item/handheld"),
            Map.entry("cube_all", "block/cube_all"),
            Map.entry("door_bottom", "block/door_bottom"),
            Map.entry("fence_side", "block/fence_side"),
            Map.entry("wall_side", "block/wall_side"),
            Map.entry("stairs", "block/stairs"),
            Map.entry("slab", "block/slab"),
            Map.entry("button", "block/button"),
            Map.entry("pressure_plate", "block/pressure_plate")
        );
    }

    /**
     * Get model type for an item/block
     */
    public static String getModelType(String name) {
        for (Map.Entry<String, String> entry : ModelConfig.MODEL_TYPES.entrySet()) {
            if (name.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "cube_all";
    }

    /**
     * Get parent model for a type
     */
    public static String getParentModel(String modelType) {
        return ModelConfig.PARENT_MODELS.getOrDefault(modelType, "item/generated");
    }

    // ===== UNIFIED CUSTOM ITEM TAG CONFIGURATION =====
    /**
     * Additional item tag configuration
     * Consolidated from: ModItemTagProvider
     */
    public static class ItemTagConfig {
        public static final Map<String, List<String>> CUSTOM_ITEM_TAGS = Map.ofEntries(
            Map.entry("transformable_items", List.of("pink_garnet", "stick")),
            Map.entry("repair_materials", List.of("pink_garnet"))
        );
    }

    /**
     * Get custom item tags
     */
    public static Map<String, List<String>> getCustomItemTags() {
        return ItemTagConfig.CUSTOM_ITEM_TAGS;
    }

    /**
     * Item entry with name and instance
     */
    public record ItemEntry(String name, Item item) {
        public boolean nameContains(String keyword) {
            return name.contains(keyword.toLowerCase());
        }
    }

    /**
     * Block entry with name and instance
     */
    public record BlockEntry(String name, Block block) {
        public boolean nameContains(String keyword) {
            return name.contains(keyword.toLowerCase());
        }

        public Item asItem() {
            return block.asItem();
        }
    }

    // ===== UNIFIED BLOCK TAG PROVIDER CONFIGURATION =====
    /**
     * Centralized block tag generation configuration
     * Consolidated from: ModBlockTagProvider
     */
    public static class BlockTagProviderConfig {
        // Define all block tags and their associated keywords
        public static final Map<String, BlockTagDefinition> BLOCK_TAG_DEFINITIONS = Map.ofEntries(
            // Tool mineable tags
            Map.entry("pickaxe_mineable", new BlockTagDefinition(
                List.of("ore", "block", "lamp", "door", "trapdoor", "button", "pressure_plate", "fence", "wall", "stairs", "slab"),
                List.of("driftwood", "leaves", "sapling")
            )),
            Map.entry("axe_mineable", new BlockTagDefinition(
                List.of("log", "wood", "planks", "driftwood"),
                List.of()
            )),
            Map.entry("shovel_mineable", new BlockTagDefinition(
                List.of("dirt", "grass", "sand", "gravel"),
                List.of()
            )),

            // Tool requirement tags
            Map.entry("needs_iron_tool", new BlockTagDefinition(
                List.of("deepslate"),
                List.of()
            )),
            Map.entry("needs_diamond_tool", new BlockTagDefinition(
                List.of("magic", "obsidian"),
                List.of()
            )),

            // Block structure tags
            Map.entry("fences", new BlockTagDefinition(
                List.of("fence"),
                List.of("gate")
            )),
            Map.entry("fence_gates", new BlockTagDefinition(
                List.of("fence_gate"),
                List.of()
            )),
            Map.entry("walls", new BlockTagDefinition(
                List.of("wall"),
                List.of()
            )),
            Map.entry("doors", new BlockTagDefinition(
                List.of("door"),
                List.of()
            )),
            Map.entry("trapdoors", new BlockTagDefinition(
                List.of("trapdoor"),
                List.of()
            )),
            Map.entry("stairs", new BlockTagDefinition(
                List.of("stairs"),
                List.of()
            )),
            Map.entry("slabs", new BlockTagDefinition(
                List.of("slab"),
                List.of()
            )),

            // Plant tags
            Map.entry("logs_that_burn", new BlockTagDefinition(
                List.of("log", "wood"),
                List.of()
            )),
            Map.entry("planks", new BlockTagDefinition(
                List.of("planks"),
                List.of()
            ))
        );

        /**
         * Block tag definition with inclusions and exclusions
         */
        public static class BlockTagDefinition {
            public final List<String> inclusions;
            public final List<String> exclusions;

            public BlockTagDefinition(List<String> inclusions, List<String> exclusions) {
                this.inclusions = inclusions;
                this.exclusions = exclusions;
            }
        }
    }

    /**
     * Get blocks matching a specific tag definition
     */
    public static List<BlockEntry> getBlocksForTag(String tagName) {
        BlockTagProviderConfig.BlockTagDefinition def = BlockTagProviderConfig.BLOCK_TAG_DEFINITIONS.get(tagName);
        if (def == null) return List.of();

        return getAllBlocks().stream()
            .filter(entry -> {
                // Check if block matches any inclusion keyword
                boolean hasInclusion = def.inclusions.stream()
                    .anyMatch(keyword -> entry.name().contains(keyword));

                // Check if block matches any exclusion keyword
                boolean hasExclusion = def.exclusions.stream()
                    .anyMatch(keyword -> entry.name().contains(keyword));

                return hasInclusion && !hasExclusion;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get all applicable tags for a specific block
     */
    public static List<String> getApplicableBlockTags(String blockName) {
        List<String> applicableTags = new ArrayList<>();

        for (Map.Entry<String, BlockTagProviderConfig.BlockTagDefinition> entry :
                BlockTagProviderConfig.BLOCK_TAG_DEFINITIONS.entrySet()) {
            String tagName = entry.getKey();
            BlockTagProviderConfig.BlockTagDefinition def = entry.getValue();

            // Check if block matches inclusions but not exclusions
            boolean hasInclusion = def.inclusions.stream()
                .anyMatch(keyword -> blockName.contains(keyword));
            boolean hasExclusion = def.exclusions.stream()
                .anyMatch(keyword -> blockName.contains(keyword));

            if (hasInclusion && !hasExclusion) {
                applicableTags.add(tagName);
            }
        }

        return applicableTags;
    }

    /**
     * Convenience methods for common block tags
     */
    public static List<BlockEntry> getPickaxeMineableBlocks() {
        return getBlocksForTag("pickaxe_mineable");
    }

    public static List<BlockEntry> getAxeMineableBlocks() {
        return getBlocksForTag("axe_mineable");
    }

    public static List<BlockEntry> getShovelMineableBlocks() {
        return getBlocksForTag("shovel_mineable");
    }

    public static List<BlockEntry> getNeedsIronToolBlocks() {
        return getBlocksForTag("needs_iron_tool");
    }

    public static List<BlockEntry> getNeedsDiamondToolBlocks() {
        return getBlocksForTag("needs_diamond_tool");
    }

    public static List<BlockEntry> getFenceBlocks() {
        return getBlocksForTag("fences");
    }

    public static List<BlockEntry> getFenceGateBlocks() {
        return getBlocksForTag("fence_gates");
    }

    public static List<BlockEntry> getWallBlocks() {
        return getBlocksForTag("walls");
    }

    public static List<BlockEntry> getDoorBlocks() {
        return getBlocksForTag("doors");
    }

    public static List<BlockEntry> getTrapdoorBlocks() {
        return getBlocksForTag("trapdoors");
    }

    public static List<BlockEntry> getStairBlocks() {
        return getBlocksForTag("stairs");
    }

    public static List<BlockEntry> getSlabBlocks() {
        return getBlocksForTag("slabs");
    }

    public static List<BlockEntry> getLogBlocks() {
        return getBlocksForTag("logs_that_burn");
    }

    public static List<BlockEntry> getPlankBlocks() {
        return getBlocksForTag("planks");
    }

    // ===== UNIFIED ITEM TAG PROVIDER CONFIGURATION =====
    /**
     * Centralized item tag generation configuration
     * Consolidated from: ModItemTagProvider
     */
    public static class ItemTagProviderConfig {
        // Item tag definitions with inclusions and exclusions
        public static final Map<String, ItemTagDefinition> ITEM_TAG_DEFINITIONS = Map.ofEntries(
            // Weapon tags
            Map.entry("swords", new ItemTagDefinition(
                List.of("sword"),
                List.of()
            )),
            Map.entry("pickaxes", new ItemTagDefinition(
                List.of("pickaxe"),
                List.of("hammer")
            )),
            Map.entry("axes", new ItemTagDefinition(
                List.of("axe"),
                List.of()
            )),
            Map.entry("shovels", new ItemTagDefinition(
                List.of("shovel"),
                List.of()
            )),
            Map.entry("hoes", new ItemTagDefinition(
                List.of("hoe"),
                List.of()
            )),

            // Armor tags
            Map.entry("helmets", new ItemTagDefinition(
                List.of("helmet"),
                List.of()
            )),
            Map.entry("chestplates", new ItemTagDefinition(
                List.of("chestplate"),
                List.of("pajama")
            )),
            Map.entry("leggings", new ItemTagDefinition(
                List.of("legging", "bottoms"),
                List.of("pajama")
            )),
            Map.entry("boots", new ItemTagDefinition(
                List.of("boots"),
                List.of()
            )),

            // Trimmable armor (excludes non-trimmable)
            Map.entry("trimmable_armor", new ItemTagDefinition(
                List.of("helmet", "chestplate", "legging", "boots"),
                List.of("pajama")
            )),

            // Custom transformable items
            Map.entry("transformable_items", new ItemTagDefinition(
                List.of("pink_garnet", "stick"),
                List.of()
            ))
        );

        /**
         * Item tag definition with inclusions and exclusions
         */
        public static class ItemTagDefinition {
            public final List<String> inclusions;
            public final List<String> exclusions;

            public ItemTagDefinition(List<String> inclusions, List<String> exclusions) {
                this.inclusions = inclusions;
                this.exclusions = exclusions;
            }
        }
    }

    /**
     * Get items matching a specific item tag definition
     */
    public static List<ItemEntry> getItemsForTag(String tagName) {
        ItemTagProviderConfig.ItemTagDefinition def = ItemTagProviderConfig.ITEM_TAG_DEFINITIONS.get(tagName);
        if (def == null) return List.of();

        return getAllItems().stream()
            .filter(entry -> {
                boolean hasInclusion = def.inclusions.stream()
                    .anyMatch(keyword -> entry.name().contains(keyword));
                boolean hasExclusion = def.exclusions.stream()
                    .anyMatch(keyword -> entry.name().contains(keyword));
                return hasInclusion && !hasExclusion;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get all applicable item tags for a specific item
     */
    public static List<String> getApplicableItemTags(String itemName) {
        List<String> applicableTags = new ArrayList<>();

        for (Map.Entry<String, ItemTagProviderConfig.ItemTagDefinition> entry :
                ItemTagProviderConfig.ITEM_TAG_DEFINITIONS.entrySet()) {
            String tagName = entry.getKey();
            ItemTagProviderConfig.ItemTagDefinition def = entry.getValue();

            boolean hasInclusion = def.inclusions.stream()
                .anyMatch(keyword -> itemName.contains(keyword));
            boolean hasExclusion = def.exclusions.stream()
                .anyMatch(keyword -> itemName.contains(keyword));

            if (hasInclusion && !hasExclusion) {
                applicableTags.add(tagName);
            }
        }

        return applicableTags;
    }

    /**
     * Convenience methods for common item tags
     */
    public static List<ItemEntry> getSwordItems() {
        return getItemsForTag("swords");
    }

    public static List<ItemEntry> getPickaxeItems() {
        return getItemsForTag("pickaxes");
    }

    public static List<ItemEntry> getAxeItems() {
        return getItemsForTag("axes");
    }

    public static List<ItemEntry> getShovelItems() {
        return getItemsForTag("shovels");
    }

    public static List<ItemEntry> getHoeItems() {
        return getItemsForTag("hoes");
    }

    public static List<ItemEntry> getHelmetItems() {
        return getItemsForTag("helmets");
    }

    public static List<ItemEntry> getChestplateItems() {
        return getItemsForTag("chestplates");
    }

    public static List<ItemEntry> getLeggingItems() {
        return getItemsForTag("leggings");
    }

    public static List<ItemEntry> getBootItems() {
        return getItemsForTag("boots");
    }

    public static List<ItemEntry> getTrimmableArmorItems() {
        return getItemsForTag("trimmable_armor");
    }

    public static List<ItemEntry> getTransformableItems() {
        return getItemsForTag("transformable_items");
    }

    // ===== UNIFIED LOOT TABLE PROVIDER CONFIGURATION =====
    /**
     * Centralized loot table generation configuration
     * Consolidated from: ModLootTableProvider
     */
    public static class LootTableProviderConfig {
        // Block loot table drop configurations
        public static final Map<String, BlockLootTableDefinition> BLOCK_LOOT_TABLE_DEFINITIONS = Map.ofEntries(
            // Basic drop_self (block drops itself)
            Map.entry("ore", new BlockLootTableDefinition("drop_self", 0.25f, false)),
            Map.entry("block", new BlockLootTableDefinition("drop_self", 0.1f, false)),
            Map.entry("button", new BlockLootTableDefinition("drop_self", 0.0f, false)),
            Map.entry("pressure_plate", new BlockLootTableDefinition("drop_self", 0.0f, false)),
            Map.entry("fence", new BlockLootTableDefinition("drop_self", 0.0f, false)),
            Map.entry("wall", new BlockLootTableDefinition("drop_self", 0.0f, false)),
            Map.entry("stairs", new BlockLootTableDefinition("drop_self", 0.0f, false)),

            // Special handling for doors and trapdoors (only bottom half drops)
            Map.entry("door", new BlockLootTableDefinition("door_bottom", 0.0f, false)),
            Map.entry("trapdoor", new BlockLootTableDefinition("trapdoor", 0.0f, false)),

            // Double blocks (slabs drop 2x)
            Map.entry("slab", new BlockLootTableDefinition("slab", 0.0f, false)),

            // Leaves and plants (require silk touch or drop nothing)
            Map.entry("leaves", new BlockLootTableDefinition("leaves", 0.0f, true)),
            Map.entry("sapling", new BlockLootTableDefinition("drop_self", 0.0f, true))
        );

        /**
         * Block loot table definition
         */
        public static class BlockLootTableDefinition {
            public final String lootTableType;
            public final float experience;
            public final boolean requiresSilkTouch;

            public BlockLootTableDefinition(String lootTableType, float experience, boolean requiresSilkTouch) {
                this.lootTableType = lootTableType;
                this.experience = experience;
                this.requiresSilkTouch = requiresSilkTouch;
            }
        }
    }

    /**
     * Get loot table definition for a block
     */
    public static LootTableProviderConfig.BlockLootTableDefinition getBlockLootTableDefinition(String blockName) {
        for (Map.Entry<String, LootTableProviderConfig.BlockLootTableDefinition> entry :
                LootTableProviderConfig.BLOCK_LOOT_TABLE_DEFINITIONS.entrySet()) {
            if (blockName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        // Default to drop_self
        return new LootTableProviderConfig.BlockLootTableDefinition("drop_self", 0.0f, false);
    }

    /**
     * Get loot table type for a block
     */
    public static String getBlockLootTableType(String blockName) {
        return getBlockLootTableDefinition(blockName).lootTableType;
    }

    /**
     * Get experience reward for a block
     */
    public static float getBlockExperience(String blockName) {
        return getBlockLootTableDefinition(blockName).experience;
    }

    /**
     * Check if block requires silk touch for loot
     */
    public static boolean blockRequiresSilkTouch(String blockName) {
        return getBlockLootTableDefinition(blockName).requiresSilkTouch;
    }

    /**
     * Get blocks that need loot table generation
     */
    public static List<BlockEntry> getLootTableBlocks() {
        return getAllBlocks().stream()
            .filter(entry -> {
                String name = entry.name();
                return name.contains("ore") || name.contains("block") || name.contains("door") ||
                       name.contains("trapdoor") || name.contains("fence") || name.contains("wall") ||
                       name.contains("stairs") || name.contains("slab") || name.contains("button") ||
                       name.contains("pressure_plate") || name.contains("leaves");
            })
            .collect(Collectors.toList());
    }
}
