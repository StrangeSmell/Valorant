package com.strangesmell.valorant;

import com.strangesmell.valorant.clove.notdeadyet.CloveNotDeadYetScreen;
import com.strangesmell.valorant.phoenix.curveball.PhoenixCurveballFlashScreen;
import net.minecraft.client.Minecraft;
import com.strangesmell.valorant.clove.ruse.CloveRuseScreen;

public final class ValorantClientPacketHandlers {
    private ValorantClientPacketHandlers() {
    }

    public static void openNotDeadYetScreen() {
        Minecraft.getInstance().setScreen(new CloveNotDeadYetScreen());
    }

    public static void openRuseScreen() {
        CloveRuseScreen.openRuseScreen();
    }

    public static void flashPhoenixCurveball(int ticks) {
        PhoenixCurveballFlashScreen.flash(ticks);
    }
}
