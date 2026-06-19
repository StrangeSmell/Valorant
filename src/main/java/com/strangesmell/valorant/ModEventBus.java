package com.strangesmell.valorant;

import com.strangesmell.valorant.sage.resurrection.SageResurrectionEntity;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

import static com.strangesmell.valorant.VALORANT.TimePos;

@EventBusSubscriber(modid = VALORANT.MODID)
public class ModEventBus {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (SageResurrectionEntity.shouldAllowDeath(player)) {
            return;
        }
        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            return;
        }

        event.setCanceled(true);
        clearResurrectionMarkers(level, player);
        SageResurrectionEntity downedMarker = new SageResurrectionEntity(level, player.getX(), player.getY() + 0.3D, player.getZ(), player);
        downedMarker.setInvisible(false);
        downedMarker.setNoGravity(false);
        level.addFreshEntity(downedMarker);
        player.setHealth(1.0F);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.setGameMode(GameType.SPECTATOR);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.6D, player.getZ(), 36, 0.4D, 0.6D, 0.4D, 0.03D);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        clearResurrectionMarkers(level, player);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("clean_blockpos")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            if (TimePos.isEmpty()) {
                                context.getSource().sendSuccess(
                                        () -> Component.literal("BlockPos record is empty"),
                                        true
                                );
                                return 1;
                            }
                            List<BlockPos> list = TimePos.keySet().stream().toList();
                            for (BlockPos blockPos : list) {
                                level.destroyBlock(blockPos, false);
                                TimePos.remove(blockPos);
                            }
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Cleaned BlockPos records"),
                                    true
                            );
                            return 1;
                        })
        );
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player && event.getPlacedBlock().is(Blocks.OAK_PLANKS)) {
            TimePos.put(event.getPos(), 0);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (event.getEntity().isSpectator()) return;
        BlockPos blockPos = event.getPos().immutable();
        BlockState state = level.getBlockState(blockPos);
        if (!state.is(Blocks.OAK_PLANKS)) return;

        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) {
            return;
        }

        int i = TimePos.getOrDefault(blockPos, 0);
        if (i == 2) {
            TimePos.remove(blockPos);
            event.getEntity().level().destroyBlock(blockPos, false, event.getEntity());
        } else {
            TimePos.put(blockPos, i + 1);
        }
    }

    private static void clearResurrectionMarkers(ServerLevel currentLevel, Player player) {
        for (ServerLevel level : currentLevel.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof SageResurrectionEntity marker && marker.isOwner(player)) {
                    marker.discard();
                }
            }
        }
    }
}