package com.strangesmell.valorant.sage.barrier;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class SageBarrierEntity extends Entity {
    private static final int LIFE_TIME_TICKS = 40 * 20;
    private static final int ARM_TIME_TICKS = 2 * 20;
    private static final float INITIAL_HEALTH = 80.0F;
    private static final float ARMED_HEALTH = 120.0F;
    private static final EntityDataAccessor<Float> DATA_HEALTH = SynchedEntityData.defineId(SageBarrierEntity.class, EntityDataSerializers.FLOAT);
    private final List<BlockPos> blocks = new ArrayList<>();
    private boolean removedBarrier;

    public SageBarrierEntity(EntityType<? extends SageBarrierEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public SageBarrierEntity(Level level, List<BlockPos> blocks) {
        this(Valorant.SAGE_BARRIER.get(), level);
        this.blocks.addAll(blocks);
        if (!blocks.isEmpty()) {
            this.setPos(blocks.getFirst().getCenter());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount == ARM_TIME_TICKS) {
            this.entityData.set(DATA_HEALTH, ARMED_HEALTH);
        }
        if (!this.level().isClientSide() && this.tickCount >= LIFE_TIME_TICKS) {
            this.removeBarrier();
            this.discard();
        }
    }

    private void removeBarrier() {
        if (this.removedBarrier) {
            return;
        }
        this.removedBarrier = true;
        if (!this.blocks.isEmpty()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), Valorant.SAGE_BARRIER_ORB_DISAPPEAR.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        for (BlockPos pos : this.blocks) {
            if (this.level().getBlockState(pos).is(Blocks.BLUE_ICE)) {
                this.level().removeBlock(pos, false);
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HEALTH, INITIAL_HEALTH);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        float health = this.entityData.get(DATA_HEALTH) - amount;
        this.entityData.set(DATA_HEALTH, health);
        if (health <= 0.0F) {
            this.removeBarrier();
            this.discard();
        }
        return true;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
