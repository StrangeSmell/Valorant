package com.strangesmell.valorant.clove.pickmeup;

import com.strangesmell.valorant.Valorant;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClovePickMeUpItem extends Item {
    public ClovePickMeUpItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            ClovePickMeUpTracker.arm(serverLevel, player);
        }
        itemStack.consume(1, player);
        level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), Valorant.CLOVE_PICK_ME_UP_USE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }
}
