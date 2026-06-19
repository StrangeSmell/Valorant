package com.strangesmell.valorant.jett.bladestorm;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.world.level.block.SculkSensorBlock.COOLDOWN_TICKS;

@EventBusSubscriber(modid = Valorant.MODID)
public final class JettBladeStormTracker {
    private static final int MAX_KNIVES = 5;
    private static final long LIFE_TIME = 600L;
    private static final float KNIFE_SPEED = 2.65F;
    private static final double SPREAD_STEP = 4.0D;
    private static final Map<UUID, State> STATES = new HashMap<>();

    private JettBladeStormTracker() {
    }

    public static void use(ServerLevel level, ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        if (state == null || state.expiresAt < level.getGameTime() || state.knives <= 0) {
            setKnives(level, player, MAX_KNIVES);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), Valorant.JETT_BLADE_STORM_READY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }

        secondaryFire(level, player);
    }

    public static boolean primaryFire(ServerLevel level, ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        if (state == null || state.expiresAt < level.getGameTime() || state.knives <= 0) {
            return false;
        }

        fireOne(level, player, state, 0.0F, 0.0F, true);
        if (state.knives <= 0) {
            STATES.remove(player.getUUID());
            player.getCooldowns().addCooldown(Valorant.JETT_BLADE_STORM_ITEM.toStack(), COOLDOWN_TICKS);
        }
        return true;
    }

    public static boolean isActive(Player player) {
        State state = STATES.get(player.getUUID());
        return state != null && state.knives > 0;
    }

    public static void secondaryFire(ServerLevel level, ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        if (state == null || state.expiresAt < level.getGameTime() || state.knives <= 0) {
            return;
        }

        int knivesToFire = state.knives;
        for (int i = 0; i < knivesToFire; i++) {
            double centered = i - (knivesToFire - 1) / 2.0D;
            fireOne(level, player, state, 0.0F, (float)(centered * SPREAD_STEP), false);
        }
        player.getCooldowns().addCooldown(Valorant.JETT_BLADE_STORM_ITEM.toStack(), COOLDOWN_TICKS);

        discardOrbitKnives(level, state);
        STATES.remove(player.getUUID());
        level.playSound(null, player.getX(), player.getY(), player.getZ(), Valorant.JETT_BLADE_STORM_THROW_ALL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void fireOne(ServerLevel level, ServerPlayer player, State state, float xRotOffset, float yRotOffset, boolean playSound) {
        removeOneOrbitKnife(level, state);
        state.knives = state.orbitIds.size();
        state.expiresAt = level.getGameTime() + LIFE_TIME;
        JettBladeStormKnifeEntity knife = new JettBladeStormKnifeEntity(level, player, player.getMainHandItem());
        knife.setPos(player.getEyePosition().add(player.getLookAngle().scale(0.6D)));
        knife.shootFromRotation(player, player.getXRot() + xRotOffset, player.getYRot() + yRotOffset, 0.0F, KNIFE_SPEED, 0.0F);
        level.addFreshEntity(knife);
        if (playSound) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), Valorant.JETT_BLADE_STORM_THROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void refresh(ServerLevel level, Player player) {
        setKnives(level, player, MAX_KNIVES);
        level.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0D, player.getZ(), 32, 0.7D, 0.8D, 0.7D, 0.06D);
    }

    public static boolean isActiveOrbitKnife(Player player, int entityId) {
        State state = STATES.get(player.getUUID());
        return state != null && state.orbitIds.contains(entityId);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (source instanceof Player player && player.level() instanceof ServerLevel level && event.getEntity() instanceof Player) {
            State state = STATES.get(player.getUUID());
            if (state != null) {
                refresh(level, player);
                player.getCooldowns().removeCooldown(Valorant.JETT_BLADE_STORM_ITEM.getId());

            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        State state = STATES.get(player.getUUID());
        if (state != null && state.expiresAt < level.getGameTime()) {
            discardOrbitKnives(level, state);
            STATES.remove(player.getUUID());
        }
    }

    private static void setKnives(ServerLevel level, Player player, int knives) {
        State state = STATES.computeIfAbsent(player.getUUID(), uuid -> new State(level.getGameTime() + LIFE_TIME));
        state.knives = knives;
        state.expiresAt = level.getGameTime() + LIFE_TIME;
        syncOrbitKnives(level, player, state);
    }

    private static void syncOrbitKnives(ServerLevel level, Player player, State state) {
        discardOrbitKnives(level, state);
        state.orbitIds.clear();
        for (int i = 0; i < state.knives; i++) {
            JettBladeStormOrbitKnifeEntity orbit = new JettBladeStormOrbitKnifeEntity(level, player, i);
            level.addFreshEntity(orbit);
            state.orbitIds.add(orbit.getId());
        }
    }

    private static void discardOrbitKnives(ServerLevel level, State state) {
        Iterator<Integer> iterator = state.orbitIds.iterator();
        while (iterator.hasNext()) {
            Entity entity = level.getEntity(iterator.next());
            if (entity != null) {
                entity.discard();
            }
            iterator.remove();
        }
    }

    private static void removeOneOrbitKnife(ServerLevel level, State state) {
        if (state.orbitIds.isEmpty()) {
            return;
        }
        int entityId = state.orbitIds.remove(state.orbitIds.size() - 1);
        Entity entity = level.getEntity(entityId);
        if (entity != null) {
            entity.discard();
        }
    }

    private static final class State {
        private int knives;
        private long expiresAt;
        private final java.util.List<Integer> orbitIds = new java.util.ArrayList<>();

        private State(long expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}
