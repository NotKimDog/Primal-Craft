package net.kimdog_studios.primal_craft.network;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TemperatureSyncPayload(double temperature) implements CustomPayload {
    public static final Id<TemperatureSyncPayload> ID = new Id<>(Identifier.of(PrimalCraft.MOD_ID, "temperature_sync"));
    public static final PacketCodec<RegistryByteBuf, TemperatureSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, TemperatureSyncPayload::temperature,
            TemperatureSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
