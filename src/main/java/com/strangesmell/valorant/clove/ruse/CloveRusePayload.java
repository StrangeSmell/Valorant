package com.strangesmell.valorant.clove.ruse;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CloveRusePayload(double offsetX, double offsetZ) implements CustomPacketPayload {
    public static final Type<CloveRusePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(VALORANT.MODID, "clove_ruse"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CloveRusePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeDouble(payload.offsetX());
                buffer.writeDouble(payload.offsetZ());
            },
            buffer -> new CloveRusePayload(buffer.readDouble(), buffer.readDouble())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
