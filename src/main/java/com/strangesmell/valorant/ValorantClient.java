package com.strangesmell.valorant;

import com.strangesmell.valorant.clove.ruse.CloveRuseScreen;
import com.strangesmell.valorant.jett.bladestorm.JettBladeStormPrimaryFirePayload;
import com.strangesmell.valorant.jett.bladestorm.JettBladeStormSecondaryFirePayload;
import com.strangesmell.valorant.leizhi.bomb.BombEntity;
import com.strangesmell.valorant.leizhi.bomb.SmellBombEntity;
import com.strangesmell.valorant.leizhi.blastpack.BlastPackEntity;
import com.strangesmell.valorant.leizhi.boombot.BoomBotEntity;
import com.strangesmell.valorant.phoenix.blaze.PhoenixBlazeWallEntity;
import com.strangesmell.valorant.skillbar.ValorantSkillBar;
import com.strangesmell.valorant.skillbar.ValorantSkillBarScreen;
import com.strangesmell.valorant.skillbar.ValorantSkillUsePayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public class ValorantClient {
    private static final KeyMapping.Category VALORANT_CATEGORY = new KeyMapping.Category(Identifier.withDefaultNamespace("key.categories.valorant"));
    public static final KeyMapping SKILL_BAR_KEY = new KeyMapping("key.valorant.skill_bar", InputConstants.Type.KEYSYM, InputConstants.KEY_G, VALORANT_CATEGORY);
    public static final KeyMapping SKILL_SLOT_1_KEY = new KeyMapping("key.valorant.skill_slot_1", InputConstants.Type.KEYSYM, InputConstants.KEY_Z, VALORANT_CATEGORY);
    public static final KeyMapping SKILL_SLOT_2_KEY = new KeyMapping("key.valorant.skill_slot_2", InputConstants.Type.KEYSYM, InputConstants.KEY_X, VALORANT_CATEGORY);
    public static final KeyMapping SKILL_SLOT_3_KEY = new KeyMapping("key.valorant.skill_slot_3", InputConstants.Type.KEYSYM, InputConstants.KEY_V, VALORANT_CATEGORY);
    public static final KeyMapping SKILL_SLOT_4_KEY = new KeyMapping("key.valorant.skill_slot_4", InputConstants.Type.KEYSYM, InputConstants.KEY_B, VALORANT_CATEGORY);
    private static final Set<Integer> LOOPING_ENTITY_SOUNDS = new HashSet<>();
    private static final boolean[] SKILL_KEY_DOWN = new boolean[ValorantSkillBar.HUD_SIZE];
    private static int activeMouseSkillSlot = -1;
    private static int bladeStormShotsRemaining;
    private static boolean sageBarrierRotated;
    private static boolean sageBarrierPending;
    private static int tailwindReadyTicks;
    private static boolean lastPrimaryDown;
    private static boolean lastSecondaryDown;

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            LOOPING_ENTITY_SOUNDS.clear();
            tailwindReadyTicks = 0;
            return;
        }
        if (tailwindReadyTicks > 0) {
            tailwindReadyTicks--;
        }

        handleSkillBarKeys(minecraft);
        LOOPING_ENTITY_SOUNDS.removeIf(id -> {
            Entity entity = minecraft.level.getEntity(id);
            return entity == null || entity.isRemoved();
        });

        for (Entity entity : minecraft.level.entitiesForRendering()) {
            SoundEvent loopSound = null;
            if (entity instanceof BombEntity || entity instanceof SmellBombEntity) {
                loopSound = Valorant.LEIZHI_PAINT_SHELLS_LOOP.get();
            } else if (entity instanceof BoomBotEntity) {
                loopSound = Valorant.LEIZHI_BOOM_BOT_LOOP.get();
            } else if (entity instanceof BlastPackEntity) {
                loopSound = Valorant.LEIZHI_BLAST_PACK_LOOP.get();
            } else if (entity instanceof PhoenixBlazeWallEntity) {
                loopSound = Valorant.PHOENIX_BLAZE_LOOP.get();
            }

            if (loopSound != null && LOOPING_ENTITY_SOUNDS.add(entity.getId())) {
                minecraft.getSoundManager().play(new LeizhiLoopSoundInstance(loopSound, SoundSource.NEUTRAL, entity, net.minecraft.util.RandomSource.create().nextLong()));
            }
        }
    }

    @SubscribeEvent
    static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            return;
        }
        handleSkillBarKeys(minecraft);
    }

    @SubscribeEvent
    static void onRenderCrosshair(RenderGuiLayerEvent.Post event) {
        if (event.getName() == VanillaGuiLayers.CROSSHAIR) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && false) {
                /* event not cancelable in 26.1 */
            }
        }
    }

    @SubscribeEvent
    static void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (event.getName() == VanillaGuiLayers.HOTBAR) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null) {
                return;
            }
            GuiGraphicsExtractor graphics = event.getGuiGraphics();
            handleMouseSkill(minecraft, player);
            renderSkillBarHud(minecraft, graphics, player);
        }
    }

    private static void handleSkillBarKeys(Minecraft minecraft) {
        if (minecraft.screen != null) {
            return;
        }
        KeyMapping[] keys = skillKeys();
        for (int slot = 0; slot < ValorantSkillBar.HUD_SIZE; slot++) {
            boolean down = keys[slot].isDown();
            handleSkillKey(slot, down);
        }
        if (SKILL_BAR_KEY.consumeClick()) {
            ValorantSkillBarScreen.open();
        }
    }

    private static void handleMouseSkill(Minecraft minecraft, LocalPlayer player) {
        if (minecraft.screen != null) {
            activeMouseSkillSlot = -1;
            bladeStormShotsRemaining = 0;
            return;
        }
        boolean primary = minecraft.options.keyAttack.isDown();
        boolean secondary = minecraft.options.keyUse.isDown();

        // BladeStorm: always send on edge trigger; server checks isActive
        if (primary && !lastPrimaryDown) {
            ValorantNetwork.sendToServer(JettBladeStormPrimaryFirePayload.INSTANCE);
        }
        if (secondary && !lastSecondaryDown) {
            ValorantNetwork.sendToServer(JettBladeStormSecondaryFirePayload.INSTANCE);
        }

        // Sage Barrier mouse follow-up (still needs skillbar slot)
        if (activeMouseSkillSlot >= 0) {
            Identifier itemId = ValorantSkillBar.get(activeMouseSkillSlot);
            if (itemId != null && itemId.equals(itemId(Valorant.SAGE_BARRIER_ITEM.get()))) {
                if (primary && sageBarrierPending) {
                    ValorantNetwork.sendToServer(new com.strangesmell.valorant.sage.barrier.SageBarrierModePayload(sageBarrierRotated));
                    sageBarrierPending = false;
                    sageBarrierRotated = false;
                    activeMouseSkillSlot = -1;
                }
            }
        }

        lastPrimaryDown = primary;
        lastSecondaryDown = secondary;
    }

    private static void handleSkillKey(int slot, boolean down) {
        Identifier itemId = ValorantSkillBar.get(slot);
        if (down && !SKILL_KEY_DOWN[slot]) {
            if (itemId != null) {
                ValorantNetwork.sendToServer(new ValorantSkillUsePayload(itemId, ValorantSkillUsePayload.Action.PRESS));
                if (itemId.equals(itemId(Valorant.CLOVE_RUSE_ITEM.get()))) {
                    CloveRuseScreen.openRuseScreen();
                }
                if (itemId.equals(itemId(Valorant.JETT_TAILWIND_ITEM.get()))) {
                    tailwindReadyTicks = 150;
                }
                if (itemId.equals(itemId(Valorant.SAGE_BARRIER_ITEM.get()))) {
                    if (sageBarrierPending) {
                        sageBarrierRotated = !sageBarrierRotated;
                    } else {
                        sageBarrierPending = true;
                        sageBarrierRotated = false;
                    }
                }
                if (isMouseFollowupSkill(itemId)) {
                    activeMouseSkillSlot = slot;
                    if (itemId.equals(itemId(Valorant.JETT_BLADE_STORM_ITEM.get()))) {
                        bladeStormShotsRemaining = 5;
                    }
                }
            }
        } else if (!down && SKILL_KEY_DOWN[slot]) {
            if (itemId != null) {
                ValorantNetwork.sendToServer(new ValorantSkillUsePayload(itemId, ValorantSkillUsePayload.Action.RELEASE));
            }
        }
        SKILL_KEY_DOWN[slot] = down;
    }

    private static void renderSkillBarHud(Minecraft minecraft, GuiGraphicsExtractor graphics, LocalPlayer player) {
        KeyMapping[] keys = skillKeys();
        int startX = graphics.guiWidth() / 2 - 91;
        int spacing = 52;
        int y = graphics.guiHeight() - 72;
        for (int slot = 0; slot < ValorantSkillBar.HUD_SIZE; slot++) {
            int x = startX + slot * spacing;
            ItemStack stack = ValorantSkillBar.getDisplayStack(slot);
            if (!stack.isEmpty()) {
                graphics.item(stack, x, y);
                int count = countItem(player, stack.getItem());
                graphics.itemDecorations(minecraft.font, stack, x, y, String.valueOf(count));
                Component key = keys[slot].getTranslatedKeyMessage();
                graphics.centeredText(minecraft.font, key, x + 8, y + 19, 0xD8D8D8);
            }
        }
    }

    private static int countItem(LocalPlayer player, Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static KeyMapping[] skillKeys() {
        return new KeyMapping[]{SKILL_SLOT_1_KEY, SKILL_SLOT_2_KEY, SKILL_SLOT_3_KEY, SKILL_SLOT_4_KEY};
    }

    private static boolean isMouseFollowupSkill(Identifier itemId) {
        return itemId != null && (itemId.equals(itemId(Valorant.SAGE_BARRIER_ITEM.get()))
                || itemId.equals(itemId(Valorant.JETT_BLADE_STORM_ITEM.get())));
    }

    private static Identifier itemId(Item item) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
    }

    private static Vec3 getDashDirection() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return Vec3.ZERO;
        }
        float forward = (minecraft.options.keyUp.isDown() ? 1.0F : 0.0F) - (minecraft.options.keyDown.isDown() ? 1.0F : 0.0F);
        float strafe = (minecraft.options.keyRight.isDown() ? 1.0F : 0.0F) - (minecraft.options.keyLeft.isDown() ? 1.0F : 0.0F);
        Vec3 look = minecraft.player.getLookAngle();
        Vec3 forwardVec = new Vec3(look.x, 0.0D, look.z);
        if (forwardVec.lengthSqr() < 1.0E-4D) {
            forwardVec = Vec3.directionFromRotation(0.0F, minecraft.player.getYRot());
        }
        forwardVec = forwardVec.normalize();
        Vec3 rightVec = new Vec3(-forwardVec.z, 0.0D, forwardVec.x);
        Vec3 direction = forwardVec.scale(forward).add(rightVec.scale(strafe));
        if (direction.lengthSqr() < 1.0E-4D) {
            Vec3 motion = minecraft.player.getDeltaMovement();
            direction = new Vec3(motion.x, 0.0D, motion.z);
        }
        if (direction.lengthSqr() < 1.0E-4D) {
            return forwardVec;
        }
        return direction.normalize();
    }
}
