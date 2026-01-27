package net.kaupenjoe.tutorialmod.util;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;

/**
 * System to calculate stamina drain based on item weight in inventory.
 * Heavier items = more stamina drain while carrying them.
 */
public final class ItemWeightSystem {
    // ...existing code...
    // Base weights for vanilla items (in stamina drain per tick per item)
    private static final Map<String, Double> ITEM_WEIGHTS = new HashMap<>();

    static {
        // === TOOLS & WEAPONS ===
        // Pickaxes
        registerItem("wooden_pickaxe", 0.05);
        registerItem("stone_pickaxe", 0.08);
        registerItem("iron_pickaxe", 0.12);
        registerItem("golden_pickaxe", 0.10);
        registerItem("diamond_pickaxe", 0.15);
        registerItem("netherite_pickaxe", 0.18);

        // Axes
        registerItem("wooden_axe", 0.06);
        registerItem("stone_axe", 0.09);
        registerItem("iron_axe", 0.13);
        registerItem("golden_axe", 0.11);
        registerItem("diamond_axe", 0.16);
        registerItem("netherite_axe", 0.19);

        // Swords
        registerItem("wooden_sword", 0.04);
        registerItem("stone_sword", 0.07);
        registerItem("iron_sword", 0.11);
        registerItem("golden_sword", 0.09);
        registerItem("diamond_sword", 0.14);
        registerItem("netherite_sword", 0.17);

        // Shovels
        registerItem("wooden_shovel", 0.03);
        registerItem("stone_shovel", 0.06);
        registerItem("iron_shovel", 0.10);
        registerItem("golden_shovel", 0.08);
        registerItem("diamond_shovel", 0.13);
        registerItem("netherite_shovel", 0.16);

        // Hoes
        registerItem("wooden_hoe", 0.02);
        registerItem("stone_hoe", 0.05);
        registerItem("iron_hoe", 0.09);
        registerItem("golden_hoe", 0.07);
        registerItem("diamond_hoe", 0.12);
        registerItem("netherite_hoe", 0.15);

        // === ARMOR ===
        // Leather
        registerItem("leather_helmet", 0.02);
        registerItem("leather_chestplate", 0.05);
        registerItem("leather_leggings", 0.04);
        registerItem("leather_boots", 0.01);

        // Chainmail
        registerItem("chainmail_helmet", 0.08);
        registerItem("chainmail_chestplate", 0.15);
        registerItem("chainmail_leggings", 0.12);
        registerItem("chainmail_boots", 0.05);

        // Iron
        registerItem("iron_helmet", 0.12);
        registerItem("iron_chestplate", 0.20);
        registerItem("iron_leggings", 0.16);
        registerItem("iron_boots", 0.08);

        // Golden
        registerItem("golden_helmet", 0.10);
        registerItem("golden_chestplate", 0.18);
        registerItem("golden_leggings", 0.14);
        registerItem("golden_boots", 0.06);

        // Diamond
        registerItem("diamond_helmet", 0.14);
        registerItem("diamond_chestplate", 0.22);
        registerItem("diamond_leggings", 0.18);
        registerItem("diamond_boots", 0.10);

        // Netherite
        registerItem("netherite_helmet", 0.16);
        registerItem("netherite_chestplate", 0.24);
        registerItem("netherite_leggings", 0.20);
        registerItem("netherite_boots", 0.12);

        // === ORE BLOCKS & MATERIALS (Heavy) ===
        registerItem("iron_ore", 0.30);
        registerItem("gold_ore", 0.32);
        registerItem("diamond_ore", 0.35);
        registerItem("emerald_ore", 0.33);
        registerItem("coal_ore", 0.25);
        registerItem("lapis_ore", 0.28);
        registerItem("redstone_ore", 0.27);

        registerItem("iron_block", 0.80);
        registerItem("gold_block", 0.85);
        registerItem("diamond_block", 0.90);
        registerItem("emerald_block", 0.88);
        registerItem("lapis_block", 0.75);
        registerItem("redstone_block", 0.70);

        registerItem("raw_iron", 0.15);
        registerItem("raw_gold", 0.18);
        registerItem("raw_copper", 0.12);

        // === INGOTS & GEMS ===
        registerItem("iron_ingot", 0.08);
        registerItem("gold_ingot", 0.09);
        registerItem("copper_ingot", 0.07);
        registerItem("diamond", 0.05);
        registerItem("emerald", 0.05);

        // === BLOCKS (Medium-Heavy) ===
        registerItem("stone", 0.20);
        registerItem("cobblestone", 0.20);
        registerItem("dirt", 0.15);
        registerItem("grass_block", 0.15);
        registerItem("sand", 0.18);
        registerItem("gravel", 0.19);
        registerItem("oak_log", 0.25);
        registerItem("spruce_log", 0.25);
        registerItem("birch_log", 0.24);
        registerItem("jungle_log", 0.26);
        registerItem("acacia_log", 0.24);
        registerItem("dark_oak_log", 0.27);
        registerItem("brick", 0.22);
        registerItem("nether_brick", 0.23);

        // === WOOD ===
        registerItem("oak_wood", 0.25);
        registerItem("spruce_wood", 0.25);
        registerItem("birch_wood", 0.24);
        registerItem("jungle_wood", 0.26);
        registerItem("acacia_wood", 0.24);
        registerItem("dark_oak_wood", 0.27);
        registerItem("oak_planks", 0.10);
        registerItem("spruce_planks", 0.10);
        registerItem("birch_planks", 0.09);
        registerItem("jungle_planks", 0.10);
        registerItem("acacia_planks", 0.09);
        registerItem("dark_oak_planks", 0.10);

        // === FOOD (Light) ===
        registerItem("apple", 0.01);
        registerItem("baked_potato", 0.01);
        registerItem("bread", 0.01);
        registerItem("carrot", 0.01);
        registerItem("cooked_beef", 0.02);
        registerItem("cooked_chicken", 0.01);
        registerItem("cooked_mutton", 0.02);
        registerItem("cooked_pork", 0.02);
        registerItem("cooked_rabbit", 0.01);
        registerItem("cooked_salmon", 0.01);
        registerItem("cooked_cod", 0.01);
        registerItem("golden_apple", 0.02);
        registerItem("melon_slice", 0.01);
        registerItem("pumpkin_pie", 0.01);

        // === POTIONS & BOTTLES (Very Light) ===
        registerItem("potion", 0.02);
        registerItem("splash_potion", 0.02);
        registerItem("lingering_potion", 0.02);
        registerItem("glass_bottle", 0.01);
        registerItem("milk_bucket", 0.08);
        registerItem("water_bucket", 0.08);
        registerItem("lava_bucket", 0.10);

        // === BUILDING MATERIALS (Light) ===
        registerItem("glass", 0.08);
        registerItem("glass_pane", 0.04);
        registerItem("crafting_table", 0.35);
        registerItem("furnace", 0.40);
        registerItem("chest", 0.30);
        registerItem("barrel", 0.28);
        registerItem("chest_minecart", 1.50);
        registerItem("furnace_minecart", 1.60);

        // === MISC ITEMS (Variable) ===
        registerItem("ender_pearl", 0.02);
        registerItem("ender_eye", 0.03);
        registerItem("book", 0.02);
        registerItem("enchanted_book", 0.03);
        registerItem("string", 0.01);
        registerItem("stick", 0.01);
        registerItem("bone", 0.02);
        registerItem("saddle", 0.05);
        registerItem("leather", 0.02);
        registerItem("feather", 0.01);
        registerItem("gunpowder", 0.02);
        registerItem("slime_ball", 0.02);
        registerItem("clay_ball", 0.02);
        registerItem("brick", 0.02);
        registerItem("nether_brick", 0.02);
        registerItem("beacon", 0.45);
        registerItem("end_crystal", 0.50);
        registerItem("dragon_egg", 2.50);
        registerItem("totem_of_undying", 0.08);

        // === SHULKER BOXES (VERY HEAVY) ===
        registerItem("white_shulker_box", 3.0);
        registerItem("orange_shulker_box", 3.0);
        registerItem("magenta_shulker_box", 3.0);
        registerItem("light_blue_shulker_box", 3.0);
        registerItem("yellow_shulker_box", 3.0);
        registerItem("lime_shulker_box", 3.0);
        registerItem("pink_shulker_box", 3.0);
        registerItem("gray_shulker_box", 3.0);
        registerItem("light_gray_shulker_box", 3.0);
        registerItem("cyan_shulker_box", 3.0);
        registerItem("purple_shulker_box", 3.0);
        registerItem("blue_shulker_box", 3.0);
        registerItem("brown_shulker_box", 3.0);
        registerItem("green_shulker_box", 3.0);
        registerItem("red_shulker_box", 3.0);
        registerItem("black_shulker_box", 3.0);

        // === MODDED ITEMS (KimDog SMP) ===
        registerItem("pink_garnet", 0.06);
        registerItem("pink_garnet_ore", 0.32);
        registerItem("pink_garnet_block", 0.85);
        registerItem("raw_pink_garnet", 0.16);
        registerItem("cauliflower", 0.01);
        registerItem("cauliflower_seeds", 0.01);
        registerItem("honey_berries", 0.01);
        registerItem("chisel", 0.10);
        registerItem("tomahawk", 0.12);
        registerItem("driftwood_log", 0.24);
        registerItem("driftwood_wood", 0.24);
        registerItem("stripped_driftwood_log", 0.23);
        registerItem("stripped_driftwood_wood", 0.23);
        registerItem("driftwood_planks", 0.09);
    }

