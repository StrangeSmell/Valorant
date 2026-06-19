package com.strangesmell.valorant.leizhi.boombot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.cart.MinecartModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
public class BoomBotRenderer extends EntityRenderer<BoomBotEntity, MinecartRenderState> {
    private static final Identifier MINECART_LOCATION = Identifier.withDefaultNamespace("textures/entity/minecart/minecart.png");
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final MinecartModel model;
    private final BlockModelResolver blockModelResolver;

    public BoomBotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MinecartModel(context.bakeLayer(ModelLayers.TNT_MINECART));
        this.blockModelResolver = context.getBlockModelResolver();
        this.shadowRadius = 0.35F;
    }

    @Override
    public MinecartRenderState createRenderState() {
        return new MinecartRenderState();
    }

    @Override
    public void extractRenderState(BoomBotEntity entity, MinecartRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isNewRender = true;
        state.xRot = entity.getXRot(partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        long seed = entity.getId() * 493286711L;
        state.offsetSeed = seed * seed * 4392167121L + seed * 98761L;
        state.hurtTime = 0.0F;
        state.hurtDir = 0;
        state.damageTime = 0.0F;
        state.displayOffset = 0;
        this.blockModelResolver.update(state.displayBlockModel, Blocks.TNT.defaultBlockState(), BLOCK_DISPLAY_CONTEXT);
    }

    @Override
    public void submit(MinecartRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.pushPose();
        long seed = state.offsetSeed;
        float ox = (((float)(seed >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float oy = (((float)(seed >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float oz = (((float)(seed >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        poseStack.translate(ox, oy, oz);

        // New-style minecart positioning
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-state.xRot));
        poseStack.translate(0.0F, 0.375F, 0.0F);

        // TNT block
        BlockModelRenderState blockModel = state.displayBlockModel;
        if (!blockModel.isEmpty()) {
            poseStack.pushPose();
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.translate(-0.5F, 0.0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            blockModel.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }

        // Minecart model
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        submitNodeCollector.submitModel(this.model, state, poseStack, MINECART_LOCATION, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
    }
}
