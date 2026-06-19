package com.strangesmell.valorant.clove.ruse;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = Valorant.MODID)
public final class CloveRuseTracker {
    private static final double MAX_RANGE = 64.0D;
    private static final long WINDUP = 20L;
    private static final List<PendingSmoke> PENDING = new ArrayList<>();

    private CloveRuseTracker() {
    }

    public static void queue(ServerLevel level, ServerPlayer player, double offsetX, double offsetZ) {
        double distanceSqr = offsetX * offsetX + offsetZ * offsetZ;
        if (distanceSqr > MAX_RANGE * MAX_RANGE) {
            double scale = MAX_RANGE / Math.sqrt(distanceSqr);
            offsetX *= scale;
            offsetZ *= scale;
        }
        PENDING.add(new PendingSmoke(level.dimension().toString(), player.getX() + offsetX, player.getZ() + offsetZ, CloveRuseSmokeEntity.ALIVE_LIFE_TIME, level.getGameTime() + WINDUP));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || PENDING.isEmpty()) {
            return;
        }
        long time = level.getGameTime();
        Iterator<PendingSmoke> iterator = PENDING.iterator();
        while (iterator.hasNext()) {
            PendingSmoke smoke = iterator.next();
            if (smoke.spawnAt > time || !smoke.dimension.equals(level.dimension().toString())) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(smoke.x, 0.0D, smoke.z)) + 1;
            level.addFreshEntity(new CloveRuseSmokeEntity(level, smoke.x, y, smoke.z, smoke.lifeTime));
            level.playSound(null, smoke.x, y, smoke.z, Valorant.CLOVE_RUSE_RELEASE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            iterator.remove();
        }
    }

    private record PendingSmoke(String dimension, double x, double z, int lifeTime, long spawnAt) {
    }
}
