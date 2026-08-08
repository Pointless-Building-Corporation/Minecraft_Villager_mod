package com.deepan.bettervillagers.quest.network;

import com.deepan.bettervillagers.BetterVillagers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DialogueActionPayload(int entityId, String actionId) implements CustomPacketPayload {
    public static final Type<DialogueActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterVillagers.MODID, "dialogue_action"));

    public static final StreamCodec<FriendlyByteBuf, DialogueActionPayload> STREAM_CODEC = StreamCodec.ofMember(
            DialogueActionPayload::write,
            DialogueActionPayload::new
    );

    public DialogueActionPayload(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readUtf());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(actionId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
