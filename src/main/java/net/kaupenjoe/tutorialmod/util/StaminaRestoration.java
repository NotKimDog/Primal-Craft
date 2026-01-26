package net.kaupenjoe.tutorialmod.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Stamina restoration effects for food items and rest mechanics
 */
public final class StaminaRestoration {
    private static final java.util.Map<String, Double> STAMINA_FOODS = new java.util.HashMap<>();

    static {
        // Food items that restore stamina and reduce fatigue
        STAMINA_FOODS.put("enchanted_golden_apple", 50.0);
        STAMINA_FOODS.put("golden_apple", 25.0);
        STAMINA_FOODS.put("cooked_beef", 12.0);
        STAMINA_FOODS.put("cooked_mutton", 12.0);
        STAMINA_FOODS.put("cooked_pork", 12.0);
        STAMINA_FOODS.put("cooked_salmon", 10.0);
        STAMINA_FOODS.put("cooked_cod", 8.0);
        STAMINA_FOODS.put("bread", 8.0);
        STAMINA_FOODS.put("baked_potato", 7.0);
        STAMINA_FOODS.put("apple", 5.0);
        STAMINA_FOODS.put("carrot", 4.0);
        STAMINA_FOODS.put("melon_slice", 3.0);
    }

    public static void register() {
        // Tick update for stamina restoration when resting
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                checkForRestAndRestore(player);
            }
        });
    }

    /**
     * Check if player is resting (not moving) and restore stamina/reduce fatigue
     */
    private static void checkForRestAndRestore(ServerPlayerEntity player) {
        // If player is standing still and well-fed, restore stamina and reduce fatigue faster
        if (player.getHungerManager().getFoodLevel() > 14) {
            double velocity = player.getVelocity().horizontalLengthSquared();

            // Player is mostly still
            if (velocity < 0.001) {
                // Restore stamina and reduce fatigue when resting with food
                EnhancedStaminaManager.restoreStamina(player, 0.3);
            }
        }
    }

    /**
     * Get stamina restoration amount for a food item
     */
    public static double getStaminaRestoration(ItemStack stack) {
        String itemName = stack.getItem().getTranslationKey();
        for (String key : STAMINA_FOODS.keySet()) {
            if (itemName.contains(key)) {
                return STAMINA_FOODS.get(key);
            }
        }
        return 0.0;
    }

    /**
     * Apply stamina restoration from eating a food item
     */
    public static void applyFoodRestoration(ServerPlayerEntity player, ItemStack food) {
        double restoration = getStaminaRestoration(food);
        if (restoration > 0) {
            EnhancedStaminaManager.restoreStamina(player, restoration);
        }
    }
}
