package net.kaupenjoe.tutorialmod.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldRenderer.class)
public class VeinMinerHighlightMixin {
    // Placeholder - vein highlighting is done through VeinMinerBlockHighlighter client-side
}
