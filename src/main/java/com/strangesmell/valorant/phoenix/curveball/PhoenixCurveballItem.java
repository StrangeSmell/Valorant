package com.strangesmell.valorant.phoenix.curveball;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class PhoenixCurveballItem extends Item {
    private static final int GUIDE_TIME = 18;

    public PhoenixCurveballItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileFromRotation(PhoenixCurveballEntity::new, serverLevel, itemStack, player, 0.0F, 1.1F, 0.0F);
        }

        level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), VALORANT.PHOENIX_CURVEBALL_USE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(this));
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel) || !(livingEntity instanceof Player player)) {
            return;
        }
        int usedTicks = this.getUseDuration(itemStack, livingEntity) - remainingUseDuration;
        if (usedTicks > GUIDE_TIME) {
            return;
        }

        AABB area = player.getBoundingBox().inflate(12.0D);
        for (PhoenixCurveballEntity curveball : level.getEntitiesOfClass(PhoenixCurveballEntity.class, area, entity -> entity.getOwner() == player && entity.isGuidable())) {
            curveball.guideBy(player);
        }
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 72000;
    }
}
