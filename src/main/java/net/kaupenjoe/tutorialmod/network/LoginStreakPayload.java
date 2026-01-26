package net.kaupenjoe.tutorialmod.network;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LoginStreakPayload(int streak, long lastDay, boolean increased, boolean broken, int previous) implements CustomPayload {
    public static final Id<LoginStreakPayload> ID = new Id<>(Identifier.of(TutorialMod.MOD_ID, "login_streak"));
    public static final PacketCodec<RegistryByteBuf, LoginStreakPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, LoginStreakPayload::streak,
            PacketCodecs.LONG, LoginStreakPayload::lastDay,
            PacketCodecs.BOOLEAN, LoginStreakPayload::increased,
            PacketCodecs.BOOLEAN, LoginStreakPayload::broken,
            PacketCodecs.INTEGER, LoginStreakPayload::previous,
            LoginStreakPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
