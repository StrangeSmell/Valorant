package com.strangesmell.valorant.jett.updraft;

import com.strangesmell.valorant.Valorant;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Valorant.MODID)
public final class JettWindTracker {
    private static final Map<UUID, Long> FALL_PROTECTION = new HashMap<>();

    private JettWindTracker() {
    }

    public static void grantFallProtection(Player player, long expiresAt) {
        FALL_PROTECTION.put(player.getUUID(), expiresAt);
        player.fallDistance = 0.0F;
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Long expiresAt = FALL_PROTECTION.get(player.getUUID());
        if (expiresAt == null || serverLevel.getGameTime() > expiresAt) {
            FALL_PROTECTION.remove(player.getUUID());
            return;
        }
        if (event.getSource().is(DamageTypes.FALL) || event.getSource().is(DamageTypes.FLY_INTO_WALL)) {
            event.setCanceled(true);
            player.fallDistance = 0.0F;
            FALL_PROTECTION.remove(player.getUUID());
        }
    }
}
