package com.strangesmell.valorant.clove.ruse;

import com.strangesmell.valorant.Valorant;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CloveRuseSmokeEntity extends Entity {
    public static final int ALIVE_LIFE_TIME = 280;
    public static final int DEATH_LIFE_TIME = 120;
    private static final EntityDataAccessor<Integer> DATA_LIFE_TIME = SynchedEntityData.defineId(CloveRuseSmokeEntity.class, EntityDataSerializers.INT);
    private static final double RADIUS = 4.8D;
    private static final int SURFACE_PARTICLES = 8;

    public CloveRuseSmokeEntity(EntityType<? extends CloveRuseSmokeEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public CloveRuseSmokeEntity(Level level, double x, double y, double z) {
        this(level, x, y, z, ALIVE_LIFE_TIME);
    }

    public CloveRuseSmokeEntity(Level level, double x, double y, double z, int lifeTime) {
        this(Valorant.CLOVE_RUSE_SMOKE.get(), level);
        this.setPos(x, y, z);
        this.entityData.set(DATA_LIFE_TIME, lifeTime);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < SURFACE_PARTICLES; i++) {
                double theta = this.random.nextDouble() * Math.PI * 2.0D;
                double y = this.random.nextDouble() * 2.0D - 1.0D;
                double horizontal = Math.sqrt(1.0D - y * y);
                double x = Math.cos(theta) * horizontal;
                double z = Math.sin(theta) * horizontal;
                double shell = RADIUS + (this.random.nextDouble() - 0.5D) * 0.8D;
                double px = this.getX() + x * shell;
                double py = this.getY() + 1.1D + y * RADIUS * 0.85D;
                double pz = this.getZ() + z * shell;
                serverLevel.sendParticles(Valorant.CLOVE_RUSE_SMOKE_PARTICLE.get(), px, py, pz, 1, x * 0.015D, y * 0.008D, z * 0.015D, 0.0D);
            }
        }
        if (!this.level().isClientSide() && this.tickCount > this.entityData.get(DATA_LIFE_TIME)) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LIFE_TIME, ALIVE_LIFE_TIME);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.entityData.set(DATA_LIFE_TIME, input.getIntOr("LifeTime", ALIVE_LIFE_TIME));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("LifeTime", this.entityData.get(DATA_LIFE_TIME));
    }
}
