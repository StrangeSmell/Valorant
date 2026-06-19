package com.strangesmell.valorant.leizhi.blastpack;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BlastPackEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> DATA_STUCK = SynchedEntityData.defineId(BlastPackEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_EXPLODED = SynchedEntityData.defineId(BlastPackEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_HEALTH = SynchedEntityData.defineId(BlastPackEntity.class, EntityDataSerializers.FLOAT);
    private static final double RADIUS = 4.25D;
    private static final double KNOCKBACK = 1.45D;
    private static final float MIN_DAMAGE = 4.0F;
    private static final float MAX_DAMAGE = 10.0F;
    private static final int AUTO_EXPLODE_TIME = 100;
    private static final int ARM_TIME = 30;

    public BlastPackEntity(EntityType<? extends BlastPackEntity> entityType, Level level) {
        super(entityType, level);
    }

    public BlastPackEntity(Level level, LivingEntity owner, ItemStack item) {
        super(VALORANT.BLASTPACK.get(), owner, level, item);
    }

    public BlastPackEntity(Level level, double x, double y, double z, ItemStack item) {
        super(VALORANT.BLASTPACK.get(), x, y, z, level, item);
    }

    @Override
    public void tick() {
        if (this.getStuck()) {
            this.setDeltaMovement(Vec3.ZERO);
        } else {
            super.tick();
        }

        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getRandomX(0.35D), this.getRandomY(), this.getRandomZ(0.35D), 0.0D, 0.0D, 0.0D);
        }

        if (!this.level().isClientSide() && this.tickCount > AUTO_EXPLODE_TIME) {
            this.explode();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return VALORANT.BLASTPACK_ITEM.get();
    }

    public ItemStack getRenderItem() {
        return new ItemStack(getDefaultItem());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.stick(result.getLocation());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        this.stick(result.getLocation());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && result.getType() != HitResult.Type.MISS) {
            this.stick(result.getLocation());
        }
    }

    private void stick(Vec3 pos) {
        if (this.getStuck() || this.getExploded()) {
            return;
        }

        this.setStuck(true);
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(pos.x, pos.y + 0.05D, pos.z);
        
    }

    public void explode() {
        if (this.getExploded() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        this.setExploded(true);
        this.level().broadcastEntityEvent(this, (byte)3);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), VALORANT.LEIZHI_BLAST_PACK_EXPLODE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

        Vec3 center = this.position();
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(RADIUS), LivingEntity::isAlive)) {
            double distance = Math.sqrt(target.distanceToSqr(center));
            if (distance > RADIUS) {
                continue;
            }

            double strength = 1.0D - distance / RADIUS;
            if (target != this.getOwner()) {
                float maxDamage = this.tickCount >= ARM_TIME ? MAX_DAMAGE : MIN_DAMAGE;
                float damage = (float)(MIN_DAMAGE + (maxDamage - MIN_DAMAGE) * strength);
                target.hurtServer(serverLevel, this.damageSources().explosion(this, this.getOwner()), damage);
            }

            Vec3 push = target.position().subtract(center);
            if (push.lengthSqr() < 1.0E-4D) {
                push = target.getLookAngle();
            }
            Vec3 knockback = push.normalize().scale(KNOCKBACK * (0.35D + strength)).add(0.0D, 0.35D + strength * 0.3D, 0.0D).scale(0.4);
            target.push(knockback.x, knockback.y, knockback.z);
            target.hurtMarked = true;
        }

        serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 5, 0.7D, 0.5D, 0.7D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 60, 1.4D, 1.0D, 1.4D, 0.2D);
        this.discard();
    }

    public boolean isOwnedBy(Entity entity) {
        return entity != null && entity == this.getOwner();
    }

    private ParticleOptions getParticle() {
        ItemStack itemStack = this.getItem();
        return itemStack.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, itemStack.getItem());
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 3) {
            ParticleOptions particle = this.getParticle();
            for (int i = 0; i < 12; ++i) {
                this.level().addParticle(particle, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.18D, this.random.nextGaussian() * 0.18D, this.random.nextGaussian() * 0.18D);
            }
        }

        super.handleEntityEvent(event);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STUCK, false);
        builder.define(DATA_EXPLODED, false);
        builder.define(DATA_HEALTH, 4.0F);
    }

    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        if (this.getExploded()) {
            return false;
        }
        float health = this.entityData.get(DATA_HEALTH) - amount;
        this.entityData.set(DATA_HEALTH, health);
        if (health <= 0.0F) {
            this.discard();
        }
        return true;
    }

    private boolean getStuck() {
        return this.entityData.get(DATA_STUCK);
    }

    private void setStuck(boolean stuck) {
        this.entityData.set(DATA_STUCK, stuck);
    }

    private boolean getExploded() {
        return this.entityData.get(DATA_EXPLODED);
    }

    private void setExploded(boolean exploded) {
        this.entityData.set(DATA_EXPLODED, exploded);
    }
}
