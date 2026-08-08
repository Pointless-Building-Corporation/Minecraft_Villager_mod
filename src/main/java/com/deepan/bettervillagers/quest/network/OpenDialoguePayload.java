package com.deepan.bettervillagers.quest.network;

import com.deepan.bettervillagers.BetterVillagers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public record OpenDialoguePayload(int entityId, String themeId, String text, List<DialogueOption> options) implements CustomPacketPayload {
    public static final Type<OpenDialoguePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterVillagers.MODID, "open_dialogue"));

    public static final StreamCodec<FriendlyByteBuf, OpenDialoguePayload> STREAM_CODEC = StreamCodec.ofMember(
            OpenDialoguePayload::write,
            OpenDialoguePayload::new
    );

    public OpenDialoguePayload(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readUtf(), buf.readUtf(), buf.readList(DialogueOption::new));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeUtf(this.themeId);
        buf.writeUtf(this.text);
        buf.writeCollection(this.options, (b, option) -> option.write(b));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record DialogueOption(String text, String action) {
        public DialogueOption(FriendlyByteBuf buf) {
            this(buf.readUtf(), buf.readUtf());
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeUtf(this.text);
            buf.writeUtf(this.action);
        }
    }
}
