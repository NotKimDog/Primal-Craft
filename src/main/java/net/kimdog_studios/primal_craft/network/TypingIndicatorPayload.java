package net.kimdog_studios.primal_craft.network;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TypingIndicatorPayload(String playerName, boolean isTyping, String partialText) implements CustomPayload {
    public static final CustomPayload.Id<TypingIndicatorPayload> ID = new CustomPayload.Id<>(Identifier.of(PrimalCraft.MOD_ID, "typing_indicator"));
    public static final PacketCodec<RegistryByteBuf, TypingIndicatorPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, TypingIndicatorPayload::playerName,
            PacketCodecs.BYTE.xmap(b -> b != 0, b -> b ? (byte) 1 : (byte) 0), TypingIndicatorPayload::isTyping,
            PacketCodecs.STRING, TypingIndicatorPayload::partialText,
            TypingIndicatorPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}


