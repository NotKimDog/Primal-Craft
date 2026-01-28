package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Infinite Trading System
 *
 * Prevents villager trades from locking up after purchase.
 *
 * Features:
 * - Unlimited trades with all villagers
 * - No trade lock-up
 * - All professions supported
 * - Configurable
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class InfiniteTradingHandler {
    private InfiniteTradingHandler() {}

    private static boolean lastTradingState = false;

    public static void register() {
        PrimalCraft.LOGGER.info("💰 [INFINITE_TRADE] Registering Infinite Trading Handler");

        ServerTickEvents.END_SERVER_TICK.register(InfiniteTradingHandler::onServerTick);

        PrimalCraft.LOGGER.info("✅ [INFINITE_TRADE] Infinite Trading Handler registered");
    }

    private static void onServerTick(MinecraftServer server) {
        try {
            boolean infiniteTradingEnabled = isInfiniteTradingEnabled();
            if (infiniteTradingEnabled != lastTradingState) {
                lastTradingState = infiniteTradingEnabled;
                String status = infiniteTradingEnabled ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.info("💰 [INFINITE_TRADE] Infinite trading {}", status);
            }

            if (!infiniteTradingEnabled) {
                return;
            }

            // Process all worlds
            for (ServerWorld world : server.getWorlds()) {
                processVillagersInWorld(world);
            }

        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[INFINITE_TRADE] Error during tick", e);
        }
    }

    /**
     * Process all villagers in a world to unlock trades
     */
    private static void processVillagersInWorld(ServerWorld world) {
        try {
            var villagers = world.getEntitiesByClass(
                VillagerEntity.class,
                null,
                villager -> villager.isAlive() && !villager.isRemoved()
            );

            for (VillagerEntity villager : villagers) {
                unlockVillagerTrades(villager);
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[INFINITE_TRADE] Error processing villagers", e);
        }
    }

    /**
     * Unlock all trades for a villager
     */
    private static void unlockVillagerTrades(VillagerEntity villager) {
        try {
            // Access the villager's trading offers
            var tradeOffers = villager.getOffers();
            if (tradeOffers == null) {
                return;
            }

            // Iterate through all offers and unlock them
            for (var offer : tradeOffers) {
                // Set uses to 0 to unlock the trade
                offer.resetUses();
            }

            if (PrimalCraft.LOGGER.isDebugEnabled()) {
                PrimalCraft.LOGGER.trace(
                    "💰 [INFINITE_TRADE] Unlocked trades for villager"
                );
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[INFINITE_TRADE] Failed to unlock villager trades", e);
        }
    }

    /**
     * Check if infinite trading is enabled
     */
    public static boolean isInfiniteTradingEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().features.infiniteTrading;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set infinite trading enabled state
     */
    public static void setInfiniteTradingEnabled(boolean enabled) {
        try {
            lastTradingState = enabled;

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("💰 [INFINITE_TRADE] Infinite trading {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[INFINITE_TRADE] Failed to toggle infinite trading", e);
        }
    }
}
