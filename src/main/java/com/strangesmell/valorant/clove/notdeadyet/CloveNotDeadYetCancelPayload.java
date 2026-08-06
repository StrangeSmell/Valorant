package com.strangesmell.valorant.clove.notdeadyet;

import com.strangesmell.valorant.Valorant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CloveNotDeadYetCancelPayload() implements CustomPacketPayload {
    public static final CloveNotDeadYetCancelPayload INSTANCE = new CloveNotDeadYetCancelPayload();
    public static final Type<CloveNotDeadYetCancelPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Valorant.MODID, "clove_not_dead_yet_cancel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CloveNotDeadYetCancelPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}