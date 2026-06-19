package com.strangesmell.valorant.jett.bladestorm;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class JettBladeStormKnifeEntity extends ThrowableItemProjectile {
    private static final float BODY_DAMAGE = 10.0F;
    private static final float HEAD_DAMAGE = 30.0F;
    private static final float LEG_DAMAGE = 8.4F;
    public JettBladeStormKnifeEntity(EntityType<? extends JettBladeStormKnifeEntity> entityType, Level level) {
        super(entityType, level);
    }

    public JettBladeStormKnifeEntity(Level level, LivingEntity owner, ItemStack item) {
        super(VALORANT.JETT_BLADE_STORM_KNIFE.get(), owner, level, item);
        this.setNoGravity(true);
    }

    public JettBladeStormKnifeEntity(Level level, double x, double y, double z, ItemStack item) {
        super(VALORANT.JETT_BLADE_STORM_KNIFE.get(), x, y, z, level, item);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (this.getDeltaMovement().lengthSqr() > 1.0E-4D) {
            Vec3 motion = this.getDeltaMovement();
            this.setYRot((float)(Math.atan2(motion.x, motion.z) * 57.295776D));
            this.setXRot((float)(Math.atan2(motion.y, motion.horizontalDistance()) * 57.295776D));
        }
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
        if (!this.level().isClientSide() && this.tickCount > 80) {
            this.discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.IRON_SWORD;
    }
    @Override
    public ItemStack getItem() {
        return new ItemStack(getDefaultItem());
    }
    public ItemStack getRenderItem() {
        return new ItemStack(getDefaultItem());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        float damage = this.damageForHit(target);
        if (this.level() instanceof ServerLevel serverLevel) {
            target.hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), damage);
            serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 18, 0.2D, 0.2D, 0.2D, 0.1D);
        }
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    private float damageForHit(Entity target) {
        if (target instanceof Blaze) {
            return 16.0F;
        }
        double relativeY = this.getY() - target.getY();
        double height = Math.max(0.1D, target.getBbHeight());
        if (relativeY >= height * 0.82D) {
            return HEAD_DAMAGE;
        }
        if (relativeY <= height * 0.35D) {
            return LEG_DAMAGE;
        }
        return BODY_DAMAGE;
    }
}
