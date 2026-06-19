package com.strangesmell.valorant;

import com.strangesmell.valorant.clove.meddle.CloveMeddleEntity;
import com.strangesmell.valorant.clove.meddle.CloveMeddleItem;
import com.strangesmell.valorant.clove.notdeadyet.CloveNotDeadYetItem;
import com.strangesmell.valorant.clove.notdeadyet.CloveNotDeadYetOpenScreenPayload;
import com.strangesmell.valorant.clove.notdeadyet.CloveNotDeadYetPayload;
import com.strangesmell.valorant.clove.pickmeup.ClovePickMeUpItem;
import com.strangesmell.valorant.clove.ruse.CloveRuseItem;
import com.strangesmell.valorant.clove.ruse.CloveRusePayload;
import com.strangesmell.valorant.clove.ruse.CloveRuseSmokeEntity;
import com.strangesmell.valorant.clove.ruse.CloveRuseTracker;
import com.strangesmell.valorant.jett.bladestorm.JettBladeStormItem;
import com.strangesmell.valorant.jett.bladestorm.JettBladeStormKnifeEntity;
import com.strangesmell.valorant.jett.bladestorm.JettBladeStormOrbitKnifeEntity;
import com.strangesmell.valorant.jett.bladestorm.JettBladeStormPrimaryFirePayload;
import com.strangesmell.valorant.jett.bladestorm.JettBladeStormSecondaryFirePayload;
import com.strangesmell.valorant.phoenix.curveball.PhoenixCurveballFlashPayload;
import com.strangesmell.valorant.jett.bladestorm.JettBladeStormTracker;
import com.strangesmell.valorant.jett.cloudburst.JettCloudburstEntity;
import com.strangesmell.valorant.jett.cloudburst.JettCloudburstItem;
import com.strangesmell.valorant.jett.cloudburst.JettCloudburstSmokeEntity;
import com.strangesmell.valorant.jett.tailwind.JettTailwindItem;
import com.strangesmell.valorant.jett.updraft.JettUpdraftItem;
import com.strangesmell.valorant.leizhi.bomb.BombEntity;
import com.strangesmell.valorant.leizhi.bomb.BombItem;
import com.strangesmell.valorant.leizhi.blastpack.BlastPackEntity;
import com.strangesmell.valorant.leizhi.blastpack.BlastPackItem;
import com.strangesmell.valorant.leizhi.boombot.BoomBotEntity;
import com.strangesmell.valorant.leizhi.boombot.BoomBotItem;
import com.strangesmell.valorant.leizhi.bigbomb.BigBombEntity;
import com.strangesmell.valorant.leizhi.bigbomb.BigBombItem;
import com.strangesmell.valorant.sage.heal.SageHealItem;
import com.strangesmell.valorant.sage.barrier.SageBarrierEntity;
import com.strangesmell.valorant.sage.barrier.SageBarrierItem;
import com.strangesmell.valorant.sage.resurrection.SageResurrectionEntity;
import com.strangesmell.valorant.sage.slow.SageSlowFieldEntity;
import com.strangesmell.valorant.sage.slow.SageSlowItem;
import com.strangesmell.valorant.sage.slow.SageSlowOrbEntity;
import com.strangesmell.valorant.sage.resurrection.SageResurrectionItem;
import com.strangesmell.valorant.phoenix.blaze.PhoenixBlazeItem;
import com.strangesmell.valorant.phoenix.blaze.PhoenixBlazeWallEntity;
import com.strangesmell.valorant.phoenix.curveball.PhoenixCurveballEntity;
import com.strangesmell.valorant.phoenix.curveball.PhoenixCurveballItem;
import com.strangesmell.valorant.phoenix.hothands.PhoenixHotHandsEntity;
import com.strangesmell.valorant.phoenix.hothands.PhoenixHotHandsItem;
import com.strangesmell.valorant.phoenix.hothands.PhoenixHotHandsZoneEntity;
import com.strangesmell.valorant.phoenix.runitback.PhoenixRunItBackAnchorEntity;
import com.strangesmell.valorant.phoenix.runitback.PhoenixRunItBackItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Valorant.MODID)
public class Valorant {

