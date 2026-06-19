package com.strangesmell.valorant.leizhi.bigbomb;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BigBombEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> DATA_EXPLODED = SynchedEntityData.defineId(BigBombEntity.class, EntityDataSerializers.BOOLEAN);
    private static final float MIN_DAMAGE = 6.0F;
    private static final float MAX_DAMAGE = 30.0F;
    private static final double RADIUS = 5.5D;
    private static final double KNOCKBACK = 2.2D;
    private static final int MAX_LIFE = 200;

    public BigBombEntity(EntityType<? extends BigBombEntity> entityType, Level level) {
        super(entityType, level);
    }

    public BigBombEntity(Level level, LivingEntity owner, ItemStack item) {
        super(Valorant.BIGBOMB.get(), owner, level, item);
    }

    public BigBombEntity(Level level, double x, double y, double z, ItemStack item) {
        super(Valorant.BIGBOMB.get(), x, y, z, level, item);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.scale(1.02D).add(0.0D, 0.015D, 0.0D));

        if (this.level().isClientSide()) {
            for (int i = 0; i < 4; i++) {
                this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFFFF6A00), this.getRandomX(0.8D), this.getRandomY(), this.getRandomZ(0.8D), 0.0D, 0.0D, 0.0D);
            }
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), -motion.x * 0.15D, -motion.y * 0.15D, -motion.z * 0.15D);
        }

        if (!this.level().isClientSide() && this.tickCount > MAX_LIFE) {
            this.explode();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Valorant.BIGBOMB_ITEM.get();
    }

    public ItemStack getRenderItem() {
        return new ItemStack(getDefaultItem());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.explode();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            this.explode();
        }
    }

    private void explode() {
        if (this.getExploded() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        this.setExploded(true);
        this.level().broadcastEntityEvent(this, (byte)3);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), Valorant.LEIZHI_SHOWSTOPPER_EXPLODE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

        Vec3 center = this.position();
        AABB area = this.getBoundingBox().inflate(RADIUS);
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area, target -> target.isAlive() && target != this.getOwner())) {
            double distance = Math.sqrt(target.distanceToSqr(center));
            if (distance > RADIUS || !this.hasLineOfSightTo(center, target)) {
                continue;
            }

            double strength = 1.0D - distance / RADIUS;
            float damage = (float)(MIN_DAMAGE + (MAX_DAMAGE - MIN_DAMAGE) * strength);
            target.hurtServer(serverLevel, this.damageSources().explosion(this, this.getOwner()), damage);

            Vec3 push = target.position().subtract(center);
            if (push.lengthSqr() < 1.0E-4D) {
                push = this.getDeltaMovement().normalize();
            }
            Vec3 knockback = push.normalize().scale(KNOCKBACK * (0.35D + strength)).add(0.0D, 0.55D + strength * 0.4D, 0.0D);
            target.push(knockback.x, knockback.y, knockback.z);
            target.hurtMarked = true;
        }

        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 80, 2.0D, 2.0D, 2.0D, 0.18D);
        serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 80, 2.2D, 2.2D, 2.2D, 0.12D);
        this.discard();
    }

    private boolean hasLineOfSightTo(Vec3 center, LivingEntity target) {
        Vec3 targetCenter = new Vec3(target.getX(), target.getY(0.5D), target.getZ());
        HitResult result = this.level().clip(new ClipContext(center, targetCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return result.getType() == HitResult.Type.MISS;
    }

    private ParticleOptions getParticle() {
        ItemStack itemStack = this.getItem();
        return itemStack.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(itemStack));
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 3) {
            ParticleOptions particle = this.getParticle();
            for (int i = 0; i < 16; ++i) {
                this.level().addParticle(particle, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.2D, this.random.nextGaussian() * 0.2D, this.random.nextGaussian() * 0.2D);
            }
        }

        super.handleEntityEvent(event);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_EXPLODED, false);
    }

    private boolean getExploded() {
        return this.entityData.get(DATA_EXPLODED);
    }

    private void setExploded(boolean exploded) {
        this.entityData.set(DATA_EXPLODED, exploded);
    }
}
