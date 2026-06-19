package com.strangesmell.valorant.leizhi.boombot;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BoomBotItem extends Item {
    public BoomBotItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        level.playSound(player, player.getX(), player.getY(), player.getZ(), VALORANT.LEIZHI_BOOM_BOT_USE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 look = player.getLookAngle();
            BoomBotEntity bot = new BoomBotEntity(serverLevel, player, itemStack);
            bot.setPos(player.getX(), player.getEyeY() - 0.35D, player.getZ());
            bot.setDeltaMovement(look.normalize().scale(0.05D));
            level.addFreshEntity(bot);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        itemStack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
