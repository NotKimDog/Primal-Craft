package net.kimdog_studios.primal_craft.datagen;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.block.ModBlocks;
import net.kimdog_studios.primal_craft.item.ModItems;
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
        if (cachedItems != null) {
            PrimalCraft.LOGGER.debug("📦 [CACHE HIT] Item cache hit: {} items retrieved from cache", cachedItems.size());
            return cachedItems;
        }

        synchronized (DatagenHelper.class) {
            if (cachedItems != null) {
                PrimalCraft.LOGGER.debug("📦 [CACHE HIT] Item cache hit after lock acquisition: {} items", cachedItems.size());
                return cachedItems;
            }

            PrimalCraft.LOGGER.debug("📦 [CACHE MISS] Item cache miss - initiating scan from ModItems.class");
            long startTime = System.nanoTime();
            List<ItemEntry> items = scanItems(ModItems.class);
            cachedItems = Collections.unmodifiableList(items);
            long elapsed = (System.nanoTime() - startTime) / 1_000_000;

            PrimalCraft.LOGGER.info("✅ [ITEMS CACHED] Successfully scanned and cached {} items in {}ms", cachedItems.size(), elapsed);
            PrimalCraft.LOGGER.debug("   └─ Cache now contains: {} unmodifiable item entries", cachedItems.size());
            return cachedItems;
        }
    }

    /**
     * Get all registered blocks with their names (cached)
     */
    public static List<BlockEntry> getAllBlocks() {
        if (cachedBlocks != null) {
            PrimalCraft.LOGGER.debug("🧱 [CACHE HIT] Block cache hit: {} blocks retrieved from cache", cachedBlocks.size());
            return cachedBlocks;
        }

        synchronized (DatagenHelper.class) {
            if (cachedBlocks != null) {
                PrimalCraft.LOGGER.debug("🧱 [CACHE HIT] Block cache hit after lock acquisition: {} blocks", cachedBlocks.size());
                return cachedBlocks;
            }

            PrimalCraft.LOGGER.debug("🧱 [CACHE MISS] Block cache miss - initiating scan from ModBlocks.class");
            long startTime = System.nanoTime();
            List<BlockEntry> blocks = scanBlocks(ModBlocks.class);
            cachedBlocks = Collections.unmodifiableList(blocks);
            long elapsed = (System.nanoTime() - startTime) / 1_000_000;

            PrimalCraft.LOGGER.info("✅ [BLOCKS CACHED] Successfully scanned and cached {} blocks in {}ms", cachedBlocks.size(), elapsed);
            PrimalCraft.LOGGER.debug("   └─ Cache now contains: {} unmodifiable block entries", cachedBlocks.size());
            return cachedBlocks;
        }
    }

    /**
     * Scan for items using reflection
     */
    private static List<ItemEntry> scanItems(Class<?> modClass) {
        List<ItemEntry> results = new ArrayList<>();
        int scannedFields = 0;
        int foundItems = 0;
        int skippedModifier = 0;
        int skippedType = 0;

        try {
            long startTime = System.nanoTime();
            Field[] fields = modClass.getDeclaredFields();
            scannedFields = fields.length;

            PrimalCraft.LOGGER.debug("🔍 [ITEM SCAN START] Scanning {} fields in {}", fields.length, modClass.getSimpleName());
            PrimalCraft.LOGGER.debug("   ├─ Scanning for public static final Item fields");
            PrimalCraft.LOGGER.debug("   └─ Starting reflective field analysis...");

            for (Field field : fields) {
                // Optimize: check modifiers first (fastest)
                int mods = field.getModifiers();
                if ((mods & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) !=
                    (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) {
                    skippedModifier++;
                    continue;
                }

                // Then check type
                if (!Item.class.isAssignableFrom(field.getType())) {
                    skippedType++;
                    continue;
                }

                // Finally get value
                field.setAccessible(true);
                Item value = (Item) field.get(null);
                String name = field.getName().toLowerCase();
                results.add(new ItemEntry(name, value));
                foundItems++;

                PrimalCraft.LOGGER.trace("   ✓ Found item: {} (type: {})", name, field.getType().getSimpleName());
            }

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            PrimalCraft.LOGGER.info("✅ [ITEM SCAN COMPLETE] Scanned {} total fields in {}ms", scannedFields, elapsed);
            PrimalCraft.LOGGER.debug("   ├─ ✓ Found {} item fields", foundItems);
            PrimalCraft.LOGGER.debug("   ├─ ⊘ Skipped {} non-matching modifiers", skippedModifier);
            PrimalCraft.LOGGER.debug("   └─ ⊘ Skipped {} non-Item type fields", skippedType);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("❌ [ITEM SCAN FAILED] Exception scanning {} for items", modClass.getSimpleName());
            PrimalCraft.LOGGER.error("   ├─ Error Type: {}", e.getClass().getSimpleName());
            PrimalCraft.LOGGER.error("   ├─ Error Message: {}", e.getMessage());
            PrimalCraft.LOGGER.error("   └─ Stack trace follows:", e);
        }

        return results;
    }

    /**
     * Scan for blocks using reflection
     */
    private static List<BlockEntry> scanBlocks(Class<?> modClass) {
        List<BlockEntry> results = new ArrayList<>();
        int scannedFields = 0;
        int foundBlocks = 0;
        int skippedModifier = 0;
        int skippedType = 0;

        try {
            long startTime = System.nanoTime();
            Field[] fields = modClass.getDeclaredFields();
            scannedFields = fields.length;

            PrimalCraft.LOGGER.debug("🔍 [BLOCK SCAN START] Scanning {} fields in {}", fields.length, modClass.getSimpleName());
            PrimalCraft.LOGGER.debug("   ├─ Scanning for public static final Block fields");
            PrimalCraft.LOGGER.debug("   └─ Starting reflective field analysis...");

            for (Field field : fields) {
                // Optimize: check modifiers first (fastest)
                int mods = field.getModifiers();
                if ((mods & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) !=
                    (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) {
                    skippedModifier++;
                    continue;
                }

                // Then check type
                if (!Block.class.isAssignableFrom(field.getType())) {
                    skippedType++;
                    continue;
                }

                // Finally get value
                field.setAccessible(true);
                Block value = (Block) field.get(null);
                String name = field.getName().toLowerCase();
                results.add(new BlockEntry(name, value));
                foundBlocks++;

                PrimalCraft.LOGGER.trace("   ✓ Found block: {} (type: {})", name, field.getType().getSimpleName());
            }

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            PrimalCraft.LOGGER.info("✅ [BLOCK SCAN COMPLETE] Scanned {} total fields in {}ms", scannedFields, elapsed);
            PrimalCraft.LOGGER.debug("   ├─ ✓ Found {} block fields", foundBlocks);
            PrimalCraft.LOGGER.debug("   ├─ ⊘ Skipped {} non-matching modifiers", skippedModifier);
            PrimalCraft.LOGGER.debug("   └─ ⊘ Skipped {} non-Block type fields", skippedType);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("❌ [BLOCK SCAN FAILED] Exception scanning {} for blocks", modClass.getSimpleName());
            PrimalCraft.LOGGER.error("   ├─ Error Type: {}", e.getClass().getSimpleName());
            PrimalCraft.LOGGER.error("   ├─ Error Message: {}", e.getMessage());
            PrimalCraft.LOGGER.error("   └─ Stack trace follows:", e);
        }

        return results;
    }

    // ===== ITEM FILTERING METHODS =====

    /**
     * Filter items by keywords (cached)
     */
    public static List<ItemEntry> getItemsContaining(String... keywords) {
        String cacheKey = String.join("|", keywords);
        PrimalCraft.LOGGER.debug("🔎 [ITEM FILTER] Filtering items by keywords: {}", cacheKey);

        List<ItemEntry> result = itemCategoryCache.computeIfAbsent(cacheKey, key -> {
            PrimalCraft.LOGGER.debug("   ├─ Cache miss for keyword set: {}", key);
            long startTime = System.nanoTime();

            List<ItemEntry> filtered = getAllItems().stream()
                .filter(entry -> {
                    boolean matches = containsAny(entry.name(), keywords);
                    if (matches) {
                        PrimalCraft.LOGGER.trace("     ✓ Match: {} (contains {})", entry.name(), Arrays.toString(keywords));
                    }
                    return matches;
                })
                .collect(Collectors.toUnmodifiableList());

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            PrimalCraft.LOGGER.debug("   ├─ Filtered {} items from {} total in {}ms", filtered.size(), getAllItems().size(), elapsed);
            PrimalCraft.LOGGER.debug("   └─ Cached for future use");

            return filtered;
        });

        PrimalCraft.LOGGER.debug("   └─ Returning {} matching items", result.size());
        return result;
    }

    public static List<ItemEntry> getSwords() {
        PrimalCraft.LOGGER.debug("⚔️  [SWORD FILTER] Retrieving all sword items");
        List<ItemEntry> result = getFromCache("sword", true);
        PrimalCraft.LOGGER.debug("   └─ Found {} swords", result.size());
        return result;
    }

    public static List<ItemEntry> getPickaxes() {
        PrimalCraft.LOGGER.debug("⛏️  [PICKAXE FILTER] Retrieving all pickaxe items (excluding hammers)");
        // Exclude hammers from pickaxes
        List<ItemEntry> result = getAllItems().stream()
            .filter(e -> {
                boolean matches = e.name().contains("pickaxe") && !e.name().contains("hammer");
                if (matches) {
                    PrimalCraft.LOGGER.trace("     ✓ Include: {}", e.name());
                }
                return matches;
            })
            .collect(Collectors.toUnmodifiableList());
        PrimalCraft.LOGGER.debug("   └─ Found {} pickaxes (filtered {} hammers)", result.size(), getAllItems().stream().filter(e -> e.name().contains("hammer")).count());
        return result;
    }

    public static List<ItemEntry> getHammers() {
        PrimalCraft.LOGGER.debug("🔨 [HAMMER FILTER] Retrieving all hammer items");
        List<ItemEntry> result = getFromCache("hammer", true);
        PrimalCraft.LOGGER.debug("   └─ Found {} hammers", result.size());
        return result;
    }

    public static List<ItemEntry> getShovels() {
        PrimalCraft.LOGGER.debug("🏗️  [SHOVEL FILTER] Retrieving all shovel items");
        List<ItemEntry> result = getFromCache("shovel", true);
        PrimalCraft.LOGGER.debug("   └─ Found {} shovels", result.size());
        return result;
    }

    public static List<ItemEntry> getAxes() {
        PrimalCraft.LOGGER.debug("🪓 [AXE FILTER] Retrieving all axe items");
        List<ItemEntry> result = getFromCache("axe", true);
        PrimalCraft.LOGGER.debug("   └─ Found {} axes", result.size());
        return result;
    }

    public static List<ItemEntry> getHoes() {
        PrimalCraft.LOGGER.debug("🌾 [HOE FILTER] Retrieving all hoe items");
        List<ItemEntry> result = getFromCache("hoe", true);
        PrimalCraft.LOGGER.debug("   └─ Found {} hoes", result.size());
        return result;
    }

    public static List<ItemEntry> getHelmets() {
        PrimalCraft.LOGGER.debug("🎖️  [HELMET FILTER] Retrieving all helmet items");
        List<ItemEntry> result = getFromCache("helmet", true);
        PrimalCraft.LOGGER.debug("   └─ Found {} helmets", result.size());
        return result;
    }

    public static List<ItemEntry> getChestplates() {
        PrimalCraft.LOGGER.debug("🛡️  [CHESTPLATE FILTER] Retrieving all chestplate items");
        List<ItemEntry> result = getFromCache("chestplate", true);
        PrimalCraft.LOGGER.debug("   └─ Found {} chestplates", result.size());
        return result;
    }

    public static List<ItemEntry> getLeggings() {
        PrimalCraft.LOGGER.debug("👖 [LEGGINGS FILTER] Retrieving all leggings items");
        List<ItemEntry> result = getAllItems().stream()
            .filter(e -> {
                boolean matches = e.name().contains("legging") || e.name().contains("bottoms");
                if (matches) {
                    PrimalCraft.LOGGER.trace("     ✓ Include: {}", e.name());
                }
                return matches;
            })
            .collect(Collectors.toUnmodifiableList());
        PrimalCraft.LOGGER.debug("   └─ Found {} leggings", result.size());
        return result;
    }

    public static List<ItemEntry> getBoots() {
        PrimalCraft.LOGGER.debug("👢 [BOOTS FILTER] Retrieving all boots items");
        List<ItemEntry> result = getFromCache("boots", true);
        PrimalCraft.LOGGER.debug("   └─ Found {} boots", result.size());
        return result;
    }

    /**
     * Get all tools (optimized)
     */
    public static List<ItemEntry> getTools() {
        PrimalCraft.LOGGER.debug("🛠️  [TOOLS FILTER] Retrieving all tool items (swords, pickaxes, shovels, axes, hoes, hammers)");
        long startTime = System.nanoTime();

        List<ItemEntry> result = getAllItems().stream()
            .filter(e -> {
                String name = e.name();
                boolean isTool = name.contains("sword") || name.contains("pickaxe") || name.contains("shovel") ||
                       name.contains("axe") || name.contains("hoe") || name.contains("hammer");
                if (isTool) {
                    PrimalCraft.LOGGER.trace("     ✓ Tool: {}", name);
                }
                return isTool;
            })
            .collect(Collectors.toUnmodifiableList());

        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        PrimalCraft.LOGGER.debug("   └─ Found {} total tools in {}ms", result.size(), elapsed);
        return result;
    }

    /**
     * Get all armor pieces
     */
    public static List<ItemEntry> getArmor() {
        PrimalCraft.LOGGER.debug("🗡️  [ARMOR FILTER] Retrieving all armor items (helmets, chestplates, leggings, boots)");
        long startTime = System.nanoTime();

        List<ItemEntry> result = getAllItems().stream()
            .filter(e -> {
                String name = e.name();
                boolean isArmor = name.contains("helmet") || name.contains("chestplate") ||
                       name.contains("legging") || name.contains("boots");
                if (isArmor) {
                    PrimalCraft.LOGGER.trace("     ✓ Armor: {}", name);
                }
                return isArmor;
            })
            .collect(Collectors.toUnmodifiableList());

        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        PrimalCraft.LOGGER.debug("   └─ Found {} total armor pieces in {}ms", result.size(), elapsed);
        return result;
    }

    /**
     * Get trimmable armor (excludes pajamas)
     */
    public static List<ItemEntry> getTrimmableArmor() {
        PrimalCraft.LOGGER.debug("✨ [TRIMMABLE ARMOR FILTER] Retrieving trimmable armor items (excluding pajamas)");
        long startTime = System.nanoTime();

        List<ItemEntry> result = getArmor().stream()
            .filter(entry -> {
                boolean isTrimmable = !entry.name().contains("pajama");
                if (!isTrimmable) {
                    PrimalCraft.LOGGER.trace("     ⊘ Excluded (pajama): {}", entry.name());
                }
                return isTrimmable;
            })
            .collect(Collectors.toUnmodifiableList());

        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        int excluded = getArmor().size() - result.size();
        PrimalCraft.LOGGER.debug("   └─ Found {} trimmable items (excluded {} pajamas) in {}ms", result.size(), excluded, elapsed);
        return result;
    }

    /**
     * Helper method for cached single-keyword filtering
     */
    private static List<ItemEntry> getFromCache(String keyword, boolean cached) {
        if (!cached) {
            PrimalCraft.LOGGER.debug("   ├─ Cache disabled for keyword: {}", keyword);
            List<ItemEntry> result = getAllItems().stream()
                .filter(e -> {
                    boolean matches = e.name().contains(keyword);
                    if (matches) {
                        PrimalCraft.LOGGER.trace("     ✓ Match: {}", e.name());
                    }
                    return matches;
                })
                .collect(Collectors.toUnmodifiableList());
            PrimalCraft.LOGGER.debug("   └─ Found {} items (uncached)", result.size());
            return result;
        }

        return itemCategoryCache.computeIfAbsent(keyword, key -> {
            PrimalCraft.LOGGER.debug("   ├─ Cache miss for keyword: {}", key);
            long startTime = System.nanoTime();

            List<ItemEntry> filtered = getAllItems().stream()
                .filter(e -> {
                    boolean matches = e.name().contains(key);
                    if (matches) {
                        PrimalCraft.LOGGER.trace("     ✓ Match: {}", e.name());
                    }
                    return matches;
                })
                .collect(Collectors.toUnmodifiableList());

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            PrimalCraft.LOGGER.debug("   └─ Found {} items in {}ms and cached", filtered.size(), elapsed);

            return filtered;
        });
    }

    // ===== BLOCK FILTERING METHODS =====

    /**
     * Filter blocks by keywords (cached)
     */
    public static List<BlockEntry> getBlocksContaining(String... keywords) {
        String cacheKey = "blocks_" + String.join("|", keywords);
        PrimalCraft.LOGGER.debug("🔎 [BLOCK FILTER] Filtering blocks by keywords: {}", Arrays.toString(keywords));

        List<BlockEntry> result = blockCategoryCache.computeIfAbsent(cacheKey, key -> {
            PrimalCraft.LOGGER.debug("   ├─ Cache miss for keyword set: {}", key);
            long startTime = System.nanoTime();

            List<BlockEntry> filtered = getAllBlocks().stream()
                .filter(entry -> {
                    boolean matches = containsAny(entry.name(), keywords);
                    if (matches) {
                        PrimalCraft.LOGGER.trace("     ✓ Match: {} (contains {})", entry.name(), Arrays.toString(keywords));
                    }
                    return matches;
                })
                .collect(Collectors.toUnmodifiableList());

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            PrimalCraft.LOGGER.debug("   ├─ Filtered {} blocks from {} total in {}ms", filtered.size(), getAllBlocks().size(), elapsed);
            PrimalCraft.LOGGER.debug("   └─ Cached for future use");

            return filtered;
        });

        PrimalCraft.LOGGER.debug("   └─ Returning {} matching blocks", result.size());
        return result;
    }

    public static List<BlockEntry> getOres() {
        PrimalCraft.LOGGER.debug("⛏️  [ORE FILTER] Retrieving all ore blocks");
        List<BlockEntry> result = getFromBlockCache("_ore");
        PrimalCraft.LOGGER.debug("   └─ Found {} ores", result.size());
        return result;
    }

    public static List<BlockEntry> getStairs() {
        PrimalCraft.LOGGER.debug("🪜 [STAIRS FILTER] Retrieving all stair blocks");
        List<BlockEntry> result = getFromBlockCache("stairs");
        PrimalCraft.LOGGER.debug("   └─ Found {} stair variants", result.size());
        return result;
    }

    public static List<BlockEntry> getSlabs() {
        PrimalCraft.LOGGER.debug("📦 [SLAB FILTER] Retrieving all slab blocks");
        List<BlockEntry> result = getFromBlockCache("slab");
        PrimalCraft.LOGGER.debug("   └─ Found {} slab variants", result.size());
        return result;
    }

    public static List<BlockEntry> getFences() {
        PrimalCraft.LOGGER.debug("🚧 [FENCE FILTER] Retrieving all fence blocks");
        List<BlockEntry> result = getFromBlockCache("fence");
        PrimalCraft.LOGGER.debug("   └─ Found {} fence variants", result.size());
        return result;
    }

    public static List<BlockEntry> getWalls() {
        PrimalCraft.LOGGER.debug("🧱 [WALL FILTER] Retrieving all wall blocks");
        List<BlockEntry> result = getFromBlockCache("wall");
        PrimalCraft.LOGGER.debug("   └─ Found {} wall variants", result.size());
        return result;
    }

    public static List<BlockEntry> getDoors() {
        PrimalCraft.LOGGER.debug("🚪 [DOOR FILTER] Retrieving all door blocks");
        List<BlockEntry> result = getFromBlockCache("door");
        PrimalCraft.LOGGER.debug("   └─ Found {} door variants", result.size());
        return result;
    }

    public static List<BlockEntry> getTrapdoors() {
        PrimalCraft.LOGGER.debug("🪵 [TRAPDOOR FILTER] Retrieving all trapdoor blocks");
        List<BlockEntry> result = getFromBlockCache("trapdoor");
        PrimalCraft.LOGGER.debug("   └─ Found {} trapdoor variants", result.size());
        return result;
    }

    public static List<BlockEntry> getButtons() {
        PrimalCraft.LOGGER.debug("🔘 [BUTTON FILTER] Retrieving all button blocks");
        List<BlockEntry> result = getFromBlockCache("button");
        PrimalCraft.LOGGER.debug("   └─ Found {} button variants", result.size());
        return result;
    }

    public static List<BlockEntry> getPressurePlates() {
        PrimalCraft.LOGGER.debug("⚖️  [PRESSURE PLATE FILTER] Retrieving all pressure plate blocks");
        List<BlockEntry> result = getFromBlockCache("pressure_plate");
        PrimalCraft.LOGGER.debug("   └─ Found {} pressure plate variants", result.size());
        return result;
    }

    public static List<BlockEntry> getLogs() {
        PrimalCraft.LOGGER.debug("🌳 [LOG FILTER] Retrieving all log and wood blocks");
        List<BlockEntry> result = getAllBlocks().stream()
            .filter(e -> {
                boolean matches = e.name().contains("log") || e.name().contains("wood");
                if (matches) {
                    PrimalCraft.LOGGER.trace("     ✓ Match: {}", e.name());
                }
                return matches;
            })
            .collect(Collectors.toUnmodifiableList());
        PrimalCraft.LOGGER.debug("   └─ Found {} log/wood variants", result.size());
        return result;
    }

    public static List<BlockEntry> getPlanks() {
        PrimalCraft.LOGGER.debug("🪵 [PLANKS FILTER] Retrieving all plank blocks");
        List<BlockEntry> result = getFromBlockCache("planks");
        PrimalCraft.LOGGER.debug("   └─ Found {} plank variants", result.size());
        return result;
    }

    /**
     * Helper method for cached single-keyword block filtering
     */
    private static List<BlockEntry> getFromBlockCache(String keyword) {
        String cacheKey = "block_" + keyword;
        return blockCategoryCache.computeIfAbsent(cacheKey, key -> {
            PrimalCraft.LOGGER.debug("   ├─ Cache miss for block keyword: {}", keyword);
            long startTime = System.nanoTime();

            List<BlockEntry> filtered = getAllBlocks().stream()
                .filter(e -> {
                    boolean matches = e.name().contains(keyword);
                    if (matches) {
                        PrimalCraft.LOGGER.trace("     ✓ Match: {}", e.name());
                    }
                    return matches;
                })
                .collect(Collectors.toUnmodifiableList());

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            PrimalCraft.LOGGER.debug("   └─ Found {} blocks in {}ms and cached", filtered.size(), elapsed);

            return filtered;
        });
    }

    // ===== UTILITY METHODS =====

    /**
     * Get material name from item/block name (e.g., "pink_garnet_sword" -> "pink_garnet")
     */
    public static String getMaterialName(String fullName) {
        PrimalCraft.LOGGER.trace("🔧 [MATERIAL PARSING] Extracting material name from: {}", fullName);

        // Remove common suffixes
        String[] suffixes = {"_sword", "_pickaxe", "_shovel", "_axe", "_hoe", "_hammer",
                            "_helmet", "_chestplate", "_leggings", "_boots",
                            "_block", "_ore", "_stairs", "_slab", "_wall", "_fence", "_door",
                            "_trapdoor", "_button", "_pressure_plate"};

        for (String suffix : suffixes) {
            if (fullName.endsWith(suffix)) {
                String materialName = fullName.substring(0, fullName.length() - suffix.length());
                PrimalCraft.LOGGER.trace("   └─ Extracted material: {} (removed suffix: {})", materialName, suffix);
                return materialName;
            }
        }

        PrimalCraft.LOGGER.trace("   └─ No suffix matched, returning original: {}", fullName);
        return fullName;
    }

    /**
     * Find material item by name (e.g., "pink_garnet" -> ModItems.PINK_GARNET)
     */
    public static Item findMaterialItem(String materialName) {
        PrimalCraft.LOGGER.debug("🔍 [ITEM LOOKUP] Finding material item: {}", materialName);

        Item foundItem = getAllItems().stream()
            .filter(entry -> {
                boolean matches = entry.name().equals(materialName);
                if (matches) {
                    PrimalCraft.LOGGER.debug("   ✓ Found matching item: {}", entry.name());
                }
                return matches;
            })
            .map(ItemEntry::item)
            .findFirst()
            .orElse(null);

        if (foundItem == null) {
            PrimalCraft.LOGGER.warn("   ✗ Material item NOT found: {}", materialName);
        }

        return foundItem;
    }

    /**
     * Find material block by name
     */
    public static Block findMaterialBlock(String materialName) {
        PrimalCraft.LOGGER.debug("🔍 [BLOCK LOOKUP] Finding material block: {}", materialName);

        Block foundBlock = getAllBlocks().stream()
            .filter(entry -> {
                boolean matches = entry.name().equals(materialName);
                if (matches) {
                    PrimalCraft.LOGGER.debug("   ✓ Found matching block: {}", entry.name());
                }
                return matches;
            })
            .map(BlockEntry::block)
            .findFirst()
            .orElse(null);

        if (foundBlock == null) {
            PrimalCraft.LOGGER.warn("   ✗ Material block NOT found: {}", materialName);
        }

        return foundBlock;
    }

    /**
     * Check if item is a tool/armor that should have recipes
     */
    public static boolean isCraftable(String name) {
        boolean result = name.contains("sword") || name.contains("pickaxe") || name.contains("shovel") ||
               name.contains("axe") || name.contains("hoe") || name.contains("hammer") ||
               name.contains("helmet") || name.contains("chestplate") ||
               name.contains("leggings") || name.contains("boots");

        PrimalCraft.LOGGER.trace("   ├─ Checking if craftable: {} -> {}", name, result);
        return result;
    }

    /**
     * Check if item is a tool
     */
    public static boolean isTool(String name) {
        boolean result = name.contains("sword") || name.contains("pickaxe") || name.contains("shovel") ||
               name.contains("axe") || name.contains("hoe") || name.contains("hammer");

        PrimalCraft.LOGGER.trace("   ├─ Checking if tool: {} -> {}", name, result);
        return result;
    }

    /**
     * Check if item is armor
     */
    public static boolean isArmor(String name) {
        boolean result = name.contains("helmet") || name.contains("chestplate") ||
               name.contains("leggings") || name.contains("boots");

        PrimalCraft.LOGGER.trace("   ├─ Checking if armor: {} -> {}", name, result);
        return result;
    }

    /**
     * Get count of discovered items
     */
    public static int getItemCount() {
        int count = getAllItems().size();
        PrimalCraft.LOGGER.debug("📊 [ITEM COUNT] Total items discovered: {}", count);
        return count;
    }

    /**
     * Get count of discovered blocks
     */
    public static int getBlockCount() {
        int count = getAllBlocks().size();
        PrimalCraft.LOGGER.debug("📊 [BLOCK COUNT] Total blocks discovered: {}", count);
        return count;
    }

    /**
     * Clear all caches (useful for testing)
     */
    public static void clearCaches() {
        PrimalCraft.LOGGER.info("🧹 [CACHE CLEAR] Starting cache clearing operation");
        int itemCacheBefore = itemCategoryCache.size();
        int blockCacheBefore = blockCategoryCache.size();

        PrimalCraft.LOGGER.debug("   ├─ Item category caches before: {}", itemCacheBefore);
        PrimalCraft.LOGGER.debug("   ├─ Block category caches before: {}", blockCacheBefore);

        itemCategoryCache.clear();
        blockCategoryCache.clear();
        cachedItems = null;
        cachedBlocks = null;

        PrimalCraft.LOGGER.debug("   ├─ Cleared {} item category caches", itemCacheBefore);
        PrimalCraft.LOGGER.debug("   ├─ Cleared {} block category caches", blockCacheBefore);
        PrimalCraft.LOGGER.debug("   ├─ Primary item cache: CLEARED");
        PrimalCraft.LOGGER.debug("   └─ Primary block cache: CLEARED");
        PrimalCraft.LOGGER.info("✅ [CACHE CLEAR COMPLETE] All caches successfully cleared and reset");
    }

    /**
     * Get cache statistics
     */
    public static String getCacheStats() {
        int itemsInPrimary = cachedItems != null ? cachedItems.size() : 0;
        int blocksInPrimary = cachedBlocks != null ? cachedBlocks.size() : 0;
        int itemCategoryCaches = itemCategoryCache.size();
        int blockCategoryCaches = blockCategoryCache.size();

        String stats = String.format(
            "Items: %d primary + %d category caches | Blocks: %d primary + %d category caches",
            itemsInPrimary, itemCategoryCaches,
            blocksInPrimary, blockCategoryCaches
        );

        PrimalCraft.LOGGER.info("📊 [CACHE STATS]");
        PrimalCraft.LOGGER.info("   ├─ Primary Caches:");
        PrimalCraft.LOGGER.info("   │  ├─ Items: {} entries", itemsInPrimary);
        PrimalCraft.LOGGER.info("   │  └─ Blocks: {} entries", blocksInPrimary);
        PrimalCraft.LOGGER.info("   ├─ Category Caches:");
        PrimalCraft.LOGGER.info("   │  ├─ Item categories: {} cached", itemCategoryCaches);
        PrimalCraft.LOGGER.info("   │  └─ Block categories: {} cached", blockCategoryCaches);
        PrimalCraft.LOGGER.info("   └─ Total Memory: ~{} KB", (itemsInPrimary + blocksInPrimary + itemCategoryCaches + blockCategoryCaches) * 2);

        return stats;
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                PrimalCraft.LOGGER.trace("       └─ Contains keyword: {}", keyword);
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
            boolean result = name.contains(keyword.toLowerCase());
            if (result) {
                PrimalCraft.LOGGER.trace("     ✓ ItemEntry match: {} contains {}", name, keyword);
            }
            return result;
        }
    }

    /**
     * Block entry with name and instance
     */
    public record BlockEntry(String name, Block block) {
        public boolean nameContains(String keyword) {
            boolean result = name.contains(keyword.toLowerCase());
            if (result) {
                PrimalCraft.LOGGER.trace("     ✓ BlockEntry match: {} contains {}", name, keyword);
            }
            return result;
        }

        public Item asItem() {
            Item result = block.asItem();
            PrimalCraft.LOGGER.trace("       └─ Converted block to item: {}", result);
            return result;
        }
    }
}
