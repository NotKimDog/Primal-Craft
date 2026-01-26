package net.kaupenjoe.tutorialmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WeatherNotificationPayload(String message, int color) implements CustomPayload {
    public static final CustomPayload.Id<WeatherNotificationPayload> ID =
            new CustomPayload.Id<>(Identifier.of("tutorialmod", "weather_notification"));

    public static final PacketCodec<RegistryByteBuf, WeatherNotificationPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, WeatherNotificationPayload::message,
                    PacketCodecs.INTEGER, WeatherNotificationPayload::color,
                    WeatherNotificationPayload::new
            );

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
