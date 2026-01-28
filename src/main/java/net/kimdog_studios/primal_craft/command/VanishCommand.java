package net.kimdog_studios.primal_craft.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class VanishCommand {
    private VanishCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("vanish")
                .executes(ctx -> enable(ctx.getSource()))
        );
    }

    private static int enable(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("§cYou must be a player to use /vanish."));
            return 0;
        }
        player.setInvisible(true);
        player.setGlowing(false);
        source.sendFeedback(() -> Text.literal("§7[Vanish] §aEnabled (hard-coded)"), true);
        return 1;
    }
}
