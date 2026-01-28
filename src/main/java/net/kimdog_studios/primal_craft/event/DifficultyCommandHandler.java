package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kimdog_studios.primal_craft.command.DifficultyCommand;
import net.minecraft.server.MinecraftServer;

/**
 * 🎮 Primal Craft - Difficulty Command Registration Handler
 *
 * Registers difficulty commands when the server starts up.
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class DifficultyCommandHandler {
    private DifficultyCommandHandler() {}

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(DifficultyCommandHandler::onServerStart);
    }

    private static void onServerStart(MinecraftServer server) {
        DifficultyCommand.register(server.getCommandManager().getDispatcher());
    }
}
