package com.strangesmell.valorant.sage.barrier;

import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class SageBarrierModeTracker {
    private static final Set<UUID> ROTATED = new HashSet<>();

    private SageBarrierModeTracker() {
    }

    public static void setRotated(Player player, boolean rotated) {
        if (rotated) {
            ROTATED.add(player.getUUID());
        } else {
            ROTATED.remove(player.getUUID());
        }
    }

    public static boolean isRotated(Player player) {
        return ROTATED.contains(player.getUUID());
    }
}
