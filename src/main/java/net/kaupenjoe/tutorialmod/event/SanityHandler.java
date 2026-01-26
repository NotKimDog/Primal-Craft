package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Sanity system - players lose sanity from darkness and danger
 * Low sanity causes slowness, weakness, and nausea
 */
public class SanityHandler {
    private static final java.util.Map<ServerPlayerEntity, Integer> playerSanity = new java.util.WeakHashMap<>();
    private static final int MAX_SANITY = 100;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                tickSanity(player);
            });
        });
    }

    private static void tickSanity(ServerPlayerEntity player) {
        int sanity = playerSanity.getOrDefault(player, MAX_SANITY);

        // Lose sanity in darkness
        int lightLevel = ((ServerWorld) player.getEntityWorld()).getLightLevel(player.getBlockPos());
        if(lightLevel < 5) sanity -= 2;

        // Lose sanity when low on health
        if(player.getHealth() < 5) sanity -= 1;

        // Lose sanity when hungry
        if(player.getHungerManager().getFoodLevel() < 5) sanity -= 1;

        // Count nearby hostile mobs within 32 blocks
        long hostileMobs = ((ServerWorld) player.getEntityWorld()).getOtherEntities(player, player.getBoundingBox().expand(32))
            .stream()
            .filter(e -> e instanceof net.minecraft.entity.mob.HostileEntity)
            .count();
        if(hostileMobs > 0) sanity -= (int)Math.min(5, hostileMobs);

        // Recover sanity in daylight with full health/food
        if(lightLevel > 12 && player.getHealth() > 15 && player.getHungerManager().getFoodLevel() > 15) {
            sanity += 2;
        }

        sanity = Math.min(MAX_SANITY, Math.max(0, sanity));
        playerSanity.put(player, sanity);

        // Apply effects based on sanity level
        if(sanity < 20) {
            if(player.age % 40 == 0) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1, false, false));
                if(Math.random() < 0.4) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 80, 0, false, false));
                }
            }
        } else if(sanity < 50) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 0, false, false));
        }
    }

    public static int getSanity(ServerPlayerEntity player) {
        return playerSanity.getOrDefault(player, MAX_SANITY);
    }
}