    public static Map<BlockPos, Integer> TimePos = new HashMap<>();

    public static final String MODID = "valorant";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredItem<BombItem> LEIZHIBOMB_ITEM = ITEMS.registerItem("leizhibomb_item", BombItem::new);
    public static final DeferredItem<BigBombItem> BIGBOMB_ITEM = ITEMS.registerItem("bigbomb_item", BigBombItem::new);
    public static final DeferredItem<BoomBotItem> BOOMBOT_ITEM = ITEMS.registerItem("boombot_item", BoomBotItem::new);
    public static final DeferredItem<BlastPackItem> BLASTPACK_ITEM = ITEMS.registerItem("blastpack_item", BlastPackItem::new);
    public static final DeferredItem<SageHealItem> SAGE_HEAL_ITEM = ITEMS.registerItem("sage_heal_item", SageHealItem::new, properties -> properties.stacksTo(1));
    public static final DeferredItem<SageBarrierItem> SAGE_BARRIER_ITEM = ITEMS.registerItem("sage_barrier_item", SageBarrierItem::new, properties -> properties.stacksTo(1));
    public static final DeferredItem<SageSlowItem> SAGE_SLOW_ITEM = ITEMS.registerItem("sage_slow_item", SageSlowItem::new, properties -> properties.stacksTo(2));
    public static final DeferredItem<SageResurrectionItem> SAGE_RESURRECTION_ITEM = ITEMS.registerItem("sage_resurrection_item", SageResurrectionItem::new, properties -> properties.stacksTo(1));
    public static final DeferredItem<PhoenixCurveballItem> PHOENIX_CURVEBALL_ITEM = ITEMS.registerItem("phoenix_curveball_item", PhoenixCurveballItem::new);
    public static final DeferredItem<PhoenixHotHandsItem> PHOENIX_HOT_HANDS_ITEM = ITEMS.registerItem("phoenix_hot_hands_item", PhoenixHotHandsItem::new);
    public static final DeferredItem<PhoenixBlazeItem> PHOENIX_BLAZE_ITEM = ITEMS.registerItem("phoenix_blaze_item", PhoenixBlazeItem::new);
    public static final DeferredItem<PhoenixRunItBackItem> PHOENIX_RUN_IT_BACK_ITEM = ITEMS.registerItem("phoenix_run_it_back_item", PhoenixRunItBackItem::new);
    public static final DeferredItem<CloveRuseItem> CLOVE_RUSE_ITEM = ITEMS.registerItem("clove_ruse_item", CloveRuseItem::new);
    public static final DeferredItem<CloveMeddleItem> CLOVE_MEDDLE_ITEM = ITEMS.registerItem("clove_meddle_item", CloveMeddleItem::new);
    public static final DeferredItem<ClovePickMeUpItem> CLOVE_PICK_ME_UP_ITEM = ITEMS.registerItem("clove_pick_me_up_item", ClovePickMeUpItem::new);
    public static final DeferredItem<CloveNotDeadYetItem> CLOVE_NOT_DEAD_YET_ITEM = ITEMS.registerItem("clove_not_dead_yet_item", CloveNotDeadYetItem::new);
    public static final DeferredItem<JettCloudburstItem> JETT_CLOUDBURST_ITEM = ITEMS.registerItem("jett_cloudburst_item", JettCloudburstItem::new);
    public static final DeferredItem<JettUpdraftItem> JETT_UPDRAFT_ITEM = ITEMS.registerItem("jett_updraft_item", JettUpdraftItem::new);
    public static final DeferredItem<JettTailwindItem> JETT_TAILWIND_ITEM = ITEMS.registerItem("jett_tailwind_item", JettTailwindItem::new);
    public static final DeferredItem<JettBladeStormItem> JETT_BLADE_STORM_ITEM = ITEMS.registerItem("jett_blade_storm_item", JettBladeStormItem::new);
    public static final Supplier<SimpleParticleType> CLOVE_RUSE_SMOKE_PARTICLE = PARTICLE_TYPES.register("clove_ruse_smoke_particle", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> JETT_CLOUD_TRAIL_PARTICLE = PARTICLE_TYPES.register("jett_cloud_trail_particle", () -> new SimpleParticleType(false));

    public static final Supplier<SoundEvent> LEIZHI_PAINT_SHELLS_USE = registerSound("leizhi.paint_shells.use");
    public static final Supplier<SoundEvent> LEIZHI_PAINT_SHELLS_LOOP = registerSound("leizhi.paint_shells.loop");
    public static final Supplier<SoundEvent> LEIZHI_PAINT_SHELLS_EXPLODE = registerSound("leizhi.paint_shells.explode");
    public static final Supplier<SoundEvent> LEIZHI_SHOWSTOPPER_USE = registerSound("leizhi.showstopper.use");
    public static final Supplier<SoundEvent> LEIZHI_SHOWSTOPPER_EXPLODE = registerSound("leizhi.showstopper.explode");
    public static final Supplier<SoundEvent> LEIZHI_BOOM_BOT_USE = registerSound("leizhi.boom_bot.use");
    public static final Supplier<SoundEvent> LEIZHI_BOOM_BOT_EXPLODE = registerSound("leizhi.boom_bot.explode");
    public static final Supplier<SoundEvent> LEIZHI_BOOM_BOT_LOOP = registerSound("leizhi.boom_bot.loop");
    public static final Supplier<SoundEvent> LEIZHI_BLAST_PACK_USE = registerSound("leizhi.blast_pack.use");
    public static final Supplier<SoundEvent> LEIZHI_BLAST_PACK_LOOP = registerSound("leizhi.blast_pack.loop");
    public static final Supplier<SoundEvent> LEIZHI_BLAST_PACK_STICK = registerSound("leizhi.blast_pack.stick");
    public static final Supplier<SoundEvent> LEIZHI_BLAST_PACK_EXPLODE = registerSound("leizhi.blast_pack.explode");
    public static final Supplier<SoundEvent> SAGE_HEALING_ORB_USE = registerSound("sage.healing_orb.use");
    public static final Supplier<SoundEvent> SAGE_BARRIER_ORB_USE = registerSound("sage.barrier_orb.use");
    public static final Supplier<SoundEvent> SAGE_BARRIER_ORB_DISAPPEAR = registerSound("sage.barrier_orb.disappear");
    public static final Supplier<SoundEvent> SAGE_SLOW_ORB_USE = registerSound("sage.slow_orb.use");
    public static final Supplier<SoundEvent> SAGE_SLOW_ORB_DISAPPEAR = registerSound("sage.slow_orb.disappear");
    public static final Supplier<SoundEvent> SAGE_RESURRECTION_USE = registerSound("sage.resurrection.use");
    public static final Supplier<SoundEvent> PHOENIX_CURVEBALL_USE = registerSound("phoenix.curveball.use");
    public static final Supplier<SoundEvent> PHOENIX_CURVEBALL_EXPLODE = registerSound("phoenix.curveball.explode");
    public static final Supplier<SoundEvent> PHOENIX_HOT_HANDS_USE = registerSound("phoenix.hot_hands.use");
    public static final Supplier<SoundEvent> PHOENIX_BLAZE_USE = registerSound("phoenix.blaze.use");
    public static final Supplier<SoundEvent> PHOENIX_BLAZE_LOOP = registerSound("phoenix.blaze.loop");
    public static final Supplier<SoundEvent> PHOENIX_BLAZE_END = registerSound("phoenix.blaze.end");
    public static final Supplier<SoundEvent> PHOENIX_RUN_IT_BACK_USE = registerSound("phoenix.run_it_back.use");
    public static final Supplier<SoundEvent> CLOVE_RUSE_USE = registerSound("clove.ruse.use");
    public static final Supplier<SoundEvent> CLOVE_RUSE_RELEASE = registerSound("clove.ruse.release");
    public static final Supplier<SoundEvent> CLOVE_MEDDLE_USE = registerSound("clove.meddle.use");
    public static final Supplier<SoundEvent> CLOVE_MEDDLE_READY = registerSound("clove.meddle.ready");
    public static final Supplier<SoundEvent> CLOVE_MEDDLE_EXPLODE = registerSound("clove.meddle.explode");
    public static final Supplier<SoundEvent> CLOVE_PICK_ME_UP_USE = registerSound("clove.pick_me_up.use");
    public static final Supplier<SoundEvent> CLOVE_PICK_ME_UP_PROC = registerSound("clove.pick_me_up.proc");
    public static final Supplier<SoundEvent> CLOVE_NOT_DEAD_YET_USE = registerSound("clove.not_dead_yet.use");
    public static final Supplier<SoundEvent> JETT_CLOUDBURST_USE = registerSound("jett.cloudburst.use");
    public static final Supplier<SoundEvent> JETT_UPDRAFT_USE = registerSound("jett.updraft.use");
    public static final Supplier<SoundEvent> JETT_TAILWIND_READY = registerSound("jett.tailwind.ready");
    public static final Supplier<SoundEvent> JETT_TAILWIND_DASH = registerSound("jett.tailwind.dash");
    public static final Supplier<SoundEvent> JETT_BLADE_STORM_READY = registerSound("jett.blade_storm.ready");
    public static final Supplier<SoundEvent> JETT_BLADE_STORM_THROW = registerSound("jett.blade_storm.throw");
    public static final Supplier<SoundEvent> JETT_BLADE_STORM_THROW_ALL = registerSound("jett.blade_storm.throw_all");

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(Valorant.MODID);

    public static final Supplier<EntityType<BombEntity>> LEIZHIBOMB = ENTITY_TYPES.register("leizhibomb",
            () -> EntityType.Builder.<BombEntity>of(BombEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F).eyeHeight(0.15F).clientTrackingRange(10).updateInterval(20)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath("valorant", "leizhibomb")
                    ))
    );

    public static final Supplier<EntityType<BombEntity>> SMALLLEIZHIBOMB = ENTITY_TYPES.register("smallleizhibomb",
            () -> EntityType.Builder.<BombEntity>of(BombEntity::new, MobCategory.MISC)
                    .sized(0.15F, 0.15F).eyeHeight(0.075F).clientTrackingRange(10).updateInterval(20)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath("valorant", "smallleizhibomb")
                    ))

    );

    public static final Supplier<EntityType<BigBombEntity>> BIGBOMB = ENTITY_TYPES.register("bigbomb",
            () -> EntityType.Builder.<BigBombEntity>of(BigBombEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.8F).eyeHeight(0.4F).clientTrackingRange(10).updateInterval(10)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath("valorant", "bigbomb")
                    ))
    );

    public static final Supplier<EntityType<BoomBotEntity>> BOOMBOT = ENTITY_TYPES.register("boombot",
            () -> EntityType.Builder.<BoomBotEntity>of(BoomBotEntity::new, MobCategory.MISC)
                    .sized(0.7F, 0.45F).eyeHeight(0.25F).clientTrackingRange(10).updateInterval(2)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath("valorant", "boombot")
                    ))
    );

    public static final Supplier<EntityType<BlastPackEntity>> BLASTPACK = ENTITY_TYPES.register("blastpack",
            () -> EntityType.Builder.<BlastPackEntity>of(BlastPackEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.2F).eyeHeight(0.1F).clientTrackingRange(10).updateInterval(5)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath("valorant", "blastpack")
                    ))
    );

    public static final Supplier<EntityType<SageSlowOrbEntity>> SAGE_SLOW_ORB = ENTITY_TYPES.register("sage_slow_orb",
            () -> EntityType.Builder.<SageSlowOrbEntity>of(SageSlowOrbEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F).eyeHeight(0.175F).clientTrackingRange(10).updateInterval(5)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath("valorant", "sage_slow_orb")
                    ))
    );

    public static final Supplier<EntityType<SageSlowFieldEntity>> SAGE_SLOW_FIELD = ENTITY_TYPES.register("sage_slow_field",
            () -> EntityType.Builder.<SageSlowFieldEntity>of(SageSlowFieldEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F).clientTrackingRange(10).updateInterval(10)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath("valorant", "sage_slow_field")
                    ))
    );

    public static final Supplier<EntityType<SageBarrierEntity>> SAGE_BARRIER = ENTITY_TYPES.register("sage_barrier",
            () -> EntityType.Builder.<SageBarrierEntity>of(SageBarrierEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F).clientTrackingRange(10).updateInterval(20)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath("valorant", "sage_barrier")
                    ))
    );

    public static final Supplier<EntityType<PhoenixCurveballEntity>> PHOENIX_CURVEBALL = ENTITY_TYPES.register("phoenix_curveball",
            () -> EntityType.Builder.<PhoenixCurveballEntity>of(PhoenixCurveballEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F).eyeHeight(0.175F).clientTrackingRange(10).updateInterval(5)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "phoenix_curveball")))
    );

    public static final Supplier<EntityType<PhoenixHotHandsEntity>> PHOENIX_HOT_HANDS = ENTITY_TYPES.register("phoenix_hot_hands",
            () -> EntityType.Builder.<PhoenixHotHandsEntity>of(PhoenixHotHandsEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F).eyeHeight(0.175F).clientTrackingRange(10).updateInterval(5)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "phoenix_hot_hands")))
    );

    public static final Supplier<EntityType<PhoenixHotHandsZoneEntity>> PHOENIX_HOT_HANDS_ZONE = ENTITY_TYPES.register("phoenix_hot_hands_zone",
            () -> EntityType.Builder.<PhoenixHotHandsZoneEntity>of(PhoenixHotHandsZoneEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F).clientTrackingRange(10).updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "phoenix_hot_hands_zone")))
    );

    public static final Supplier<EntityType<PhoenixBlazeWallEntity>> PHOENIX_BLAZE_WALL = ENTITY_TYPES.register("phoenix_blaze_wall",
            () -> EntityType.Builder.<PhoenixBlazeWallEntity>of(PhoenixBlazeWallEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F).clientTrackingRange(10).updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "phoenix_blaze_wall")))
    );

    public static final Supplier<EntityType<SageResurrectionEntity>> SAGE_RESURRECTION = ENTITY_TYPES.register("sage_resurrection",
            () -> EntityType.Builder.<SageResurrectionEntity>of(SageResurrectionEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F).clientTrackingRange(10).updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "sage_resurrection")))
    );

    public static final Supplier<EntityType<PhoenixRunItBackAnchorEntity>> PHOENIX_RUN_IT_BACK_ANCHOR = ENTITY_TYPES.register("phoenix_run_it_back_anchor",
            () -> EntityType.Builder.<PhoenixRunItBackAnchorEntity>of(PhoenixRunItBackAnchorEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F).clientTrackingRange(10).updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "phoenix_run_it_back_anchor")))
    );

    public static final Supplier<EntityType<CloveRuseSmokeEntity>> CLOVE_RUSE_SMOKE = ENTITY_TYPES.register("clove_ruse_smoke",
            () -> EntityType.Builder.<CloveRuseSmokeEntity>of(CloveRuseSmokeEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F).clientTrackingRange(10).updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "clove_ruse_smoke")))
    );

    public static final Supplier<EntityType<CloveMeddleEntity>> CLOVE_MEDDLE = ENTITY_TYPES.register("clove_meddle",
            () -> EntityType.Builder.<CloveMeddleEntity>of(CloveMeddleEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F).eyeHeight(0.175F).clientTrackingRange(10).updateInterval(5)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "clove_meddle")))
    );

    public static final Supplier<EntityType<JettCloudburstEntity>> JETT_CLOUDBURST = ENTITY_TYPES.register("jett_cloudburst",
            () -> EntityType.Builder.<JettCloudburstEntity>of(JettCloudburstEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F).eyeHeight(0.175F).clientTrackingRange(10).updateInterval(5)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "jett_cloudburst")))
    );

    public static final Supplier<EntityType<JettCloudburstSmokeEntity>> JETT_CLOUDBURST_SMOKE = ENTITY_TYPES.register("jett_cloudburst_smoke",
            () -> EntityType.Builder.<JettCloudburstSmokeEntity>of(JettCloudburstSmokeEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F).clientTrackingRange(10).updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "jett_cloudburst_smoke")))
    );

    public static final Supplier<EntityType<JettBladeStormKnifeEntity>> JETT_BLADE_STORM_KNIFE = ENTITY_TYPES.register("jett_blade_storm_knife",
            () -> EntityType.Builder.<JettBladeStormKnifeEntity>of(JettBladeStormKnifeEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).eyeHeight(0.125F).clientTrackingRange(10).updateInterval(2)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "jett_blade_storm_knife")))
    );

    public static final Supplier<EntityType<JettBladeStormOrbitKnifeEntity>> JETT_BLADE_STORM_ORBIT_KNIFE = ENTITY_TYPES.register("jett_blade_storm_orbit_knife",
            () -> EntityType.Builder.<JettBladeStormOrbitKnifeEntity>of(JettBladeStormOrbitKnifeEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).eyeHeight(0.125F).clientTrackingRange(10).updateInterval(2)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("valorant", "jett_blade_storm_orbit_knife")))
    );


    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.valorant"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> JETT_BLADE_STORM_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());


    public Valorant(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        ITEMS.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);

        ENTITY_TYPES.register(modEventBus);

        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);


        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    private static Supplier<SoundEvent> registerSound(String path) {
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MODID, path)));
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(MODID)
                .playToClient(CloveNotDeadYetOpenScreenPayload.TYPE, CloveNotDeadYetOpenScreenPayload.STREAM_CODEC)
                .playToServer(CloveRusePayload.TYPE, CloveRusePayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
                        CloveRuseTracker.queue(level, player, payload.offsetX(), payload.offsetZ());
                        if (!player.getAbilities().instabuild) {
                            com.strangesmell.valorant.ValorantSkillItems.consumeFromInventory(player, CLOVE_RUSE_ITEM.get());
                        }
                    }
                }))
                .playToServer(CloveNotDeadYetPayload.TYPE, CloveNotDeadYetPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        com.strangesmell.valorant.clove.notdeadyet.CloveNotDeadYetTracker.activateFromDeathScreen(player);
                    }
                }))
                .playToServer(JettBladeStormPrimaryFirePayload.TYPE, JettBladeStormPrimaryFirePayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.level() instanceof ServerLevel level && JettBladeStormTracker.isActive(player)) {
                        JettBladeStormTracker.primaryFire(level, player);
                    }
                }))
                .playToServer(JettBladeStormSecondaryFirePayload.TYPE, JettBladeStormSecondaryFirePayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.level() instanceof ServerLevel level && JettBladeStormTracker.isActive(player)) {
                        JettBladeStormTracker.secondaryFire(level, player);
                    }
                }))
                .playToClient(PhoenixCurveballFlashPayload.TYPE, PhoenixCurveballFlashPayload.STREAM_CODEC);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == EXAMPLE_TAB.getKey()) {
            event.accept(LEIZHIBOMB_ITEM);
            event.accept(BIGBOMB_ITEM);
            event.accept(BOOMBOT_ITEM);
            event.accept(BLASTPACK_ITEM);
            event.accept(SAGE_HEAL_ITEM);
            event.accept(SAGE_BARRIER_ITEM);
            event.accept(SAGE_SLOW_ITEM);
            event.accept(SAGE_RESURRECTION_ITEM);
            event.accept(PHOENIX_CURVEBALL_ITEM);
            event.accept(PHOENIX_HOT_HANDS_ITEM);
            event.accept(PHOENIX_BLAZE_ITEM);
            event.accept(PHOENIX_RUN_IT_BACK_ITEM);
            event.accept(CLOVE_RUSE_ITEM);
            event.accept(CLOVE_MEDDLE_ITEM);
            event.accept(CLOVE_PICK_ME_UP_ITEM);
            event.accept(CLOVE_NOT_DEAD_YET_ITEM);
            event.accept(JETT_CLOUDBURST_ITEM);
            event.accept(JETT_UPDRAFT_ITEM);
            event.accept(JETT_TAILWIND_ITEM);
            event.accept(JETT_BLADE_STORM_ITEM);
        }
    }



    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

        LOGGER.info("HELLO from server starting");
    }
}
