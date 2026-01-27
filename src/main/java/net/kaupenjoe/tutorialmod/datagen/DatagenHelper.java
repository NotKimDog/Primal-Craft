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
     * Filter items by keywords (cached)
     */
    public static List<ItemEntry> getItemsContaining(String... keywords) {
        String cacheKey = String.join("|", keywords);
        return itemCategoryCache.computeIfAbsent(cacheKey, key ->
            getAllItems().stream()
                .filter(entry -> containsAny(entry.name(), keywords))
                .collect(Collectors.toUnmodifiableList())
        );
    }

    public static List<ItemEntry> getSwords() {
        return getFromCache("sword", true);
    }

    public static List<ItemEntry> getPickaxes() {
        // Exclude hammers from pickaxes
        return getAllItems().stream()
            .filter(e -> e.name().contains("pickaxe") && !e.name().contains("hammer"))
            .collect(Collectors.toUnmodifiableList());
    }

    public static List<ItemEntry> getHammers() {
        return getFromCache("hammer", true);
    }

    public static List<ItemEntry> getShovels() {
        return getFromCache("shovel", true);
    }

    public static List<ItemEntry> getAxes() {
        return getFromCache("axe", true);
    }

    public static List<ItemEntry> getHoes() {
        return getFromCache("hoe", true);
    }

    public static List<ItemEntry> getHelmets() {
        return getFromCache("helmet", true);
    }

    public static List<ItemEntry> getChestplates() {
        return getFromCache("chestplate", true);
    }

    public static List<ItemEntry> getLeggings() {
        return getAllItems().stream()
            .filter(e -> e.name().contains("legging") || e.name().contains("bottoms"))
            .collect(Collectors.toUnmodifiableList());
    }

    public static List<ItemEntry> getBoots() {
        return getFromCache("boots", true);
    }

    /**
     * Get all tools (optimized)
     */
    public static List<ItemEntry> getTools() {
        return getAllItems().stream()
            .filter(e -> {
                String name = e.name();
                return name.contains("sword") || name.contains("pickaxe") || name.contains("shovel") ||
                       name.contains("axe") || name.contains("hoe") || name.contains("hammer");
            })
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Get all armor pieces
     */
    public static List<ItemEntry> getArmor() {
        return getAllItems().stream()
            .filter(e -> {
                String name = e.name();
                return name.contains("helmet") || name.contains("chestplate") ||
                       name.contains("legging") || name.contains("boots");
            })
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Get trimmable armor (excludes pajamas)
     */
    public static List<ItemEntry> getTrimmableArmor() {
        return getArmor().stream()
            .filter(entry -> !entry.name().contains("pajama"))
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Helper method for cached single-keyword filtering
     */
    private static List<ItemEntry> getFromCache(String keyword, boolean cached) {
        if (!cached) {
            return getAllItems().stream()
                .filter(e -> e.name().contains(keyword))
                .collect(Collectors.toUnmodifiableList());
        }

        return itemCategoryCache.computeIfAbsent(keyword, key ->
            getAllItems().stream()
                .filter(e -> e.name().contains(key))
                .collect(Collectors.toUnmodifiableList())
        );
    }

    // ===== BLOCK FILTERING METHODS =====

    /**
     * Filter blocks by keywords (cached)
     */
    public static List<BlockEntry> getBlocksContaining(String... keywords) {
        String cacheKey = "blocks_" + String.join("|", keywords);
        return blockCategoryCache.computeIfAbsent(cacheKey, key ->
            getAllBlocks().stream()
                .filter(entry -> containsAny(entry.name(), keywords))
                .collect(Collectors.toUnmodifiableList())
        );
    }

    public static List<BlockEntry> getOres() {
        return getFromBlockCache("_ore");
    }

    public static List<BlockEntry> getStairs() {
        return getFromBlockCache("stairs");
    }

    public static List<BlockEntry> getSlabs() {
        return getFromBlockCache("slab");
    }

    public static List<BlockEntry> getFences() {
        return getFromBlockCache("fence");
    }

    public static List<BlockEntry> getWalls() {
        return getFromBlockCache("wall");
    }

    public static List<BlockEntry> getDoors() {
        return getFromBlockCache("door");
    }

    public static List<BlockEntry> getTrapdoors() {
        return getFromBlockCache("trapdoor");
    }

    public static List<BlockEntry> getButtons() {
        return getFromBlockCache("button");
    }

    public static List<BlockEntry> getPressurePlates() {
        return getFromBlockCache("pressure_plate");
    }

    public static List<BlockEntry> getLogs() {
        return getAllBlocks().stream()
            .filter(e -> e.name().contains("log") || e.name().contains("wood"))
            .collect(Collectors.toUnmodifiableList());
    }

    public static List<BlockEntry> getPlanks() {
        return getFromBlockCache("planks");
    }

    /**
     * Helper method for cached single-keyword block filtering
     */
    private static List<BlockEntry> getFromBlockCache(String keyword) {
        String cacheKey = "block_" + keyword;
        return blockCategoryCache.computeIfAbsent(cacheKey, key ->
            getAllBlocks().stream()
                .filter(e -> e.name().contains(keyword))
                .collect(Collectors.toUnmodifiableList())
        );
    }

    // ===== UTILITY METHODS =====

    /**
     * Get material name from item/block name (e.g., "pink_garnet_sword" -> "pink_garnet")
     */
    public static String getMaterialName(String fullName) {
        // Remove common suffixes
        String[] suffixes = {"_sword", "_pickaxe", "_shovel", "_axe", "_hoe", "_hammer",
                            "_helmet", "_chestplate", "_leggings", "_boots",
                            "_block", "_ore", "_stairs", "_slab", "_wall", "_fence", "_door",
                            "_trapdoor", "_button", "_pressure_plate"};

        for (String suffix : suffixes) {
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
     * Check if item is a tool/armor that should have recipes
     */
    public static boolean isCraftable(String name) {
        return name.contains("sword") || name.contains("pickaxe") || name.contains("shovel") ||
               name.contains("axe") || name.contains("hoe") || name.contains("hammer") ||
               name.contains("helmet") || name.contains("chestplate") ||
               name.contains("leggings") || name.contains("boots");
    }

    /**
     * Check if item is a tool
     */
    public static boolean isTool(String name) {
        return name.contains("sword") || name.contains("pickaxe") || name.contains("shovel") ||
               name.contains("axe") || name.contains("hoe") || name.contains("hammer");
    }

    /**
     * Check if item is armor
     */
    public static boolean isArmor(String name) {
        return name.contains("helmet") || name.contains("chestplate") ||
               name.contains("leggings") || name.contains("boots");
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
}
