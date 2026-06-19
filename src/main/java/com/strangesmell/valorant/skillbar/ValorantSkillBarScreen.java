package com.strangesmell.valorant.skillbar;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public class ValorantSkillBarScreen extends Screen {
    private static final Identifier CONTAINER = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 114;
    private static final int SKILL_X = 7;
    private static final int SKILL_Y = 17;
    private static final int INV_X = 7;
    private static final int INV_Y = 49;

    public ValorantSkillBarScreen() {
        super(Component.translatable("screen.valorant.skill_bar.title"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new ValorantSkillBarScreen());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int left = (this.width - IMAGE_WIDTH) / 2;
        int top = (this.height - IMAGE_HEIGHT) / 2;
        graphics.blit(CONTAINER, left, top, 0, 0, IMAGE_WIDTH, 35, 256, 256);
        graphics.blit(CONTAINER, left, top + 35, 0, 126, IMAGE_WIDTH, 89, 256, 256);
        graphics.text(this.font, this.title, left + 8, top + 6, 0x404040, false);

        for (int slot = 0; slot < ValorantSkillBar.SIZE; slot++) {
            ItemStack stack = ValorantSkillBar.getDisplayStack(slot);
            if (!stack.isEmpty()) {
                renderStack(graphics, stack, left + SKILL_X + slot * 18 + 1, top + SKILL_Y + 1);
            }
        }

        Inventory inventory = this.minecraft.player.getInventory();
        for (int slot = 9; slot < 36; slot++) {
            int index = slot - 9;
            renderInventoryStack(graphics, inventory.getItem(slot), left + INV_X + index % 9 * 18 + 1, top + INV_Y + index / 9 * 18 + 1);
        }
        for (int slot = 0; slot < 9; slot++) {
            renderInventoryStack(graphics, inventory.getItem(slot), left + INV_X + slot * 18 + 1, top + INV_Y + 58 + 1);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isBeforePic) {
        int left = (this.width - IMAGE_WIDTH) / 2;
        int top = (this.height - IMAGE_HEIGHT) / 2;
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        int skillSlot = slotAt(mouseX, mouseY, left + SKILL_X, top + SKILL_Y, ValorantSkillBar.SIZE);
        if (skillSlot >= 0) {
            if (button == 1) {
                ValorantSkillBar.set(skillSlot, null);
                return true;
            }
            ItemStack carried = findHoveredInventoryStack(mouseX, mouseY, left, top);
            if (ValorantSkillBar.isSkillItem(carried)) {
                ValorantSkillBar.set(skillSlot, BuiltInRegistries.ITEM.getKey(carried.getItem()));
                return true;
            }
        }
        ItemStack clickedStack = findHoveredInventoryStack(mouseX, mouseY, left, top);
        if (ValorantSkillBar.isSkillItem(clickedStack)) {
            int target = firstEmptySlot();
            ValorantSkillBar.set(target >= 0 ? target : 0, BuiltInRegistries.ITEM.getKey(clickedStack.getItem()));
            return true;
        }
        return super.mouseClicked(event, isBeforePic);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private ItemStack findHoveredInventoryStack(double mouseX, double mouseY, int left, int top) {
        Inventory inventory = this.minecraft.player.getInventory();
        int main = slotAt(mouseX, mouseY, left + INV_X, top + INV_Y, 27);
        if (main >= 0) {
            return inventory.getItem(main + 9);
        }
        int hotbar = slotAt(mouseX, mouseY, left + INV_X, top + INV_Y + 58, 9);
        return hotbar >= 0 ? inventory.getItem(hotbar) : ItemStack.EMPTY;
    }

    private static int slotAt(double mouseX, double mouseY, int startX, int startY, int count) {
        for (int slot = 0; slot < count; slot++) {
            int x = startX + slot % 9 * 18;
            int y = startY + slot / 9 * 18;
            if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                return slot;
            }
        }
        return -1;
    }

    private void renderInventoryStack(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            renderStack(graphics, stack, x, y);
        }
    }

    private void renderStack(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        graphics.item(stack, x, y);
        graphics.itemDecorations(this.font, stack, x, y);
    }

    private static int firstEmptySlot() {
        for (int slot = 0; slot < ValorantSkillBar.SIZE; slot++) {
            if (ValorantSkillBar.get(slot) == null) {
                return slot;
            }
        }
        return -1;
    }
}