package com.strangesmell.valorant.jett.tailwind;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record JettTailwindDashPayload(float x, float z) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JettTailwindDashPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("valorant", "jett_tailwind_dash"));
    public static final StreamCodec<FriendlyByteBuf, JettTailwindDashPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, JettTailwindDashPayload::x,
        ByteBufCodecs.FLOAT, JettTailwindDashPayload::z,
        JettTailwindDashPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}