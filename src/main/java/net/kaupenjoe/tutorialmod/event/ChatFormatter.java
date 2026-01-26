package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatFormatter {
    private static final Map<UUID, String> roles = new HashMap<>();
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_]+)");

    private ChatFormatter() {}

    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            formatAndBroadcast(sender, message);
            return false;
        });
    }

    private static void formatAndBroadcast(ServerPlayerEntity sender, SignedMessage signed) {
        String role = roles.getOrDefault(sender.getUuid(), "MEMBER");
        String rawMessage = signed.getContent().getString();
        var server = sender.getEntityWorld().getServer();

        // Handle chat commands
        if (rawMessage.startsWith("/")) {
            handleChatCommand(sender, rawMessage, server);
            return;
        }


        // Get timestamp
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timestamp = now.format(formatter);

        // Get player status
        String statusEmoji = getPlayerStatus(sender);

        // Process message formatting (**bold**, __italic__, ~~strike~~)
        String formattedMessage = processMessageFormatting(rawMessage);

        // Handle mentions
        String finalMessage = formattedMessage;
        Matcher mentionMatcher = MENTION_PATTERN.matcher(finalMessage);
        StringBuffer sb = new StringBuffer();
        List<ServerPlayerEntity> mentionedPlayers = new ArrayList<>();

        while (mentionMatcher.find()) {
            String mentionedName = mentionMatcher.group(1);
            ServerPlayerEntity mentionedPlayer = server.getPlayerManager().getPlayer(mentionedName);
            if (mentionedPlayer != null) {
                mentionedPlayers.add(mentionedPlayer);
                mentionMatcher.appendReplacement(sb, "§b@" + mentionedName + "§r");
            }
        }
        mentionMatcher.appendTail(sb);
        finalMessage = sb.toString();

        // Get player coordinates
        String coords = String.format("§7[%d, %d, %d]§r",
            (int)sender.getX(), (int)sender.getY(), (int)sender.getZ());

        // Build final message with all context - timestamp at the very front
        String messageToSend = "§8[" + timestamp + "]§r " + statusEmoji + " " + finalMessage;

        // Send to all players
        server.getPlayerManager().getPlayerList().forEach(p ->
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p,
                new net.kaupenjoe.tutorialmod.network.ChatAnimatedPayload(role, sender.getName().getString(), messageToSend))
        );

        // Play sounds
        sender.playSound(net.kaupenjoe.tutorialmod.sound.ModSounds.CHAT_SEND, 0.7f, 1.0f);
        server.getPlayerManager().getPlayerList().forEach(p -> {
            if (p != sender) {
                p.playSound(net.kaupenjoe.tutorialmod.sound.ModSounds.CHAT_RECEIVE, 0.5f, 1.0f);
            }
        });

        // Play special sound for mentioned players
        for (ServerPlayerEntity mentionedPlayer : mentionedPlayers) {
            mentionedPlayer.playSound(net.minecraft.sound.SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.8f, 1.2f);
        }

        net.kaupenjoe.tutorialmod.event.TypingIndicatorHandler.stopTyping(sender);
    }

    private static void handleChatCommand(ServerPlayerEntity sender, String command, net.minecraft.server.MinecraftServer server) {
        if (command.startsWith("/me ")) {
            String action = command.substring(4);
            String message = "§6* " + sender.getName().getString() + " " + action + "§r";
            server.getPlayerManager().getPlayerList().forEach(p ->
                p.sendMessage(Text.literal(message), false)
            );
        } else if (command.startsWith("/whisper ") || command.startsWith("/w ")) {
            String[] parts = command.split(" ", 3);
            if (parts.length < 3) {
                sender.sendMessage(Text.literal("§cUsage: /whisper <player> <message>"), false);
                return;
            }
            String targetName = parts[1];
            String message = parts[2];
            ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetName);
            if (target != null) {
                String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String dmMessage = "§5[WHISPER] [" + timestamp + "] " + sender.getName().getString() + "§5: " + message;
                target.sendMessage(Text.literal(dmMessage), false);
                sender.sendMessage(Text.literal(dmMessage), false);
                target.playSound(net.minecraft.sound.SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            } else {
                sender.sendMessage(Text.literal("§cPlayer " + targetName + " not found!"), false);
            }
        }
    }


    private static String getPlayerStatus(ServerPlayerEntity player) {
        if (player.isCreative()) {
            return "§6✦§r"; // Creative mode icon
        } else if (player.isInvisible()) {
            return "§7◇§r"; // Invisible icon
        } else {
            return "§a●§r"; // Online icon
        }
    }

    private static String processMessageFormatting(String message) {
        // Bold: **text** -> §ltext§r
        message = message.replaceAll("\\*\\*([^*]+)\\*\\*", "§l$1§r");

        // Italic: __text__ -> §otext§r
        message = message.replaceAll("__([^_]+)__", "§o$1§r");

        // Strikethrough: ~~text~~ -> §mtext§r
        message = message.replaceAll("~~([^~]+)~~", "§m$1§r");

        return message;
    }

    public static void setRole(ServerPlayerEntity player, String role) {
        roles.put(player.getUuid(), role.toUpperCase());
    }
}
