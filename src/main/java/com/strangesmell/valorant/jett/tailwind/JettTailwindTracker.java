package com.strangesmell.valorant.jett.tailwind;

import com.strangesmell.valorant.Valorant;
import com.strangesmell.valorant.jett.updraft.JettWindTracker;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Valorant.MODID)
public final class JettTailwindTracker {
    private static final long WAIT_TIME = 150L;
    private static final long WINDUP = 20L;
    private static final long DASH_TIME = 9L;
    private static final Map<UUID, State> WAITING = new HashMap<>();

    private JettTailwindTracker() {
    }

    // arm = first press: start windup
    public static void arm(ServerLevel level, ServerPlayer player) {
        WAITING.put(player.getUUID(), new State(level.getGameTime() + WINDUP, level.getGameTime() + WINDUP + WAIT_TIME));
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5D, player.getZ(), 20, 0.35D, 0.35D, 0.35D, 0.03D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), Valorant.JETT_TAILWIND_READY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    // isReady: windup finished, waiting for dash trigger
    public static boolean isReady(ServerPlayer player) {
        State state = WAITING.get(player.getUUID());
        if (state == null || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        if (state.expiresAt < level.getGameTime()) {
            WAITING.remove(player.getUUID());
            return false;
        }
        return state.readyAt <= level.getGameTime();
    }

    // dash = second press: dash toward the keys being held right now
    public static boolean dash(ServerLevel level, ServerPlayer player) {
        State state = WAITING.get(player.getUUID());
        long time = level.getGameTime();
        if (state == null || state.expiresAt < time || state.readyAt > time) {
            WAITING.remove(player.getUUID());
            return false;
        }
        WAITING.remove(player.getUUID());
        Input input = player.getLastClientInput();
        Vec3 direction = inputDirection(player, input);
        player.setDeltaMovement(direction.normalize().scale(2.35D).add(0.0D, 0.12D, 0.0D));
        player.hurtMarked = true;
        JettWindTracker.grantFallProtection(player, time + DASH_TIME);
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.4D, player.getZ(), 54, 0.8D, 0.35D, 0.8D, 0.12D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), Valorant.JETT_TAILWIND_DASH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        long time = level.getGameTime();
        Iterator<Map.Entry<UUID, State>> iterator = WAITING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, State> entry = iterator.next();
            if (entry.getValue().expiresAt < time) {
                iterator.remove();
            }
        }
    }

    private static Vec3 inputDirection(ServerPlayer player, Input input) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, player.getYRot());
        Vec3 right = Vec3.directionFromRotation(0.0F, player.getYRot() + 90.0F);
        Vec3 direction = Vec3.ZERO;
        if (input.forward()) {
            direction = direction.add(forward);
        }
        if (input.backward()) {
            direction = direction.subtract(forward);
        }
        if (input.right()) {
            direction = direction.add(right);
        }
        if (input.left()) {
            direction = direction.subtract(right);
        }
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = forward;
        }
        return direction;
    }

    private static final class State {
        private final long readyAt;
        private final long expiresAt;

        private State(long readyAt, long expiresAt) {
            this.readyAt = readyAt;
            this.expiresAt = expiresAt;
        }
    }
}