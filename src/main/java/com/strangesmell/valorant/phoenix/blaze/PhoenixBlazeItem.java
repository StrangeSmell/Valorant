package com.strangesmell.valorant.phoenix.blaze;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhoenixBlazeItem extends Item {
    private static final int WALL_LENGTH = 15;
    private static final Map<UUID, WallState> ACTIVE_WALLS = new HashMap<>();

    public PhoenixBlazeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Vec3 direction = horizontalLook(player);
            WallState state = new WallState(player.position().add(direction.scale(1.5D)), direction);
            ACTIVE_WALLS.put(player.getUUID(), state);
            spawnNextWall(serverLevel, player, state);
        }

        level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), VALORANT.PHOENIX_BLAZE_USE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(this));
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel serverLevel) || !(livingEntity instanceof Player player)) {
            return;
        }
        WallState state = ACTIVE_WALLS.get(player.getUUID());
        if (state == null) {
            return;
        }
        if (state.count >= WALL_LENGTH) {
            ACTIVE_WALLS.remove(player.getUUID());
            return;
        }
        state.direction = horizontalLook(player);
        state.nextPosition = state.nextPosition.add(state.direction);
        spawnNextWall(serverLevel, player, state);
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (level instanceof ServerLevel serverLevel && livingEntity instanceof Player player) {
            WallState state = ACTIVE_WALLS.remove(player.getUUID());
            if (state != null) {
                while (state.count < WALL_LENGTH) {
                    state.nextPosition = state.nextPosition.add(state.direction);
                    spawnNextWall(serverLevel, player, state);
                }
            }
        }
        if (livingEntity instanceof Player player && !player.getAbilities().instabuild) { itemStack.shrink(1); }
        return true;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 72000;
    }

        public static void startWall(ServerLevel level, Player player) {
        Vec3 direction = horizontalLook(player);
        WallState state = new WallState(player.position().add(direction.scale(1.5D)), direction);
        ACTIVE_WALLS.put(player.getUUID(), state);
        spawnNextWall(level, player, state);
    }

    public static boolean tickWall(ServerLevel level, Player player, Item item) {
        WallState state = ACTIVE_WALLS.get(player.getUUID());
        if (state == null) {
            return false;
        }
        if (state.count >= WALL_LENGTH) {
            ACTIVE_WALLS.remove(player.getUUID());
            return false;
        }
        state.direction = horizontalLook(player);
        state.nextPosition = state.nextPosition.add(state.direction);
        spawnNextWall(level, player, state);
        if (state.count >= WALL_LENGTH) {
            ACTIVE_WALLS.remove(player.getUUID());
            return false;
        }
        return true;
    }

    public static void releaseWall(ServerLevel level, Player player, Item item) {
        WallState state = ACTIVE_WALLS.remove(player.getUUID());
        if (state != null) {
            while (state.count < WALL_LENGTH) {
                state.nextPosition = state.nextPosition.add(state.direction);
                spawnNextWall(level, player, state);
            }
        }
    }

    public static boolean isWallActive(Player player) {
        return ACTIVE_WALLS.containsKey(player.getUUID());
    }

private static void spawnNextWall(ServerLevel level, Player player, WallState state) {
        level.addFreshEntity(new PhoenixBlazeWallEntity(level, state.nextPosition.x, state.nextPosition.y, state.nextPosition.z, player));
        state.count++;
    }

    private static Vec3 horizontalLook(Player player) {
        Vec3 direction = new Vec3(player.getLookAngle().x, 0.0D, player.getLookAngle().z);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = Vec3.directionFromRotation(0.0F, player.getYRot());
        }
        return direction.normalize();
    }

    private static final class WallState {
        private Vec3 nextPosition;
        private Vec3 direction;
        private int count;

        private WallState(Vec3 nextPosition, Vec3 direction) {
            this.nextPosition = nextPosition;
            this.direction = direction;
        }
    }
}
