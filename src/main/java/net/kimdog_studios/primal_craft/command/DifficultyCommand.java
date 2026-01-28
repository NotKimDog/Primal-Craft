package net.kimdog_studios.primal_craft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.kimdog_studios.primal_craft.util.DifficultyPreset;
import net.kimdog_studios.primal_craft.util.DifficultyProfile;
import net.kimdog_studios.primal_craft.util.DifficultySystem;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * 🎮 Primal Craft - Difficulty Command
 *
 * Command handler for difficulty system management.
 * Allows admins to view and modify difficulty settings.
 *
 * Commands:
 * - /difficulty set <preset|custom> [player] - Set difficulty for a player
 * - /difficulty profile [player] - View difficulty profile
 * - /difficulty metrics [player] - View progression metrics
 * - /difficulty reload - Reload difficulty configuration
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class DifficultyCommand {
    private DifficultyCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("difficulty")
                // Allow player access (admins can add permission checks to their server)

                // /difficulty set <preset> [player]
                .then(CommandManager.literal("set")
                    .then(CommandManager.argument("preset", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (DifficultyPreset preset : DifficultyPreset.values()) {
                                builder.suggest(preset.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> setDifficultyPreset(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "preset"),
                            ctx.getSource().getPlayer()
                        ))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> setDifficultyPreset(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "preset"),
                                EntityArgumentType.getPlayer(ctx, "player")
                            ))
                        )
                    )
                )

                // /difficulty profile [player]
                .then(CommandManager.literal("profile")
                    .executes(ctx -> showProfile(
                        ctx.getSource(),
                        ctx.getSource().getPlayer()
                    ))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> showProfile(
                            ctx.getSource(),
                            EntityArgumentType.getPlayer(ctx, "player")
                        ))
                    )
                )

                // /difficulty metrics [player]
                .then(CommandManager.literal("metrics")
                    .executes(ctx -> showMetrics(
                        ctx.getSource(),
                        ctx.getSource().getPlayer()
                    ))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> showMetrics(
                            ctx.getSource(),
                            EntityArgumentType.getPlayer(ctx, "player")
                        ))
                    )
                )

                // /difficulty scaling [enable|disable] [player]
                .then(CommandManager.literal("scaling")
                    .then(CommandManager.literal("enable")
                        .executes(ctx -> setDynamicScaling(
                            ctx.getSource(),
                            true,
                            ctx.getSource().getPlayer()
                        ))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> setDynamicScaling(
                                ctx.getSource(),
                                true,
                                EntityArgumentType.getPlayer(ctx, "player")
                            ))
                        )
                    )
                    .then(CommandManager.literal("disable")
                        .executes(ctx -> setDynamicScaling(
                            ctx.getSource(),
                            false,
                            ctx.getSource().getPlayer()
                        ))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> setDynamicScaling(
                                ctx.getSource(),
                                false,
                                EntityArgumentType.getPlayer(ctx, "player")
                            ))
                        )
                    )
                )

                // /difficulty help
                .then(CommandManager.literal("help")
                    .executes(ctx -> showHelp(ctx.getSource()))
                )
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // COMMAND HANDLERS
    // ═══════════════════════════════════════════════════════════════════════════════

    private static int setDifficultyPreset(ServerCommandSource source, String presetName, ServerPlayerEntity player) {
        try {
            DifficultyPreset preset = DifficultyPreset.fromString(presetName);
            DifficultySystem.setDifficultyPreset(player, preset);
            DifficultyProfile profile = DifficultySystem.getProfile(player);
            String playerName = profile != null ? profile.getPlayerName() : player.getUuid().toString();

            source.sendFeedback(
                () -> Text.literal(String.format(
                    "§6✓ Set difficulty for §e%s §6to §e%s",
                    playerName,
                    preset.getDisplayName()
                )),
                true // Show to ops
            );

            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("§c✗ Failed to set difficulty: " + e.getMessage()));
            return 0;
        }
    }

    private static int showProfile(ServerCommandSource source, ServerPlayerEntity player) {
        DifficultyProfile profile = DifficultySystem.getProfile(player);
        if (profile == null) {
            source.sendFeedback(
                () -> Text.literal("§cNo difficulty profile found for " + player.getUuid()),
                false
            );
            return 0;
        }

        source.sendFeedback(
            () -> Text.literal(String.format(
                "§e════════════════════════════════════════%n" +
                "§6Player: §e%s%n" +
                "§6Difficulty: §e%s §7(Level: %d)%n" +
                "§6Multipliers:%n" +
                "  §7├ Stamina: §e%.2fx%n" +
                "  §7├ Thirst: §e%.2fx%n" +
                "  §7├ Temperature: §e%.2fx%n" +
                "  §7├ Hazards: §e%.2fx%n" +
                "  §7├ Damage: §e%.2fx%n" +
                "  §7└ Mobs: §e%.2fx%n" +
                "§6Dynamic Scaling: §e%s%n" +
                "§e════════════════════════════════════════",
                profile.getPlayerName(),
                profile.getPreset().getDisplayName(),
                profile.getScalingLevel(),
                profile.getStaminaMultiplier(),
                profile.getThirstMultiplier(),
                profile.getTemperatureMultiplier(),
                profile.getHazardMultiplier(),
                profile.getDamageMultiplier(),
                profile.getMobMultiplier(),
                profile.isDynamicScalingEnabled() ? "§aEnabled" : "§cDisabled"
            )),
            false
        );

        return 1;
    }

    private static int showMetrics(ServerCommandSource source, ServerPlayerEntity player) {
        DifficultyProfile profile = DifficultySystem.getProfile(player);
        if (profile == null) {
            source.sendFeedback(
                () -> Text.literal("§cNo difficulty profile found for " + player.getUuid()),
                false
            );
            return 0;
        }

        source.sendFeedback(
            () -> Text.literal(String.format(
                "§e════════════════════════════════════════%n" +
                "§6Player: §e%s%n" +
                "§6Progression Metrics:%n" +
                "  §7├ Playtime: §e%.1f hours%n" +
                "  §7├ Total Damage: §e%.1f%n" +
                "  §7├ Resources Gathered: §e%d%n" +
                "  §7├ Deaths: §e%d%n" +
                "  §7└ Progression Score: §e%.1f%n" +
                "§6Dynamic Scaling Level: §e%d%n" +
                "§e════════════════════════════════════════",
                profile.getPlayerName(),
                profile.getPlaytimeHours(),
                profile.getTotalDamageTaken(),
                profile.getTotalResourcesGathered(),
                profile.getDeathCount(),
                profile.calculateProgressionScore(),
                profile.getScalingLevel()
            )),
            false
        );

        return 1;
    }

    private static int setDynamicScaling(ServerCommandSource source, boolean enabled, ServerPlayerEntity player) {
        DifficultyProfile profile = DifficultySystem.getOrCreateProfile(player);
        profile.setDynamicScalingEnabled(enabled);

        source.sendFeedback(
            () -> Text.literal(String.format(
                "§6✓ Dynamic scaling for §e%s §6set to §e%s",
                profile.getPlayerName(),
                enabled ? "§aEnabled" : "§cDisabled"
            )),
            true
        );

        return 1;
    }

    private static int showHelp(ServerCommandSource source) {
        source.sendFeedback(
            () -> Text.literal(
                "§e════════════════════════════════════════%n" +
                "§6Difficulty Command Help%n" +
                "§e════════════════════════════════════════%n" +
                "§7/difficulty set <preset> [player]%n" +
                "  §6Set difficulty to a preset (Easy, Normal, Hard, Hardcore)%n%n" +
                "§7/difficulty profile [player]%n" +
                "  §6Show difficulty profile and multipliers%n%n" +
                "§7/difficulty metrics [player]%n" +
                "  §6Show progression metrics and scaling level%n%n" +
                "§7/difficulty scaling <enable|disable> [player]%n" +
                "  §6Enable or disable dynamic difficulty scaling%n%n" +
                "§7/difficulty help%n" +
                "  §6Show this help message%n" +
                "§e════════════════════════════════════════"
            ),
            false
        );

        return 1;
    }
}
