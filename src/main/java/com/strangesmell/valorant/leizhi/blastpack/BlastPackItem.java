package com.strangesmell.valorant.leizhi.blastpack;

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
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BlastPackItem extends Item implements ProjectileItem {
    private static final int USE_COOLDOWN_TICKS = 5;

    public BlastPackItem(Properties properties) {
        super(properties);
    }

    public static boolean hasOwnedPacks(ServerLevel level, Player player) {
        return !level.getEntitiesOfClass(BlastPackEntity.class, new net.minecraft.world.phys.AABB(player.blockPosition()).inflate(64.0D), pack -> pack.isOwnedBy(player)).isEmpty();
    }


    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        }

        if (level instanceof ServerLevel serverLevel) {

            List<BlastPackEntity> packs = serverLevel.getEntitiesOfClass(BlastPackEntity.class, new AABB(player.blockPosition()).inflate(64.0D), pack -> pack.isOwnedBy(player));
            if (!packs.isEmpty()) {
                packs.forEach(BlastPackEntity::explode);
                player.getCooldowns().addCooldown(itemStack, USE_COOLDOWN_TICKS);
                player.awardStat(Stats.ITEM_USED.get(this));
                itemStack.consume(1, player);
                return InteractionResult.SUCCESS;
            }else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), Valorant.LEIZHI_BLAST_PACK_USE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);

            }
            Projectile.spawnProjectileFromRotation(BlastPackEntity::new, serverLevel, itemStack, player, 0.0F, 0.85F, 0.0F);
            player.getCooldowns().addCooldown(itemStack, USE_COOLDOWN_TICKS);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new BlastPackEntity(level, position.x(), position.y(), position.z(), itemStack);
    }
}
