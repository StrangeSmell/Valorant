package com.strangesmell.valorant.clove.meddle;

import com.strangesmell.valorant.Valorant;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Valorant.MODID)
public final class CloveMeddleDecayTracker {
    private static final long DECAY_TIME = 100L;
    private static final float MIN_HEALTH_AFTER_DECAY = 2.0F;
    private static final Map<UUID, DecayState> DECAYED = new HashMap<>();

    private CloveMeddleDecayTracker() {
    }

    public static void apply(ServerLevel level, LivingEntity target) {
        float originalHealth = Math.max(target.getHealth(), MIN_HEALTH_AFTER_DECAY);
        DECAYED.put(target.getUUID(), new DecayState(level.dimension().toString(), originalHealth, level.getGameTime() + DECAY_TIME));
        if (target.getHealth() > MIN_HEALTH_AFTER_DECAY) {
            target.setHealth(MIN_HEALTH_AFTER_DECAY);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || DECAYED.isEmpty()) {
            return;
        }
        long time = level.getGameTime();
        Iterator<Map.Entry<UUID, DecayState>> iterator = DECAYED.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, DecayState> entry = iterator.next();
            DecayState state = entry.getValue();
            if (state.expiresAt > time || !state.dimension.equals(level.dimension().toString())) {
                continue;
            }
            if (level.getEntity(entry.getKey()) instanceof LivingEntity target && target.isAlive()) {
                target.setHealth(Math.min(target.getMaxHealth(), Math.max(target.getHealth(), state.originalHealth)));
            }
            iterator.remove();
        }
    }

    private record DecayState(String dimension, float originalHealth, long expiresAt) {
    }
}
