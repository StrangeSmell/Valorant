package com.strangesmell.valorant.phoenix.hothands;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PhoenixHotHandsEntity extends ThrowableItemProjectile {
    private boolean spawnedZone;

    public PhoenixHotHandsEntity(EntityType<? extends PhoenixHotHandsEntity> entityType, Level level) {
        super(entityType, level);
    }

    public PhoenixHotHandsEntity(Level level, LivingEntity owner, ItemStack item) {
        super(VALORANT.PHOENIX_HOT_HANDS.get(), owner, level, item);
    }

    public PhoenixHotHandsEntity(Level level, double x, double y, double z, ItemStack item) {
        super(VALORANT.PHOENIX_HOT_HANDS.get(), x, y, z, level, item);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
        if (!this.level().isClientSide() && this.tickCount > 30) {
            this.spawnZoneAt(this.findSurfacePosition());
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    public ItemStack getRenderItem() {
        return new ItemStack(getDefaultItem());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            if (result instanceof BlockHitResult blockHitResult) {
                this.spawnZoneAt(Vec3.atBottomCenterOf(blockHitResult.getBlockPos().above()));
            } else {
                this.spawnZoneAt(this.position());
            }
        }
    }

    private Vec3 findSurfacePosition() {
        BlockPos pos = this.blockPosition();
        // Search down from current position to find solid ground
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        int worldBottom = this.level().getMinY();
        while (mutable.getY() > worldBottom) {
            if (!this.level().getBlockState(mutable).isAir()) {
                return Vec3.atBottomCenterOf(mutable.above());
            }
            mutable.move(0, -1, 0);
        }
        // Fallback: use current XZ at world minimum Y + 1
        return new Vec3(this.getX(), worldBottom + 1, this.getZ());
    }

    private void spawnZoneAt(Vec3 position) {
        if (this.spawnedZone) {
            return;
        }
        this.spawnedZone = true;
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(new PhoenixHotHandsZoneEntity(serverLevel, position.x, position.y, position.z, this.getOwner()));
            serverLevel.sendParticles(ParticleTypes.FLAME, position.x, position.y, position.z, 60, 1.2D, 0.4D, 1.2D, 0.08D);
        }
        this.discard();
    }
}
