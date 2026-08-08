package com.deepan.bettervillagers.quest.network;

import com.deepan.bettervillagers.BetterVillagers;
import com.deepan.bettervillagers.quest.Bounty;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record OpenQuestBoardPayload(BlockPos pos, List<Bounty> bounties) implements CustomPacketPayload {
    public static final Type<OpenQuestBoardPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterVillagers.MODID, "open_quest_board"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenQuestBoardPayload> STREAM_CODEC = StreamCodec.ofMember(
            OpenQuestBoardPayload::write,
            OpenQuestBoardPayload::new
    );

    public OpenQuestBoardPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos(), readBounties(buf));
    }

    private static List<Bounty> readBounties(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Bounty> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new Bounty(net.minecraft.world.item.ItemStack.STREAM_CODEC.decode(buf), buf.readInt(), net.minecraft.world.item.ItemStack.STREAM_CODEC.decode(buf), buf.readInt()));
        }
        return list;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(bounties.size());
        for (Bounty bounty : bounties) {
            net.minecraft.world.item.ItemStack.STREAM_CODEC.encode(buf, bounty.getObjectiveItem());
            buf.writeInt(bounty.getCountRequired());
            net.minecraft.world.item.ItemStack.STREAM_CODEC.encode(buf, bounty.getRewardItem());
            buf.writeInt(bounty.getRewardCount());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
