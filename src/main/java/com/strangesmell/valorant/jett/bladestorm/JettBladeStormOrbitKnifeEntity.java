package com.strangesmell.valorant.jett.bladestorm;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class JettBladeStormOrbitKnifeEntity extends Entity implements net.minecraft.world.entity.projectile.ItemSupplier {
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = SynchedEntityData.defineId(JettBladeStormOrbitKnifeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_INDEX = SynchedEntityData.defineId(JettBladeStormOrbitKnifeEntity.class, EntityDataSerializers.INT);
    private UUID ownerUuid;
    private int index;

    public JettBladeStormOrbitKnifeEntity(EntityType<? extends JettBladeStormOrbitKnifeEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public JettBladeStormOrbitKnifeEntity(Level level, Player owner, int index) {
        this(VALORANT.JETT_BLADE_STORM_ORBIT_KNIFE.get(), level);
        this.ownerUuid = owner.getUUID();
        this.index = index;
        this.entityData.set(DATA_OWNER_ID, owner.getId());
        this.entityData.set(DATA_INDEX, index);
        this.setPos(owner.getX(), owner.getY() + 1.3D, owner.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            Entity owner = this.level().getEntity(this.entityData.get(DATA_OWNER_ID));
            if (owner == null) {
                return;
            }
            // Store old pos/rot so vanilla lerping can work
            this.setOldPosAndRot(this.position(), this.getYRot(), this.getXRot());
            KnifePose pose = calculateViewPose(owner, 1.0F, this.tickCount);
            this.setPos(pose.position.x, pose.position.y, pose.position.z);
            this.setYRot(pose.yRot);
            this.setXRot(pose.xRot);
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel) || this.ownerUuid == null) {
            return;
        }
        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(this.ownerUuid);
        if (owner == null || owner.isRemoved() || !JettBladeStormTracker.isActiveOrbitKnife(owner, this.getId())) {
            this.discard();
            return;
        }
        this.setPos(owner.getX(), owner.getY() + 1.2D, owner.getZ());
    }

    public KnifePose calculateViewPose(Entity owner, float partialTick, float age) {
        Vec3 forward = owner.getViewVector(partialTick).normalize();
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        if (right.lengthSqr() < 1.0E-4D) {
            right = Vec3.directionFromRotation(0.0F, owner.getYRot(partialTick) + 90.0F);
        }
        right = right.normalize();
        Vec3 up = forward.cross(right).normalize();
        double[] horizontalOffsets = {-0.82D, -0.55D, 0.0D, 0.55D, 0.82D};
        double[] verticalOffsets = {-0.22D, -0.38D, -0.62D, -0.38D, -0.22D};
        int slot = Math.max(0, Math.min(this.entityData.get(DATA_INDEX), horizontalOffsets.length - 1));
        double bob = Math.sin((age + slot * 7.0F) * 0.12D) * 0.018D;
        Vec3 pos = owner.getEyePosition(partialTick)
                .add(forward.scale(1.15D))
                .add(right.scale(horizontalOffsets[slot]))
                .add(up.scale(verticalOffsets[slot] + bob));
        return new KnifePose(
                pos,
                (float)(Math.atan2(forward.y, forward.horizontalDistance()) * 57.295776D),
                (float)(Math.atan2(forward.x, forward.z) * 57.295776D)
        );
    }

    public KnifePose calculateClientRenderPose(float partialTick) {
        Entity owner = this.level().getEntity(this.entityData.get(DATA_OWNER_ID));
        if (owner != null) {
            return this.calculateViewPose(owner, partialTick, this.tickCount + partialTick);
        }
        return new KnifePose(this.getPosition(partialTick), this.getViewXRot(partialTick), this.getViewYRot(partialTick));
    }

    public float getLerpedXRot(float partialTick) {
        return Mth.rotLerp(partialTick, this.xRotO, this.getXRot());
    }

    public float getLerpedYRot(float partialTick) {
        return Mth.rotLerp(partialTick, this.yRotO, this.getYRot());
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.IRON_SWORD);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER_ID, -1);
        builder.define(DATA_INDEX, 0);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        input.getString("Owner").ifPresent(value -> this.ownerUuid = UUID.fromString(value));
        this.index = input.getIntOr("Index", 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.ownerUuid != null) {
            output.putString("Owner", this.ownerUuid.toString());
        }
        output.putInt("Index", this.index);
    }

    public record KnifePose(Vec3 position, float xRot, float yRot) {
    }
}