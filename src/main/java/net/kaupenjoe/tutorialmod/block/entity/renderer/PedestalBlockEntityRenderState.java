package net.kaupenjoe.tutorialmod.block.entity.renderer;

import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PedestalBlockEntityRenderState extends BlockEntityRenderState {
    public BlockPos lightPosition;
    public World blockEntityWorld;
    public float rotation;

    final ItemRenderState itemRenderState = new ItemRenderState();
}
