package net.kimdog_studios.primal_craft.network;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StaminaSyncPayload(double stamina, double maxStamina) implements CustomPayload {
    public static final Id<StaminaSyncPayload> ID = new Id<>(Identifier.of(PrimalCraft.MOD_ID, "stamina_sync"));
    public static final PacketCodec<RegistryByteBuf, StaminaSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, StaminaSyncPayload::stamina,
            PacketCodecs.DOUBLE, StaminaSyncPayload::maxStamina,
            StaminaSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
