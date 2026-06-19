package com.strangesmell.valorant.jett.bladestorm;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record JettBladeStormSecondaryFirePayload() implements CustomPacketPayload {
    public static final JettBladeStormSecondaryFirePayload INSTANCE = new JettBladeStormSecondaryFirePayload();
    public static final Type<JettBladeStormSecondaryFirePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(VALORANT.MODID, "jett_blade_storm_secondary_fire"));
    public static final StreamCodec<RegistryFriendlyByteBuf, JettBladeStormSecondaryFirePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}