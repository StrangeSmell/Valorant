package com.strangesmell.valorant.sage.resurrection;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SageResurrectionEntity extends Entity implements ItemSupplier {
    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(
            SageResurrectionEntity.class,
            EntityDataSerializers.STRING
    );
    public static final EntityDataAccessor<String> OWNER_NAME = SynchedEntityData.defineId(
            SageResurrectionEntity.class,
            EntityDataSerializers.STRING
    );
    private static final int LIFE_TIME = 20 * 60;
    private static final int REVIVE_PROTECTION_TIME = 60;
    private static final Set<UUID> ALLOW_DEATH = new HashSet<>();

    private UUID ownerUuid;
    private String ownerName = "";

    public SageResurrectionEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
        this.setCustomNameVisible(true);
    }

    public SageResurrectionEntity(Level level, double x, double y, double z, Player owner) {
        this(VALORANT.SAGE_RESURRECTION.get(), level);
        this.setOwner(owner);
        this.setPos(x, y, z);
        this.setCustomNameVisible(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_UUID, "");
        builder.define(OWNER_NAME, "");
    }

    @Override
    public void tick() {
        super.tick();
        String name = this.getOwnerName();
        if (!name.isEmpty()) {
            this.setCustomName(Component.literal(name));
        }

        if (this.level().isClientSide()) {
            return;
        }

        ServerPlayer owner = this.getOwnerPlayer();
        if (owner == null) {
            this.discard();
            return;
        }
        if (this.tickCount > LIFE_TIME) {
            expireDownedOwner(owner);
            this.discard();
            return;
        }

        if (owner.isAlive() && owner.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            this.discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {}

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {}    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }




    public boolean isPickable() {
        return true;
    }

        public boolean isInvulnerableTo2(DamageSource source) {
        return true;
    }

    @Override
    public ItemStack getItem() {
        return Items.PLAYER_HEAD.getDefaultInstance();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 clickPos) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!itemStack.is(VALORANT.SAGE_RESURRECTION_ITEM.get())) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer reviver)) {
            return InteractionResult.PASS;
        }

        if (!reviveFromInteraction(reviver, this)) {
            return InteractionResult.PASS;
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), VALORANT.SAGE_RESURRECTION_USE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        return InteractionResult.SUCCESS;
    }

    public void setOwner(Player owner) {
        this.ownerUuid = owner.getUUID();
        this.ownerName = owner.getName().getString();
        this.entityData.set(OWNER_UUID, this.ownerUuid.toString());
        this.entityData.set(OWNER_NAME, this.ownerName);
    }

    public String getOwnerName() {
        String syncedName = this.entityData.get(OWNER_NAME);
        if (!syncedName.isEmpty()) {
            this.ownerName = syncedName;
            return syncedName;
        }
        return this.ownerName;
    }

    public boolean isOwner(Player player) {
        return this.ownerUuid != null && this.ownerUuid.equals(player.getUUID());
    }

    public static boolean shouldAllowDeath(Player player) {
        return ALLOW_DEATH.remove(player.getUUID());
    }

    public ServerPlayer getOwnerPlayer() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        UUID uuid = this.ownerUuid;
        if (uuid == null) {
            String value = this.entityData.get(OWNER_UUID);
            if (!value.isEmpty()) {
                uuid = UUID.fromString(value);
                this.ownerUuid = uuid;
            }
        }
        return uuid == null ? null : serverLevel.getServer().getPlayerList().getPlayer(uuid);
    }

    public static boolean reviveFromInteraction(ServerPlayer reviver, SageResurrectionEntity interaction) {
        ServerPlayer downed = interaction.getOwnerPlayer();
        if (downed == null) {
            return false;
        }
        if (downed.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            downed.setGameMode(GameType.SURVIVAL);
        }
        downed.setDeltaMovement(Vec3.ZERO);
        downed.fallDistance = 0.0F;
        downed.setHealth(downed.getMaxHealth());
        downed.teleportTo(interaction.getX(), interaction.getY(), interaction.getZ());
        downed.invulnerableTime = REVIVE_PROTECTION_TIME;
        downed.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, REVIVE_PROTECTION_TIME, 2, false, false, true));
        downed.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false, true));
        ServerLevel level = (ServerLevel) interaction.level();
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, interaction.getX(), interaction.getY() + 0.8D, interaction.getZ(), 32, 0.7D, 0.8D, 0.7D, 0.08D);
        interaction.discard();
        return true;
    }

    private static void expireDownedOwner(ServerPlayer owner) {
        if (owner.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            owner.setGameMode(GameType.SURVIVAL);
        }
        ALLOW_DEATH.add(owner.getUUID());
        owner.setHealth(1.0F);
        if (owner.level() instanceof ServerLevel sl) { owner.kill(sl); }
    }
}