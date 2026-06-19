package com.strangesmell.valorant.phoenix.curveball;

import com.strangesmell.valorant.Valorant;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class PhoenixCurveballFlashScreen {
    private static final int FADE_TICKS = 16;
    private static int remainingTicks;
    private static int totalTicks;

    private PhoenixCurveballFlashScreen() {
    }

    public static void flash(int ticks) {
        remainingTicks = Math.max(remainingTicks, ticks);
        totalTicks = Math.max(1, remainingTicks);
    }

    @SubscribeEvent
    public static void renderFlash(RenderGuiLayerEvent.Post event) {
        if (remainingTicks <= 0) {
            return;
        }
        float alpha = remainingTicks > FADE_TICKS ? 1.0F : remainingTicks / (float)FADE_TICKS;
        int a = Math.max(0, Math.min(255, (int)(alpha * 255.0F)));
        event.getGuiGraphics().fill(0, 0, event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight(), (a << 24) | 0xE33622);
        remainingTicks--;
    }
}
