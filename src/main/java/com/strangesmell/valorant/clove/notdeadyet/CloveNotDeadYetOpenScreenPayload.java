package com.strangesmell.valorant.clove.notdeadyet;

import com.strangesmell.valorant.VALORANT;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CloveNotDeadYetOpenScreenPayload() implements CustomPacketPayload {
    public static final CloveNotDeadYetOpenScreenPayload INSTANCE = new CloveNotDeadYetOpenScreenPayload();
    public static final Type<CloveNotDeadYetOpenScreenPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(VALORANT.MODID, "clove_not_dead_yet_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CloveNotDeadYetOpenScreenPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
