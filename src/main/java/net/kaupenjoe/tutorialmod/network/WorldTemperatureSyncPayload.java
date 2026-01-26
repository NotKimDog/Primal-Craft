package net.kaupenjoe.tutorialmod.network;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WorldTemperatureSyncPayload(double worldTemperature) implements CustomPayload {
    public static final Id<WorldTemperatureSyncPayload> ID = new Id<>(Identifier.of(TutorialMod.MOD_ID, "world_temperature_sync"));
    public static final PacketCodec<RegistryByteBuf, WorldTemperatureSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, WorldTemperatureSyncPayload::worldTemperature,
            WorldTemperatureSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
