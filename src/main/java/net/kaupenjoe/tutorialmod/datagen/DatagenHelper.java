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
        if (cachedItems != null) {
            TutorialMod.LOGGER.debug("📦 [CACHE HIT] Item cache hit: {} items retrieved from cache", cachedItems.size());
            return cachedItems;
        }

        synchronized (DatagenHelper.class) {
            if (cachedItems != null) {
                TutorialMod.LOGGER.debug("📦 [CACHE HIT] Item cache hit after lock acquisition: {} items", cachedItems.size());
                return cachedItems;
            }

            TutorialMod.LOGGER.debug("📦 [CACHE MISS] Item cache miss - initiating scan from ModItems.class");
            long startTime = System.nanoTime();
            List<ItemEntry> items = scanItems(ModItems.class);
            cachedItems = Collections.unmodifiableList(items);
            long elapsed = (System.nanoTime() - startTime) / 1_000_000;

            TutorialMod.LOGGER.info("✅ [ITEMS CACHED] Successfully scanned and cached {} items in {}ms", cachedItems.size(), elapsed);
            TutorialMod.LOGGER.debug("   └─ Cache now contains: {} unmodifiable item entries", cachedItems.size());
            return cachedItems;
        }
    }

    /**
     * Get all registered blocks with their names (cached)
     */
    public static List<BlockEntry> getAllBlocks() {
        if (cachedBlocks != null) {
            TutorialMod.LOGGER.debug("🧱 [CACHE HIT] Block cache hit: {} blocks retrieved from cache", cachedBlocks.size());
            return cachedBlocks;
        }

        synchronized (DatagenHelper.class) {
            if (cachedBlocks != null) {
                TutorialMod.LOGGER.debug("🧱 [CACHE HIT] Block cache hit after lock acquisition: {} blocks", cachedBlocks.size());
                return cachedBlocks;
            }

            TutorialMod.LOGGER.debug("🧱 [CACHE MISS] Block cache miss - initiating scan from ModBlocks.class");
            long startTime = System.nanoTime();
            List<BlockEntry> blocks = scanBlocks(ModBlocks.class);
            cachedBlocks = Collections.unmodifiableList(blocks);
            long elapsed = (System.nanoTime() - startTime) / 1_000_000;

            TutorialMod.LOGGER.info("✅ [BLOCKS CACHED] Successfully scanned and cached {} blocks in {}ms", cachedBlocks.size(), elapsed);
            TutorialMod.LOGGER.debug("   └─ Cache now contains: {} unmodifiable block entries", cachedBlocks.size());
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

            TutorialMod.LOGGER.debug("🔍 [ITEM SCAN START] Scanning {} fields in {}", fields.length, modClass.getSimpleName());
            TutorialMod.LOGGER.debug("   ├─ Scanning for public static final Item fields");
            TutorialMod.LOGGER.debug("   └─ Starting reflective field analysis...");

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

                TutorialMod.LOGGER.trace("   ✓ Found item: {} (type: {})", name, field.getType().getSimpleName());
            }

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            TutorialMod.LOGGER.info("✅ [ITEM SCAN COMPLETE] Scanned {} total fields in {}ms", scannedFields, elapsed);
            TutorialMod.LOGGER.debug("   ├─ ✓ Found {} item fields", foundItems);
            TutorialMod.LOGGER.debug("   ├─ ⊘ Skipped {} non-matching modifiers", skippedModifier);
            TutorialMod.LOGGER.debug("   └─ ⊘ Skipped {} non-Item type fields", skippedType);
        } catch (Exception e) {
            TutorialMod.LOGGER.error("❌ [ITEM SCAN FAILED] Exception scanning {} for items", modClass.getSimpleName());
            TutorialMod.LOGGER.error("   ├─ Error Type: {}", e.getClass().getSimpleName());
            TutorialMod.LOGGER.error("   ├─ Error Message: {}", e.getMessage());
            TutorialMod.LOGGER.error("   └─ Stack trace follows:", e);
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

            TutorialMod.LOGGER.debug("🔍 [BLOCK SCAN START] Scanning {} fields in {}", fields.length, modClass.getSimpleName());
            TutorialMod.LOGGER.debug("   ├─ Scanning for public static final Block fields");
            TutorialMod.LOGGER.debug("   └─ Starting reflective field analysis...");

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

                TutorialMod.LOGGER.trace("   ✓ Found block: {} (type: {})", name, field.getType().getSimpleName());
            }

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            TutorialMod.LOGGER.info("✅ [BLOCK SCAN COMPLETE] Scanned {} total fields in {}ms", scannedFields, elapsed);
            TutorialMod.LOGGER.debug("   ├─ ✓ Found {} block fields", foundBlocks);
            TutorialMod.LOGGER.debug("   ├─ ⊘ Skipped {} non-matching modifiers", skippedModifier);
            TutorialMod.LOGGER.debug("   └─ ⊘ Skipped {} non-Block type fields", skippedType);
        } catch (Exception e) {
            TutorialMod.LOGGER.error("❌ [BLOCK SCAN FAILED] Exception scanning {} for blocks", modClass.getSimpleName());
            TutorialMod.LOGGER.error("   ├─ Error Type: {}", e.getClass().getSimpleName());
            TutorialMod.LOGGER.error("   ├─ Error Message: {}", e.getMessage());
            TutorialMod.LOGGER.error("   └─ Stack trace follows:", e);
        }

        return results;
    }

    // ===== ITEM FILTERING METHODS =====

    /**
     * Filter items by keywords (cached)
     */
    public static List<ItemEntry> getItemsContaining(String... keywords) {
        String cacheKey = String.join("|", keywords);
        TutorialMod.LOGGER.debug("🔎 [ITEM FILTER] Filtering items by keywords: {}", cacheKey);

        List<ItemEntry> result = itemCategoryCache.computeIfAbsent(cacheKey, key -> {
            TutorialMod.LOGGER.debug("   ├─ Cache miss for keyword set: {}", key);
            long startTime = System.nanoTime();

            List<ItemEntry> filtered = getAllItems().stream()
                .filter(entry -> {
                    boolean matches = containsAny(entry.name(), keywords);
                    if (matches) {
                        TutorialMod.LOGGER.trace("     ✓ Match: {} (contains {})", entry.name(), Arrays.toString(keywords));
                    }
                    return matches;
                })
                .collect(Collectors.toUnmodifiableList());

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            TutorialMod.LOGGER.debug("   ├─ Filtered {} items from {} total in {}ms", filtered.size(), getAllItems().size(), elapsed);
            TutorialMod.LOGGER.debug("   └─ Cached for future use");

            return filtered;
        });

        TutorialMod.LOGGER.debug("   └─ Returning {} matching items", result.size());
        return result;
    }

    public static List<ItemEntry> getSwords() {
        TutorialMod.LOGGER.debug("⚔️  [SWORD FILTER] Retrieving all sword items");
        List<ItemEntry> result = getFromCache("sword", true);
        TutorialMod.LOGGER.debug("   └─ Found {} swords", result.size());
        return result;
    }

    public static List<ItemEntry> getPickaxes() {
        TutorialMod.LOGGER.debug("⛏️  [PICKAXE FILTER] Retrieving all pickaxe items (excluding hammers)");
        // Exclude hammers from pickaxes
        List<ItemEntry> result = getAllItems().stream()
            .filter(e -> {
                boolean matches = e.name().contains("pickaxe") && !e.name().contains("hammer");
                if (matches) {
                    TutorialMod.LOGGER.trace("     ✓ Include: {}", e.name());
                }
                return matches;
            })
            .collect(Collectors.toUnmodifiableList());
        TutorialMod.LOGGER.debug("   └─ Found {} pickaxes (filtered {} hammers)", result.size(), getAllItems().stream().filter(e -> e.name().contains("hammer")).count());
        return result;
    }

    public static List<ItemEntry> getHammers() {
        TutorialMod.LOGGER.debug("🔨 [HAMMER FILTER] Retrieving all hammer items");
        List<ItemEntry> result = getFromCache("hammer", true);
        TutorialMod.LOGGER.debug("   └─ Found {} hammers", result.size());
        return result;
    }

    public static List<ItemEntry> getShovels() {
        TutorialMod.LOGGER.debug("🏗️  [SHOVEL FILTER] Retrieving all shovel items");
        List<ItemEntry> result = getFromCache("shovel", true);
        TutorialMod.LOGGER.debug("   └─ Found {} shovels", result.size());
        return result;
    }

    public static List<ItemEntry> getAxes() {
        TutorialMod.LOGGER.debug("🪓 [AXE FILTER] Retrieving all axe items");
        List<ItemEntry> result = getFromCache("axe", true);
        TutorialMod.LOGGER.debug("   └─ Found {} axes", result.size());
        return result;
    }

    public static List<ItemEntry> getHoes() {
        TutorialMod.LOGGER.debug("🌾 [HOE FILTER] Retrieving all hoe items");
        List<ItemEntry> result = getFromCache("hoe", true);
        TutorialMod.LOGGER.debug("   └─ Found {} hoes", result.size());
        return result;
    }

    public static List<ItemEntry> getHelmets() {
        TutorialMod.LOGGER.debug("🎖️  [HELMET FILTER] Retrieving all helmet items");
        List<ItemEntry> result = getFromCache("helmet", true);
        TutorialMod.LOGGER.debug("   └─ Found {} helmets", result.size());
        return result;
    }

    public static List<ItemEntry> getChestplates() {
        TutorialMod.LOGGER.debug("🛡️  [CHESTPLATE FILTER] Retrieving all chestplate items");
        List<ItemEntry> result = getFromCache("chestplate", true);
        TutorialMod.LOGGER.debug("   └─ Found {} chestplates", result.size());
        return result;
    }

    public static List<ItemEntry> getLeggings() {
        TutorialMod.LOGGER.debug("👖 [LEGGINGS FILTER] Retrieving all leggings items");
        List<ItemEntry> result = getAllItems().stream()
            .filter(e -> {
                boolean matches = e.name().contains("legging") || e.name().contains("bottoms");
                if (matches) {
                    TutorialMod.LOGGER.trace("     ✓ Include: {}", e.name());
                }
                return matches;
            })
            .collect(Collectors.toUnmodifiableList());
        TutorialMod.LOGGER.debug("   └─ Found {} leggings", result.size());
        return result;
    }

    public static List<ItemEntry> getBoots() {
        TutorialMod.LOGGER.debug("👢 [BOOTS FILTER] Retrieving all boots items");
        List<ItemEntry> result = getFromCache("boots", true);
        TutorialMod.LOGGER.debug("   └─ Found {} boots", result.size());
        return result;
    }

    /**
     * Get all tools (optimized)
     */
    public static List<ItemEntry> getTools() {
        TutorialMod.LOGGER.debug("🛠️  [TOOLS FILTER] Retrieving all tool items (swords, pickaxes, shovels, axes, hoes, hammers)");
        long startTime = System.nanoTime();

        List<ItemEntry> result = getAllItems().stream()
            .filter(e -> {
                String name = e.name();
                boolean isTool = name.contains("sword") || name.contains("pickaxe") || name.contains("shovel") ||
                       name.contains("axe") || name.contains("hoe") || name.contains("hammer");
                if (isTool) {
                    TutorialMod.LOGGER.trace("     ✓ Tool: {}", name);
                }
                return isTool;
            })
            .collect(Collectors.toUnmodifiableList());

        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        TutorialMod.LOGGER.debug("   └─ Found {} total tools in {}ms", result.size(), elapsed);
        return result;
    }

    /**
     * Get all armor pieces
     */
    public static List<ItemEntry> getArmor() {
        TutorialMod.LOGGER.debug("🗡️  [ARMOR FILTER] Retrieving all armor items (helmets, chestplates, leggings, boots)");
        long startTime = System.nanoTime();

        List<ItemEntry> result = getAllItems().stream()
            .filter(e -> {
                String name = e.name();
                boolean isArmor = name.contains("helmet") || name.contains("chestplate") ||
                       name.contains("legging") || name.contains("boots");
                if (isArmor) {
                    TutorialMod.LOGGER.trace("     ✓ Armor: {}", name);
                }
                return isArmor;
            })
            .collect(Collectors.toUnmodifiableList());

        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        TutorialMod.LOGGER.debug("   └─ Found {} total armor pieces in {}ms", result.size(), elapsed);
        return result;
    }

    /**
     * Get trimmable armor (excludes pajamas)
     */
    public static List<ItemEntry> getTrimmableArmor() {
        TutorialMod.LOGGER.debug("✨ [TRIMMABLE ARMOR FILTER] Retrieving trimmable armor items (excluding pajamas)");
        long startTime = System.nanoTime();

        List<ItemEntry> result = getArmor().stream()
            .filter(entry -> {
                boolean isTrimmable = !entry.name().contains("pajama");
                if (!isTrimmable) {
                    TutorialMod.LOGGER.trace("     ⊘ Excluded (pajama): {}", entry.name());
                }
                return isTrimmable;
            })
            .collect(Collectors.toUnmodifiableList());

        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        int excluded = getArmor().size() - result.size();
        TutorialMod.LOGGER.debug("   └─ Found {} trimmable items (excluded {} pajamas) in {}ms", result.size(), excluded, elapsed);
        return result;
    }

    /**
     * Helper method for cached single-keyword filtering
     */
    private static List<ItemEntry> getFromCache(String keyword, boolean cached) {
        if (!cached) {
            TutorialMod.LOGGER.debug("   ├─ Cache disabled for keyword: {}", keyword);
            List<ItemEntry> result = getAllItems().stream()
                .filter(e -> {
                    boolean matches = e.name().contains(keyword);
                    if (matches) {
                        TutorialMod.LOGGER.trace("     ✓ Match: {}", e.name());
                    }
                    return matches;
                })
                .collect(Collectors.toUnmodifiableList());
            TutorialMod.LOGGER.debug("   └─ Found {} items (uncached)", result.size());
            return result;
        }

        return itemCategoryCache.computeIfAbsent(keyword, key -> {
            TutorialMod.LOGGER.debug("   ├─ Cache miss for keyword: {}", key);
            long startTime = System.nanoTime();

            List<ItemEntry> filtered = getAllItems().stream()
                .filter(e -> {
                    boolean matches = e.name().contains(key);
                    if (matches) {
                        TutorialMod.LOGGER.trace("     ✓ Match: {}", e.name());
                    }
                    return matches;
                })
                .collect(Collectors.toUnmodifiableList());

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            TutorialMod.LOGGER.debug("   └─ Found {} items in {}ms and cached", filtered.size(), elapsed);

            return filtered;
        });
    }

    // ===== BLOCK FILTERING METHODS =====

    /**
     * Filter blocks by keywords (cached)
     */
    public static List<BlockEntry> getBlocksContaining(String... keywords) {
        String cacheKey = "blocks_" + String.join("|", keywords);
        TutorialMod.LOGGER.debug("🔎 [BLOCK FILTER] Filtering blocks by keywords: {}", Arrays.toString(keywords));

        List<BlockEntry> result = blockCategoryCache.computeIfAbsent(cacheKey, key -> {
            TutorialMod.LOGGER.debug("   ├─ Cache miss for keyword set: {}", key);
            long startTime = System.nanoTime();

            List<BlockEntry> filtered = getAllBlocks().stream()
                .filter(entry -> {
                    boolean matches = containsAny(entry.name(), keywords);
                    if (matches) {
                        TutorialMod.LOGGER.trace("     ✓ Match: {} (contains {})", entry.name(), Arrays.toString(keywords));
                    }
                    return matches;
                })
                .collect(Collectors.toUnmodifiableList());

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            TutorialMod.LOGGER.debug("   ├─ Filtered {} blocks from {} total in {}ms", filtered.size(), getAllBlocks().size(), elapsed);
            TutorialMod.LOGGER.debug("   └─ Cached for future use");

            return filtered;
        });

        TutorialMod.LOGGER.debug("   └─ Returning {} matching blocks", result.size());
        return result;
    }

    public static List<BlockEntry> getOres() {
        TutorialMod.LOGGER.debug("⛏️  [ORE FILTER] Retrieving all ore blocks");
        List<BlockEntry> result = getFromBlockCache("_ore");
        TutorialMod.LOGGER.debug("   └─ Found {} ores", result.size());
        return result;
    }

    public static List<BlockEntry> getStairs() {
        TutorialMod.LOGGER.debug("🪜 [STAIRS FILTER] Retrieving all stair blocks");
        List<BlockEntry> result = getFromBlockCache("stairs");
        TutorialMod.LOGGER.debug("   └─ Found {} stair variants", result.size());
        return result;
    }

    public static List<BlockEntry> getSlabs() {
        TutorialMod.LOGGER.debug("📦 [SLAB FILTER] Retrieving all slab blocks");
        List<BlockEntry> result = getFromBlockCache("slab");
        TutorialMod.LOGGER.debug("   └─ Found {} slab variants", result.size());
        return result;
    }

    public static List<BlockEntry> getFences() {
        TutorialMod.LOGGER.debug("🚧 [FENCE FILTER] Retrieving all fence blocks");
        List<BlockEntry> result = getFromBlockCache("fence");
        TutorialMod.LOGGER.debug("   └─ Found {} fence variants", result.size());
        return result;
    }

    public static List<BlockEntry> getWalls() {
        TutorialMod.LOGGER.debug("🧱 [WALL FILTER] Retrieving all wall blocks");
        List<BlockEntry> result = getFromBlockCache("wall");
        TutorialMod.LOGGER.debug("   └─ Found {} wall variants", result.size());
        return result;
    }

    public static List<BlockEntry> getDoors() {
        TutorialMod.LOGGER.debug("🚪 [DOOR FILTER] Retrieving all door blocks");
        List<BlockEntry> result = getFromBlockCache("door");
        TutorialMod.LOGGER.debug("   └─ Found {} door variants", result.size());
        return result;
    }

    public static List<BlockEntry> getTrapdoors() {
        TutorialMod.LOGGER.debug("🪵 [TRAPDOOR FILTER] Retrieving all trapdoor blocks");
        List<BlockEntry> result = getFromBlockCache("trapdoor");
        TutorialMod.LOGGER.debug("   └─ Found {} trapdoor variants", result.size());
        return result;
    }

    public static List<BlockEntry> getButtons() {
        TutorialMod.LOGGER.debug("🔘 [BUTTON FILTER] Retrieving all button blocks");
        List<BlockEntry> result = getFromBlockCache("button");
        TutorialMod.LOGGER.debug("   └─ Found {} button variants", result.size());
        return result;
    }

    public static List<BlockEntry> getPressurePlates() {
        TutorialMod.LOGGER.debug("⚖️  [PRESSURE PLATE FILTER] Retrieving all pressure plate blocks");
        List<BlockEntry> result = getFromBlockCache("pressure_plate");
        TutorialMod.LOGGER.debug("   └─ Found {} pressure plate variants", result.size());
        return result;
    }

    public static List<BlockEntry> getLogs() {
        TutorialMod.LOGGER.debug("🌳 [LOG FILTER] Retrieving all log and wood blocks");
        List<BlockEntry> result = getAllBlocks().stream()
            .filter(e -> {
                boolean matches = e.name().contains("log") || e.name().contains("wood");
                if (matches) {
                    TutorialMod.LOGGER.trace("     ✓ Match: {}", e.name());
                }
                return matches;
            })
            .collect(Collectors.toUnmodifiableList());
        TutorialMod.LOGGER.debug("   └─ Found {} log/wood variants", result.size());
        return result;
    }

    public static List<BlockEntry> getPlanks() {
        TutorialMod.LOGGER.debug("🪵 [PLANKS FILTER] Retrieving all plank blocks");
        List<BlockEntry> result = getFromBlockCache("planks");
        TutorialMod.LOGGER.debug("   └─ Found {} plank variants", result.size());
        return result;
    }

    /**
     * Helper method for cached single-keyword block filtering
     */
    private static List<BlockEntry> getFromBlockCache(String keyword) {
        String cacheKey = "block_" + keyword;
        return blockCategoryCache.computeIfAbsent(cacheKey, key -> {
            TutorialMod.LOGGER.debug("   ├─ Cache miss for block keyword: {}", keyword);
            long startTime = System.nanoTime();

            List<BlockEntry> filtered = getAllBlocks().stream()
                .filter(e -> {
                    boolean matches = e.name().contains(keyword);
                    if (matches) {
                        TutorialMod.LOGGER.trace("     ✓ Match: {}", e.name());
                    }
                    return matches;
                })
                .collect(Collectors.toUnmodifiableList());

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            TutorialMod.LOGGER.debug("   └─ Found {} blocks in {}ms and cached", filtered.size(), elapsed);

            return filtered;
        });
    }

    // ===== UTILITY METHODS =====

    /**
     * Get material name from item/block name (e.g., "pink_garnet_sword" -> "pink_garnet")
     */
    public static String getMaterialName(String fullName) {
        TutorialMod.LOGGER.trace("🔧 [MATERIAL PARSING] Extracting material name from: {}", fullName);

        // Remove common suffixes
        String[] suffixes = {"_sword", "_pickaxe", "_shovel", "_axe", "_hoe", "_hammer",
                            "_helmet", "_chestplate", "_leggings", "_boots",
                            "_block", "_ore", "_stairs", "_slab", "_wall", "_fence", "_door",
                            "_trapdoor", "_button", "_pressure_plate"};

        for (String suffix : suffixes) {
            if (fullName.endsWith(suffix)) {
                String materialName = fullName.substring(0, fullName.length() - suffix.length());
                TutorialMod.LOGGER.trace("   └─ Extracted material: {} (removed suffix: {})", materialName, suffix);
                return materialName;
            }
        }

        TutorialMod.LOGGER.trace("   └─ No suffix matched, returning original: {}", fullName);
        return fullName;
    }

    /**
     * Find material item by name (e.g., "pink_garnet" -> ModItems.PINK_GARNET)
     */
    public static Item findMaterialItem(String materialName) {
        TutorialMod.LOGGER.debug("🔍 [ITEM LOOKUP] Finding material item: {}", materialName);

        Item foundItem = getAllItems().stream()
            .filter(entry -> {
                boolean matches = entry.name().equals(materialName);
                if (matches) {
                    TutorialMod.LOGGER.debug("   ✓ Found matching item: {}", entry.name());
                }
                return matches;
            })
            .map(ItemEntry::item)
            .findFirst()
            .orElse(null);

        if (foundItem == null) {
            TutorialMod.LOGGER.warn("   ✗ Material item NOT found: {}", materialName);
        }

        return foundItem;
    }

    /**
     * Find material block by name
     */
    public static Block findMaterialBlock(String materialName) {
        TutorialMod.LOGGER.debug("🔍 [BLOCK LOOKUP] Finding material block: {}", materialName);

        Block foundBlock = getAllBlocks().stream()
            .filter(entry -> {
                boolean matches = entry.name().equals(materialName);
                if (matches) {
                    TutorialMod.LOGGER.debug("   ✓ Found matching block: {}", entry.name());
                }
                return matches;
            })
            .map(BlockEntry::block)
            .findFirst()
            .orElse(null);

        if (foundBlock == null) {
            TutorialMod.LOGGER.warn("   ✗ Material block NOT found: {}", materialName);
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

        TutorialMod.LOGGER.trace("   ├─ Checking if craftable: {} -> {}", name, result);
        return result;
    }

    /**
     * Check if item is a tool
     */
    public static boolean isTool(String name) {
        boolean result = name.contains("sword") || name.contains("pickaxe") || name.contains("shovel") ||
               name.contains("axe") || name.contains("hoe") || name.contains("hammer");

        TutorialMod.LOGGER.trace("   ├─ Checking if tool: {} -> {}", name, result);
        return result;
    }

    /**
     * Check if item is armor
     */
    public static boolean isArmor(String name) {
        boolean result = name.contains("helmet") || name.contains("chestplate") ||
               name.contains("leggings") || name.contains("boots");

        TutorialMod.LOGGER.trace("   ├─ Checking if armor: {} -> {}", name, result);
        return result;
    }

    /**
     * Get count of discovered items
     */
    public static int getItemCount() {
        int count = getAllItems().size();
        TutorialMod.LOGGER.debug("📊 [ITEM COUNT] Total items discovered: {}", count);
        return count;
    }

    /**
     * Get count of discovered blocks
     */
    public static int getBlockCount() {
        int count = getAllBlocks().size();
        TutorialMod.LOGGER.debug("📊 [BLOCK COUNT] Total blocks discovered: {}", count);
        return count;
    }

    /**
     * Clear all caches (useful for testing)
     */
    public static void clearCaches() {
        TutorialMod.LOGGER.info("🧹 [CACHE CLEAR] Starting cache clearing operation");
        int itemCacheBefore = itemCategoryCache.size();
        int blockCacheBefore = blockCategoryCache.size();

        TutorialMod.LOGGER.debug("   ├─ Item category caches before: {}", itemCacheBefore);
        TutorialMod.LOGGER.debug("   ├─ Block category caches before: {}", blockCacheBefore);

        itemCategoryCache.clear();
        blockCategoryCache.clear();
        cachedItems = null;
        cachedBlocks = null;

        TutorialMod.LOGGER.debug("   ├─ Cleared {} item category caches", itemCacheBefore);
        TutorialMod.LOGGER.debug("   ├─ Cleared {} block category caches", blockCacheBefore);
        TutorialMod.LOGGER.debug("   ├─ Primary item cache: CLEARED");
        TutorialMod.LOGGER.debug("   └─ Primary block cache: CLEARED");
        TutorialMod.LOGGER.info("✅ [CACHE CLEAR COMPLETE] All caches successfully cleared and reset");
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

        TutorialMod.LOGGER.info("📊 [CACHE STATS]");
        TutorialMod.LOGGER.info("   ├─ Primary Caches:");
        TutorialMod.LOGGER.info("   │  ├─ Items: {} entries", itemsInPrimary);
        TutorialMod.LOGGER.info("   │  └─ Blocks: {} entries", blocksInPrimary);
        TutorialMod.LOGGER.info("   ├─ Category Caches:");
        TutorialMod.LOGGER.info("   │  ├─ Item categories: {} cached", itemCategoryCaches);
        TutorialMod.LOGGER.info("   │  └─ Block categories: {} cached", blockCategoryCaches);
        TutorialMod.LOGGER.info("   └─ Total Memory: ~{} KB", (itemsInPrimary + blocksInPrimary + itemCategoryCaches + blockCategoryCaches) * 2);

        return stats;
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                TutorialMod.LOGGER.trace("       └─ Contains keyword: {}", keyword);
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
                TutorialMod.LOGGER.trace("     ✓ ItemEntry match: {} contains {}", name, keyword);
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
                TutorialMod.LOGGER.trace("     ✓ BlockEntry match: {} contains {}", name, keyword);
            }
            return result;
        }

        public Item asItem() {
            Item result = block.asItem();
            TutorialMod.LOGGER.trace("       └─ Converted block to item: {}", result);
            return result;
        }
    }
}
