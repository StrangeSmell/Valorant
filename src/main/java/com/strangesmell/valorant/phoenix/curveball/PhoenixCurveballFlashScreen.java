package com.strangesmell.valorant.phoenix.curveball;

import com.strangesmell.valorant.Valorant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class PhoenixCurveballFlashScreen {
    private static final int FADE_TICKS = 16;
    private static int remainingTicks;
    private static double flashX, flashY, flashZ;
    private static boolean hasFlashPos;

    private PhoenixCurveballFlashScreen() {
    }

    public static void flash(int ticks, double x, double y, double z) {
        remainingTicks = Math.max(remainingTicks, ticks);
        flashX = x;
        flashY = y;
        flashZ = z;
        hasFlashPos = true;
    }

    @SubscribeEvent
    public static void renderFlash(RenderGuiLayerEvent.Post event) {
        if (remainingTicks <= 0) {
            hasFlashPos = false;
            return;
        }

        float alpha = remainingTicks > FADE_TICKS ? 1.0F : remainingTicks / (float) FADE_TICKS;
        int a = Math.max(0, Math.min(255, (int)(alpha * 255.0F)));

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int sw = graphics.guiWidth();
        int sh = graphics.guiHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        graphics.fill(0, 0, sw, sh, (a << 24) | 0xE33622);

        if (hasFlashPos) {
            renderFlashOrb(graphics, cx, cy, sw, sh, alpha);
        }

        remainingTicks--;
    }

    private static void renderFlashOrb(GuiGraphicsExtractor graphics, int cx, int cy, int sw, int sh, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = mc.player.getLookAngle();
        Vec3 flashPos = new Vec3(flashX, flashY, flashZ);

        Vec3 toFlash = flashPos.subtract(eyePos);
        double distance = toFlash.length();

        if (distance < 1.0E-4D) return;

        Vec3 dir = toFlash.normalize();

        // 计算左右和上下向量
        Vec3 right = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
        if (right.lengthSqr() < 0.1D) {
            right = new Vec3(1, 0, 0);
        }
        Vec3 up = right.cross(lookVec).normalize();

        double dotForward = dir.dot(lookVec);
        double dotRight = dir.dot(right);
        double dotUp = dir.dot(up);

        // 超出视野范围不画
        double maxAngleDot = Math.cos(Math.toRadians(100.0D));
        if (dotForward < maxAngleDot) return;

        double fov = mc.options.fov().get();
        double fovRad = Math.toRadians(fov);
        double halfScreenFov = fovRad * 0.5D;

        double angleH = Math.atan2(dotRight, dotForward);
        double angleV = Math.atan2(dotUp, dotForward);

        double screenFactorX = angleH / halfScreenFov;
        double screenFactorY = angleV / halfScreenFov;

        int orbScreenX = cx + (int)(screenFactorX * cx);
        int orbScreenY = cy - (int)(screenFactorY * cy);

        double maxDist = 12.0D;
        double distFactor = 1.0D - Math.min(1.0D, Math.max(0.0D, distance / maxDist));
        int minSize = Math.min(sw, sh) / 16;
        int maxSize = Math.min(sw, sh) / 8;
        int orbRadius = minSize + (int)((maxSize - minSize) * distFactor);

        orbScreenX = Mth.clamp(orbScreenX, orbRadius, sw - orbRadius);
        orbScreenY = Mth.clamp(orbScreenY, orbRadius, sh - orbRadius);

        int rayCount = 16;
        float rayAlpha = alpha * 0.35F;

        for (int i = 0; i < rayCount; i++) {
            double angle = i * Math.PI * 2.0D / rayCount + (remainingTicks * 0.05D);
            double rayLength = orbRadius * (1.2D + Math.sin(i * 2.3D + remainingTicks * 0.08D) * 0.6D);
            int endX = orbScreenX + (int)(Math.cos(angle) * rayLength);
            int endY = orbScreenY + (int)(Math.sin(angle) * rayLength);

            int rayColor = ((int)(rayAlpha * 255) << 24) | 0xFFFFFF;
            int minX = Math.min(orbScreenX, endX);
            int minY = Math.min(orbScreenY, endY);
            int maxX = Math.max(orbScreenX, endX) + 1;
            int maxY = Math.max(orbScreenY, endY) + 1;
            graphics.fill(minX, minY, maxX, maxY, rayColor);

            int dotSize = 2;
            int dotColor = Math.min(255, (int)(rayAlpha * 1.5D * 255)) << 24 | 0xFFFFFF;
            graphics.fill(endX - dotSize, endY - dotSize, endX + dotSize + 1, endY + dotSize + 1, dotColor);
        }

        // 光球光晕
        int orbAlpha = (int)(alpha * 60);
        drawGradientCircle(graphics, orbScreenX, orbScreenY, orbRadius + 4, orbAlpha << 24 | 0xFFFFFF);
        orbAlpha = (int)(alpha * 120);
        drawGradientCircle(graphics, orbScreenX, orbScreenY, orbRadius + 1, orbAlpha << 24 | 0xFFFFFF);
        orbAlpha = (int)(alpha * 200);
        graphics.fill(orbScreenX - orbRadius / 2, orbScreenY - orbRadius / 2,
                      orbScreenX + orbRadius / 2 + 1, orbScreenY + orbRadius / 2 + 1,
                      orbAlpha << 24 | 0xFFFFFF);
    }

    private static void drawGradientCircle(GuiGraphicsExtractor graphics, int cx, int cy, int radius, int color) {
        int a = (color >> 24) & 0xFF;
        int rgb = color & 0x00FFFFFF;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > radius) continue;
                float falloff = 1.0F - (float)(dist / radius);
                falloff = falloff * falloff;
                int pixelAlpha = Math.max(0, Math.min(255, (int)(a * falloff)));
                if (pixelAlpha > 0) {
                    graphics.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1,
                                  (pixelAlpha << 24) | rgb);
                }
            }
        }
    }
}