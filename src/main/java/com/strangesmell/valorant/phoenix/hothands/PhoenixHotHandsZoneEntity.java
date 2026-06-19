package com.strangesmell.valorant.phoenix.hothands;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class PhoenixHotHandsZoneEntity extends Entity {
    private static final int LIFE_TIME = 65;
    private static final double RADIUS = 3.2D;
    private static final float DAMAGE_PER_TICK = 0.6F;
    private Entity owner;

    public PhoenixHotHandsZoneEntity(EntityType<? extends PhoenixHotHandsZoneEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public PhoenixHotHandsZoneEntity(Level level, double x, double y, double z, Entity owner) {
        this(VALORANT.PHOENIX_HOT_HANDS_ZONE.get(), level);
        this.owner = owner;
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel) {
            AABB area = this.getBoundingBox().inflate(RADIUS, 1.0D, RADIUS);
            for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                if (target.distanceToSqr(this.position()) <= RADIUS * RADIUS) {
                    if (target == this.owner) {
                        target.heal(0.5F);
                    } else {
                        target.igniteForTicks(40);
                        target.hurtServer(serverLevel, this.damageSources().onFire(), DAMAGE_PER_TICK);
                    }
                }
            }
            serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.1D, this.getZ(), 18, RADIUS * 0.45D, 0.1D, RADIUS * 0.45D, 0.03D);
        }
        if (!this.level().isClientSide() && this.tickCount > LIFE_TIME) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
