package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.TutorialMod;
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
    private static int swingAttacksTriggered = 0;

    public static void registerClient() {
        TutorialMod.LOGGER.info("⚔️  [SWING_ATTACK] Registering client-side swing attack handler");

        // Register swing attack on client tick
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.handSwinging && client.player.handSwingTicks == 0) {
                TutorialMod.LOGGER.debug("👊 [SWING_ATTACK] Client swing detected, sending to server");
                TutorialMod.LOGGER.trace("   ├─ Player: {}", client.player.getName().getString());
                TutorialMod.LOGGER.trace("   ├─ Position: X={}, Y={}, Z={}",
                    Math.round(client.player.getX()), Math.round(client.player.getY()), Math.round(client.player.getZ()));
                TutorialMod.LOGGER.trace("   └─ Payload sent");

                // Send swing attack to server
                ClientPlayNetworking.send(new SwingAttackPayload());
            }
        });

        TutorialMod.LOGGER.info("✅ [SWING_ATTACK] Client-side handler registered");
    }

    public static void registerServer() {
        TutorialMod.LOGGER.info("⚔️  [SWING_ATTACK] Registering server-side swing attack handler");

        // Server-side handler for swing attacks
        ServerPlayNetworking.registerGlobalReceiver(
            SwingAttackPayload.ID,
            (payload, context) -> {
                ServerPlayerEntity player = context.player();
                if (player != null) {
                    swingAttacksTriggered++;

                    TutorialMod.LOGGER.debug("👊 [SWING_ATTACK] Event #{} - {} air swing received",
                        swingAttacksTriggered, player.getName().getString());

                    double weightPenalty = ItemWeightSystem.calculateInventoryWeightPenalty(player);
                    double swingCost = COST_AIR_SWING * (1.0 + weightPenalty * 0.5);

                    TutorialMod.LOGGER.trace("   ├─ Weight penalty: {}", String.format("%.2f%%", weightPenalty * 100));
                    TutorialMod.LOGGER.trace("   ├─ Swing cost: {}", String.format("%.2f", swingCost));
                    TutorialMod.LOGGER.trace("   ├─ Current stamina: {}", String.format("%.1f", StaminaSystem.get(player)));

                    if (!StaminaSystem.tryConsume(player, swingCost)) {
                        TutorialMod.LOGGER.debug("   └─ ✗ Insufficient stamina, swing blocked");
                    } else {
                        TutorialMod.LOGGER.trace("   └─ ✓ Swing approved");
                    }
                }
            }
        );

        TutorialMod.LOGGER.info("✅ [SWING_ATTACK] Server-side handler registered");
    }
}


