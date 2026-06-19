package com.strangesmell.valorant.clove.ruse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import com.strangesmell.valorant.ValorantNetwork;

public class CloveRuseScreen extends Screen {
    private static final double RANGE = 64.0D;
    private static final int MAP_SIZE = 216;
    private static final int SAMPLE_COUNT = 36;

    public CloveRuseScreen() {
        super(Component.translatable("screen.valorant.clove_ruse.title"));
    }

    public static void openRuseScreen() {
        Minecraft.getInstance().setScreen(new CloveRuseScreen());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x66000000);
        int left = this.width / 2 - MAP_SIZE / 2;
        int top = this.height / 2 - MAP_SIZE / 2;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        graphics.fill(left, top, left + MAP_SIZE, top + MAP_SIZE, 0x99181824);
        this.renderMapTiles(graphics, left, top);
        this.renderEntityIcons(graphics, left, top);
        for (int i = 0; i <= 6; i++) {
            int line = left + i * MAP_SIZE / 6;
            graphics.verticalLine(line, top, top + MAP_SIZE, 0x664D4D68);
            line = top + i * MAP_SIZE / 6;
            graphics.horizontalLine(left, left + MAP_SIZE, line, 0x664D4D68);
        }
        graphics.horizontalLine(left, left + MAP_SIZE, centerY, 0xAA9D5CFF);
        graphics.verticalLine(centerX, top, top + MAP_SIZE, 0xAA9D5CFF);
        graphics.fill(centerX - 3, centerY - 3, centerX + 4, centerY + 4, 0xFFFF66FF);

        if (isInsideMap(mouseX, mouseY, left, top)) {
            graphics.fill(mouseX - 4, mouseY - 4, mouseX + 5, mouseY + 5, 0xCCFFFFFF);
        }

        graphics.centeredText(this.font, this.title, this.width / 2, top - 24, 0xFFFFFF);
        graphics.centeredText(this.font, Component.translatable("screen.valorant.clove_ruse.subtitle"), this.width / 2, top + MAP_SIZE + 12, 0xC8C8D8);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void renderMapTiles(GuiGraphicsExtractor graphics, int left, int top) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        ClientLevel level = minecraft.level;
        double playerX = minecraft.player.getX();
        double playerZ = minecraft.player.getZ();
        int tile = MAP_SIZE / SAMPLE_COUNT;
        int lastHeight = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)Math.floor(playerX), (int)Math.floor(playerZ));
        for (int z = 0; z < SAMPLE_COUNT; z++) {
            for (int x = 0; x < SAMPLE_COUNT; x++) {
                double offsetX = ((x + 0.5D) / SAMPLE_COUNT * 2.0D - 1.0D) * RANGE;
                double offsetZ = ((z + 0.5D) / SAMPLE_COUNT * 2.0D - 1.0D) * RANGE;
                int worldX = (int)Math.floor(playerX + offsetX);
                int worldZ = (int)Math.floor(playerZ + offsetZ);
                int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ);
                BlockPos pos = new BlockPos(worldX, height - 1, worldZ);
                BlockState state = level.getBlockState(pos);
                MapColor mapColor = state.getMapColor(level, pos);
                int color = mapColor == MapColor.NONE ? 0xFF242432 : mapColor.col;
                float shade = 0.86F + Math.max(-0.16F, Math.min(0.18F, (height - lastHeight) * 0.035F));
                int red = Math.min(255, Math.max(0, (int)(ARGB.red(color) * shade)));
                int green = Math.min(255, Math.max(0, (int)(ARGB.green(color) * shade)));
                int blue = Math.min(255, Math.max(0, (int)(ARGB.blue(color) * shade)));
                color = ARGB.color(190, red, green, blue);
                graphics.fill(left + x * tile, top + z * tile, left + (x + 1) * tile, top + (z + 1) * tile, color);
                lastHeight = height;
            }
        }
    }

    private void renderEntityIcons(GuiGraphicsExtractor graphics, int left, int top) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        Player player = minecraft.player;
        AABB area = new AABB(player.getX() - RANGE, player.getY() - 12.0D, player.getZ() - RANGE, player.getX() + RANGE, player.getY() + 12.0D, player.getZ() + RANGE);
        for (LivingEntity entity : minecraft.level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
            double offsetX = entity.getX() - player.getX();
            double offsetZ = entity.getZ() - player.getZ();
            if (offsetX * offsetX + offsetZ * offsetZ > RANGE * RANGE) {
                continue;
            }
            int x = left + MAP_SIZE / 2 + (int)Math.round(offsetX / RANGE * (MAP_SIZE / 2.0D));
            int y = top + MAP_SIZE / 2 + (int)Math.round(offsetZ / RANGE * (MAP_SIZE / 2.0D));
            if (entity instanceof AbstractClientPlayer clientPlayer) {
                PlayerFaceExtractor.extractRenderState(graphics, clientPlayer.getSkin(), x - 5, y - 5, 10);
                graphics.fill(x - 6, y - 6, x + 6, y - 5, 0xFFFFFFFF);
                graphics.fill(x - 6, y + 5, x + 6, y + 6, 0xFFFFFFFF);
                graphics.fill(x - 6, y - 6, x - 5, y + 6, 0xFFFFFFFF);
                graphics.fill(x + 5, y - 6, x + 6, y + 6, 0xFFFFFFFF);
            } else {
                int color = entity == player ? 0xFFFF66FF : hostileColor(entity);
                graphics.fill(x - 4, y - 4, x + 5, y + 5, 0xEE000000);
                graphics.fill(x - 3, y - 3, x + 4, y + 4, color);
                graphics.text(this.font, "M", x - 2, y - 4, 0xFF101018, false);
            }
        }
    }

    private static int hostileColor(Entity entity) {
        return entity.getType().getCategory().isFriendly() ? 0xFF5CE1A6 : 0xFFFF6B6B;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isBeforePic) {
        int left = this.width / 2 - MAP_SIZE / 2;
        int top = this.height / 2 - MAP_SIZE / 2;
        int button = event.button();
        double mouseX = event.x();
        double mouseY = event.y();
        if (button == 0 && isInsideMap(mouseX, mouseY, left, top)) {
            double offsetX = (mouseX - (this.width / 2.0D)) / (MAP_SIZE / 2.0D) * RANGE;
            double offsetZ = (mouseY - (this.height / 2.0D)) / (MAP_SIZE / 2.0D) * RANGE;
            ValorantNetwork.sendToServer(new CloveRusePayload(offsetX, offsetZ));
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.mouseClicked(event, isBeforePic);
    }

    private static boolean isInsideMap(double mouseX, double mouseY, int left, int top) {
        return mouseX >= left && mouseX <= left + MAP_SIZE && mouseY >= top && mouseY <= top + MAP_SIZE;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}