package com.strangesmell.valorant.clove.meddle;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class CloveMeddleEntity extends ThrowableItemProjectile {
    private static final double RADIUS = 4.0D;
    private static final int GROUND_DELAY = 15;
    private boolean primed;
    private boolean burst;
    private int primedTicks;

    public CloveMeddleEntity(EntityType<? extends CloveMeddleEntity> entityType, Level level) {
        super(entityType, level);
    }

    public CloveMeddleEntity(Level level, LivingEntity owner, ItemStack item) {
        super(Valorant.CLOVE_MEDDLE.get(), owner, level, item);
    }

    public CloveMeddleEntity(Level level, double x, double y, double z, ItemStack item) {
        super(Valorant.CLOVE_MEDDLE.get(), x, y, z, level, item);
    }

    @Override
    public void tick() {
        if (this.primed) {
            this.setDeltaMovement(0.0D, 0.0D, 0.0D);
            this.primedTicks++;
        } else {
            super.tick();
        }

        if (this.level().isClientSide()) {
            this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF7C4DFF), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
        if (!this.level().isClientSide() && this.primed && this.primedTicks >= GROUND_DELAY) {
            this.burst();
        }
        if (!this.level().isClientSide() && this.tickCount > 80) {
            this.burst();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    public ItemStack getRenderItem() {
        return new ItemStack(getDefaultItem());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        this.primeOnHit(result);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.primeOnHit(result);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        this.primeOnHit(result);
    }

    private void primeOnHit(HitResult result) {
        if (this.level().isClientSide() || this.primed || this.burst) {
            return;
        }

        this.primed = true;
        this.primedTicks = 0;
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        if (result.getType() != HitResult.Type.MISS) {
            this.setPos(result.getLocation());
        }
    }

    private void burst() {
        if (this.burst) {
            return;
        }
        this.burst = true;
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity owner = this.getOwner();
            AABB area = this.getBoundingBox().inflate(RADIUS);
            for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area, target -> target.isAlive() && target != owner)) {
                if (target.distanceToSqr(this.position()) <= RADIUS * RADIUS) {
                    CloveMeddleDecayTracker.apply(serverLevel, target);
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
                }
            }
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), Valorant.CLOVE_MEDDLE_EXPLODE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF7C4DFF), this.getX(), this.getY(), this.getZ(), 60, 1.5D, 0.8D, 1.5D, 0.05D);
        }
        this.discard();
    }
}
