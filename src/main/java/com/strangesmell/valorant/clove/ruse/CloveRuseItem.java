package com.strangesmell.valorant.clove.ruse;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.strangesmell.valorant.clove.ruse.CloveRuseScreen.openRuseScreen;


public class CloveRuseItem extends Item {
    public CloveRuseItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), VALORANT.CLOVE_RUSE_USE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        if (level.isClientSide()) {
            openRuseScreen();
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }


}
