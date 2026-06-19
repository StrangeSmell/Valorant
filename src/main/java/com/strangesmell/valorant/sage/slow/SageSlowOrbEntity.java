package com.strangesmell.valorant.sage.slow;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class SageSlowOrbEntity extends ThrowableItemProjectile {
    public SageSlowOrbEntity(EntityType<? extends SageSlowOrbEntity> entityType, Level level) {
        super(entityType, level);
    }

    public SageSlowOrbEntity(Level level, LivingEntity owner, ItemStack item) {
        super(VALORANT.SAGE_SLOW_ORB.get(), owner, level, item);
    }

    public SageSlowOrbEntity(Level level, double x, double y, double z, ItemStack item) {
        super(VALORANT.SAGE_SLOW_ORB.get(), x, y, z, level, item);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
        if (!this.level().isClientSide() && this.tickCount > 80) {
            this.spawnField();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    public ItemStack getRenderItem() {
        return new ItemStack(getDefaultItem());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.spawnField();
        }
    }

    private void spawnField() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(new SageSlowFieldEntity(serverLevel, this.getX(), this.getY(), this.getZ()));
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), VALORANT.SAGE_SLOW_ORB_DISAPPEAR.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 80, 1.8D, 0.4D, 1.8D, 0.05D);
        }
        this.discard();
    }

    private ParticleOptions getParticle() {
        return new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(this.getItem()));
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 3) {
            ParticleOptions particle = this.getParticle();
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(particle, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
        super.handleEntityEvent(event);
    }
}
