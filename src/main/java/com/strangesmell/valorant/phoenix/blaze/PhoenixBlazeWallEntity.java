package com.strangesmell.valorant.phoenix.blaze;

import com.strangesmell.valorant.Valorant;
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

public class PhoenixBlazeWallEntity extends Entity {
    private static final int LIFE_TIME = 160;
    private static final float DAMAGE_PER_TICK = 0.3F;
    private Entity owner;

    public PhoenixBlazeWallEntity(EntityType<? extends PhoenixBlazeWallEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;

    }

    public PhoenixBlazeWallEntity(Level level, double x, double y, double z, Entity owner) {
        this(Valorant.PHOENIX_BLAZE_WALL.get(), level);
        this.owner = owner;
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel) {
            AABB area = this.getBoundingBox().inflate(0.8D, 1.5D, 0.8D);
            for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                if (target == this.owner) {
                    target.heal(0.25F);
                } else {
                    target.igniteForTicks(30);
                    target.hurtServer(serverLevel, this.damageSources().onFire(), DAMAGE_PER_TICK);
                }
            }
            serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.7D, this.getZ(), 3, 0.35D, 0.8D, 0.35D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 4, 0.45D, 0.9D, 0.45D, 0.015D);
            serverLevel.sendParticles(ParticleTypes.WHITE_SMOKE, this.getX(), this.getY() + 1.2D, this.getZ(), 2, 0.5D, 0.8D, 0.5D, 0.01D);
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
