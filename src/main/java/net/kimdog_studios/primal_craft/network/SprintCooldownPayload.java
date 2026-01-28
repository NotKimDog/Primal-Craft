package net.kimdog_studios.primal_craft.network;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SprintCooldownPayload(int cooldownTicks) implements CustomPayload {
    public static final Id<SprintCooldownPayload> ID = new Id<>(Identifier.of(PrimalCraft.MOD_ID, "sprint_cooldown"));
    public static final PacketCodec<RegistryByteBuf, SprintCooldownPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SprintCooldownPayload::cooldownTicks,
            SprintCooldownPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
