package com.strangesmell.valorant.sage.slow;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SageSlowFieldEntity extends Entity {
    private static final double RADIUS = 4.0D;
    private static final int LIFE_TIME_TICKS = 7 * 20;
    private static final Identifier SLOW_MODIFIER = Identifier.fromNamespaceAndPath(Valorant.MODID, "sage_slow_field");
    private static final AttributeModifier SLOWNESS = new AttributeModifier(SLOW_MODIFIER, -0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final Map<UUID, Integer> SLOW_COUNTS = new HashMap<>();
    private final Set<LivingEntity> slowedTargets = new HashSet<>();

    public SageSlowFieldEntity(EntityType<? extends SageSlowFieldEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public SageSlowFieldEntity(Level level, double x, double y, double z) {
        this(Valorant.SAGE_SLOW_FIELD.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel) {
            AABB area = this.getBoundingBox().inflate(RADIUS, 1.5D, RADIUS);
            Set<LivingEntity> currentTargets = new HashSet<>();
            for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                if (target.distanceToSqr(this.position()) <= RADIUS * RADIUS) {
                    if (!this.slowedTargets.contains(target)) {
                        this.applySlow(target);
                    }
                    currentTargets.add(target);
                }
            }
            this.slowedTargets.removeIf(target -> {
                if (currentTargets.contains(target)) {
                    return false;
                }
                removeSlow(target);
                return true;
            });
            this.slowedTargets.addAll(currentTargets);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 0.1D, this.getZ(), 16, RADIUS * 0.45D, 0.15D, RADIUS * 0.45D, 0.02D);
        }

        if (!this.level().isClientSide() && this.tickCount > LIFE_TIME_TICKS) {
            this.discard();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        this.removeAllSlows();
        super.remove(reason);
    }

    private void applySlow(LivingEntity target) {
        AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            SLOW_COUNTS.merge(target.getUUID(), 1, Integer::sum);
            speed.addOrUpdateTransientModifier(SLOWNESS);
        }
    }

    private static void removeSlow(LivingEntity target) {
        UUID targetId = target.getUUID();
        int remainingFields = SLOW_COUNTS.getOrDefault(targetId, 0) - 1;
        if (remainingFields > 0) {
            SLOW_COUNTS.put(targetId, remainingFields);
            return;
        }

        SLOW_COUNTS.remove(targetId);
        AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SLOW_MODIFIER);
        }
    }

    private void removeAllSlows() {
        for (LivingEntity target : this.slowedTargets) {
            removeSlow(target);
        }
        this.slowedTargets.clear();
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
