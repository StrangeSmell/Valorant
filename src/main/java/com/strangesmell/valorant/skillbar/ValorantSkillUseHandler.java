package com.strangesmell.valorant.skillbar;

import com.strangesmell.valorant.VALORANT;
import com.strangesmell.valorant.leizhi.blastpack.BlastPackItem;
import com.strangesmell.valorant.jett.bladestorm.JettBladeStormTracker;
import com.strangesmell.valorant.jett.cloudburst.JettCloudburstEntity;
import com.strangesmell.valorant.phoenix.blaze.PhoenixBlazeItem;
import com.strangesmell.valorant.phoenix.curveball.PhoenixCurveballEntity;
import com.strangesmell.valorant.sage.barrier.SageBarrierModeTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = VALORANT.MODID)
public final class ValorantSkillUseHandler {
    private static final Set<Identifier> SKILL_ITEMS = new HashSet<>();
    private static final Map<UUID, HeldSkill> HELD = new HashMap<>();
    private static final Set<UUID> BARRIER_PENDING = new HashSet<>();

    private ValorantSkillUseHandler() {
    }

    public static void refreshSkillItems() {
        SKILL_ITEMS.clear();
        add(VALORANT.LEIZHIBOMB_ITEM.get());
        add(VALORANT.BIGBOMB_ITEM.get());
        add(VALORANT.BOOMBOT_ITEM.get());
        add(VALORANT.BLASTPACK_ITEM.get());
        add(VALORANT.SAGE_HEAL_ITEM.get());
        add(VALORANT.SAGE_BARRIER_ITEM.get());
        add(VALORANT.SAGE_SLOW_ITEM.get());
        add(VALORANT.SAGE_RESURRECTION_ITEM.get());
        add(VALORANT.PHOENIX_CURVEBALL_ITEM.get());
        add(VALORANT.PHOENIX_HOT_HANDS_ITEM.get());
        add(VALORANT.PHOENIX_BLAZE_ITEM.get());
        add(VALORANT.PHOENIX_RUN_IT_BACK_ITEM.get());
        add(VALORANT.CLOVE_RUSE_ITEM.get());
        add(VALORANT.CLOVE_MEDDLE_ITEM.get());
        add(VALORANT.CLOVE_PICK_ME_UP_ITEM.get());
        add(VALORANT.CLOVE_NOT_DEAD_YET_ITEM.get());
        add(VALORANT.JETT_CLOUDBURST_ITEM.get());
        add(VALORANT.JETT_UPDRAFT_ITEM.get());
        add(VALORANT.JETT_TAILWIND_ITEM.get());
        add(VALORANT.JETT_BLADE_STORM_ITEM.get());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tick(player);
        }
    }

    public static void use(ServerPlayer player, Identifier itemId, ValorantSkillUsePayload.Action action) {
        if (!SKILL_ITEMS.contains(itemId)) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        if (item == null) {
            return;
        }

        if (action == ValorantSkillUsePayload.Action.PRIMARY) {
            primary(player);
            return;
        }
        if (action == ValorantSkillUsePayload.Action.SECONDARY) {
            secondary(player);
            return;
        }
        if (action == ValorantSkillUsePayload.Action.RELEASE) {
            release(player);
            return;
        }

        if (isBarrier(item)) {
            toggleBarrier(player);
            return;
        }
        if (item == VALORANT.BLASTPACK_ITEM.get()) {
            useItem(player, item);
            return;
        }
        if (item == VALORANT.JETT_BLADE_STORM_ITEM.get()) {
            if (!JettBladeStormTracker.isActive(player)) {
                useItem(player, item);
            }
            return;
        }
        if (isHoldSkill(item)) {
            startHeld(player, itemId, item);
            return;
        }
        useItem(player, item);
    }

    public static void tick(ServerPlayer player) {
        HeldSkill held = HELD.get(player.getUUID());
        if (held == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.getValue(held.itemId);
        if (item == null) {
            HELD.remove(player.getUUID());
            return;
        }
        if (item == VALORANT.JETT_CLOUDBURST_ITEM.get()) {
            JettCloudburstEntity.guideOwned(level, player);
        } else if (item == VALORANT.PHOENIX_CURVEBALL_ITEM.get()) {
            PhoenixCurveballEntity.guideOwned(level, player);
        } else if (item == VALORANT.PHOENIX_BLAZE_ITEM.get() && !PhoenixBlazeItem.tickWall(level, player, item)) {
            HELD.remove(player.getUUID());
        }
    }

    public static boolean hasActiveMouseSkill(ServerPlayer player) {
        return BARRIER_PENDING.contains(player.getUUID()) || JettBladeStormTracker.isActive(player);
    }

    private static void primary(ServerPlayer player) {
        if (BARRIER_PENDING.remove(player.getUUID())) {
            if (!useItem(player, VALORANT.SAGE_BARRIER_ITEM.get())) {
                BARRIER_PENDING.add(player.getUUID());
            }
            return;
        }
        if (player.level() instanceof ServerLevel level) {
            JettBladeStormTracker.primaryFire(level, player);
        }
    }

    private static void secondary(ServerPlayer player) {
        // Secondary fire for BladeStorm is handled by re-pressing the skill key (use() method)
        // Barrier rotation is handled via the barrier toggle
    }

    private static void release(ServerPlayer player) {
        HeldSkill held = HELD.remove(player.getUUID());
        if (held == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.getValue(held.itemId);
        if (item == VALORANT.PHOENIX_BLAZE_ITEM.get()) {
            PhoenixBlazeItem.releaseWall(level, player, item);
        }
    }

    private static void toggleBarrier(ServerPlayer player) {
        if (BARRIER_PENDING.contains(player.getUUID())) {
            SageBarrierModeTracker.setRotated(player, !SageBarrierModeTracker.isRotated(player));
        } else if (findSlot(player, VALORANT.SAGE_BARRIER_ITEM.get()) >= 0) {
            SageBarrierModeTracker.setRotated(player, false);
            BARRIER_PENDING.add(player.getUUID());
        }
    }

    private static void startHeld(ServerPlayer player, Identifier itemId, Item item) {
        if (findSlot(player, item) < 0) {
            return;
        }
        useItem(player, item);
        HELD.put(player.getUUID(), new HeldSkill(itemId));
    }

    private static boolean useItem(ServerPlayer player, Item item) {
        int slot = findSlot(player, item);
        if (slot < 0 && item == VALORANT.BLASTPACK_ITEM.get() && player.level() instanceof ServerLevel level && BlastPackItem.hasOwnedPacks(level, player)) {
            return item.use(player.level(), player, InteractionHand.MAIN_HAND).consumesAction();
        }
        if (slot < 0) {
            return false;
        }
        InteractionHand hand = InteractionHand.MAIN_HAND;
        int selectedSlot = player.getInventory().getSelectedSlot();
        ItemStack previous = player.getMainHandItem();
        ItemStack skillStack = player.getInventory().getItem(slot);
        player.getInventory().setItem(selectedSlot, skillStack);
        boolean success;
        try {
            InteractionResult result = item.use(player.level(), player, hand);
            success = result.consumesAction();
        } finally {
            if (slot != selectedSlot) {
                player.getInventory().setItem(slot, player.getMainHandItem());
                player.getInventory().setItem(selectedSlot, previous);
            }
        }
        return success;
    }

    private static void add(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id != null) {
            SKILL_ITEMS.add(id);
        }
    }

    private static int findSlot(ServerPlayer player, Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(item)) {
                return slot;
            }
        }
        return -1;
    }

    private static boolean isHoldSkill(Item item) {
        return item == VALORANT.JETT_CLOUDBURST_ITEM.get()
                || item == VALORANT.PHOENIX_CURVEBALL_ITEM.get()
                || item == VALORANT.PHOENIX_BLAZE_ITEM.get();
    }

    private static boolean isBarrier(Item item) {
        return item == VALORANT.SAGE_BARRIER_ITEM.get();
    }

    private record HeldSkill(Identifier itemId) {
    }
}