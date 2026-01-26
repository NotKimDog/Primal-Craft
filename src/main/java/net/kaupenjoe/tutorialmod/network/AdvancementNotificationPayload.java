package net.kaupenjoe.tutorialmod.network;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AdvancementNotificationPayload(String title, String description, String advancementId, ItemStack icon) implements CustomPayload {
    public static final CustomPayload.Id<AdvancementNotificationPayload> ID = new CustomPayload.Id<>(Identifier.of(TutorialMod.MOD_ID, "advancement_notification"));
    public static final PacketCodec<RegistryByteBuf, AdvancementNotificationPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, AdvancementNotificationPayload::title,
            PacketCodecs.STRING, AdvancementNotificationPayload::description,
            PacketCodecs.STRING, AdvancementNotificationPayload::advancementId,
            ItemStack.PACKET_CODEC, AdvancementNotificationPayload::icon,
            AdvancementNotificationPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}


