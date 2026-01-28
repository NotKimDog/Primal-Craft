package net.kimdog_studios.primal_craft.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kimdog_studios.primal_craft.network.ChatAnimatedPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMessageMixin {
    @Inject(method = "sendMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"), cancellable = true)
    private void tutorialmod$routingAnimated(Text message, boolean overlay, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        // Route all system/command feedback into animated chat
        ServerPlayNetworking.send(self, new ChatAnimatedPayload("SYS", "System", message.getString()));
        ci.cancel();
    }
}
