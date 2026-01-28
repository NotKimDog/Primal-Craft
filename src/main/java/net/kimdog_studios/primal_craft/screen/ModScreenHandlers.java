package net.kimdog_studios.primal_craft.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.screen.custom.GrowthChamberScreenHandler;
import net.kimdog_studios.primal_craft.screen.custom.PedestalScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModScreenHandlers {
    public static final ScreenHandlerType<PedestalScreenHandler> PEDESTAL_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(PrimalCraft.MOD_ID, "pedestal_screen_handler"),
                    new ExtendedScreenHandlerType<>(PedestalScreenHandler::new, BlockPos.PACKET_CODEC));

    public static final ScreenHandlerType<GrowthChamberScreenHandler> GROWTH_CHAMBER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(PrimalCraft.MOD_ID, "growth_chamber_screen_handler"),
                    new ExtendedScreenHandlerType<>(GrowthChamberScreenHandler::new, BlockPos.PACKET_CODEC));


    public static void registerScreenHandlers() {
        PrimalCraft.LOGGER.info("Registering Screen Handlers for " + PrimalCraft.MOD_ID);
    }
}
