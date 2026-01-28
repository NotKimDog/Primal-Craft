package net.kimdog_studios.primal_craft.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.kimdog_studios.primal_craft.client.hud.ThirstHudOverlay;
import net.kimdog_studios.primal_craft.network.ThirstSyncPayload;

public final class ThirstHudClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Receive thirst sync via payload API
        ClientPlayNetworking.registerGlobalReceiver(ThirstSyncPayload.ID, (payload, context) -> {
            double value = payload.thirst();
            double max = payload.maxThirst();
            context.client().execute(() -> ThirstHudOverlay.update(value, max));
        });

        // Render HUD
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> ThirstHudOverlay.render(matrices));
    }
}
