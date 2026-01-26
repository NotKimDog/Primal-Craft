package net.kaupenjoe.tutorialmod.network;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ThirstSyncPayload(double thirst, double maxThirst) implements CustomPayload {
    public static final CustomPayload.Id<ThirstSyncPayload> ID = new CustomPayload.Id<>(Identifier.of(TutorialMod.MOD_ID, "thirst_sync"));
    public static final PacketCodec<RegistryByteBuf, ThirstSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, ThirstSyncPayload::thirst,
            PacketCodecs.DOUBLE, ThirstSyncPayload::maxThirst,
            ThirstSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
