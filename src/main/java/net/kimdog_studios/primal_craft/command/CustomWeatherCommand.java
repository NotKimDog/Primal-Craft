package net.kimdog_studios.primal_craft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kimdog_studios.primal_craft.util.WindSystem;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * Custom weather command for setting advanced weather states
 * Usage: /customweather <type>
 * Types: clear, windy, rain, thunderstorm, blizzard, heatwave, dust_storm, foggy
 */
public class CustomWeatherCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("customweather")
                .then(CommandManager.argument("type", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        builder.suggest("clear");
                        builder.suggest("windy");
                        builder.suggest("rain");
                        builder.suggest("thunderstorm");
                        builder.suggest("blizzard");
                        builder.suggest("heatwave");
                        builder.suggest("dust_storm");
                        builder.suggest("foggy");
                        return builder.buildFuture();
                    })
                    .executes(CustomWeatherCommand::executeSetWeather)
                )
        );
    }

    private static int executeSetWeather(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String weatherType = StringArgumentType.getString(context, "type");
        ServerWorld world = source.getWorld();

        try {
            WindSystem.CustomWeatherType customWeather = parseWeatherType(weatherType);
            WindSystem.setCustomWeather(world, customWeather);

            source.sendFeedback(
                () -> Text.literal("§6[Weather] §fSet weather to: §e" + customWeather.name()),
                true
            );

            return 1;
        } catch (IllegalArgumentException e) {
            source.sendError(Text.literal("§c[Weather] Unknown weather type: " + weatherType));
            source.sendError(Text.literal("§cAvailable: clear, windy, rain, thunderstorm, blizzard, heatwave, dust_storm, foggy"));
            return 0;
        }
    }

    private static WindSystem.CustomWeatherType parseWeatherType(String input) {
        return switch (input.toLowerCase()) {
            case "clear" -> WindSystem.CustomWeatherType.CLEAR;
            case "windy" -> WindSystem.CustomWeatherType.WINDY;
            case "rain" -> WindSystem.CustomWeatherType.RAIN;
            case "thunderstorm", "thunder" -> WindSystem.CustomWeatherType.THUNDERSTORM;
            case "blizzard", "snow" -> WindSystem.CustomWeatherType.BLIZZARD;
            case "heatwave", "heat" -> WindSystem.CustomWeatherType.HEATWAVE;
            case "dust_storm", "duststorm", "dust" -> WindSystem.CustomWeatherType.DUST_STORM;
            case "foggy", "fog" -> WindSystem.CustomWeatherType.FOGGY;
            default -> throw new IllegalArgumentException("Unknown weather type: " + input);
        };
    }
}
