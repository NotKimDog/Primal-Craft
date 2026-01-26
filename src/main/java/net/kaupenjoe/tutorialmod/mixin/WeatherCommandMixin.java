package net.kaupenjoe.tutorialmod.mixin;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.WeatherCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeatherCommand.class)
public class WeatherCommandMixin {

    @Inject(method = "executeClear", at = @At("HEAD"))
    private static void onWeatherClear(ServerCommandSource source, int duration, CallbackInfoReturnable<Integer> cir) {
        net.kaupenjoe.tutorialmod.util.WeatherNotificationSystem.broadcastWeatherCommand(source.getServer(), "Clear");
    }

    @Inject(method = "executeRain", at = @At("HEAD"))
    private static void onWeatherRain(ServerCommandSource source, int duration, CallbackInfoReturnable<Integer> cir) {
        net.kaupenjoe.tutorialmod.util.WeatherNotificationSystem.broadcastWeatherCommand(source.getServer(), "Rain");
    }

    @Inject(method = "executeThunder", at = @At("HEAD"))
    private static void onWeatherThunder(ServerCommandSource source, int duration, CallbackInfoReturnable<Integer> cir) {
        net.kaupenjoe.tutorialmod.util.WeatherNotificationSystem.broadcastWeatherCommand(source.getServer(), "Thunder");
    }
}

