package com.strangesmell.valorant.phoenix.hothands;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PhoenixHotHandsItem extends Item implements ProjectileItem {
    public PhoenixHotHandsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileFromRotation(PhoenixHotHandsEntity::new, serverLevel, itemStack, player, 0.0F, 0.95F, 0.0F);
        }

        level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), VALORANT.PHOENIX_HOT_HANDS_USE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        if (!player.getAbilities().instabuild) { itemStack.shrink(1); }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        PhoenixHotHandsEntity entity = new PhoenixHotHandsEntity(level, position.x(), position.y(), position.z(), itemStack);
        entity.setDeltaMovement(new Vec3(direction.step()).scale(0.4D));
        return entity;
    }
}