    private static void registerItem(String itemName, double weight) {
        ITEM_WEIGHTS.put(itemName, weight);
    }

    /**
     * Calculate total weight penalty for player inventory.
     * Returns stamina drain per tick based on carried items.
     */
    public static double calculateInventoryWeightPenalty(net.minecraft.entity.player.PlayerEntity player) {
        double totalWeight = 0.0;
        int itemCount = 0;

        TutorialMod.LOGGER.trace("⚖️  [WEIGHT_CALC] Computing inventory weight for {}", player.getName().getString());
        TutorialMod.LOGGER.trace("   ├─ Scanning {} inventory slots...", player.getInventory().size());

        // Check all inventory slots (0-35 main, 36-39 armor, 40 offhand)
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                double itemWeight = getItemWeight(stack);
                double stackWeight = itemWeight * stack.getCount();
                totalWeight += stackWeight;
                itemCount++;

                String itemName = Registries.ITEM.getId(stack.getItem()).getPath();
                TutorialMod.LOGGER.trace("   │  ├─ Slot {}: {} x{} ({}): {}",
                    i, itemName, stack.getCount(), String.format("%.2f", itemWeight),
                    String.format("%.2f", stackWeight));
            }
        }

        double penalty = Math.min(totalWeight, 5.0);
        TutorialMod.LOGGER.trace("   ├─ Total weight: {} (capped at 5.0)", String.format("%.2f", totalWeight));
        TutorialMod.LOGGER.trace("   ├─ Items carried: {}", itemCount);
        TutorialMod.LOGGER.trace("   └─ Final penalty: {} (~{}%)", String.format("%.2f", penalty), String.format("%.1f", penalty * 100));

        return penalty;
    }

    /**
     * Get weight of a single item stack.
     */
    private static double getItemWeight(ItemStack stack) {
        String itemName = Registries.ITEM.getId(stack.getItem()).getPath();
        double weight = ITEM_WEIGHTS.getOrDefault(itemName, 0.01);
        TutorialMod.LOGGER.trace("       └─ Item weight lookup: {}", String.format("%.2f", weight));
        return weight;
    }

    public static double getItemWeight(String itemName) {
        double weight = ITEM_WEIGHTS.getOrDefault(itemName, 0.01);
        TutorialMod.LOGGER.debug("⚖️  [WEIGHT_LOOKUP] {} weight: {}", itemName, String.format("%.2f", weight));
        return weight;
    }
}
