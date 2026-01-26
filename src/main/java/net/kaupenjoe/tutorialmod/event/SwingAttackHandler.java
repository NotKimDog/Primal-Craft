package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.network.SwingAttackPayload;
import net.kaupenjoe.tutorialmod.util.ItemWeightSystem;
import net.kaupenjoe.tutorialmod.util.StaminaSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handles air swing attacks (attacking without hitting entities)
 */
public final class SwingAttackHandler {
    private static final double COST_AIR_SWING = 5.0;

    public static void registerClient() {
        // Register swing attack on client tick
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.handSwinging && client.player.handSwingTicks == 0) {
                // Send swing attack to server
                ClientPlayNetworking.send(new SwingAttackPayload());
            }
        });
    }

    public static void registerServer() {
        // Server-side handler for swing attacks
        ServerPlayNetworking.registerGlobalReceiver(
            SwingAttackPayload.ID,
            (payload, context) -> {
                ServerPlayerEntity player = context.player();
                if (player != null) {
                    double weightPenalty = ItemWeightSystem.calculateInventoryWeightPenalty(player);
                    double swingCost = COST_AIR_SWING * (1.0 + weightPenalty * 0.5);
                    StaminaSystem.tryConsume(player, swingCost);
                }
            }
        );
    }
}


