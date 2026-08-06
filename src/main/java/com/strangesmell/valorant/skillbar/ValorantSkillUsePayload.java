package com.strangesmell.valorant.skillbar;

import com.strangesmell.valorant.Valorant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ValorantSkillUsePayload(Identifier itemId, Action action) implements CustomPacketPayload {
    public static final Type<ValorantSkillUsePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Valorant.MODID, "skill_use"));
    public static final StreamCodec<FriendlyByteBuf, ValorantSkillUsePayload> STREAM_CODEC = StreamCodec.of(
        (buffer, payload) -> {
            buffer.writeIdentifier(payload.itemId());
            buffer.writeEnum(payload.action());
        },
        buffer -> new ValorantSkillUsePayload(buffer.readIdentifier(), buffer.readEnum(Action.class))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Action {
        PRESS,
        RELEASE,
        PRIMARY,
        SECONDARY
    }
}