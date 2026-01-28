package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kimdog_studios.primal_craft.api.TemperatureAPI;
import net.minecraft.server.network.ServerPlayerEntity;

public final class InventoryTemperatureHandler {
    private InventoryTemperatureHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // Update tracking
                net.kimdog_studios.primal_craft.util.InventoryTemperatureSystem.tick(player);

                // Compute heat modifiers and apply externally
                double extraHeat = TemperatureAPI.getInventoryHeatModifier(player) + TemperatureAPI.getArmorHeatModifier(player);
                if (Math.abs(extraHeat) > 0.01) {
                    TemperatureAPI.applyExternalHeat(player, extraHeat);
                }
            }
        });
    }
}
