package com.strangesmell.valorant.skillbar;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public record ValorantSkillUsePayload(Identifier itemId, Action action) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, ValorantSkillUsePayload> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of(
        (buffer, payload) -> {
            buffer.writeIdentifier(payload.itemId());
            buffer.writeEnum(payload.action());
        },
        buffer -> new ValorantSkillUsePayload(buffer.readIdentifier(), buffer.readEnum(Action.class))
    );

        public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ValorantSkillUsePayload> TYPE = new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("valorant", "skill_use"));

    @Override
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Action {
        PRESS,
        RELEASE,
        PRIMARY,
        SECONDARY
    }
}
