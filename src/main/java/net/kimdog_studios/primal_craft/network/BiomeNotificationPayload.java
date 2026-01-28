package net.kimdog_studios.primal_craft.network;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BiomeNotificationPayload(String message, int color) implements CustomPayload {
    public static final Id<BiomeNotificationPayload> ID = new Id<>(Identifier.of(PrimalCraft.MOD_ID, "biome_notification"));
    public static final PacketCodec<RegistryByteBuf, BiomeNotificationPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, BiomeNotificationPayload::message,
            PacketCodecs.INTEGER, BiomeNotificationPayload::color,
            BiomeNotificationPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
