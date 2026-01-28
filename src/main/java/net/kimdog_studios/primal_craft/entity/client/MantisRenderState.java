package net.kimdog_studios.primal_craft.entity.client;

import net.kimdog_studios.primal_craft.entity.custom.MantisVariant;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.AnimationState;

public class MantisRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public MantisVariant variant;
}
