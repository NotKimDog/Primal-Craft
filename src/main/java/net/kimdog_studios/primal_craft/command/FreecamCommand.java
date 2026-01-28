package net.kimdog_studios.primal_craft.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FreecamCommand {
    private static final Map<UUID, Session> sessions = new HashMap<>();
    private static final int HARD_DURATION_SECONDS = 20; // hard-coded freecam duration

    private FreecamCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("freecam")
                .executes(ctx -> start(ctx.getSource()))
        );

        // Tick handler to countdown and auto-restore
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var entry : sessions.entrySet()) {
                UUID id = entry.getKey();
                Session s = entry.getValue();
                if (s == null) continue;
                s.ticksLeft--;
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(id);
                if (player != null && (s.ticksLeft % 20) == 0 && s.ticksLeft > 0) {
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,
                        new net.kimdog_studios.primal_craft.network.FreecamCountdownPayload(s.ticksLeft, HARD_DURATION_SECONDS * 20));
                }
                if (s.ticksLeft <= 0) {
                    if (player != null) {
                        restore(player, s);
                        player.sendMessage(Text.literal("§7[Freecam] §eTime up. Restoring."));
                    }
                }
            }
            sessions.values().removeIf(sess -> sess != null && sess.ticksLeft <= 0);
        });
    }

    private static int start(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("§cYou must be a player to use /freecam."));
            return 0;
        }
        UUID id = player.getUuid();
        if (sessions.containsKey(id)) {
            source.sendError(Text.literal("§cFreecam already active."));
            return 0;
        }
        // Save current state
        Session s = new Session();
        s.originalMode = player.interactionManager.getGameMode();
        s.originalPos = new Vec3d(player.getX(), player.getY(), player.getZ());
        s.originalYaw = player.getYaw();
        s.originalPitch = player.getPitch();
        s.ticksLeft = HARD_DURATION_SECONDS * 20; // hard-coded duration
        sessions.put(id, s);

        // Switch to spectator
        player.changeGameMode(GameMode.SPECTATOR);
        source.sendFeedback(() -> Text.literal("§7[Freecam] §aEnabled for " + HARD_DURATION_SECONDS + "s"), true);
        return 1;
    }

    private static void restore(ServerPlayerEntity player, Session s) {
        // Restore gamemode and position
        player.changeGameMode(s.originalMode);
        // Use server-side teleport with yaw/pitch via network handler (dimension unchanged)
        player.networkHandler.requestTeleport(s.originalPos.x, s.originalPos.y, s.originalPos.z, s.originalYaw, s.originalPitch);
    }

    private static class Session {
        GameMode originalMode;
        Vec3d originalPos;
        float originalYaw;
        float originalPitch;
        int ticksLeft;
    }
}