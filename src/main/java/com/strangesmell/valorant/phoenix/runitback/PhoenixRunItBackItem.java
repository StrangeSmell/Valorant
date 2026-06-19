package com.strangesmell.valorant.phoenix.runitback;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PhoenixRunItBackItem extends Item {
    public PhoenixRunItBackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            if (!PhoenixRunItBackTracker.returnToAnchor(serverLevel, player)) {
                PhoenixRunItBackTracker.mark(serverLevel, player);
                level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), VALORANT.PHOENIX_RUN_IT_BACK_USE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }else{
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_DEATH, SoundSource.PLAYERS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) { itemStack.shrink(1); }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }
}
