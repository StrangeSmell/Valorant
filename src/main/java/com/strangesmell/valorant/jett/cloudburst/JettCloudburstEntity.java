package com.strangesmell.valorant.jett.cloudburst;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class JettCloudburstEntity extends ThrowableItemProjectile {
    private static final int GUIDE_TIME = 50;
    private static final double GUIDE_SPEED = 0.95D;
    private boolean spawned;

    public JettCloudburstEntity(EntityType<? extends JettCloudburstEntity> entityType, Level level) {
        super(entityType, level);
    }

    public JettCloudburstEntity(Level level, LivingEntity owner, ItemStack item) {
        super(VALORANT.JETT_CLOUDBURST.get(), owner, level, item);
    }

    public JettCloudburstEntity(Level level, double x, double y, double z, ItemStack item) {
        super(VALORANT.JETT_CLOUDBURST.get(), x, y, z, level, item);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            Vec3 motion = this.getDeltaMovement();
            this.level().addParticle(VALORANT.JETT_CLOUD_TRAIL_PARTICLE.get(), this.getX(), this.getY(), this.getZ(), -motion.x, -motion.y, -motion.z);
        }
        if (!this.level().isClientSide() && this.tickCount > 70) {
            this.spawnSmoke();
        }
    }

    public boolean isGuidable() {
        return this.tickCount <= GUIDE_TIME && !this.isRemoved();
    }    public static void guideOwned(ServerLevel level, ServerPlayer player) {
        for (JettCloudburstEntity cloudburst : level.getEntitiesOfClass(JettCloudburstEntity.class, player.getBoundingBox().inflate(18.0D), entity -> entity.getOwner() == player && entity.isGuidable())) {
            cloudburst.guideBy(player);
        }
    }



    public void guideBy(LivingEntity owner) {
        if (!this.isGuidable()) {
            return;
        }
        Vec3 look = owner.getLookAngle().normalize();
        this.setDeltaMovement(look.scale(GUIDE_SPEED));
        
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
            this.spawnSmoke();
        }
    }

    private void spawnSmoke() {
        if (this.spawned) {
            return;
        }
        this.spawned = true;
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(new JettCloudburstSmokeEntity(serverLevel, this.getX(), this.getY(), this.getZ()));
            serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 40, 1.2D, 0.6D, 1.2D, 0.04D);
        }
        this.discard();
    }
}
