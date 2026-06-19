package com.strangesmell.valorant.sage.heal;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SageHealItem extends Item {
    private static final double RANGE = 17.0D;
    private static final float ALLY_HEAL = 20.0F;
    private static final float SELF_HEAL = 10.0F;
    private static final int HEAL_TIME_TICKS = 5 * 20;
    private static final int COOLDOWN_TICKS = 45 * 20;

    public SageHealItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            LivingEntity target = this.findTarget(serverLevel, player);
            if (target == null) {
                target = player;
            }

            float healAmount = target == player ? SELF_HEAL : ALLY_HEAL;
            target.heal(healAmount);
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, HEAL_TIME_TICKS, target == player ? 0 : 1));
            player.getCooldowns().addCooldown(itemStack, COOLDOWN_TICKS);
            serverLevel.sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + 1.0D, target.getZ(), 14, 0.6D, 0.8D, 0.6D, 0.04D);
        }

        level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), VALORANT.SAGE_HEALING_ORB_USE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    private LivingEntity findTarget(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(RANGE));
        AABB search = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0D);
        EntityHitResult result = ProjectileUtil.getEntityHitResult(level, player, eye, end, search, entity -> entity instanceof LivingEntity && entity != player && entity.isAlive(), 0.3F);
        if (result != null && result.getEntity() instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }
}
