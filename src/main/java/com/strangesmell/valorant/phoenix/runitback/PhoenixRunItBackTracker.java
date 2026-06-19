package com.strangesmell.valorant.phoenix.runitback;

import com.strangesmell.valorant.Valorant;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Valorant.MODID)
public final class PhoenixRunItBackTracker {
    private static final Map<UUID, Integer> ANCHORS = new HashMap<>();

    private PhoenixRunItBackTracker() {
    }

    public static void mark(ServerLevel level, Player player) {
        discardAnchor(level, player);
        PhoenixRunItBackAnchorEntity anchor = new PhoenixRunItBackAnchorEntity(level, player);
        level.addFreshEntity(anchor);
        ANCHORS.put(player.getUUID(), anchor.getId());
        level.sendParticles(ParticleTypes.FLAME, anchor.getX(), anchor.getY() + 1.0D, anchor.getZ(), 48, 0.8D, 1.0D, 0.8D, 0.08D);
    }

    public static boolean returnToAnchor(ServerLevel currentLevel, Player player) {
        PhoenixRunItBackAnchorEntity anchor = findAnchor(currentLevel, player);
        if (anchor == null) {
            return false;
        }
        teleportToAnchor(currentLevel, player, anchor);

        return true;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        PhoenixRunItBackAnchorEntity anchor = findAnchor(serverLevel, player);
        if (anchor == null) {
            return;
        }

        event.setCanceled(true);
        teleportToAnchor(serverLevel, player, anchor);
    }

    private static PhoenixRunItBackAnchorEntity findAnchor(ServerLevel currentLevel, Player player) {
        Integer id = ANCHORS.get(player.getUUID());
        if (id != null) {
            for (ServerLevel level : currentLevel.getServer().getAllLevels()) {
                if (level.getEntity(id) instanceof PhoenixRunItBackAnchorEntity anchor && anchor.isOwner(player)) {
                    return anchor;
                }
            }
            ANCHORS.remove(player.getUUID());
        }

        for (ServerLevel level : currentLevel.getServer().getAllLevels()) {
            for (PhoenixRunItBackAnchorEntity anchor : level.getEntitiesOfClass(PhoenixRunItBackAnchorEntity.class, player.getBoundingBox().inflate(512.0D), entity -> entity.isOwner(player))) {
                ANCHORS.put(player.getUUID(), anchor.getId());
                return anchor;
            }
        }
        return null;
    }

    private static void teleportToAnchor(ServerLevel currentLevel, Player player, PhoenixRunItBackAnchorEntity anchor) {
        ServerLevel targetLevel = (ServerLevel)anchor.level();
        Vec3 position = anchor.returnPosition();
        ANCHORS.remove(player.getUUID());
        player.setHealth(player.getMaxHealth());
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        player.teleportTo(targetLevel, position.x, position.y, position.z, Set.of(), player.getYRot(), player.getXRot(), true);
        targetLevel.sendParticles(ParticleTypes.FLAME, position.x, position.y + 1.0D, position.z, 64, 0.9D, 1.0D, 0.9D, 0.1D);
        if (currentLevel != targetLevel) {
            currentLevel.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 24, 0.5D, 0.7D, 0.5D, 0.08D);
        }
        anchor.discard();
        if (!player.getAbilities().instabuild) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack s = player.getInventory().getItem(i);
                if (s.is(Valorant.PHOENIX_RUN_IT_BACK_ITEM.get())) {
                    s.shrink(1);
                    break;
                }
            }
        }
    }

    private static void discardAnchor(ServerLevel currentLevel, Player player) {
        PhoenixRunItBackAnchorEntity anchor = findAnchor(currentLevel, player);
        if (anchor != null) {
            ANCHORS.remove(player.getUUID());
            anchor.discard();
        }
    }
}
