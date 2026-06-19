package com.strangesmell.valorant.phoenix.curveball;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PhoenixCurveballEntity extends ThrowableItemProjectile {
    private static final int GUIDE_TIME = 18;
    private static final int WINDUP_TIME = 10;
    private static final int BLIND_TIME = 30;
    private static final double GUIDE_SPEED = 1.1D;
    private static final double FLASH_RADIUS = 10.0D;
    private static final double FLASH_VIEW_DOT = Math.cos(Math.toRadians(60.0D));

    public PhoenixCurveballEntity(EntityType<? extends PhoenixCurveballEntity> entityType, Level level) {
        super(entityType, level);
    }

    public PhoenixCurveballEntity(Level level, LivingEntity owner, ItemStack item) {
        super(VALORANT.PHOENIX_CURVEBALL.get(), owner, level, item);
    }

    public PhoenixCurveballEntity(Level level, double x, double y, double z, ItemStack item) {
        super(VALORANT.PHOENIX_CURVEBALL.get(), x, y, z, level, item);
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY()-0.2, this.getZ(), 0.0D, 0.0D, 0.0D);
        }
        if (!this.level().isClientSide() && this.tickCount > WINDUP_TIME) {
            this.flash();
        }
    }

    public boolean isGuidable() {
        return this.tickCount <= GUIDE_TIME && !this.isRemoved();
    }    public static void guideOwned(ServerLevel level, ServerPlayer player) {
        for (PhoenixCurveballEntity curveball : level.getEntitiesOfClass(PhoenixCurveballEntity.class, player.getBoundingBox().inflate(12.0D), entity -> entity.getOwner() == player && entity.isGuidable())) {
            curveball.guideBy(player);
        }
    }



    public void guideBy(LivingEntity owner) {
        if (!this.isGuidable()) {
            return;
        }
        Vec3 look = owner.getLookAngle().normalize();
        this.setDeltaMovement(look.scale(GUIDE_SPEED));
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        
    }

    @Override
    protected Item getDefaultItem() {
        return Items.BLAZE_POWDER;
    }

    public ItemStack getRenderItem() {
        return new ItemStack(getDefaultItem());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.flash();
        }
    }

    private void flash() {
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 center = this.position();
            for (ServerPlayer target : serverLevel.getPlayers(target -> target.isAlive() && target.distanceToSqr(center) <= FLASH_RADIUS * FLASH_RADIUS)) {
                Vec3 toFlash = center.subtract(target.getEyePosition());
                if ((toFlash.lengthSqr() < 1.0E-4D || target.getLookAngle().normalize().dot(toFlash.normalize()) >= FLASH_VIEW_DOT) && this.hasLineOfSight(target, center)) {
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLIND_TIME, 1));
                }
            }
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), VALORANT.PHOENIX_CURVEBALL_EXPLODE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFC04D), this.getX(), this.getY(), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        ((Player)this.getOwner()).getUseItem().consume(1, (Player)this.getOwner());
        this.discard();
    }

    private boolean hasLineOfSight(ServerPlayer target, Vec3 center) {
        HitResult result = this.level().clip(new ClipContext(target.getEyePosition(), center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target));
        return result.getType() == HitResult.Type.MISS;
    }
}
