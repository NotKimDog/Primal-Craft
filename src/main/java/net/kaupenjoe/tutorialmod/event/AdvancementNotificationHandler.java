package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.network.AdvancementNotificationPayload;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class AdvancementNotificationHandler {
    private static final Map<UUID, Set<String>> playerAdvancements = new HashMap<>();
    private static MinecraftServer currentServer;

    private AdvancementNotificationHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            currentServer = server;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                checkAndNotifyAdvancements(player, server);
            }
        });
    }

    private static void checkAndNotifyAdvancements(ServerPlayerEntity player, MinecraftServer server) {
        UUID playerId = player.getUuid();
        Set<String> playerSeenAdvancements = playerAdvancements.getOrDefault(playerId, new HashSet<>());

        // Check all advancements for this player
        for (AdvancementEntry advancementEntry : server.getAdvancementLoader().getAdvancements()) {
            if (advancementEntry == null || advancementEntry.value().display().isEmpty()) continue;

            String advancementId = advancementEntry.id().toString();

            // Check if player has earned this advancement and we haven't notified them yet
            if (player.getAdvancementTracker().getProgress(advancementEntry).isDone() &&
                !playerSeenAdvancements.contains(advancementId)) {

                // Mark as notified
                playerSeenAdvancements.add(advancementId);

                // Send notification
                var display = advancementEntry.value().display().get();
                String title = display.getTitle().getString();
                String description = display.getDescription().getString();

                // Get the advancement icon
                var icon = display.getIcon();

                // Send to client
                ServerPlayNetworking.send(player, new AdvancementNotificationPayload(
                    title,
                    description,
                    advancementId,
                    icon
                ));

                // Play advancement sound
                player.playSound(net.minecraft.sound.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
            }
        }

        playerAdvancements.put(playerId, playerSeenAdvancements);
    }
}
