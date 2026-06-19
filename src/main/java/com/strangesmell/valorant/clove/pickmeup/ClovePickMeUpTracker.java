package com.strangesmell.valorant.clove.pickmeup;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Valorant.MODID)
public final class ClovePickMeUpTracker {
    private static final long WINDOW = 200L;
    private static final long BUFF_TIME = 200L;
    private static final float MAX_OVERHEAL = 10.0F;
    private static final Identifier SPEED_MODIFIER = Identifier.fromNamespaceAndPath(Valorant.MODID, "clove_pick_me_up_speed");
    private static final Map<UUID, Long> ARMED = new HashMap<>();
    private static final Map<UUID, UUID> RECENT_DAMAGE = new HashMap<>();
    private static final Map<UUID, Long> BUFFED = new HashMap<>();

    private ClovePickMeUpTracker() {
    }

    public static void arm(ServerLevel level, Player player) {
        ARMED.put(player.getUUID(), level.getGameTime() + WINDOW);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF9D5CFF), player.getX(), player.getY() + 1.0D, player.getZ(), 32, 0.6D, 0.8D, 0.6D, 0.04D);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof Player player) || !(player.level() instanceof ServerLevel serverLevel) || !(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        Long expiresAt = ARMED.get(player.getUUID());
        if (expiresAt == null || serverLevel.getGameTime() > expiresAt || target == player) {
            return;
        }
        RECENT_DAMAGE.put(target.getUUID(), player.getUUID());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        UUID cloveId = source instanceof Player player ? player.getUUID() : RECENT_DAMAGE.get(event.getEntity().getUUID());
        if (cloveId == null || !(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Player player = serverLevel.getPlayerByUUID(cloveId);
        if (player == null) {
            return;
        }
        Long expiresAt = ARMED.get(player.getUUID());
        if (expiresAt == null || serverLevel.getGameTime() > expiresAt) {
            ARMED.remove(player.getUUID());
            return;
        }

        ARMED.remove(player.getUUID());
        applyBuff(serverLevel, player);
        RECENT_DAMAGE.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        long time = serverLevel.getGameTime();
        ARMED.entrySet().removeIf(entry -> entry.getValue() < time);
        Long buffExpiresAt = BUFFED.get(player.getUUID());
        if (buffExpiresAt != null && buffExpiresAt < time) {
            BUFFED.remove(player.getUUID());
            removeSpeed(player);
        }
    }

    private static void applyBuff(ServerLevel level, Player player) {
        player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), MAX_OVERHEAL));
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(SPEED_MODIFIER, 0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        BUFFED.put(player.getUUID(), level.getGameTime() + BUFF_TIME);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), Valorant.CLOVE_PICK_ME_UP_PROC.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFFB477FF), player.getX(), player.getY() + 1.0D, player.getZ(), 48, 0.7D, 0.9D, 0.7D, 0.04D);
    }

    private static void removeSpeed(Player player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED_MODIFIER);
        }
    }
}
