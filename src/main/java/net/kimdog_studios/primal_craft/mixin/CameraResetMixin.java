package net.kimdog_studios.primal_craft.mixin;

import net.kimdog_studios.primal_craft.event.CameraResetHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class CameraResetMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        if (CameraResetHandler.isResetting()) {
            float progress = CameraResetHandler.getResetProgress();

            // CINEMATIC interpolation with cubic ease-in-out
            float smoothProgress;
            if (progress < 0.5f) {
                smoothProgress = (float) (4 * progress * progress * progress);
            } else {
                float f = (float) ((2 * progress) - 2);
                smoothProgress = (float) (0.5 * f * f * f + 1);
            }

            // Get current rotation
            float currentYaw = player.getYaw();
            float currentPitch = player.getPitch();

            // Calculate target (centered forward)
            float targetYaw = Math.round(currentYaw / 90.0f) * 90.0f; // Snap to nearest cardinal direction
            float targetPitch = 0.0f; // Level horizon

            // Interpolate towards target (cinematic - slow and smooth)
            float newYaw = currentYaw + (targetYaw - currentYaw) * smoothProgress * 0.5f;
            float newPitch = currentPitch + (targetPitch - currentPitch) * smoothProgress * 0.5f;

            player.setYaw(newYaw);
            player.setPitch(newPitch);
        }
    }
}
