package com.strangesmell.valorant.skillbar;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashSet;
import java.util.Set;

public final class ValorantSkillBar {
    public static final int SIZE = 9;
    public static final int HUD_SIZE = 4;
    private static final Identifier[] SLOTS = new Identifier[SIZE];
    private static final Set<Identifier> SKILL_ITEMS = new HashSet<Identifier>();

    private ValorantSkillBar() {
    }

    public static void refreshSkillItems() {
        SKILL_ITEMS.clear();
        addSkill(VALORANT.LEIZHIBOMB_ITEM.get());
        addSkill(VALORANT.BIGBOMB_ITEM.get());
        addSkill(VALORANT.BOOMBOT_ITEM.get());
        addSkill(VALORANT.BLASTPACK_ITEM.get());
        addSkill(VALORANT.SAGE_HEAL_ITEM.get());
        addSkill(VALORANT.SAGE_BARRIER_ITEM.get());
        addSkill(VALORANT.SAGE_SLOW_ITEM.get());
        addSkill(VALORANT.SAGE_RESURRECTION_ITEM.get());
        addSkill(VALORANT.PHOENIX_CURVEBALL_ITEM.get());
        addSkill(VALORANT.PHOENIX_HOT_HANDS_ITEM.get());
        addSkill(VALORANT.PHOENIX_BLAZE_ITEM.get());
        addSkill(VALORANT.PHOENIX_RUN_IT_BACK_ITEM.get());
        addSkill(VALORANT.CLOVE_RUSE_ITEM.get());
        addSkill(VALORANT.CLOVE_MEDDLE_ITEM.get());
        addSkill(VALORANT.CLOVE_PICK_ME_UP_ITEM.get());
        addSkill(VALORANT.CLOVE_NOT_DEAD_YET_ITEM.get());
        addSkill(VALORANT.JETT_CLOUDBURST_ITEM.get());
        addSkill(VALORANT.JETT_UPDRAFT_ITEM.get());
        addSkill(VALORANT.JETT_TAILWIND_ITEM.get());
        addSkill(VALORANT.JETT_BLADE_STORM_ITEM.get());
    }

    public static boolean isSkillItem(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && SKILL_ITEMS.contains(id);
    }

    public static Identifier get(int slot) {
        return slot >= 0 && slot < SLOTS.length ? SLOTS[slot] : null;
    }

    public static void set(int slot, Identifier itemId) {
        if (slot >= 0 && slot < SLOTS.length) {
            SLOTS[slot] = itemId;
        }
    }

    public static ItemStack getDisplayStack(int slot) {
        Identifier id = get(slot);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getValue(id);
        return item == null ? ItemStack.EMPTY : item.getDefaultInstance();
    }

    private static void addSkill(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id != null) {
            SKILL_ITEMS.add(id);
        }
    }
}
