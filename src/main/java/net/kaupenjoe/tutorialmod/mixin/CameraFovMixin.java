package net.kaupenjoe.tutorialmod.mixin;

import net.kaupenjoe.tutorialmod.event.ZoomHandler;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class, priority = 1001)
public class CameraFovMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, remap = false)
    private void tutorialmod$applyZoom(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue((float)(cir.getReturnValue() * ZoomHandler.getZoomLevel()));
    }
}
