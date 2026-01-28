package net.kimdog_studios.primal_craft.mixin;

import net.kimdog_studios.primal_craft.event.AnimatedChatHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void tutorialmod$redirectGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        Text content = packet.content();
        pushAnimated(packet.overlay() ? "SYS" : "GAME", packet.overlay() ? "System" : "Mojang", content);
        ci.cancel();
    }

    private void pushAnimated(String role, String name, Text content) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        // Modern color scheme with better contrast
        int roleColor = switch(role) {
            case "MEMBER" -> 0x888888;
            case "SYS" -> 0x5599FF;
            case "GAME" -> 0x7744DD;
            case "VEIN" -> 0x44DD77;
            default -> 0x666666;
        };

        int nameColor = name.equals("Mojang") ? 0xFF3333 :
                       name.equals("System") ? 0x66AAFF :
                       name.equals("VeinMiner") ? 0x55DD88 :
                       0xFFDD55; // Default gold for players

        // Priority: GAME=2 (important commands), SYS=1, others=0
        int priority = switch(role) {
            case "GAME" -> 2;
            case "SYS" -> 1;
            default -> 0;
        };

        var styled = Text.empty()
                .append(Text.literal("[" + role + "]").styled(s -> s.withColor(roleColor)))
                .append(Text.literal(" "))
                .append(Text.literal(name).styled(s -> s.withColor(nameColor).withBold(true)))
                .append(Text.literal(" › ").styled(s -> s.withColor(0x555555)))
                .append(content.copy().styled(s -> s.withColor(0xEEEEEE)));
        AnimatedChatHud.push(styled, role, priority);
    }
}
