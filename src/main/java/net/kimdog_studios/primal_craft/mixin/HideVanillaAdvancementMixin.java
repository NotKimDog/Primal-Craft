package net.kimdog_studios.primal_craft.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HideVanillaAdvancementMixin {

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void hideAdvancementMessage(net.minecraft.text.Text message, boolean actionBar, CallbackInfo ci) {
        // Hide vanilla advancement notifications by cancelling the method
        if (message != null && message.getString().contains("advancement")) {
            ci.cancel();
        }
    }
}
