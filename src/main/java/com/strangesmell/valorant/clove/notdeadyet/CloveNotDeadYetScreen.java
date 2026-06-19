package com.strangesmell.valorant.clove.notdeadyet;

import com.strangesmell.valorant.ValorantNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CloveNotDeadYetScreen extends Screen {
    public CloveNotDeadYetScreen() {
        super(Component.translatable("screen.valorant.clove_not_dead_yet.title"));
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("screen.valorant.clove_not_dead_yet.button"), button -> {
            ValorantNetwork.sendToServer(CloveNotDeadYetPayload.INSTANCE);
            Minecraft.getInstance().setScreen(null);
        }).bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xC0000000);
        graphics.centeredText(this.font, this.title, this.width / 2, this.height / 4 + 24, 0xFFFFFF);
        graphics.centeredText(this.font, Component.translatable("screen.valorant.clove_not_dead_yet.subtitle"), this.width / 2, this.height / 4 + 48, 0xA0A0A0);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}