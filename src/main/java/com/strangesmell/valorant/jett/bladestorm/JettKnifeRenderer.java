package com.strangesmell.valorant.jett.bladestorm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;


public class JettKnifeRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T, JettKnifeRenderer.KnifeRenderState> {
    private final ItemModelResolver itemModelResolver;

    public JettKnifeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.shadowRadius = 0.12F;
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        return 15;
    }

    @Override
    public KnifeRenderState createRenderState() {
        return new KnifeRenderState();
    }

    @Override
    public void extractRenderState(T entity, KnifeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (entity instanceof JettBladeStormOrbitKnifeEntity orbit) {
            JettBladeStormOrbitKnifeEntity.KnifePose pose = orbit.calculateClientRenderPose(partialTicks);
            state.x = pose.position().x;
            state.y = pose.position().y;
            state.z = pose.position().z;
            state.xRot = pose.xRot();
            state.yRot = pose.yRot();
            state.isOrbit = true;
        } else {
            // Thrown knife: use entity interpolated rotation (velocity direction)
            state.xRot = Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot());
            state.yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
            state.isOrbit = false;
        }
        ItemStack renderItem = entity.getItem();
        this.itemModelResolver.updateForNonLiving(state.item, renderItem, ItemDisplayContext.FIXED, entity);
    }

    @Override
    public void submit(KnifeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        if (state.isOrbit) {
            // Orbit knife: same as 1.20.1
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
        } else {
            // Thrown knife: tip points in flight direction
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0F));
        poseStack.scale(0.85F, 0.85F, 0.85F);
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    public static class KnifeRenderState extends ThrownItemRenderState {
        public float xRot;
        public float yRot;
        public boolean isOrbit;
    }
}