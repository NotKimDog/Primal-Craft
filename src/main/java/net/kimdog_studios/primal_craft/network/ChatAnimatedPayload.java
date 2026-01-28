package net.kimdog_studios.primal_craft.network;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChatAnimatedPayload(String role, String name, String message) implements CustomPayload {
    public static final CustomPayload.Id<ChatAnimatedPayload> ID = new CustomPayload.Id<>(Identifier.of(PrimalCraft.MOD_ID, "chat_animated"));
    public static final PacketCodec<RegistryByteBuf, ChatAnimatedPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ChatAnimatedPayload::role,
            PacketCodecs.STRING, ChatAnimatedPayload::name,
            PacketCodecs.STRING, ChatAnimatedPayload::message,
            ChatAnimatedPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
