package com.strangesmell.valorant.clove.notdeadyet;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CloveNotDeadYetPayload() implements CustomPacketPayload {
    public static final CloveNotDeadYetPayload INSTANCE = new CloveNotDeadYetPayload();
    public static final Type<CloveNotDeadYetPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(VALORANT.MODID, "clove_not_dead_yet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CloveNotDeadYetPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
