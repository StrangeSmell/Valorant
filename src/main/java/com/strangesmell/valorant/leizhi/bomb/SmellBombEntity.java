package com.strangesmell.valorant.leizhi.bomb;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SmellBombEntity extends ThrowableItemProjectile {
    private static final double EXPLOSION_RADIUS = 2.1D;
    private static final float MIN_DAMAGE = 0.2F;
    private static final float MAX_DAMAGE = 7.0F;
    protected static final EntityDataAccessor<Float> DATA_DISTANCE = SynchedEntityData.defineId(SmellBombEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Boolean> DATA_HAVE = SynchedEntityData.defineId(SmellBombEntity.class, EntityDataSerializers.BOOLEAN);

    public SmellBombEntity(EntityType<? extends BombEntity> p_37391_, Level p_37392_) {
        super(p_37391_, p_37392_);
    }

    public SmellBombEntity(Level level, LivingEntity owner, ItemStack item) {
        super(Valorant.LEIZHIBOMB.get(), owner, level, item);
    }

    public SmellBombEntity(Level level, double x, double y, double z, ItemStack item) {
        super(Valorant.LEIZHIBOMB.get(), x, y, z, level, item);
    }

    public void tick() {
        super.tick();
        if(level().isClientSide()){
            level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT,  0xFFFFFF00), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);
            level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT,  0xFFFFFF00), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);

        }
        setDis(getDis() + 1);
        if(!this.level().isClientSide() && getDis() > 20 && !this.getHave()) {
            this.setHave(true);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), Valorant.LEIZHI_PAINT_SHELLS_EXPLODE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            this.damageInRadius(EXPLOSION_RADIUS, MAX_DAMAGE);
            this.discard();
        }
    }

    protected Item getDefaultItem() {
        return Valorant.LEIZHIBOMB_ITEM.get();
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_DISTANCE, 0f);
        pBuilder.define(DATA_HAVE, false);
    }

    public float getDis() {
        return  this.entityData.get(DATA_DISTANCE);
    }

    public void setDis(float dis) {
        this.entityData.set(DATA_DISTANCE, dis);
    }
    public boolean getHave() {
        return  this.entityData.get(DATA_HAVE);
    }

    public void setHave(boolean have) {
        this.entityData.set(DATA_HAVE, have);
    }

    public boolean canHitEntity(Entity target) {
        return false;
    }

    protected void damageInRadius(double radius, float maxDamage) {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        Vec3 center = this.position();
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius), target -> target.isAlive() && target != this.getOwner())) {
            double distance = Math.sqrt(target.distanceToSqr(center));
            if (distance > radius) {
                continue;
            }
            double strength = 1.0D - distance / radius;
            float damage = (float)(MIN_DAMAGE + (maxDamage - MIN_DAMAGE) * strength);
            target.hurtServer(serverLevel, this.damageSources().explosion(this, this.getOwner()), damage);
        }
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 3, 0.5D, 0.35D, 0.5D, 0.04D);
        serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 16, 0.7D, 0.45D, 0.7D, 0.06D);
    }

    @Override
    public void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide()) {
            return;
        }
        Direction direction = result.getDirection();
        Vec3 vec3 = this.getDeltaMovement();
        SmellBombEntity bomb = new SmellBombEntity(level(), this.getX(), this.getY()+0.1, this.getZ(), new ItemStack(Valorant.LEIZHIBOMB_ITEM.get()));
        bomb.setDis(this.getDis()+5);
        bomb.setHave(this.getHave());
        this.discard();
        if(direction == Direction.UP ||direction == Direction.DOWN){
            bomb.setDeltaMovement(vec3.add(0, vec3.y * -1.5, 0));
        }else if (direction == Direction.EAST || direction == Direction.WEST){
            bomb.setDeltaMovement(vec3.add(vec3.x * -1.5, 0, 0));
        } else if (direction == Direction.NORTH || direction == Direction.SOUTH){
            bomb.setDeltaMovement(vec3.add(0, 0, vec3.z * -1.5));
        }
        level().addFreshEntity(bomb);
    }



}
