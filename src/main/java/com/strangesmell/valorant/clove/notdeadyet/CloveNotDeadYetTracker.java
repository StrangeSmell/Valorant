package com.strangesmell.valorant.clove.notdeadyet;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = VALORANT.MODID)
public final class CloveNotDeadYetTracker {
    private static final long REVIVE_WINDOW = 200L;
    private static final long BUTTON_WINDOW = 200L;
    private static final int INVULNERABLE_TIME = 40;
    private static final Map<UUID, Long> PENDING = new HashMap<>();
    private static final Map<UUID, Long> REVIVED = new HashMap<>();

    private CloveNotDeadYetTracker() {
    }

    public static void activateFromDeathScreen(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Long expiresAt = PENDING.remove(player.getUUID());
        if (expiresAt == null || serverLevel.getGameTime() > expiresAt || !hasSkillItem(player)) {
            player.kill(serverLevel);
            return;
        }

        player.setHealth(player.getMaxHealth());
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, INVULNERABLE_TIME, 4, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, INVULNERABLE_TIME, 0, false, false, true));
        REVIVED.put(player.getUUID(), serverLevel.getGameTime() + REVIVE_WINDOW);
        serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFFFF66FF), player.getX(), player.getY() + 1.0D, player.getZ(), 80, 0.9D, 1.1D, 0.9D, 0.08D);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (REVIVED.containsKey(player.getUUID()) || PENDING.containsKey(player.getUUID()) || !hasSkillItem(player)) {
            return;
        }
        if (event.getAmount() < player.getHealth()) {
            return;
        }

        event.setCanceled(true);
        player.setHealth(1.0F);
        PENDING.put(player.getUUID(), serverLevel.getGameTime() + BUTTON_WINDOW);
        PacketDistributor.sendToPlayer(player, CloveNotDeadYetOpenScreenPayload.INSTANCE);
        serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFFDB66FF), player.getX(), player.getY() + 1.0D, player.getZ(), 48, 0.8D, 1.0D, 0.8D, 0.04D);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (source instanceof Player killer && killer.level() instanceof ServerLevel killerLevel) {
            confirm(killerLevel, killer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long time = serverLevel.getGameTime();
        Iterator<Map.Entry<UUID, Long>> pendingIterator = PENDING.entrySet().iterator();
        while (pendingIterator.hasNext()) {
            Map.Entry<UUID, Long> entry = pendingIterator.next();
            if (entry.getValue() >= time) {
                continue;
            }
            pendingIterator.remove();
            if (entry.getKey().equals(player.getUUID())) {
                player.kill(serverLevel);
            }
        }

        Iterator<Map.Entry<UUID, Long>> iterator = REVIVED.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (entry.getValue() >= time) {
                continue;
            }
            iterator.remove();
            if (entry.getKey().equals(player.getUUID())) {
                player.kill(serverLevel);
            }
        }
    }

    private static void confirm(ServerLevel level, Player player) {
        if (REVIVED.remove(player.getUUID()) != null) {
            player.setHealth(player.getMaxHealth());
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
            level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF80FFCC), player.getX(), player.getY() + 1.0D, player.getZ(), 64, 0.8D, 1.0D, 0.8D, 0.05D);
        }
    }

    private static boolean hasSkillItem(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(VALORANT.CLOVE_NOT_DEAD_YET_ITEM.get())) {
                return true;
            }
        }
        return false;
    }
}
