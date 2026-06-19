package com.strangesmell.valorant.phoenix.runitback;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class PhoenixRunItBackAnchorEntity extends Entity {
    public static final int LIFE_TIME = 200;
    private UUID ownerUuid;

    public PhoenixRunItBackAnchorEntity(EntityType<? extends PhoenixRunItBackAnchorEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public PhoenixRunItBackAnchorEntity(Level level, Player owner) {
        this(Valorant.PHOENIX_RUN_IT_BACK_ANCHOR.get(), level);
        this.ownerUuid = owner.getUUID();
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, this.getX(), this.getY() + 0.05D, this.getZ(), 2, 0.28D, 0.03D, 0.28D, 0.01D);
        }
        if (!this.level().isClientSide() && this.tickCount > LIFE_TIME) {
            if (this.level() instanceof ServerLevel serverLevel && this.ownerUuid != null && serverLevel.getPlayerByUUID(this.ownerUuid) instanceof Player player) {
                PhoenixRunItBackTracker.returnToAnchor(serverLevel, player);
            } else {
                this.discard();
            }
        }
    }

    public boolean isOwner(Player player) {
        return this.ownerUuid != null && this.ownerUuid.equals(player.getUUID());
    }

    public Vec3 returnPosition() {
        return this.position();
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
        Optional<String> owner = input.getString("Owner");
        owner.ifPresent(value -> this.ownerUuid = UUID.fromString(value));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.ownerUuid != null) {
            output.putString("Owner", this.ownerUuid.toString());
        }
    }
}
