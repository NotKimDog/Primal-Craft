package net.kaupenjoe.tutorialmod.mixin;

import net.kaupenjoe.tutorialmod.event.ZoomHudOverlay;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HideHudMixin {

    // Hide hotbar when zooming
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void hideHotbar(CallbackInfo ci) {
        if (ZoomHudOverlay.shouldHideHud()) {
            ci.cancel();
        }
    }

    // Hide health/armor/hunger when zooming
    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void hideStatusBars(CallbackInfo ci) {
        if (ZoomHudOverlay.shouldHideHud()) {
            ci.cancel();
        }
    }

    // Hide mount health when zooming
    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void hideMountHealth(CallbackInfo ci) {
        if (ZoomHudOverlay.shouldHideHud()) {
            ci.cancel();
        }
    }
}
