package com.strangesmell.valorant.leizhi.boombot;

import com.strangesmell.valorant.Valorant;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;

public class BoomBotEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> DATA_EXPLODED = SynchedEntityData.defineId(BoomBotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_TARGET = SynchedEntityData.defineId(BoomBotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TARGET_WINDUP_TICKS = SynchedEntityData.defineId(BoomBotEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_HEALTH = SynchedEntityData.defineId(BoomBotEntity.class, EntityDataSerializers.FLOAT);
    private static final double TARGET_RANGE = 10.0D;
    private static final double EXPLOSION_RADIUS = 3.5D;
    private static final double SPEED = 0.4D;
    private static final double GRAVITY = 0.04D;
    private static final double TRACKING_GRAVITY = 0.08D;
    private static final double GROUND_BOUNCE_MIN_SPEED = 0.28D;
    private static final double GROUND_BOUNCE = 0.28D;
    private static final double TARGET_CHARGE_SPEED = 0.58D;
    private static final double TARGET_POP_UP_SPEED = 0.24D;
    private static final float MIN_DAMAGE = 6.0F;
    private static final float MAX_DAMAGE = 16.0F;
    private static final int TARGET_WINDUP_TIME = 12;
    private static final int SEARCH_TIME = 100;
    private static final int MAX_LIFE = 100;

    private Vec3 targetChargeDirection = Vec3.ZERO;

    public BoomBotEntity(EntityType<? extends BoomBotEntity> entityType, Level level) {
        super(entityType, level);
    }

    public BoomBotEntity(Level level, LivingEntity owner, ItemStack item) {
        super(Valorant.BOOMBOT.get(), owner, level, item);
    }

    public BoomBotEntity(Level level, double x, double y, double z, ItemStack item) {
        super(Valorant.BOOMBOT.get(), x, y, z, level, item);
    }

    @Override
    public void tick() {
        Vec3 motion = this.getDeltaMovement();
        if (!this.level().isClientSide()) {
            Optional<Vec3> targetDirection = this.findTargetDirection();
            if (targetDirection.isPresent()) {
                this.targetChargeDirection = targetDirection.get();
                if (!this.getHasTarget()) {
                    this.setHasTarget(true);
                    this.setTargetWindupTicks(TARGET_WINDUP_TIME);
                    this.setDeltaMovement(0.0D, TARGET_POP_UP_SPEED, 0.0D);
                } else if (this.getTargetWindupTicks() > 0) {
                    this.setDeltaMovement(0.0D, motion.y - GRAVITY, 0.0D);
                    this.setTargetWindupTicks(this.getTargetWindupTicks() - 1);
                } else {
                    this.setDeltaMovement(targetDirection.get().scale(TARGET_CHARGE_SPEED).add(0.0D, motion.y - TRACKING_GRAVITY, 0.0D));
                }
            } else if (this.getTargetWindupTicks() > 0) {
                this.setDeltaMovement(0.0D, motion.y - GRAVITY, 0.0D);
                this.setTargetWindupTicks(this.getTargetWindupTicks() - 1);
            } else if (this.getHasTarget()) {
                this.setDeltaMovement(this.currentChargeDirection().scale(TARGET_CHARGE_SPEED).add(0.0D, motion.y - TRACKING_GRAVITY, 0.0D));
            } else {
                Vec3 direction = this.currentDirection();
                double yMotion = motion.y - GRAVITY;
                this.setDeltaMovement(direction.scale(SPEED).add(0.0D, yMotion, 0.0D));
            }
        }

        Vec3 beforeMove = this.position();
        Vec3 requestedMove = this.getDeltaMovement();
        this.setOnGround(false);
        this.move(MoverType.SELF, requestedMove);
        this.handleMoveCollision(requestedMove, this.position().subtract(beforeMove));
        this.updateRotation();
        this.applyEffectsFromBlocks();

        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.01D, 0.0D);
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY() + 0.1D, this.getZ(), -this.getDeltaMovement().x * 0.2D, 0.0D, -this.getDeltaMovement().z * 0.2D);
        }

        if (!this.level().isClientSide()) {
            if ((!this.getHasTarget() && this.tickCount > SEARCH_TIME) || this.tickCount > MAX_LIFE || (this.getTargetWindupTicks() <= 0 && this.hasCloseTarget())) {
                this.explode();
            }
        }

        this.checkBelowWorld();
        super.baseTick();
    }

    @Override
    protected Item getDefaultItem() {
        return Items.TNT_MINECART;
    }

    public ItemStack getRenderItem() {
        return new ItemStack(getDefaultItem());
    }

    @Override
    public boolean canHitEntity(Entity target) {
        return false;
    }

    private void handleMoveCollision(Vec3 requestedMove, Vec3 actualMove) {
        Vec3 motion = this.getDeltaMovement();
        double x = motion.x;
        double y = motion.y;
        double z = motion.z;
        boolean changed = false;

        if (this.horizontalCollision) {
            if (Math.abs(actualMove.x - requestedMove.x) > 1.0E-5D) {
                x = -x;
                changed = true;
            }
            if (Math.abs(actualMove.z - requestedMove.z) > 1.0E-5D) {
                z = -z;
                changed = true;
            }
        }

        if (this.verticalCollisionBelow) {
            if (requestedMove.y < -GROUND_BOUNCE_MIN_SPEED) {
                y = -requestedMove.y * GROUND_BOUNCE;
                this.setOnGround(false);
            } else {
                y = 0.0D;
                this.setOnGround(true);
            }
            changed = true;
        } else if (this.verticalCollision) {
            y = 0.0D;
            changed = true;
        }

        if (changed) {
            this.setDeltaMovement(x, y, z);
            
        }
    }

    private Optional<Vec3> findTargetDirection() {
        Entity owner = this.getOwner();
        AABB searchArea = this.getBoundingBox().inflate(TARGET_RANGE, 3.0D, TARGET_RANGE);
        return this.level().getEntitiesOfClass(LivingEntity.class, searchArea, target -> target.isAlive() && target != owner && this.hasLineOfSightTo(target))
                .stream()
                .min(Comparator.comparingDouble(target -> target.distanceToSqr(this)))
                .map(target -> new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ()))
                .filter(vec -> vec.lengthSqr() > 1.0E-4D)
                .map(Vec3::normalize);
    }

    private boolean hasCloseTarget() {
        Entity owner = this.getOwner();
        return !this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.1D), target -> target.isAlive() && target != owner).isEmpty();
    }

    private Vec3 currentDirection() {
        Vec3 motion = this.getDeltaMovement();
        Vec3 flat = new Vec3(motion.x, 0.0D, motion.z);
        if (flat.lengthSqr() < 1.0E-4D) {
            return Vec3.directionFromRotation(0.0F, this.getYRot());
        }
        return flat.normalize();
    }

    private Vec3 currentChargeDirection() {
        if (this.targetChargeDirection.lengthSqr() > 1.0E-4D) {
            return this.targetChargeDirection;
        }
        return this.currentDirection();
    }

    private boolean hasLineOfSightTo(LivingEntity target) {
        Vec3 targetCenter = new Vec3(target.getX(), target.getY(0.5D), target.getZ());
        HitResult result = this.level().clip(new ClipContext(this.position().add(0.0D, 0.25D, 0.0D), targetCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return result.getType() == HitResult.Type.MISS;
    }

    private void explode() {
        if (this.getExploded() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        this.setExploded(true);
        this.level().broadcastEntityEvent(this, (byte)3);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), Valorant.LEIZHI_BOOM_BOT_EXPLODE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

        Vec3 center = this.position();
        Entity owner = this.getOwner();
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(EXPLOSION_RADIUS), target -> target.isAlive() && target != owner)) {
            double distance = Math.sqrt(target.distanceToSqr(center));
            if (distance > EXPLOSION_RADIUS || !this.hasLineOfSightTo(target)) {
                continue;
            }

            double strength = 1.0D - distance / EXPLOSION_RADIUS;
            float damage = (float)(MIN_DAMAGE + (MAX_DAMAGE - MIN_DAMAGE) * strength);
            target.hurtServer(serverLevel, this.damageSources().explosion(this, owner), damage);
            Vec3 push = target.position().subtract(center);
            if (push.lengthSqr() > 1.0E-4D) {
                Vec3 knockback = push.normalize().scale(1.15D + strength).add(0.0D, 0.35D, 0.0D);
                target.push(knockback.x, knockback.y, knockback.z);
                target.hurtMarked = true;
            }
        }

        serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 8, 0.7D, 0.5D, 0.7D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 45, 1.2D, 0.8D, 1.2D, 0.08D);
        this.discard();
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
        builder.define(DATA_EXPLODED, false);
        builder.define(DATA_HAS_TARGET, false);
        builder.define(DATA_TARGET_WINDUP_TICKS, 0);
        builder.define(DATA_HEALTH, 12.0F);
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

    private boolean getExploded() {
        return this.entityData.get(DATA_EXPLODED);
    }

    private void setExploded(boolean exploded) {
        this.entityData.set(DATA_EXPLODED, exploded);
    }

    private boolean getHasTarget() {
        return this.entityData.get(DATA_HAS_TARGET);
    }

    private void setHasTarget(boolean hasTarget) {
        this.entityData.set(DATA_HAS_TARGET, hasTarget);
    }

    public int getTargetWindupTicks() {
        return this.entityData.get(DATA_TARGET_WINDUP_TICKS);
    }

    private void setTargetWindupTicks(int ticks) {
        this.entityData.set(DATA_TARGET_WINDUP_TICKS, ticks);
    }
}
