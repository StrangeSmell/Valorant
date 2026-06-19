package com.strangesmell.valorant;

import com.strangesmell.valorant.clove.ruse.CloveRuseSmokeParticle;
import com.strangesmell.valorant.clove.notdeadyet.CloveNotDeadYetOpenScreenPayload;
import com.strangesmell.valorant.clove.notdeadyet.CloveNotDeadYetScreen;
import com.strangesmell.valorant.jett.cloudburst.JettCloudTrailParticle;
import com.strangesmell.valorant.phoenix.curveball.PhoenixCurveballFlashPayload;
import com.strangesmell.valorant.phoenix.curveball.PhoenixCurveballFlashScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import com.strangesmell.valorant.leizhi.bomb.BombRenderer;
import com.strangesmell.valorant.leizhi.bomb.SmallBombRenderer;
import com.strangesmell.valorant.leizhi.bigbomb.BigBombRenderer;
import com.strangesmell.valorant.leizhi.boombot.BoomBotRenderer;
import com.strangesmell.valorant.leizhi.blastpack.BlastPackRenderer;
import com.strangesmell.valorant.jett.bladestorm.JettKnifeRenderer;
import com.strangesmell.valorant.sage.resurrection.SageResurrectionRenderer;
import com.strangesmell.valorant.InvisibleEntityRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = VALORANT.MODID, value = Dist.CLIENT)
public class ValorantClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        VALORANT.LOGGER.info("HELLO FROM CLIENT SETUP");
        VALORANT.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(VALORANT.CLOVE_RUSE_SMOKE_PARTICLE.get(), CloveRuseSmokeParticle.Provider::new);
        event.registerSpriteSet(VALORANT.JETT_CLOUD_TRAIL_PARTICLE.get(), JettCloudTrailParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(VALORANTClient.SKILL_BAR_KEY);
        event.register(VALORANTClient.SKILL_SLOT_1_KEY);
        event.register(VALORANTClient.SKILL_SLOT_2_KEY);
        event.register(VALORANTClient.SKILL_SLOT_3_KEY);
        event.register(VALORANTClient.SKILL_SLOT_4_KEY);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(VALORANT.LEIZHIBOMB.get(), BombRenderer::new);
        event.registerEntityRenderer(VALORANT.SMALLLEIZHIBOMB.get(), SmallBombRenderer::new);
        event.registerEntityRenderer(VALORANT.BIGBOMB.get(), BigBombRenderer::new);
        event.registerEntityRenderer(VALORANT.BOOMBOT.get(), BoomBotRenderer::new);
        event.registerEntityRenderer(VALORANT.BLASTPACK.get(), BlastPackRenderer::new);
        event.registerEntityRenderer(VALORANT.SAGE_SLOW_ORB.get(), BombRenderer::new);
        event.registerEntityRenderer(VALORANT.SAGE_SLOW_FIELD.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(VALORANT.SAGE_BARRIER.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(VALORANT.SAGE_RESURRECTION.get(), SageResurrectionRenderer::new);
        event.registerEntityRenderer(VALORANT.PHOENIX_CURVEBALL.get(), BombRenderer::new);
        event.registerEntityRenderer(VALORANT.PHOENIX_HOT_HANDS.get(), BombRenderer::new);
        event.registerEntityRenderer(VALORANT.PHOENIX_HOT_HANDS_ZONE.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(VALORANT.PHOENIX_BLAZE_WALL.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(VALORANT.PHOENIX_RUN_IT_BACK_ANCHOR.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(VALORANT.CLOVE_RUSE_SMOKE.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(VALORANT.CLOVE_MEDDLE.get(), BombRenderer::new);
        event.registerEntityRenderer(VALORANT.JETT_CLOUDBURST.get(), BombRenderer::new);
        event.registerEntityRenderer(VALORANT.JETT_CLOUDBURST_SMOKE.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(VALORANT.JETT_BLADE_STORM_KNIFE.get(), JettKnifeRenderer::new);
        event.registerEntityRenderer(VALORANT.JETT_BLADE_STORM_ORBIT_KNIFE.get(), JettKnifeRenderer::new);
    }

    @SubscribeEvent
    public static void registerClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(CloveNotDeadYetOpenScreenPayload.TYPE, (IPayloadHandler<CloveNotDeadYetOpenScreenPayload>) (payload, context) ->
            context.enqueueWork(() ->
                Minecraft.getInstance().setScreen(new CloveNotDeadYetScreen())
            )
        );
        event.register(PhoenixCurveballFlashPayload.TYPE, (IPayloadHandler<PhoenixCurveballFlashPayload>) (payload, context) ->
            context.enqueueWork(() ->
                PhoenixCurveballFlashScreen.flash(payload.ticks())
            )
        );
    }
}