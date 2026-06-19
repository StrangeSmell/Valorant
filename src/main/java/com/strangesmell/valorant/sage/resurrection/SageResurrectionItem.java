package com.strangesmell.valorant.sage.resurrection;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class SageResurrectionItem extends Item {
    private static final double RANGE = 4.0D;

    public SageResurrectionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }



    public static HitResult getHitResult(Level level, Player player,double range) {
        AABB aabb;
        //double range = Objects.requireNonNull(player.getAttribute(BLOCK_INTERACTION_RANGE)).getValue();
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0f);
        Vec3 endPosition = eyePosition.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);
        //may false
        HitResult hitResult = ProjectileUtil.getEntityHitResult((Level)level, (Entity)player, (Vec3)eyePosition, (Vec3)endPosition, (AABB)(aabb = player.getBoundingBox().expandTowards(viewVector.scale(range)).inflate(1.0, 1.0, 1.0)), entity -> !entity.isSpectator() , 1.0f);
        if (hitResult == null) {
            hitResult = level.clip(new ClipContext(eyePosition, endPosition, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, (Entity)player));
        }
        return hitResult;
    }

}
