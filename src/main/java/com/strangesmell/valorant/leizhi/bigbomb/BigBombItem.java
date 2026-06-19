package com.strangesmell.valorant.leizhi.bigbomb;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

public class BigBombItem extends Item implements ProjectileItem {
    public BigBombItem(Properties properties) {
        super(properties);
    }

    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), Valorant.LEIZHI_SHOWSTOPPER_USE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);

        if (level instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileFromRotation(BigBombEntity::new, serverLevel, itemStack, player, 0.0F, 1.8F, 0.0F);
            player.push(player.getLookAngle().scale(-0.75D).add(0.0D, 0.1D, 0.0D));
            player.hurtMarked = true;
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        itemStack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new BigBombEntity(level, position.x(), position.y(), position.z(), itemStack);
    }
}
