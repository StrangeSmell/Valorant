package com.strangesmell.valorant.sage.barrier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SageBarrierModePayload(boolean rotated) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SageBarrierModePayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("valorant", "sage_barrier_mode"));
    public static final StreamCodec<FriendlyByteBuf, SageBarrierModePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, SageBarrierModePayload::rotated,
        SageBarrierModePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}