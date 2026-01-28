package net.kimdog_studios.primal_craft.mixin;

import net.kimdog_studios.primal_craft.event.ZoomHandler;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Mouse.class)
public class MouseSensitivityMixin {

    @ModifyVariable(method = "updateMouse", at = @At("STORE"), ordinal = 2)
    private double reduceHorizontalSensitivity(double deltaX) {
        if (ZoomHandler.isZooming()) {
            double zoomMultiplier = ZoomHandler.getZoomMultiplier();
            // Only reduce sensitivity when actually zoomed in (> 1x)
            if (zoomMultiplier > 1.0) {
                // Progressive reduction: more zoom = less sensitivity
                // At 2x zoom: ~70% sensitivity, at 64x zoom: ~12.5% sensitivity
                double sensitivityReduction = 1.0 / Math.sqrt(zoomMultiplier);
                return deltaX * sensitivityReduction;
            }
        }
        return deltaX;
    }

    // Vertical sensitivity remains normal - no reduction applied
    // The ordinal = 3 method has been removed so up/down mouse movement stays at 100% sensitivity
}
