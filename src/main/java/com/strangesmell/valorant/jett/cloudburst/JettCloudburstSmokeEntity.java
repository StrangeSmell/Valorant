package com.strangesmell.valorant.jett.cloudburst;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class JettCloudburstSmokeEntity extends Entity {
    private static final int LIFE_TIME = 50;
    private static final double RADIUS = 3.6D;

    public JettCloudburstSmokeEntity(EntityType<? extends JettCloudburstSmokeEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public JettCloudburstSmokeEntity(Level level, double x, double y, double z) {
        this(Valorant.JETT_CLOUDBURST_SMOKE.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.9D, this.getZ(), 18, RADIUS * 0.45D, 0.9D, RADIUS * 0.45D, 0.01D);
            serverLevel.sendParticles(ParticleTypes.WHITE_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 6, RADIUS * 0.35D, 0.8D, RADIUS * 0.35D, 0.0D);
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
