package com.strangesmell.valorant.phoenix.curveball;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record PhoenixCurveballFlashPayload(int ticks, double flashX, double flashY, double flashZ) implements CustomPacketPayload {
    public static final Type<PhoenixCurveballFlashPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("valorant", "phoenix_curveball_flash"));
    public static final StreamCodec<FriendlyByteBuf, PhoenixCurveballFlashPayload> STREAM_CODEC = StreamCodec.of(
        (buffer, payload) -> {
            buffer.writeVarInt(payload.ticks);
            buffer.writeDouble(payload.flashX);
            buffer.writeDouble(payload.flashY);
            buffer.writeDouble(payload.flashZ);
        },
        buffer -> new PhoenixCurveballFlashPayload(buffer.readVarInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}