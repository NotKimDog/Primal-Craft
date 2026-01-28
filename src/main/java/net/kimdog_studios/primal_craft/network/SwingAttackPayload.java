package net.kimdog_studios.primal_craft.network;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SwingAttackPayload() implements CustomPayload {
    public static final Id<SwingAttackPayload> ID = new Id<>(Identifier.of(PrimalCraft.MOD_ID, "swing_attack"));
    public static final PacketCodec<RegistryByteBuf, SwingAttackPayload> CODEC = PacketCodec.unit(new SwingAttackPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
