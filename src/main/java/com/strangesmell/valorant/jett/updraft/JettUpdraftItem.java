package com.strangesmell.valorant.jett.updraft;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.Vec3;

public class JettUpdraftItem extends Item {
    public JettUpdraftItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, 1.3D, motion.z);
            player.hurtMarked = true;
            JettWindTracker.grantFallProtection(player, serverLevel.getGameTime() + 12L);
            serverLevel.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 36, 0.6D, 0.2D, 0.6D, 0.08D);
        }
        itemStack.consume(1, player);
        level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), Valorant.JETT_UPDRAFT_USE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }
}
