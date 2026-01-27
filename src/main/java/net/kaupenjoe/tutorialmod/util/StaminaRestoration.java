package net.kaupenjoe.tutorialmod.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Stamina restoration effects for food items and rest mechanics
 */
public final class StaminaRestoration {
    private static final java.util.Map<String, Double> STAMINA_FOODS = new java.util.HashMap<>();
    private static int restEvents = 0;
    private static int foodRestorations = 0;

    static {
        // ...existing food registrations...
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
        TutorialMod.LOGGER.info("⚙️  [STAMINA_RESTORATION] Initializing StaminaRestoration");
        TutorialMod.LOGGER.debug("   ├─ Registered food types: {}", STAMINA_FOODS.size());
        TutorialMod.LOGGER.debug("   └─ Registering rest detection...");

        // Tick update for stamina restoration when resting
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                checkForRestAndRestore(player);
            }
        });

        TutorialMod.LOGGER.info("✅ [STAMINA_RESTORATION] Rest detection registered");
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
                restEvents++;
                TutorialMod.LOGGER.trace("💤 [REST] Event #{} - {} is resting",
                    restEvents, player.getName().getString());
                TutorialMod.LOGGER.trace("   ├─ Food level: {}", player.getHungerManager().getFoodLevel());
                TutorialMod.LOGGER.trace("   ├─ Velocity: {}", String.format("%.6f", velocity));
                TutorialMod.LOGGER.trace("   └─ Restoring +0.3 stamina");

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
                double restoration = STAMINA_FOODS.get(key);
                TutorialMod.LOGGER.trace("🍽️  [FOOD_LOOKUP] {} restores {} stamina",
                    itemName, String.format("%.1f", restoration));
                return restoration;
            }
        }
        TutorialMod.LOGGER.trace("🍽️  [FOOD_LOOKUP] {} not in stamina food database", itemName);
        return 0.0;
    }

    /**
     * Apply stamina restoration from eating a food item
     */
    public static void applyFoodRestoration(ServerPlayerEntity player, ItemStack food) {
        double restoration = getStaminaRestoration(food);
        if (restoration > 0) {
            foodRestorations++;
            TutorialMod.LOGGER.debug("🍽️  [FOOD_RESTORE] Event #{} - {} ate food",
                foodRestorations, player.getName().getString());
            TutorialMod.LOGGER.trace("   ├─ Food: {}", food.getItem().getName().getString());
            TutorialMod.LOGGER.trace("   ├─ Restoration: {}", String.format("%.1f", restoration));
            TutorialMod.LOGGER.trace("   └─ Applying stamina restoration");

            EnhancedStaminaManager.restoreStamina(player, restoration);
        } else {
            TutorialMod.LOGGER.trace("   └─ No stamina restoration for this food");
        }
    }
}
