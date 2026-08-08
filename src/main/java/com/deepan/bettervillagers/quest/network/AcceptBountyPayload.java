package com.deepan.bettervillagers.quest.network;

import com.deepan.bettervillagers.BetterVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AcceptBountyPayload(BlockPos boardPos, int posterVillagerId) implements CustomPacketPayload {
    public static final Type<AcceptBountyPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterVillagers.MODID, "accept_bounty"));

    public static final StreamCodec<FriendlyByteBuf, AcceptBountyPayload> STREAM_CODEC = StreamCodec.ofMember(
            AcceptBountyPayload::write,
            AcceptBountyPayload::new
    );

    public AcceptBountyPayload(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.boardPos);
        buf.writeInt(this.posterVillagerId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
