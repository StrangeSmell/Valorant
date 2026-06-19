package com.strangesmell.valorant.clove.notdeadyet;

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
import net.minecraft.network.chat.Component;

public class CloveNotDeadYetItem extends Item {
    public CloveNotDeadYetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            player.sendSystemMessage(Component.translatable("message.valorant.clove_not_dead_yet.death_only"));
        }

        level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), Valorant.CLOVE_NOT_DEAD_YET_USE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }
}
