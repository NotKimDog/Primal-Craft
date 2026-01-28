package net.kimdog_studios.primal_craft.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WindSyncPayload(double dirX, double dirY, double dirZ, double strength, boolean stormy) implements CustomPayload {
    public static final CustomPayload.Id<WindSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of("primal-craft", "wind_sync"));

    public static final PacketCodec<RegistryByteBuf, WindSyncPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.DOUBLE, WindSyncPayload::dirX,
                    PacketCodecs.DOUBLE, WindSyncPayload::dirY,
                    PacketCodecs.DOUBLE, WindSyncPayload::dirZ,
                    PacketCodecs.DOUBLE, WindSyncPayload::strength,
                    PacketCodecs.BOOLEAN, WindSyncPayload::stormy,
                    WindSyncPayload::new
            );

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
