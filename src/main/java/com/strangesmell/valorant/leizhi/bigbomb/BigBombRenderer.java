package com.strangesmell.valorant.leizhi.bigbomb;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SkullBlock;

public class BigBombRenderer extends EntityRenderer<BigBombEntity, BigBombRendererState> {
    private static final RenderType DRAGON_HEAD_RENDER_TYPE = RenderTypes.entityCutoutZOffset(
            Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon.png")
    );
    private final SkullModelBase model;

    public BigBombRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = net.minecraft.client.renderer.blockentity.SkullBlockRenderer.createModel(context.getModelSet(), SkullBlock.Types.DRAGON);
        this.shadowRadius = 0.35F;
    }

    @Override
    protected int getBlockLightLevel(BigBombEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(BigBombRendererState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.scale(1.35F, 1.35F, 1.35F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
        poseStack.translate(0.0F, 0.25F, 0.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        collector.submitModel(this.model, state.skull, poseStack, DRAGON_HEAD_RENDER_TYPE, state.lightCoords, 0, state.outlineColor, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, cameraState);
    }

    @Override
    public BigBombRendererState createRenderState() {
        return new BigBombRendererState();
    }

    @Override
    public void extractRenderState(BigBombEntity entity, BigBombRendererState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.xRot = entity.getXRot(partialTick);
        state.yRot = entity.getYRot(partialTick);
        if (entity.getDeltaMovement().lengthSqr() > 1.0E-4D) {
            state.xRot = (float)(Mth.atan2(entity.getDeltaMovement().y, entity.getDeltaMovement().horizontalDistance()) * Mth.RAD_TO_DEG);
            state.yRot = (float)(Mth.atan2(entity.getDeltaMovement().x, entity.getDeltaMovement().z) * Mth.RAD_TO_DEG);
        }
        state.skull.animationPos = state.ageInTicks;
        state.skull.xRot = 0.0F;
        state.skull.yRot = 0.0F;
    }
}