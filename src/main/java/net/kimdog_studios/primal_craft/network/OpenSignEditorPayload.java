package net.kimdog_studios.primal_craft.network;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record OpenSignEditorPayload(BlockPos pos) implements CustomPayload {
    public static final CustomPayload.Id<OpenSignEditorPayload> ID = new CustomPayload.Id<>(Identifier.of(PrimalCraft.MOD_ID, "open_sign_editor"));
    public static final PacketCodec<RegistryByteBuf, OpenSignEditorPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBlockPos(value.pos()),
            (buf) -> new OpenSignEditorPayload(buf.readBlockPos())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
