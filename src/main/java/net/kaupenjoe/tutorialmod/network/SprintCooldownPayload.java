package net.kaupenjoe.tutorialmod.network;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SprintCooldownPayload(int cooldownTicks) implements CustomPayload {
    public static final Id<SprintCooldownPayload> ID = new Id<>(Identifier.of(TutorialMod.MOD_ID, "sprint_cooldown"));
    public static final PacketCodec<RegistryByteBuf, SprintCooldownPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SprintCooldownPayload::cooldownTicks,
            SprintCooldownPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
