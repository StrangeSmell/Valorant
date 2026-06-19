package com.strangesmell.valorant.leizhi.bomb;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BombEntity extends ThrowableItemProjectile {
    private static final double EXPLOSION_RADIUS = 3.2D;
    private static final float MIN_DAMAGE = 0.2F;
    private static final float MAX_DAMAGE = 11.0F;

    protected static final EntityDataAccessor<Float> DATA_DISTANCE = SynchedEntityData.defineId(BombEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Boolean> DATA_HAVE = SynchedEntityData.defineId(BombEntity.class, EntityDataSerializers.BOOLEAN);

    public BombEntity(EntityType<? extends BombEntity> p_37391_, Level p_37392_) {
        super(p_37391_, p_37392_);
    }

    public BombEntity(Level level, LivingEntity owner, ItemStack item) {
        super(VALORANT.LEIZHIBOMB.get(), owner, level, item);
    }

    public BombEntity(Level level, double x, double y, double z, ItemStack item) {
        super(VALORANT.LEIZHIBOMB.get(), x, y, z, level, item);
    }

    public void tick() {
        super.tick();
        if(level().isClientSide()){
            level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT,  0xFFFFFF00), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);
            level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT,  0xFFFFFF00), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);
        }
        setDis(getDis() + 1);
        if(!this.level().isClientSide() && getDis() > 40 && !this.getHave()) {
            this.setHave(true);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), VALORANT.LEIZHI_PAINT_SHELLS_EXPLODE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            this.damageInRadius(EXPLOSION_RADIUS, MAX_DAMAGE);

            //闂傚倸鍊风粈渚€骞夐垾鎰佹綎缂備焦蓱閸欏繘鏌熺紒銏犳灈闁活厽顨婇弻娑㈠焺閸愵亖妲堥梺缁樺姇閿曘儳鎹㈠☉銏犵闁绘垵妫欓悿渚€姊洪幖鐐测偓鏇㈩敄婢舵劕绠栨俊顖濇硶閻も偓闂佽鍎虫晶搴敊婵犲洦鈷戠紒瀣儥閸庡秹鏌涢妸銉хШ妞?

            if(level() instanceof ServerLevel serverLevel){
                SmellBombEntity smellBombEntity1 = new SmellBombEntity(serverLevel, this.getX(), this.getY(), this.getZ(), new ItemStack(VALORANT.LEIZHIBOMB_ITEM.get()));
                level().addFreshEntity(smellBombEntity1);
                smellBombEntity1.setDeltaMovement(0.25, 0.25, 0.25);

                SmellBombEntity smellBombEntity2 = new SmellBombEntity(serverLevel, this.getX(), this.getY(), this.getZ(), new ItemStack(VALORANT.LEIZHIBOMB_ITEM.get()));
                level().addFreshEntity(smellBombEntity2);
                smellBombEntity2.setDeltaMovement(-0.25, 0.25, 0.25);

                SmellBombEntity smellBombEntity3 = new SmellBombEntity(serverLevel, this.getX(), this.getY(), this.getZ(), new ItemStack(VALORANT.LEIZHIBOMB_ITEM.get()));
                level().addFreshEntity(smellBombEntity3);
                smellBombEntity3.setDeltaMovement(0.25, 0.25, -0.25);

                SmellBombEntity smellBombEntity4 = new SmellBombEntity(serverLevel, this.getX(), this.getY(), this.getZ(), new ItemStack(VALORANT.LEIZHIBOMB_ITEM.get()));
                level().addFreshEntity(smellBombEntity4);
                smellBombEntity4.setDeltaMovement(-0.25, 0.25, -0.25);
            }
            this.discard();

        }

    }

    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    public ItemStack getRenderItem() {
        return new ItemStack(getDefaultItem());
    }


    private ParticleOptions getParticle() {
        ItemStack itemstack = this.getItem();
        return (itemstack.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(itemstack)));
    }

    public void handleEntityEvent(byte p_37402_) {
        if (p_37402_ == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for(int i = 0; i < 8; ++i) {
                this.level().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }

    }

    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        int i = entity instanceof Blaze ? 3 : 0;
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), (float)i);
    }



    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }

    public boolean canHitEntity(Entity target) {
        return false;
    }

    protected void damageInRadius(double radius, float maxDamage) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
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
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 5, 0.8D, 0.5D, 0.8D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 30, 1.0D, 0.7D, 1.0D, 0.08D);
    }

    @Override
    public void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide()) {
            return;
        }
        Direction direction = result.getDirection();
        Vec3 vec3 = this.getDeltaMovement();
        BombEntity bomb = new BombEntity(level(), this.getX(), this.getY()+0.1, this.getZ(), new ItemStack(VALORANT.LEIZHIBOMB_ITEM.get()));
        bomb.setDis(this.getDis()+5);
        bomb.setHave(this.getHave());
        this.discard();
        if(direction == Direction.UP || direction == Direction.DOWN){
            bomb.setDeltaMovement(vec3.add(0, vec3.y * -1.3, 0));
        }else if (direction == Direction.EAST || direction == Direction.WEST){
            bomb.setDeltaMovement(vec3.add(vec3.x * -1.3, 0, 0));
        } else if (direction == Direction.NORTH || direction == Direction.SOUTH){
            bomb.setDeltaMovement(vec3.add(0, 0, vec3.z * -1.3));
        }
        level().addFreshEntity(bomb);
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
}
