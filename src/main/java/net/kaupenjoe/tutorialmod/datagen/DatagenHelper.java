package net.kaupenjoe.tutorialmod.datagen;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.kaupenjoe.tutorialmod.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Automated datagen helper that uses reflection to discover all registered items and blocks.
 * Provides utilities for pattern matching and automatic categorization.
 *
 * @author KimDog Studios
 */
public class DatagenHelper {

    // Cached collections for performance
    private static List<ItemEntry> cachedItems = null;
    private static List<BlockEntry> cachedBlocks = null;

    /**
     * Get all registered items with their names
     */
    public static List<ItemEntry> getAllItems() {
        if (cachedItems != null) return cachedItems;

        List<ItemEntry> items = new ArrayList<>();

        try {
            Field[] fields = ModItems.class.getDeclaredFields();
            for (Field field : fields) {
                // Only process public static final Item fields
                if (Modifier.isPublic(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers()) &&
                    Item.class.isAssignableFrom(field.getType())) {

                    field.setAccessible(true);
                    Item item = (Item) field.get(null);
                    String name = field.getName().toLowerCase();
                    items.add(new ItemEntry(name, item));
                }
            }
        } catch (Exception e) {
            TutorialMod.LOGGER.error("Failed to scan ModItems for datagen: {}", e.getMessage(), e);
        }

        cachedItems = items;
        TutorialMod.LOGGER.info("DatagenHelper: Discovered {} items for automation", items.size());
        return items;
    }

    /**
     * Get all registered blocks with their names
     */
    public static List<BlockEntry> getAllBlocks() {
        if (cachedBlocks != null) return cachedBlocks;

        List<BlockEntry> blocks = new ArrayList<>();

        try {
            Field[] fields = ModBlocks.class.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isPublic(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers()) &&
                    Block.class.isAssignableFrom(field.getType())) {

                    field.setAccessible(true);
                    Block block = (Block) field.get(null);
                    String name = field.getName().toLowerCase();
                    blocks.add(new BlockEntry(name, block));
                }
            }
        } catch (Exception e) {
            TutorialMod.LOGGER.error("Failed to scan ModBlocks for datagen: {}", e.getMessage(), e);
        }

        cachedBlocks = blocks;
        TutorialMod.LOGGER.info("DatagenHelper: Discovered {} blocks for automation", blocks.size());
        return blocks;
    }

    // ===== ITEM FILTERING METHODS =====

    public static List<ItemEntry> getItemsContaining(String... keywords) {
        return getAllItems().stream()
            .filter(entry -> containsAny(entry.name(), keywords))
            .collect(Collectors.toList());
    }

    public static List<ItemEntry> getSwords() {
        return getItemsContaining("sword");
    }

    public static List<ItemEntry> getPickaxes() {
        return getItemsContaining("pickaxe", "hammer");
    }

    public static List<ItemEntry> getShovels() {
        return getItemsContaining("shovel");
    }

    public static List<ItemEntry> getAxes() {
        return getItemsContaining("axe");
    }

    public static List<ItemEntry> getHoes() {
        return getItemsContaining("hoe");
    }

    public static List<ItemEntry> getHelmets() {
        return getItemsContaining("helmet");
    }

    public static List<ItemEntry> getChestplates() {
        return getItemsContaining("chestplate", "top");
    }

    public static List<ItemEntry> getLeggings() {
        return getItemsContaining("leggings", "bottoms");
    }

    public static List<ItemEntry> getBoots() {
        return getItemsContaining("boots");
    }

    public static List<ItemEntry> getArmor() {
        List<ItemEntry> armor = new ArrayList<>();
        armor.addAll(getHelmets());
        armor.addAll(getChestplates());
        armor.addAll(getLeggings());
        armor.addAll(getBoots());
        return armor;
    }

    public static List<ItemEntry> getTrimmableArmor() {
        // Exclude pajamas and other non-trimmable armor
        return getArmor().stream()
            .filter(entry -> !entry.name().contains("pajama"))
            .collect(Collectors.toList());
    }

    public static List<ItemEntry> getTools() {
        List<ItemEntry> tools = new ArrayList<>();
        tools.addAll(getSwords());
        tools.addAll(getPickaxes());
        tools.addAll(getShovels());
        tools.addAll(getAxes());
        tools.addAll(getHoes());
        return tools;
    }

    // ===== BLOCK FILTERING METHODS =====

    public static List<BlockEntry> getBlocksContaining(String... keywords) {
        return getAllBlocks().stream()
            .filter(entry -> containsAny(entry.name(), keywords))
            .collect(Collectors.toList());
    }

    public static List<BlockEntry> getOres() {
        return getBlocksContaining("_ore");
    }

    public static List<BlockEntry> getStairs() {
        return getBlocksContaining("stairs");
    }

    public static List<BlockEntry> getSlabs() {
        return getBlocksContaining("slab");
    }

    public static List<BlockEntry> getFences() {
        return getBlocksContaining("fence");
    }

    public static List<BlockEntry> getWalls() {
        return getBlocksContaining("wall");
    }

    public static List<BlockEntry> getDoors() {
        return getBlocksContaining("door");
    }

    public static List<BlockEntry> getTrapdoors() {
        return getBlocksContaining("trapdoor");
    }

    public static List<BlockEntry> getButtons() {
        return getBlocksContaining("button");
    }

    public static List<BlockEntry> getPressurePlates() {
        return getBlocksContaining("pressure_plate");
    }

    public static List<BlockEntry> getLogs() {
        return getBlocksContaining("log", "wood");
    }

    public static List<BlockEntry> getPlanks() {
        return getBlocksContaining("planks");
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
     * Check if item is a tool/armor that should have recipes
     */
    public static boolean isCraftable(String name) {
        return name.contains("sword") || name.contains("pickaxe") || name.contains("shovel") ||
               name.contains("axe") || name.contains("hoe") || name.contains("hammer") ||
               name.contains("helmet") || name.contains("chestplate") ||
               name.contains("leggings") || name.contains("boots");
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
