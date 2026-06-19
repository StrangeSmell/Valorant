package com.strangesmell.valorant.jett.bladestorm;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record JettBladeStormPrimaryFirePayload() implements CustomPacketPayload {
    public static final JettBladeStormPrimaryFirePayload INSTANCE = new JettBladeStormPrimaryFirePayload();
    public static final Type<JettBladeStormPrimaryFirePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(VALORANT.MODID, "jett_blade_storm_primary_fire"));
    public static final StreamCodec<RegistryFriendlyByteBuf, JettBladeStormPrimaryFirePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}