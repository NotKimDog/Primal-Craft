package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.network.TypingIndicatorPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class TypingIndicatorHandler {
    private static final Map<UUID, Long> typingPlayers = new HashMap<>();
    private static final long TYPING_TIMEOUT_MS = 3000; // 3 seconds without input = stop typing

    private TypingIndicatorHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long currentTime = System.currentTimeMillis();
            List<UUID> toRemove = new ArrayList<>();

            // Check for expired typing indicators
            for (Map.Entry<UUID, Long> entry : typingPlayers.entrySet()) {
                if (currentTime - entry.getValue() > TYPING_TIMEOUT_MS) {
                    toRemove.add(entry.getKey());
                }
            }

            // Broadcast stop typing for expired entries
            for (UUID uuid : toRemove) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                if (player != null) {
                    broadcastTypingState(server, player.getName().getString(), false, "");
                }
                typingPlayers.remove(uuid);
            }
        });
    }

    public static void notifyTyping(ServerPlayerEntity player, String partialText) {
        // Update the player's typing timestamp
        typingPlayers.put(player.getUuid(), System.currentTimeMillis());

        // Broadcast to all players that this player is typing
        var server = player.getEntityWorld().getServer();
        if (server != null) {
            broadcastTypingState(server, player.getName().getString(), true, partialText);
        }
    }

    private static void broadcastTypingState(net.minecraft.server.MinecraftServer server, String playerName, boolean isTyping, String partialText) {
        TypingIndicatorPayload payload = new TypingIndicatorPayload(playerName, isTyping, partialText);
        server.getPlayerManager().getPlayerList().forEach(p ->
            ServerPlayNetworking.send(p, payload)
        );
    }

    public static void stopTyping(ServerPlayerEntity player) {
        if (typingPlayers.remove(player.getUuid()) != null) {
            var server = player.getEntityWorld().getServer();
            if (server != null) {
                broadcastTypingState(server, player.getName().getString(), false, "");
            }
        }
    }
}
