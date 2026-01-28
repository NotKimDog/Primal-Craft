package net.kimdog_studios.primal_craft.network;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StaminaUpdatePayload(double stamina, double maxStamina, double fatigue, float regenRate) implements CustomPayload {
    public static final Id<StaminaUpdatePayload> ID = new Id<>(Identifier.of(PrimalCraft.MOD_ID, "stamina_update"));
    public static final PacketCodec<RegistryByteBuf, StaminaUpdatePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, StaminaUpdatePayload::stamina,
            PacketCodecs.DOUBLE, StaminaUpdatePayload::maxStamina,
            PacketCodecs.DOUBLE, StaminaUpdatePayload::fatigue,
            PacketCodecs.FLOAT, StaminaUpdatePayload::regenRate,
            StaminaUpdatePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
