package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kaupenjoe.tutorialmod.api.TemperatureAPI;
import net.minecraft.server.network.ServerPlayerEntity;

public final class InventoryTemperatureHandler {
    private InventoryTemperatureHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // Update tracking
                net.kaupenjoe.tutorialmod.util.InventoryTemperatureSystem.tick(player);

                // Compute heat modifiers and apply externally
                double extraHeat = TemperatureAPI.getInventoryHeatModifier(player) + TemperatureAPI.getArmorHeatModifier(player);
                if (Math.abs(extraHeat) > 0.01) {
                    TemperatureAPI.applyExternalHeat(player, extraHeat);
                }
            }
        });
    }
}
