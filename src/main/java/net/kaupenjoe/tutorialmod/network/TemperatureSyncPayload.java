package net.kaupenjoe.tutorialmod.network;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TemperatureSyncPayload(double temperature) implements CustomPayload {
    public static final Id<TemperatureSyncPayload> ID = new Id<>(Identifier.of(TutorialMod.MOD_ID, "temperature_sync"));
    public static final PacketCodec<RegistryByteBuf, TemperatureSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, TemperatureSyncPayload::temperature,
            TemperatureSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
