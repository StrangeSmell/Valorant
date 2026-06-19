package com.strangesmell.valorant;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ValorantSkillItems {
    private ValorantSkillItems() {
    }

    public static void consumeHeld(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    public static boolean consumeFromInventory(Player player, Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                consumeHeld(player, stack);
                return true;
            }
        }
        return false;
    }
}
