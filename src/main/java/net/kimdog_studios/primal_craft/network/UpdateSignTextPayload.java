package net.kimdog_studios.primal_craft.network;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record UpdateSignTextPayload(BlockPos pos, int line, String text, String formatting) implements CustomPayload {
    public static final CustomPayload.Id<UpdateSignTextPayload> ID = new CustomPayload.Id<>(Identifier.of(PrimalCraft.MOD_ID, "update_sign_text"));
    public static final PacketCodec<RegistryByteBuf, UpdateSignTextPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeBlockPos(value.pos());
                buf.writeInt(value.line());
                buf.writeString(value.text());
                buf.writeString(value.formatting());
            },
            (buf) -> new UpdateSignTextPayload(
                buf.readBlockPos(),
                buf.readInt(),
                buf.readString(),
                buf.readString()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
