package com.strangesmell.valorant.phoenix.curveball;

import net.minecraft.network.FriendlyByteBuf;

public record PhoenixCurveballFlashPayload(int ticks) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<PhoenixCurveballFlashPayload> TYPE = new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(net.minecraft.resources.Identifier.fromNamespaceAndPath("valorant", "phoenix_curveball_flash"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, PhoenixCurveballFlashPayload> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of(
        (buffer, payload) -> buffer.writeVarInt(payload.ticks),
        buffer -> new PhoenixCurveballFlashPayload(buffer.readVarInt())
    );

    @Override
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return TYPE;
    }
}
