package com.strangesmell.valorant.sage.barrier;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class SageBarrierItem extends Item {
    private static final double CAST_RANGE = 7.0D;

    public SageBarrierItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            BlockPos center = this.findBarrierCenter(player);
            this.buildBarrier(serverLevel, player, center);
            itemStack.consume(1, player);
        }

        level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), Valorant.SAGE_BARRIER_ORB_USE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    private BlockPos findBarrierCenter(Player player) {
        HitResult result = player.pick(CAST_RANGE, 0.0F, false);
        Vec3 target = result.getLocation();
        if (target.distanceToSqr(player.getEyePosition()) > CAST_RANGE * CAST_RANGE) {
            target = player.getEyePosition().add(player.getLookAngle().normalize().scale(CAST_RANGE));
        }

        if (result instanceof BlockHitResult blockHitResult && result.getType() != HitResult.Type.MISS) {
            return blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
        }
        return BlockPos.containing(target);
    }

    private void buildBarrier(ServerLevel level, Player player, BlockPos center) {
        Direction side = player.getDirection().getClockWise();
        BlockState ice = Blocks.BLUE_ICE.defaultBlockState();
        List<BlockPos> placed = new ArrayList<>();
        for (int width = -2; width <= 2; width++) {
            for (int height = 0; height < 3; height++) {
                BlockPos pos = center.relative(side, width).above(height);
                if (level.getBlockState(pos).canBeReplaced()) {
                    level.setBlock(pos, ice, 3);
                    placed.add(pos.immutable());
                }
            }
        }
        if (!placed.isEmpty()) {
            level.addFreshEntity(new SageBarrierEntity(level, placed));
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.getX() + 0.5D, center.getY() + 1.5D, center.getZ() + 0.5D, 40, 2.5D, 1.5D, 0.4D, 0.05D);
    }
}
