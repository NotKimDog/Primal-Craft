package net.kimdog_studios.primal_craft.entity.client;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.entity.custom.TomahawkProjectileEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class TomahawkProjectileRenderer extends EntityRenderer<TomahawkProjectileEntity, EntityRenderState> {
    protected TomahawkProjectileModel model;

    public TomahawkProjectileRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new TomahawkProjectileModel(ctx.getPart(TomahawkProjectileModel.TOMAHAWK));
    }

    @Override
    public void render(EntityRenderState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        // matrices.push();
//
        // VertexConsumer vertexconsumer = ItemRenderer.getItemGlintConsumer(vertexConsumers,
        //         this.model.getLayer(Identifier.of(PrimalCraft.MOD_ID, "textures/entity/tomahawk/tomahawk.png")), false, false);
        // this.model.render(matrices, vertexconsumer, light, OverlayTexture.DEFAULT_UV);
//
        // matrices.pop();

        super.render(renderState, matrices, queue, cameraState);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
